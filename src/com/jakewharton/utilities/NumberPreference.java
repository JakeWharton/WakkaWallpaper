package com.jakewharton.utilities;

import com.jakewharton.wakkawallpaper.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

public class NumberPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener {
	private SeekBar mSeekBar;
	private TextView mValueText;
	private String mSuffix;
	private int mMax;
	private int mMin;
	private int mValue = 0;

	public NumberPreference(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		this.setPersistent(true);

		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EditNumberPreference, 0, 0);
		this.mSuffix = a.getString(R.styleable.EditNumberPreference_suffix);
		this.mMin = a.getInt(R.styleable.EditNumberPreference_min, 0);
		this.mMax = a.getInt(R.styleable.EditNumberPreference_max, 100);

		this.setDialogLayoutResource(R.layout.number_preference);
	}

	@Override
	protected void onBindDialogView(final View view) {
		super.onBindDialogView(view);
		
		((TextView)view.findViewById(R.id.dialogMessage)).setText(this.getDialogMessage());

		this.mValueText = (TextView)view.findViewById(R.id.actualValue);

		this.mSeekBar = (SeekBar)view.findViewById(R.id.myBar);
		this.mSeekBar.setOnSeekBarChangeListener(this);
		this.mSeekBar.setMax(this.mMax - this.mMin);
		this.mSeekBar.setProgress(this.mValue - this.mMin);

		final String t = String.valueOf(this.mValue);
		this.mValueText.setText(this.mSuffix == null ? t : t.concat(this.mSuffix));
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
			final int value = this.mSeekBar.getProgress() + this.mMin;
			if (this.callChangeListener(value)) {
				this.setValue(value);
			}
		}
	}

	public void setValue(int value) {
		if (value > this.mMax) {
			value = this.mMax;
		} else if (value < this.mMin) {
			value = this.mMin;
		}
		this.mValue = value;
		this.persistInt(value);
	}

	public void setMax(final int max) {
		this.mMax = max;
		if (this.mValue > this.mMax) {
			this.setValue(this.mMax);
		}
	}

	public void setMin(final int min) {
		if (min < this.mMax) {
			this.mMin = min;
		}
	}

	public void onProgressChanged(final SeekBar seek, final int value, final boolean fromTouch) {
		final String t = String.valueOf(value + this.mMin);
		this.mValueText.setText(this.mSuffix == null ? t : t.concat(this.mSuffix));
	}

	public void onStartTrackingTouch(final SeekBar seek) {}

	public void onStopTrackingTouch(final SeekBar seek) {}
}