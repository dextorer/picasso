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
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

class ImageViewAction extends Action<ImageView> {

  Callback callback;
  boolean round;
  int borderSize;
  int borderColor;
  int roundSize;
  boolean forceFade;

  ImageViewAction(Picasso picasso, ImageView imageView, Request data, boolean skipCache,
      boolean noFade, boolean forceFade, int errorResId, Drawable errorDrawable, String key, Callback callback,
      boolean round, int borderSize, int borderColor, int roundSize) {
    super(picasso, imageView, data, skipCache, noFade, errorResId, errorDrawable, key);
    this.callback = callback;
    this.round = round;
    this.borderSize = borderSize;
    this.borderColor = borderColor;
    this.roundSize = roundSize;
    this.forceFade = forceFade;
  }

  @Override public void complete(Bitmap result, Picasso.LoadedFrom from) {
    if (result == null) {
      throw new AssertionError(
          String.format("Attempted to complete action with no result!\n%s", this));
    }

    ImageView target = this.target.get();
    if (target == null) {
      return;
    }

    Context context = picasso.context;
    boolean indicatorsEnabled = picasso.indicatorsEnabled;
      if (round) {
        PicassoRoundDrawable.setBitmap(target, context, result, from, noFade, forceFade, indicatorsEnabled, borderSize, borderColor, roundSize);
      } else {
        PicassoDrawable.setBitmap(target, context, result, from, noFade, forceFade, indicatorsEnabled);
      }

    if (callback != null) {
      callback.onSuccess();
    }
  }

  @Override public void error() {
    ImageView target = this.target.get();
    if (target == null) {
      return;
    }
    if (errorResId != 0) {
      target.setImageResource(errorResId);
    } else if (errorDrawable != null) {
      target.setImageDrawable(errorDrawable);
    }

    if (callback != null) {
      callback.onError();
    }
  }

  @Override void cancel() {
    super.cancel();
    if (callback != null) {
      callback = null;
    }
  }
}
