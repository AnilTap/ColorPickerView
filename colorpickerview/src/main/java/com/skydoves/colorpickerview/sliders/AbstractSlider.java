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

package com.skydoves.colorpickerview.sliders;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.FloatRange;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import com.skydoves.colorpickerview.ActionMode;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.R;

/** AbstractSlider is the abstract class for implementing sliders. */
@SuppressWarnings("unused")
abstract class AbstractSlider extends FrameLayout {

  public ColorPickerView colorPickerView;
  protected Paint colorPaint;
  protected Paint outerBorderPaint;
  protected Paint innerBorderPaint;
  protected float selectorPosition = 1.0f;
  protected int selectedX = 0;
  protected Drawable selectorDrawable;
  protected int color = Color.WHITE;
  protected ImageView selector;
  protected String preferenceName;
  protected float cornerRadius;
  protected int barVerticalPadding = 0;
  private int outerBorderWidth = 0;
  private int outerBorderColor = Color.GRAY;
  private int innerBorderWidth = 0;
  private int innerBorderColor = Color.WHITE;
  protected RectF drawRect;
  protected RectF outerBorderRect;
  protected RectF innerBorderRect;

  public AbstractSlider(Context context) {
    super(context);
    onCreate();
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    selector.setVisibility(enabled ? VISIBLE : INVISIBLE);
    this.setClickable(enabled);
  }

  public AbstractSlider(Context context, AttributeSet attrs) {
    super(context, attrs);
    useAttrs(attrs);
    getAttrs(attrs);
    onCreate();
  }

  public AbstractSlider(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    useAttrs(attrs);
    getAttrs(attrs);
    onCreate();
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public AbstractSlider(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    useAttrs(attrs);
    getAttrs(attrs);
    onCreate();
  }

  private void useAttrs(AttributeSet attrs){
    TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.AbstractSlider);
    try {
      if (a.hasValue(R.styleable.AbstractSlider_outerBorderWidth)) {
        this.outerBorderWidth = a.getDimensionPixelSize(R.styleable.AbstractSlider_outerBorderWidth, outerBorderWidth);
      }
      if (a.hasValue(R.styleable.AbstractSlider_outerBorderColor)) {
        this.outerBorderColor = a.getColor(R.styleable.AbstractSlider_outerBorderColor, outerBorderColor);
      }
      if (a.hasValue(R.styleable.AbstractSlider_innerBorderWidth)) {
        this.innerBorderWidth = a.getDimensionPixelSize(R.styleable.AbstractSlider_innerBorderWidth, innerBorderWidth);
      }
      if (a.hasValue(R.styleable.AbstractSlider_barVerticalPadding)) {
        this.barVerticalPadding = a.getDimensionPixelSize(R.styleable.AbstractSlider_barVerticalPadding, barVerticalPadding);
      }
      if (a.hasValue(R.styleable.AbstractSlider_innerBorderColor)) {
        this.innerBorderColor = a.getColor(R.styleable.AbstractSlider_innerBorderColor, innerBorderColor);
      }
      if (a.hasValue(R.styleable.AbstractSlider_selector)) {
        int resourceId = a.getResourceId(R.styleable.AbstractSlider_selector, -1);
        if (resourceId != -1) {
          selectorDrawable = AppCompatResources.getDrawable(getContext(), resourceId);
        }
      }
    } finally {
      a.recycle();
    }
  }

  /** gets attribute sets style from layout */
  protected abstract void getAttrs(AttributeSet attrs);

  /** update paint color whenever the triggered colors are changed. */
  protected abstract void updatePaint(Paint colorPaint);

  /**
   * assembles about the selected color.
   *
   * @return assembled color.
   */
  public abstract @ColorInt int assembleColor();

  private void onCreate() {
    this.setBackgroundColor(Color.TRANSPARENT);

    this.colorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    this.outerBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    this.innerBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    this.outerBorderPaint.setStyle(Paint.Style.STROKE);
    this.outerBorderPaint.setStrokeWidth(outerBorderWidth);
    this.outerBorderPaint.setColor(outerBorderColor);

    this.innerBorderPaint.setStyle(Paint.Style.STROKE);
    this.innerBorderPaint.setStrokeWidth(innerBorderWidth);
    this.innerBorderPaint.setColor(innerBorderColor);

    selector = new ImageView(getContext());
    if (selectorDrawable != null) {
      setSelectorDrawable(selectorDrawable);
    }

    initializeSelector();
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    cornerRadius = h / 2f;
    float drawOffset = getBorderSize();
    drawRect = new RectF(drawOffset, drawOffset + barVerticalPadding, w - drawOffset, h - drawOffset - barVerticalPadding);
    float innerBorderOffset = (innerBorderWidth / 2f) + outerBorderWidth;
    innerBorderRect = new RectF(innerBorderOffset, innerBorderOffset + barVerticalPadding, w - innerBorderOffset, h - innerBorderOffset - barVerticalPadding);
    float outerBorderOffset = outerBorderWidth / 2f;
    outerBorderRect = new RectF(outerBorderOffset, outerBorderOffset + barVerticalPadding, w - outerBorderOffset, h - outerBorderOffset - barVerticalPadding);
  }

  @SuppressLint("NewApi")
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    canvas.drawRoundRect(drawRect, cornerRadius, cornerRadius, colorPaint);
    canvas.drawRoundRect(outerBorderRect, cornerRadius, cornerRadius, outerBorderPaint);
    canvas.drawRoundRect(innerBorderRect, cornerRadius, cornerRadius, innerBorderPaint);
  }

  /** called by {@link ColorPickerView} whenever {@link ColorPickerView} is triggered. */
  public void notifyColor() {
    color = colorPickerView.getPureColor();
    updatePaint(colorPaint);
    invalidate();
  }

  @SuppressLint("ClickableViewAccessibility")
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (!this.isEnabled()) {
      return false;
    }

    if (colorPickerView != null) {
      switch (event.getActionMasked()) {
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_DOWN:
        case MotionEvent.ACTION_MOVE:
          selector.setPressed(true);
          onTouchReceived(event);
          return true;
        default:
          selector.setPressed(false);
          return false;
      }
    } else {
      return false;
    }
  }

  private void onTouchReceived(MotionEvent event) {
    float eventX = event.getX();
    float left = selector.getMeasuredWidth();
    float right = getMeasuredWidth() - selector.getMeasuredWidth();
    if (eventX < left) eventX = left;
    if (eventX > right) eventX = right;
    selectorPosition = (eventX - left) / (right - left);
    if (selectorPosition > 1.0f) selectorPosition = 1.0f;

    Point snapPoint = new Point((int) event.getX(), (int) event.getY());
    selectedX = (int) getBoundaryX(snapPoint.x);
    selector.setX(selectedX);
    if (colorPickerView.getActionMode() == ActionMode.LAST) {
      if (event.getAction() == MotionEvent.ACTION_UP) {
        colorPickerView.fireColorListener(assembleColor(), true);
      }
    } else {
      colorPickerView.fireColorListener(assembleColor(), true);
    }

    if (colorPickerView.getFlagView() != null) {
      colorPickerView.getFlagView().receiveOnTouchEvent(event);
    }

    int maxPos = getMeasuredWidth() - selector.getMeasuredWidth();
    if (selector.getX() >= maxPos) selector.setX(maxPos);
    if (selector.getX() <= 0) selector.setX(0);
  }

  public void updateSelectorX(int x) {
    float left = selector.getMeasuredWidth();
    float right = getMeasuredWidth() - selector.getMeasuredWidth();
    selectorPosition = (x - left) / (right - left);
    if (selectorPosition > 1.0f) selectorPosition = 1.0f;
    selectedX = (int) getBoundaryX(x);
    selector.setX(selectedX);
    colorPickerView.fireColorListener(assembleColor(), false);
  }

  public void setSelectorPosition(@FloatRange(from = 0.0, to = 1.0) float selectorPosition) {
    this.selectorPosition = Math.min(selectorPosition, 1.0f);
    float x = (getMeasuredWidth() * selectorPosition) - getSelectorSize() - getBorderSize();
    selectedX = (int) getBoundaryX(x);
    selector.setX(selectedX);
  }

  public void setSelectorByHalfSelectorPosition(
      @FloatRange(from = 0.0, to = 1.0) float selectorPosition) {
    this.selectorPosition = Math.min(selectorPosition, 1.0f);
    float x = (getMeasuredWidth() * selectorPosition) - (getSelectorSize() * 0.5f) - getBorderSize();
    selectedX = (int) getBoundaryX(x);
    selector.setX(selectedX);
  }

  private float getBoundaryX(float x) {
    int maxPos = getMeasuredWidth() - getSelectorSize();
    if (x >= maxPos) return maxPos;
    if (x <= getSelectorSize()) return 0;
    return x - getSelectorSize();
  }

  protected int getSelectorSize() {
    return (int) (selector.getMeasuredWidth());
  }

  protected int getBorderSize() {
    return outerBorderWidth+innerBorderWidth;
  }

  private void initializeSelector() {
    getViewTreeObserver()
        .addOnGlobalLayoutListener(
            new ViewTreeObserver.OnGlobalLayoutListener() {
              @Override
              public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < 16) {
                  getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                  getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                onInflateFinished();
              }
            });
  }

  /**
   * sets a drawable of the selector.
   *
   * @param drawable drawable of the selector.
   */
  public void setSelectorDrawable(Drawable drawable) {
    removeView(selector);
    this.selectorDrawable = drawable;
    this.selector.setImageDrawable(drawable);
    FrameLayout.LayoutParams thumbParams =
        new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    thumbParams.gravity = Gravity.CENTER_VERTICAL;
    addView(selector, thumbParams);
  }

  /**
   * sets a drawable resource of the selector.
   *
   * @param resource a drawable resource of the selector.
   */
  public void setSelectorDrawableRes(@DrawableRes int resource) {
    Drawable drawable = ResourcesCompat.getDrawable(getContext().getResources(), resource, null);
    setSelectorDrawable(drawable);
  }

  /**
   * sets a color of the slider outerBorder.
   *
   * @param color color of the slider outerBorder.
   */
  public void setOuterBorderColor(@ColorInt int color) {
    this.outerBorderColor = color;
    this.outerBorderPaint.setColor(color);
    invalidate();
  }

  /**
   * sets a color resource of the slider outerBorder.
   *
   * @param resource color resource of the slider outerBorder.
   */
  public void setOuterBorderColorRes(@ColorRes int resource) {
    int color = ContextCompat.getColor(getContext(), resource);
    setOuterBorderColor(color);
  }

  /**
   * sets a size of the slide outerBorder.
   *
   * @param outerBorderWidth ize of the slide outerBorder.
   */
  public void setOuterBorderWidth(int outerBorderWidth) {
    this.outerBorderWidth = outerBorderWidth;
    this.outerBorderPaint.setStrokeWidth(outerBorderWidth);
    invalidate();
  }

  /**
   * sets a size of the slide outerBorder using dimension resource.
   *
   * @param resource a size of the slide outerBorder.
   */
  public void setOuterBorderSizeRes(@DimenRes int resource) {
    int outerBorderSize = (int) getContext().getResources().getDimension(resource);
    setOuterBorderWidth(outerBorderSize);
  }

  /**
   * sets a color of the slider innerBorder.
   *
   * @param color color of the slider innerBorder.
   */
  public void setInnerBorderColor(@ColorInt int color) {
    this.innerBorderColor = color;
    this.innerBorderPaint.setColor(color);
    invalidate();
  }

  /**
   * sets a color resource of the slider innerBorder.
   *
   * @param resource color resource of the slider innerBorder.
   */
  public void setInnerBorderColorRes(@ColorRes int resource) {
    int color = ContextCompat.getColor(getContext(), resource);
    setInnerBorderColor(color);
  }

  /**
   * sets a size of the slide innerBorder.
   *
   * @param innerBorderWidth ize of the slide innerBorder.
   */
  public void setInnerBorderWidth(int innerBorderWidth) {
    this.innerBorderWidth = innerBorderWidth;
    this.innerBorderPaint.setStrokeWidth(innerBorderWidth);
    invalidate();
  }

  /**
   * sets a size of the slide innerBorder using dimension resource.
   *
   * @param resource a size of the slide innerBorder.
   */
  public void setInnerBorderSizeRes(@DimenRes int resource) {
    int innerBorderSize = (int) getContext().getResources().getDimension(resource);
    setInnerBorderWidth(innerBorderSize);
  }

  /** called when the inflating finished. */
  public abstract void onInflateFinished();

  /**
   * gets assembled color
   *
   * @return color
   */
  public int getColor() {
    return color;
  }

  /**
   * attaches {@link ColorPickerView} to slider.
   *
   * @param colorPickerView {@link ColorPickerView}.
   */
  public void attachColorPickerView(ColorPickerView colorPickerView) {
    this.colorPickerView = colorPickerView;
  }

  /**
   * gets selector's position ratio.
   *
   * @return selector's position ratio.
   */
  protected float getSelectorPosition() {
    return this.selectorPosition;
  }

  /**
   * gets selected x coordinate.
   *
   * @return selected x coordinate.
   */
  public int getSelectedX() {
    return this.selectedX;
  }

  /**
   * gets the preference name.
   *
   * @return preference name.
   */
  public String getPreferenceName() {
    return preferenceName;
  }

  /**
   * sets the preference name.
   *
   * @param preferenceName preference name.
   */
  public void setPreferenceName(String preferenceName) {
    this.preferenceName = preferenceName;
  }
}
