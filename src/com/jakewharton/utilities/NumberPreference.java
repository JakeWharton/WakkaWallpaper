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

	public NumberPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setPersistent(true);

		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EditNumberPreference, 0, 0);
		this.mSuffix = a.getString(R.styleable.EditNumberPreference_suffix);
		this.mMin = a.getInt(R.styleable.EditNumberPreference_min, 0);
		this.mMax = a.getInt(R.styleable.EditNumberPreference_max, 100);

		this.setDialogLayoutResource(R.layout.number_preference);
	}

	@Override
	protected void onBindDialogView(View v) {
		super.onBindDialogView(v);
		
		TextView dialogMessage = (TextView)v.findViewById(R.id.dialogMessage);
		dialogMessage.setText(this.getDialogMessage());

		this.mValueText = (TextView)v.findViewById(R.id.actualValue);

		this.mSeekBar = (SeekBar)v.findViewById(R.id.myBar);
		this.mSeekBar.setOnSeekBarChangeListener(this);
		this.mSeekBar.setMax(this.mMax - this.mMin);
		this.mSeekBar.setProgress(this.mValue - this.mMin);

		String t = String.valueOf(this.mValue);
		this.mValueText.setText(this.mSuffix == null ? t : t.concat(this.mSuffix));
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
			int value = this.mSeekBar.getProgress() + this.mMin;
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

	public void setMax(int max) {
		this.mMax = max;
		if (this.mValue > this.mMax) {
			this.setValue(this.mMax);
		}
	}

	public void setMin(int min) {
		if (min < this.mMax) {
			this.mMin = min;
		}
	}

	public void onProgressChanged(SeekBar seek, int value, boolean fromTouch) {
		String t = String.valueOf(value + this.mMin);
		this.mValueText.setText(this.mSuffix == null ? t : t.concat(this.mSuffix));
	}

	public void onStartTrackingTouch(SeekBar seek) {}

	public void onStopTrackingTouch(SeekBar seek) {}
}