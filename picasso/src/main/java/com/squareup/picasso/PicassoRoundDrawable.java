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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.widget.ImageView;

import static android.graphics.Color.WHITE;
import static com.squareup.picasso.Picasso.LoadedFrom.MEMORY;

final class PicassoRoundDrawable extends Drawable {
	// Only accessed from main thread.
	private static final Paint DEBUG_PAINT = new Paint();

	private static final float FADE_DURATION = 200f; //ms

	private final float mCornerRadius;
	private final RectF mRect = new RectF();
	private final BitmapShader mBitmapShader;
	private final Paint mPaint;


	/**
	 * Create or update the drawable on the target {@link android.widget.ImageView} to display the supplied bitmap
	 * image.
	 */
	static void setBitmap(ImageView target, Context context, Bitmap bitmap,
	                      Picasso.LoadedFrom loadedFrom, boolean noFade, boolean debugging) {
		Drawable placeholder = target.getDrawable();
		if (placeholder instanceof AnimationDrawable) {
			((AnimationDrawable) placeholder).stop();
		}
		PicassoRoundDrawable drawable =
				new PicassoRoundDrawable(context, placeholder, bitmap, loadedFrom, noFade, debugging);
		target.setImageDrawable(drawable);
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

	private final boolean debugging;
	private final float density;
	private final Picasso.LoadedFrom loadedFrom;
	final BitmapDrawable image;

	Drawable placeholder;

	long startTimeMillis;
	boolean animating;
	int alpha = 0xFF;

	PicassoRoundDrawable(Context context, Drawable placeholder, Bitmap bitmap,
	                     Picasso.LoadedFrom loadedFrom, boolean noFade, boolean debugging) {
		Resources res = context.getResources();

		this.debugging = debugging;
		this.density = res.getDisplayMetrics().density;

		this.loadedFrom = loadedFrom;

		this.image = new BitmapDrawable(res, bitmap);

		boolean fade = loadedFrom != MEMORY && !noFade;
		if (fade) {
			this.placeholder = placeholder;
			animating = true;
			startTimeMillis = SystemClock.uptimeMillis();
		}

		int minMeasure = bitmap.getHeight() > bitmap.getWidth() ? bitmap.getWidth() : bitmap.getHeight();
		mCornerRadius = minMeasure / 2;

		mBitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setShader(mBitmapShader);

	}

	@Override
	public void draw(Canvas canvas) {

		if (!animating) {
			canvas.drawRoundRect(mRect, mCornerRadius, mCornerRadius, mPaint);
		} else {
			float normalized = (SystemClock.uptimeMillis() - startTimeMillis) / FADE_DURATION;
			if (normalized >= 1f) {
				animating = false;
				placeholder = null;
				canvas.drawRoundRect(mRect, mCornerRadius, mCornerRadius, mPaint);
			} else {
				if (placeholder != null) {
					placeholder.draw(canvas);
				}

				int partialAlpha = (int) (alpha * normalized);
				mPaint.setAlpha(partialAlpha);
				canvas.drawRoundRect(mRect, mCornerRadius, mCornerRadius, mPaint);
				mPaint.setAlpha(alpha);
				invalidateSelf();
			}
		}

		if (debugging) {
			drawDebugIndicator(canvas);
		}
	}

	@Override
	public int getIntrinsicWidth() {
		return image.getIntrinsicWidth();
	}

	@Override
	public int getIntrinsicHeight() {
		return image.getIntrinsicHeight();
	}

	@Override
	public void setAlpha(int alpha) {
		this.alpha = alpha;
		if (placeholder != null) {
			placeholder.setAlpha(alpha);
		}
		image.setAlpha(alpha);
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		if (placeholder != null) {
			placeholder.setColorFilter(cf);
		}
		image.setColorFilter(cf);
	}

	@Override
	public int getOpacity() {
		return image.getOpacity();
	}

	@Override
	protected void onBoundsChange(Rect bounds) {
		super.onBoundsChange(bounds);
		mRect.set(0, 0, bounds.width(), bounds.height());

//    image.setBounds(bounds);
		if (placeholder != null) {
			placeholder.setBounds(bounds);
		}
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
