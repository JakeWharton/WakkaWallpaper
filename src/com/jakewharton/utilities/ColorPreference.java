package com.jakewharton.utilities;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.preference.DialogPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

public class ColorPreference extends DialogPreference {
	private class ColorPickerView extends View {
		private static final int CENTER_X = 100;
		private static final int CENTER_Y = 100;
		private static final int CENTER_RADIUS = 32;
		private static final float PI = 3.1415926f;
		
		private final Paint mPaint;
		private final Paint mCenterPaint;
		private final int[] mColors;

		ColorPickerView(final Context c, final int color) {
			super(c);
			
			this.mColors = new int[] { 0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF, 0xFF00FF00, 0xFFFFFF00, 0xFFFFFFFF, 0xFF808080, 0xFF000000, 0xFFFF0000 };

			this.mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			this.mPaint.setShader(new SweepGradient(0, 0, this.mColors, null));
			this.mPaint.setStyle(Paint.Style.STROKE);
			this.mPaint.setStrokeWidth(32);

			this.mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			this.mCenterPaint.setColor(color);
			this.mCenterPaint.setStrokeWidth(5);
		}

		@Override
		protected void onDraw(final Canvas canvas) {
			final float r = CENTER_X - this.mPaint.getStrokeWidth() * 0.5f;

			canvas.translate(CENTER_X, CENTER_X);

			canvas.drawOval(new RectF(-r, -r, r, r), this.mPaint);
			canvas.drawCircle(0, 0, CENTER_RADIUS, this.mCenterPaint);
		}

		@Override
		protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
			this.setMeasuredDimension(CENTER_X * 2, CENTER_Y * 2);
		}

		public void setCenterColor(final int color) {
			this.mCenterPaint.setColor(color);
			this.invalidate();
		}

		private int ave(final int s, final int d, final float p) {
			return s + Math.round(p * (d - s));
		}

		private int interpColor(final int colors[], final float unit) {
			if (unit <= 0) {
				return colors[0];
			}
			if (unit >= 1) {
				return colors[colors.length - 1];
			}

			float p = unit * (colors.length - 1);
			final int i = (int)p;
			p -= i;

			// now p is just the fractional part [0...1) and i is the index
			final int c0 = colors[i];
			final int c1 = colors[i + 1];
			final int r = ave(Color.red(c0), Color.red(c1), p);
			final int g = ave(Color.green(c0), Color.green(c1), p);
			final int b = ave(Color.blue(c0), Color.blue(c1), p);

			return Color.argb(255, r, g, b);
		}

		@Override
		public boolean onTouchEvent(final MotionEvent event) {
			final float x = event.getX() - CENTER_X;
			final float y = event.getY() - CENTER_Y;

			switch (event.getAction()) {
				case MotionEvent.ACTION_MOVE:
					if (Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)) > ColorPickerView.CENTER_RADIUS) {
						final float angle = (float)Math.atan2(y, x);
						// need to turn angle [-PI ... PI] into unit [0....1]
						float unit = angle / (2 * PI);
						if (unit < 0) {
							unit += 1;
						}
						final int color = this.interpColor(this.mColors, unit);
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
	
	private EditText mEditText;
	private ColorPickerView mColorPickerView;
	private int mValue;
	private int mTempValue;
	
	private final TextWatcher mEditTextListener = new TextWatcher() {
		public void afterTextChanged(final Editable s) {}
		public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {}
		public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
			try {
				final String s2 = s.toString().replace("#", "");
				if (s2.length() == 6) {
					ColorPreference.this.mColorPickerView.setCenterColor(ColorPreference.convertToColorInt(s2));
				}
			} catch (final NumberFormatException e) {}
		}
	};

	public ColorPreference(final Context context, final AttributeSet attrs) {
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

		this.mColorPickerView = new ColorPickerView(this.getContext(), this.mValue);
		layout.addView(this.mColorPickerView, layoutParams);

		this.mEditText = new EditText(context);
		this.mEditText.addTextChangedListener(this.mEditTextListener);
		this.mEditText.setText(ColorPreference.convertToARGB(this.mValue));
		layout.addView(this.mEditText, layoutParams);
		
		return layout;
	}

	@Override
	protected Object onGetDefaultValue(final TypedArray a, final int index) {
		return a.getInt(index, 0);
	}

	@Override
	protected void onSetInitialValue(final boolean restore, final Object defaultValue) {
		this.mValue = this.getPersistedInt(defaultValue == null ? 0 : (Integer)defaultValue);
	}

	@Override
	protected void onDialogClosed(final boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		if (positiveResult) {
			if (this.callChangeListener(this.mTempValue)) {
				this.setValue(this.mTempValue);
			}
		}
	}

	public void setValue(final int value) {
		this.mValue = value;
		this.persistInt(value);
	}
	
	private static String convertToARGB(final int color) {
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

		return "#" + red + green + blue;
	}
	
	private static int convertToColorInt(final String argb) throws NumberFormatException {
		int alpha = -1;
		int red = -1;
		int green = -1;
		int blue = -1;

		if (argb.length() == 6) {
			alpha = 255;
			red = Integer.parseInt(argb.substring(0, 2), 16);
			green = Integer.parseInt(argb.substring(2, 4), 16);
			blue = Integer.parseInt(argb.substring(4, 6), 16);
		}

		return Color.argb(alpha, red, green, blue);
	}
}