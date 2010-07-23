package com.jakewharton.utilities;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.Typeface;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.preference.DialogPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.StateSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;

public class ColorPreference extends DialogPreference {
	private class ColorPickerView extends View {
		private static final int CENTER_X = 100;
		private static final int CENTER_Y = 100;
		private static final int CENTER_RADIUS = 32;
		private static final float PI = 3.1415926f;
		
		private Paint mPaint;
		private Paint mCenterPaint;
		private final int[] mColors;

		ColorPickerView(Context c, int color) {
			super(c);
			
			this.mColors = new int[] { 0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF, 0xFF00FF00, 0xFFFFFF00, 0xFFFFFFFF, 0xFF808080, 0xFF000000, 0xFFFF0000 };
			Shader s = new SweepGradient(0, 0, this.mColors, null);

			this.mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			this.mPaint.setShader(s);
			this.mPaint.setStyle(Paint.Style.STROKE);
			this.mPaint.setStrokeWidth(32);

			this.mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			this.mCenterPaint.setColor(color);
			this.mCenterPaint.setStrokeWidth(5);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			float r = CENTER_X - this.mPaint.getStrokeWidth() * 0.5f;

			canvas.translate(CENTER_X, CENTER_X);

			canvas.drawOval(new RectF(-r, -r, r, r), this.mPaint);
			canvas.drawCircle(0, 0, CENTER_RADIUS, this.mCenterPaint);
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			this.setMeasuredDimension(CENTER_X * 2, CENTER_Y * 2);
		}

		public void setCenterColor(int color) {
			this.mCenterPaint.setColor(color);
			this.invalidate();
		}

		public void setTransparency(int alpha) {
			int color = this.mCenterPaint.getColor();
			int newColor = Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
			this.mCenterPaint.setColor(newColor);
			ColorPreference.this.mEditText.setText(ColorPreference.convertToARGB(newColor));
			invalidate();
		}

		private int ave(int s, int d, float p) {
			return s + Math.round(p * (d - s));
		}

		private int interpColor(int colors[], float unit) {
			if (unit <= 0) {
				return colors[0];
			}
			if (unit >= 1) {
				return colors[colors.length - 1];
			}

			float p = unit * (colors.length - 1);
			int i = (int)p;
			p -= i;

			// now p is just the fractional part [0...1) and i is the index
			int c0 = colors[i];
			int c1 = colors[i + 1];
			int a = ave(Color.alpha(c0), Color.alpha(c1), p);
			int r = ave(Color.red(c0), Color.red(c1), p);
			int g = ave(Color.green(c0), Color.green(c1), p);
			int b = ave(Color.blue(c0), Color.blue(c1), p);

			return Color.argb(a, r, g, b);
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			float x = event.getX() - CENTER_X;
			float y = event.getY() - CENTER_Y;

			switch (event.getAction()) {
				case MotionEvent.ACTION_MOVE:
					if (Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)) > ColorPickerView.CENTER_RADIUS) {
						float angle = (float)Math.atan2(y, x);
						// need to turn angle [-PI ... PI] into unit [0....1]
						float unit = angle / (2 * PI);
						if (unit < 0) {
							unit += 1;
						}
						int color = interpColor(this.mColors, unit);
						this.mCenterPaint.setColor(color);
						ColorPreference.this.mTempValue = color;
						ColorPreference.this.mEditText.setText(ColorPreference.convertToARGB(color));
						this.invalidate();
					}
					break;
			}
			return true;
		}
	}
	private static class TextSeekBarDrawable extends Drawable implements Runnable {
		// Source: http://www.anddev.org/announce_color_picker_dialog-t10771.html
		private static final int[] STATE_FOCUSED = { android.R.attr.state_focused };
		private static final int[] STATE_PRESSED = { android.R.attr.state_pressed };
		private static final long DELAY = 50;
		
		private String mText;
		private Drawable mProgress;
		private Paint mPaint;
		private Paint mOutlinePaint;
		private float mTextWidth;
		private boolean mActive;
		private float mTextXScale;
		private int mDelta;
		private ScrollAnimation mAnimation;

		public TextSeekBarDrawable(Resources res, String label, boolean labelOnRight) {
			this.mText = label;
			this.mProgress = res.getDrawable(android.R.drawable.progress_horizontal);
			this.mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			this.mPaint.setTypeface(Typeface.DEFAULT_BOLD);
			this.mPaint.setTextSize(16);
			this.mPaint.setColor(0xff000000);
			this.mOutlinePaint = new Paint(this.mPaint);
			this.mOutlinePaint.setStyle(Style.STROKE);
			this.mOutlinePaint.setStrokeWidth(3);
			this.mOutlinePaint.setColor(0xbbffc300);
			this.mOutlinePaint.setMaskFilter(new BlurMaskFilter(1, Blur.NORMAL));
			this.mTextWidth = this.mOutlinePaint.measureText(this.mText);
			this.mTextXScale = labelOnRight ? 1 : 0;
			this.mAnimation = new ScrollAnimation();
		}

		@Override
		protected void onBoundsChange(Rect bounds) {
			this.mProgress.setBounds(bounds);
		}

		@Override
		protected boolean onStateChange(int[] state) {
			this.mActive = StateSet.stateSetMatches(STATE_FOCUSED, state) | StateSet.stateSetMatches(STATE_PRESSED, state);
			this.invalidateSelf();
			return false;
		}

		@Override
		public boolean isStateful() {
			return true;
		}

		@Override
		protected boolean onLevelChange(int level) {
			// Log.d(TAG, "onLevelChange " + level);
			if ((level < 4000) && (this.mDelta <= 0)) {
				// Log.d(TAG, "onLevelChange scheduleSelf ++");
				this.mDelta = 1;
				this.mAnimation.startScrolling(this.mTextXScale, 1);
				this.scheduleSelf(this, SystemClock.uptimeMillis() + DELAY);
			} else if ((level > 6000) && (mDelta >= 0)) {
				// Log.d(TAG, "onLevelChange scheduleSelf --");
				this.mDelta = -1;
				this.mAnimation.startScrolling(this.mTextXScale, 0);
				this.scheduleSelf(this, SystemClock.uptimeMillis() + DELAY);
			}
			return this.mProgress.setLevel(level);
		}

		@Override
		public void draw(Canvas canvas) {
			this.mProgress.draw(canvas);

			if (this.mAnimation.hasStarted() && !this.mAnimation.hasEnded()) {
				// pending animation
				this.mAnimation.getTransformation(AnimationUtils.currentAnimationTimeMillis(), null);
				this.mTextXScale = this.mAnimation.getCurrent();
				// Log.d(TAG, "draw " + mTextX + " " +
				// SystemClock.uptimeMillis());
			}

			Rect bounds = getBounds();
			float x = 6 + this.mTextXScale * (bounds.width() - this.mTextWidth - 6 - 6);
			float y = (bounds.height() + this.mPaint.getTextSize()) / 2;
			this.mOutlinePaint.setAlpha(this.mActive ? 255 : 255 / 2);
			this.mPaint.setAlpha(mActive ? 255 : 255 / 2);
			canvas.drawText(this.mText, x, y, this.mOutlinePaint);
			canvas.drawText(this.mText, x, y, this.mPaint);
		}

		@Override
		public int getOpacity() {
			return PixelFormat.TRANSLUCENT;
		}

		@Override
		public void setAlpha(int alpha) {}

		@Override
		public void setColorFilter(ColorFilter cf) {}

		public void run() {
			this.mAnimation.getTransformation(AnimationUtils.currentAnimationTimeMillis(), null);
			// close interpolation of mTextX
			this.mTextXScale = mAnimation.getCurrent();
			if (!this.mAnimation.hasEnded()) {
				this.scheduleSelf(this, SystemClock.uptimeMillis() + DELAY);
			}
			this.invalidateSelf();
			// Log.d(TAG, "run " + mTextX + " " + SystemClock.uptimeMillis());
		}
	}
	private static class ScrollAnimation extends Animation {
		private static final long DURATION = 750;
		private float mFrom;
		private float mTo;
		private float mCurrent;

		public ScrollAnimation() {
			this.setDuration(DURATION);
			this.setInterpolator(new DecelerateInterpolator());
		}

		public void startScrolling(float from, float to) {
			this.mFrom = from;
			this.mTo = to;
			this.startNow();
		}

		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {
			this.mCurrent = this.mFrom + (this.mTo - this.mFrom) * interpolatedTime;
			// Log.d(TAG, "applyTransformation " + mCurrent);
		}

		public float getCurrent() {
			return this.mCurrent;
		}
	}
	
	private EditText mEditText;
	private ColorPickerView mColorPickerView;
	private SeekBar mTransparencyBar;
	private int mValue;
	private int mTempValue;
	
	private SeekBar.OnSeekBarChangeListener onTransparencyChangedListener = new SeekBar.OnSeekBarChangeListener() {
		public void onStartTrackingTouch(SeekBar seekBar) {}
		public void onStopTrackingTouch(SeekBar seekBar) {}
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			ColorPreference.this.mColorPickerView.setTransparency(progress);
		}
	};
	private TextWatcher mEditTextListener = new TextWatcher() {
		public void afterTextChanged(Editable s) {}
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			try {
				String s2 = s.toString().replace("#", "");
				if ((s2.length() == 6) || (s2.length() == 8)) {
					int color = ColorPreference.convertToColorInt(s2);
					ColorPreference.this.mColorPickerView.setCenterColor(color);
					ColorPreference.this.mTransparencyBar.setProgress(Color.alpha(color));
				}
			} catch (NumberFormatException e) {}
		}
	};

	public ColorPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		this.setPersistent(true);
	}

	@Override
	protected View onCreateDialogView() {
		final Context context = this.getContext();
		final LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setGravity(android.view.Gravity.CENTER);

		final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.setMargins(10, 0, 10, 5);

		this.mColorPickerView = new ColorPickerView(getContext(), this.mValue);
		layout.addView(this.mColorPickerView, layoutParams);

		this.mTransparencyBar = new SeekBar(context);
		this.mTransparencyBar.setMax(255);
		this.mTransparencyBar.setProgressDrawable(new TextSeekBarDrawable(this.getContext().getResources(), "alpha", true));
		this.mTransparencyBar.setProgress(Color.alpha(this.mValue));
		this.mTransparencyBar.setOnSeekBarChangeListener(this.onTransparencyChangedListener);
		layout.addView(this.mTransparencyBar, layoutParams);

		this.mEditText = new EditText(context);
		this.mEditText.addTextChangedListener(this.mEditTextListener);
		this.mEditText.setText(ColorPreference.convertToARGB(this.mValue));
		layout.addView(this.mEditText, layoutParams);
		
		return layout;
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getInt(index, 0);
	}

	@Override
	protected void onSetInitialValue(boolean restore, Object defaultValue) {
		this.mValue = this.getPersistedInt(defaultValue == null ? 0 : (Integer)defaultValue);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		if (positiveResult) {
			if (this.callChangeListener(this.mTempValue)) {
				this.setValue(this.mTempValue);
			}
		}
	}

	public void setValue(int value) {
		this.mValue = value;
		this.persistInt(value);
	}
	
	
	private static String convertToARGB(int color) {
		String alpha = Integer.toHexString(Color.alpha(color));
		String red = Integer.toHexString(Color.red(color));
		String green = Integer.toHexString(Color.green(color));
		String blue = Integer.toHexString(Color.blue(color));

		if (alpha.length() == 1) {
			alpha = "0" + alpha;
		}
		if (red.length() == 1) {
			red = "0" + red;
		}
		if (green.length() == 1) {
			green = "0" + green;
		}
		if (blue.length() == 1) {
			blue = "0" + blue;
		}

		return "#" + alpha + red + green + blue;
	}
	private static int convertToColorInt(String argb) throws NumberFormatException {

		int alpha = -1, red = -1, green = -1, blue = -1;

		if (argb.length() == 8) {
			alpha = Integer.parseInt(argb.substring(0, 2), 16);
			red = Integer.parseInt(argb.substring(2, 4), 16);
			green = Integer.parseInt(argb.substring(4, 6), 16);
			blue = Integer.parseInt(argb.substring(6, 8), 16);
		} else if (argb.length() == 6) {
			alpha = 255;
			red = Integer.parseInt(argb.substring(0, 2), 16);
			green = Integer.parseInt(argb.substring(2, 4), 16);
			blue = Integer.parseInt(argb.substring(4, 6), 16);
		}

		return Color.argb(alpha, red, green, blue);
	}
}