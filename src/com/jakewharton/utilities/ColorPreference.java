package com.jakewharton.utilities;

import com.jakewharton.wakkawallpaper.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class ColorPreference extends DialogPreference {
	private static final int ALPHA = 0xff;
	
	private SurfaceView mPreview;
	private SeekBar mR;
	private SeekBar mG;
	private SeekBar mB;
	private TextView mRValue;
	private TextView mGValue;
	private TextView mBValue;
	private int mColor;
	private Integer mTempColor;
	
	private final OnSeekBarChangeListener mSeekBarChangeListener = new OnSeekBarChangeListener() {
		public void onStopTrackingTouch(SeekBar seekBar) {}
		public void onStartTrackingTouch(SeekBar seekBar) {}
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			final int red = ColorPreference.this.mR.getProgress();
			final int green = ColorPreference.this.mG.getProgress();
			final int blue = ColorPreference.this.mB.getProgress();
			final int color = Color.argb(ColorPreference.ALPHA, red, green, blue);
			
			ColorPreference.this.mRValue.setText(Integer.toString(red));
			ColorPreference.this.mGValue.setText(Integer.toString(green));
			ColorPreference.this.mBValue.setText(Integer.toString(blue));
			
			ColorPreference.this.setValue(color);
		}
	};

	public ColorPreference(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		
		this.setPersistent(true);
		this.setDialogLayoutResource(R.layout.color_preference);
	}
	
	@Override
	protected void onBindDialogView(final View view) {
		super.onBindDialogView(view);
		
		this.mPreview = (SurfaceView)view.findViewById(R.id.preview);
		this.mPreview.setBackgroundColor(this.mColor);
		
		this.mR = (SeekBar)view.findViewById(R.id.red);
		this.mR.setProgress(Color.red(this.mColor));
		this.mR.setOnSeekBarChangeListener(this.mSeekBarChangeListener);
		this.mG = (SeekBar)view.findViewById(R.id.green);
		this.mG.setProgress(Color.green(this.mColor));
		this.mG.setOnSeekBarChangeListener(this.mSeekBarChangeListener);
		this.mB = (SeekBar)view.findViewById(R.id.blue);
		this.mB.setProgress(Color.blue(this.mColor));
		this.mB.setOnSeekBarChangeListener(this.mSeekBarChangeListener);
		
		this.mRValue = (TextView)view.findViewById(R.id.red_value);
		this.mRValue.setText(Integer.toString(Color.red(this.mColor)));
		this.mGValue = (TextView)view.findViewById(R.id.green_value);
		this.mGValue.setText(Integer.toString(Color.green(this.mColor)));
		this.mBValue = (TextView)view.findViewById(R.id.blue_value);
		this.mBValue.setText(Integer.toString(Color.blue(this.mColor)));
	}

	@Override
	protected Object onGetDefaultValue(final TypedArray a, final int index) {
		return a.getInt(index, 0);
	}

	@Override
	protected void onSetInitialValue(final boolean restore, final Object defaultValue) {
		final int color = this.getPersistedInt(defaultValue == null ? 0 : (Integer)defaultValue);
		this.mColor = color;
	}

	@Override
	protected void onDialogClosed(final boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		if (positiveResult) {
			this.mTempColor = this.mColor;
			if (this.callChangeListener(this.mTempColor)) {
				this.saveValue(this.mTempColor);
			}
		}
	}

	public void setValue(final int color) {
		this.mColor = color;
		this.mPreview.setBackgroundColor(color);
	}
	
	public void saveValue(final int color) {
		this.setValue(color);
		this.persistInt(color);
	}
}