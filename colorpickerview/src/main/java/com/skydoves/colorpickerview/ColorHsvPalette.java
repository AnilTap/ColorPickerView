/*
 * Designed and developed by 2017 skydoves (Jaewoong Eum)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.skydoves.colorpickerview;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.drawable.BitmapDrawable;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * ColorHsvPalette is a default drawable palette built by HSV (hue, saturation, value) color model
 * for alternating representations of the RGB color model.
 */
public class ColorHsvPalette extends BitmapDrawable {

  private final Paint huePaint= new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Paint saturationPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Paint outerBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Paint innerBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final int outerBorderWidth;
  private final int innerBorderWidth;

  public ColorHsvPalette(Resources resources, Bitmap bitmap, int outerBorderWidth, @ColorInt int outerBorderColor, int innerBorderWidth, @ColorInt int innerBorderColor) {
    super(resources, bitmap);
    this.outerBorderWidth = outerBorderWidth;
    this.innerBorderWidth = innerBorderWidth;
    outerBorderPaint.setStyle(Paint.Style.STROKE);
    outerBorderPaint.setColor(outerBorderColor);
    outerBorderPaint.setStrokeWidth(outerBorderWidth);

    innerBorderPaint.setStyle(Paint.Style.STROKE);
    innerBorderPaint.setColor(innerBorderColor);
    innerBorderPaint.setStrokeWidth(innerBorderWidth);
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    int width = getBounds().width();
    int height = getBounds().height();
    float centerX = width * 0.5f;
    float centerY = height * 0.5f;
    float radius = Math.min(width, height) * 0.5f;
    float paletteRadius = radius - innerBorderWidth - outerBorderWidth;

    Shader sweepShader =
        new SweepGradient(
            centerX,
            centerY,
            new int[] {
              Color.RED, Color.MAGENTA, Color.BLUE, Color.CYAN, Color.GREEN, Color.YELLOW, Color.RED
            },
            new float[] {0.000f, 0.166f, 0.333f, 0.499f, 0.666f, 0.833f, 0.999f});
    huePaint.setShader(sweepShader);

    Shader saturationShader = new RadialGradient(centerX, centerY, paletteRadius, Color.WHITE, 0x00FFFFFF, Shader.TileMode.CLAMP);
    saturationPaint.setShader(saturationShader);

    canvas.drawCircle(centerX, centerY, paletteRadius, huePaint);
    canvas.drawCircle(centerX, centerY, paletteRadius, saturationPaint);
    canvas.drawCircle(centerX, centerY, radius - (innerBorderWidth / 2f) - outerBorderWidth, innerBorderPaint);
    canvas.drawCircle(centerX, centerY, radius - (outerBorderWidth / 2f), outerBorderPaint);
  }

  @Override
  public void setAlpha(int alpha) {
    huePaint.setAlpha(alpha);
  }

  @Override
  public void setColorFilter(@Nullable ColorFilter colorFilter) {
    huePaint.setColorFilter(colorFilter);
  }

  @Override
  public int getOpacity() {
    return PixelFormat.OPAQUE;
  }
}
