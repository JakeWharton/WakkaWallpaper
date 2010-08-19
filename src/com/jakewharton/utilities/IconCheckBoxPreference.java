package com.jakewharton.utilities;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import com.jakewharton.wakkawallpaper.R;

/**
 * A simple CheckBoxPreference which also allows for the display of an icon.
 * 
 * @author Jake Wharton
 */
public class IconCheckBoxPreference extends CheckBoxPreference {
	/**
	 * The icon.
	 */
	private Drawable mIcon;
    
	
	
	/**
	 * Create a new instance of the IconCheckBoxPreference.
	 * 
	 * @param context Context.
	 * @param attrs Attributes.
	 * @param defStyle Style.
	 */
    public IconCheckBoxPreference(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        
        this.setLayoutResource(R.layout.icon_checkbox_preference);
        
        this.mIcon = context.obtainStyledAttributes(attrs, R.styleable.IconPreference, defStyle, 0).getDrawable(R.styleable.IconPreference_icon);
    }

    /**
     * Create a new instance of the IconCheckBoxPreference.
     * 
     * @param context Context.
     * @param attrs Attributes.
     */
    public IconCheckBoxPreference(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    
    
    @Override
    protected void onBindView(final View view) {
        super.onBindView(view);
        
        final ImageView imageView = (ImageView)view.findViewById(R.id.icon);
        if ((imageView != null) && (this.mIcon != null)) {
            imageView.setImageDrawable(this.mIcon);
        }
    }

	/**
     * Sets the icon for this Preference with a Drawable.
     *
     * @param icon The icon for this Preference
     */
    public void setIcon(final Drawable icon) {
        if (((icon == null) && (this.mIcon != null)) || ((icon != null) && (!icon.equals(this.mIcon)))) {
            this.mIcon = icon;
            this.notifyChanged();
        }
    }

    /**
     * Returns the icon of this Preference.
     *
     * @return The icon.
     * @see #setIcon(Drawable)
     */
    public Drawable getIcon() {
        return this.mIcon;
    }
}