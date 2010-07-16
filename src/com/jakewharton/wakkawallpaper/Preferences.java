package com.jakewharton.wakkawallpaper;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Settings activity for WakkaWallpaper
 * 
 * @author Jake Wharton
 */
public class Preferences extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
	public static final String SHARED_NAME = "WakkaWallpaper";
	
    @Override
    protected void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        this.getPreferenceManager().setSharedPreferencesName(Preferences.SHARED_NAME);
        this.addPreferencesFromResource(R.xml.preferences);
        this.getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onDestroy() {
        this.getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
    	//no op
    }
}
