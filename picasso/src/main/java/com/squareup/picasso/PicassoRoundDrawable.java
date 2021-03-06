/*
 * Copyright (C) 2013 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.picasso;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.drawable.*;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.SystemClock;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import static android.graphics.Color.WHITE;
import static com.squareup.picasso.Picasso.LoadedFrom.MEMORY;

final class PicassoRoundDrawable extends BitmapDrawable {
  // Only accessed from main thread.
  private static final Paint DEBUG_PAINT = new Paint();
  private static final float FADE_DURATION = 200f; //ms

  private float mCornerRadius;
  private final RectF mRect = new RectF();
  private BitmapShader mBitmapShader;
  private Paint mPaint;

  private int targetWidth;
  private int targetHeight;

  private int borderSize;
  private int borderColor;

  private int roundSize;

  /**
   * Create or update the drawable on the target {@link android.widget.ImageView} to display the supplied bitmap
   * image.
   */
  @SuppressLint("NewApi")
  static void setBitmap(ImageView target, Context context, Bitmap bitmap,
      Picasso.LoadedFrom loadedFrom, boolean noFade, boolean forceFade, boolean debugging,
      int borderSize, int borderColor, int roundSize) {

    int minMeasure = Math.min(bitmap.getHeight(), bitmap.getWidth());

    Drawable placeholder = target.getDrawable();
    if (placeholder instanceof AnimationDrawable) {
      ((AnimationDrawable) placeholder).stop();
    }
    PicassoRoundDrawable drawable =
        new PicassoRoundDrawable(context, bitmap, placeholder, loadedFrom, noFade, forceFade, debugging, target);

    if (borderSize > 0) {
      drawable.setBorder(borderSize, borderColor);
    }

    if (roundSize > 0) {
      drawable.setRoundSize(roundSize);
    }

    int sdk = android.os.Build.VERSION.SDK_INT;
    if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
      target.setBackgroundDrawable(createStateListDrawable(context, minMeasure, drawable));
    } else {
      target.setBackground(createStateListDrawable(context, minMeasure, drawable));
    }

    target.setImageDrawable(drawable);
  }

  static StateListDrawable createStateListDrawable(Context context, int size, PicassoRoundDrawable drawable) {
    StateListDrawable stateListDrawable = new StateListDrawable();

    ShapeDrawable shapeDrawable;
    if (drawable.roundSize > 0) {
      RoundRectShape roundRectShape = new RoundRectShape(new float[] {(int) size, (int) size, (int) size, (int) size, (int) size, (int) size, (int) size, (int) size}, null, null);
      shapeDrawable = new ShapeDrawable(roundRectShape);
    } else {
      OvalShape ovalShape = new OvalShape();
      ovalShape.resize(size, size);
      shapeDrawable = new ShapeDrawable(ovalShape);
    }

    try {
      int color = context.getResources().getColor(drawable.borderColor);
      shapeDrawable.getPaint().setColor(context.getResources().getColor(color));
    } catch (Resources.NotFoundException e) {
      shapeDrawable.getPaint().setColor(drawable.borderColor);
    }

    stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, shapeDrawable);
    stateListDrawable.addState(new int[]{android.R.attr.state_focused}, shapeDrawable);
    stateListDrawable.addState(new int[]{}, null);

    return stateListDrawable;
  }

  /**
   * Create or update the drawable on the target {@link android.widget.ImageView} to display the supplied
   * placeholder image.
   */
  static void setPlaceholder(ImageView target, int placeholderResId, Drawable placeholderDrawable) {
    if (placeholderResId != 0) {
      target.setImageResource(placeholderResId);
    } else {
      target.setImageDrawable(placeholderDrawable);
    }
    if (target.getDrawable() instanceof AnimationDrawable) {
      ((AnimationDrawable) target.getDrawable()).start();
    }
  }

  public void setBorder(int borderSize, int borderColor) {
    this.borderSize = borderSize;
    this.borderColor = borderColor;
  }

  public void setRoundSize(int roundSize) {
    this.roundSize = roundSize;
  }

  private final boolean debugging;
  private final float density;
  private final Picasso.LoadedFrom loadedFrom;

  Drawable placeholder;

  long startTimeMillis;
  boolean animating;
  int alpha = 0xFF;

  @SuppressLint("NewApi")
  PicassoRoundDrawable(Context context, Bitmap bitmap, Drawable placeholder,
                       Picasso.LoadedFrom loadedFrom, boolean noFade, boolean forceFade, boolean debugging, final ImageView target) {
    super(context.getResources(), bitmap);

    this.debugging = debugging;
    this.density = context.getResources().getDisplayMetrics().density;

    this.loadedFrom = loadedFrom;

    targetWidth = bitmap.getWidth();
    targetHeight = bitmap.getHeight();

    this.placeholder = placeholder;

    boolean fade = loadedFrom != MEMORY && !noFade;

    if (forceFade) {
      fade = true;
    }
    if (fade) {
      this.placeholder = placeholder;
      animating = true;
      startTimeMillis = SystemClock.uptimeMillis();
    }

    mBitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

    mPaint = new Paint();
    mPaint.setAntiAlias(true);
    mPaint.setShader(mBitmapShader);
  }

  @Override public void draw(Canvas canvas) {

    float minMeasure = Math.min(targetHeight, targetWidth);
    mCornerRadius = minMeasure / 2f;

    if (!animating) {
      if (roundSize > 0) {
        canvas.drawRoundRect(mRect, mCornerRadius, mCornerRadius, mPaint);
      } else {
        canvas.drawCircle(mCornerRadius, mCornerRadius, mCornerRadius - borderSize, mPaint);
      }
    } else {
      float normalized = (SystemClock.uptimeMillis() - startTimeMillis) / FADE_DURATION;
      if (normalized >= 1f) {
        animating = false;
        placeholder = null;
        if (roundSize > 0) {
          canvas.drawRoundRect(mRect, mCornerRadius, mCornerRadius, mPaint);
        } else {
          canvas.drawCircle(mCornerRadius, mCornerRadius, mCornerRadius - borderSize, mPaint);
        }
      } else {
        if (placeholder != null) {
          placeholder.draw(canvas);
        }
        int partialAlpha = (int) (alpha * normalized);
        mPaint.setAlpha(partialAlpha);
        if (roundSize > 0) {
          canvas.drawRoundRect(mRect, mCornerRadius, mCornerRadius, mPaint);
        } else {
          canvas.drawCircle(mCornerRadius, mCornerRadius, mCornerRadius - borderSize, mPaint);
        }
        mPaint.setAlpha(alpha);
        invalidateSelf();
      }
    }

    if (debugging) {
      drawDebugIndicator(canvas);
    }
  }

  @Override public void setAlpha(int alpha) {
    if (placeholder != null) {
      placeholder.setAlpha(alpha);
    }
    super.setAlpha(alpha);
  }

  @Override public void setColorFilter(ColorFilter cf) {
    if (placeholder != null) {
      placeholder.setColorFilter(cf);
    }
    super.setColorFilter(cf);
  }

  @Override protected void onBoundsChange(Rect bounds) {
    if (placeholder != null) {
      placeholder.setBounds(bounds);
    }
    mRect.set(borderSize, borderSize, bounds.width() - borderSize, bounds.height() - borderSize);

    super.onBoundsChange(bounds);
  }

  private void drawDebugIndicator(Canvas canvas) {
    DEBUG_PAINT.setColor(WHITE);
    Path path = getTrianglePath(new Point(0, 0), (int) (16 * density));
    canvas.drawPath(path, DEBUG_PAINT);

    DEBUG_PAINT.setColor(loadedFrom.debugColor);
    path = getTrianglePath(new Point(0, 0), (int) (15 * density));
    canvas.drawPath(path, DEBUG_PAINT);
  }

  private static Path getTrianglePath(Point p1, int width) {
    Point p2 = new Point(p1.x + width, p1.y);
    Point p3 = new Point(p1.x, p1.y + width);

    Path path = new Path();
    path.moveTo(p1.x, p1.y);
    path.lineTo(p2.x, p2.y);
    path.lineTo(p3.x, p3.y);

    return path;
  }
}
