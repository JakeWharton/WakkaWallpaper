package com.jakewharton.utilities;

import java.util.LinkedList;
import java.util.List;
import com.jakewharton.wakkawallpaper.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class WidgetLocationsPreference extends DialogPreference {
	private static final String LOG = "WidgetLocationsPreference";
	private static final int RECTANGLE_LENGTH = 4;
	private static final int PADDING = 10;
	
	private WidgetLocatorView mView;
	private String mValue;
	private String mTempValue;
	private int mIconRows;
	private int mIconCols;

	public WidgetLocationsPreference(final Context context, final AttributeSet attrs) {
		super(context, attrs);

		this.setPersistent(true);
	}

	@Override
	protected View onCreateDialogView() {
		final Context context = this.getContext();
		final LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(WidgetLocationsPreference.PADDING, WidgetLocationsPreference.PADDING, WidgetLocationsPreference.PADDING, WidgetLocationsPreference.PADDING);
		
		final TextView text = new TextView(context);
		text.setText(R.string.widgetlocations_howto);
		layout.addView(text, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		
		this.mView = new WidgetLocatorView(context, this.mIconRows, this.mIconCols, this.mValue);
		layout.addView(this.mView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		
		return layout;
	}
	
	public void setIconCounts(final int iconRows, final int iconCols) {
		this.mIconRows = iconRows;
		this.mIconCols = iconCols;
	}

	@Override
	protected Object onGetDefaultValue(final TypedArray a, final int index) {
		return a.getString(index);
	}

	@Override
	protected void onSetInitialValue(final boolean restore, final Object defaultValue) {
		this.mValue = this.getPersistedString(defaultValue == null ? "" : (String)defaultValue);
	}

	@Override
	protected void onDialogClosed(final boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		if (positiveResult) {
			this.mTempValue = this.mValue;
			if (this.callChangeListener(this.mTempValue)) {
				this.saveValue(this.mTempValue);
			}
		}
	}

	private void saveValue(final String value) {
		this.setValue(value);
		this.persistString(value);
	}
	
	private void setValue(final String value) {
		this.mValue = value;
	}
	
	public static List<Rect> convertStringToWidgetList(final String string) {
		final List<Rect> list = new LinkedList<Rect>();
		
		if ((string.length() % WidgetLocationsPreference.RECTANGLE_LENGTH) != 0) {
			throw new IllegalArgumentException("String length must be a multiple of four.");
		}
		
		int i = 0;
		while (i < string.length()) {
			try {
				final Rect r = new Rect();
				r.left = Integer.parseInt(String.valueOf(string.charAt(i)));
				r.top = Integer.parseInt(String.valueOf(string.charAt(i+1)));
				r.right = Integer.parseInt(String.valueOf(string.charAt(i+2)));
				r.bottom = Integer.parseInt(String.valueOf(string.charAt(i+3)));
				list.add(r);
			} catch (NumberFormatException e) {
				Log.w(WidgetLocationsPreference.LOG, "Invalid rectangle: " + string.substring(i, WidgetLocationsPreference.RECTANGLE_LENGTH));
			} finally {
				i += WidgetLocationsPreference.RECTANGLE_LENGTH;
			}
		}
		
		return list;
	}
	
	private class WidgetLocatorView extends View {
		private static final float OFFSET = 5;
		
		private Point mTouchStart;
		private Point mTouchEnd;
		private final int mRows;
		private final int mCols;
		private float mIconWidth;
		private float mIconHeight;
		private float mWidth;
		private float mHeight;
		private final Paint mLine;
		private final Paint mDrawing;
		private final Paint mWidget;
		private final List<Rect> mWidgets;
		
		private final GestureDetector gestureDetector = new GestureDetector(new GestureDetector.OnGestureListener() {
			public boolean onSingleTapUp(MotionEvent e) { return false; }
			public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) { return false; }
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) { return false; }
			public boolean onDown(MotionEvent e) { return false; }
			public void onShowPress(MotionEvent e) {}
			public void onLongPress(MotionEvent e) {
				WidgetLocatorView.this.delete();
			}
		});
		
		public WidgetLocatorView(final Context context, final int rows, final int cols, final String value) {
			super(context);
			
			this.mRows = rows;
			this.mCols = cols;
			
			this.mLine = new Paint(Paint.ANTI_ALIAS_FLAG);
			this.mLine.setColor(Color.GRAY);
			this.mLine.setStrokeWidth(2);
			
			this.mDrawing = new Paint(Paint.ANTI_ALIAS_FLAG);
			this.mDrawing.setColor(Color.RED);
			this.mDrawing.setStyle(Paint.Style.STROKE);
			
			this.mWidget = new Paint(Paint.ANTI_ALIAS_FLAG);
			this.mWidget.setColor(Color.GREEN);
			this.mWidget.setStyle(Paint.Style.STROKE);
			
			this.mWidgets = WidgetLocationsPreference.convertStringToWidgetList(value);
		}

		@Override
		protected void onDraw(final Canvas c) {
			c.save();
			c.translate(WidgetLocatorView.OFFSET, WidgetLocatorView.OFFSET);
			
			//Draw lines
			for (int row = 0; row <= this.mRows; row++) {
				final float rowPosition = row * this.mIconHeight;
				c.drawLine(0, rowPosition, this.mWidth, rowPosition, this.mLine);
			}
			for (int col = 0; col <= this.mCols; col++) {
				final float colPosition = col * this.mIconWidth;
				c.drawLine(colPosition, 0, colPosition, this.mHeight, this.mLine);
			}
			
			final float iconWidthOverTwo = this.mIconWidth / 2.0f;
			final float iconHeightOverTwo = this.mIconHeight / 2.0f;
			final float offset = ((this.mIconHeight < this.mIconWidth) ? this.mIconHeight : this.mIconWidth) / 4.0f;
			
			//Saved widgets
			for (final Rect widget : this.mWidgets) {
				final float left = (widget.left * this.mIconWidth) + iconWidthOverTwo - offset;
				final float right = (widget.right * this.mIconWidth) + iconWidthOverTwo + offset;
				final float top = (widget.top * this.mIconHeight) + iconHeightOverTwo - offset;
				final float bottom = (widget.bottom * this.mIconHeight) + iconHeightOverTwo + offset;
				
				c.drawRect(left, top, right, bottom, this.mWidget);
				c.drawLine(left, top, right, bottom, this.mWidget);
				c.drawLine(left, bottom, right, top, this.mWidget);
			}
			
			//Currently drawing widget
			if (this.mTouchStart != null) {
				final Rect pointRect = this.toRectangle();
				final float left = (pointRect.left * this.mIconWidth) + iconWidthOverTwo - offset;
				final float right = (pointRect.right * this.mIconWidth) + iconWidthOverTwo + offset;
				final float top = (pointRect.top * this.mIconHeight) + iconHeightOverTwo - offset;
				final float bottom = (pointRect.bottom * this.mIconHeight) + iconHeightOverTwo + offset;
				
				c.drawRect(left, top, right, bottom, this.mDrawing);
			}
			
			c.restore();
		}

		@Override
		protected void onSizeChanged(final int width, final int height, final int oldWidth, final int oldHeight) {
			super.onSizeChanged(width, height, oldWidth, oldHeight);
			
			this.mWidth = width - (2 * WidgetLocatorView.OFFSET);
			this.mHeight = height - (2 * WidgetLocatorView.OFFSET);
			this.mIconWidth = this.mWidth / (1.0f * this.mCols);
			this.mIconHeight = this.mHeight / (1.0f * this.mRows);
		}

		@Override
		public boolean onTouchEvent(final MotionEvent event) {
			if (this.gestureDetector.onTouchEvent(event)) {
				return true;
			}
			
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					this.mTouchStart = this.mTouchEnd = this.getPoint(event.getX(), event.getY());
					
					this.invalidate();
					return true;
					
				case MotionEvent.ACTION_MOVE:
					this.mTouchEnd = this.getPoint(event.getX(), event.getY());
					
					this.invalidate();
					return true;
					
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL:
					this.mTouchEnd = this.getPoint(event.getX(), event.getY());
					this.add();
					
					this.mTouchStart = null;
					this.mTouchEnd = null;
					
					this.invalidate();
					return true;
					
				default:
					return super.onTouchEvent(event);
			}
		}
		
		private void add() {
			final Rect newWidget = this.toRectangle();
			final Rect insetWidget = new Rect(newWidget);
			
			//This is so that intersect returns true if they are actually adjacent
			insetWidget.inset(-1, -1);
			
			for (final Rect widget : this.mWidgets) {
				if (Rect.intersects(widget, insetWidget)) {
					return;
				}
			}
			
			if ((newWidget.height() == 0) && (newWidget.width() == 0)) {
				return;
			}
			
			this.mWidgets.add(newWidget);
			this.save();
		}
		
		private void delete() {
			for (final Rect widget : this.mWidgets) {
				if ((this.mTouchEnd.x >= widget.left) && (this.mTouchEnd.x <= widget.right) && (this.mTouchEnd.y >= widget.top) && (this.mTouchEnd.y <= widget.bottom)) {
					this.mWidgets.remove(widget);
					break;
				}
			}
			this.save();
			this.invalidate();
		}
		
		private void save() {
			final StringBuilder builder = new StringBuilder();
			for (final Rect widget : this.mWidgets) {
				builder.append(Integer.toString(widget.left));
				builder.append(Integer.toString(widget.top));
				builder.append(Integer.toString(widget.right));
				builder.append(Integer.toString(widget.bottom));
			}
			WidgetLocationsPreference.this.setValue(builder.toString());
		}
		
		private Point getPoint(float x, float y) {
			x -= WidgetLocatorView.OFFSET;
			y -= WidgetLocatorView.OFFSET;
			int newX = (int)(x / this.mIconWidth);
			int newY = (int)(y / this.mIconHeight);
			
			if (newX < 0) {
				newX = 0;
			} else if (newX >= this.mCols) {
				newX = this.mCols - 1;
			}
			if (newY < 0) {
				newY = 0;
			} else if (newY >= this.mRows) {
				newY = this.mRows - 1;
			}
			
			return new Point(newX, newY);
		}
		
		private Rect toRectangle() {
			final boolean isStartXSmaller = (this.mTouchStart.x < this.mTouchEnd.x);
			final boolean isStartYSmaller = (this.mTouchStart.y < this.mTouchEnd.y);
			
			final Rect r = new Rect();
			r.left = isStartXSmaller ? this.mTouchStart.x : this.mTouchEnd.x;
			r.right = isStartXSmaller ? this.mTouchEnd.x : this.mTouchStart.x;
			r.top = isStartYSmaller ? this.mTouchStart.y : this.mTouchEnd.y;
			r.bottom = isStartYSmaller ? this.mTouchEnd.y : this.mTouchStart.y;
			
			return r;
		}
	}
}
