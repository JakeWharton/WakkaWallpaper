package com.jakewharton.wakkawallpaper;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;

/**
 * Settings activity for WakkaWallpaper
 * 
 * @author Jake Wharton
 */
public class Preferences extends PreferenceActivity {
	public static final String SHARED_NAME = "WakkaWallpaper";
	
    @Override
    protected void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        
        this.getPreferenceManager().setSharedPreferencesName(Preferences.SHARED_NAME);
        this.addPreferencesFromResource(R.xml.preferences);
        
        final Resources resources = this.getResources();
        
        //reset display
        //TODO: add display settings reset
        
        //reset game
        //TODO: add game settings reset
        
        //reset colors
        //TODO: add color settings reset
        
        //info email
        this.findPreference(resources.getString(R.string.information_contact_email_key)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				final Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("plain/text");
				intent.putExtra(Intent.EXTRA_EMAIL, new String[] { resources.getString(R.string.information_contact_email_data) });
				intent.putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.title));
				
				//launcher email activity
				Preferences.this.startActivity(Intent.createChooser(intent, resources.getString(R.string.information_contact_email)));
				return true;
			}
		});
        
        //info twitter
        this.findPreference(resources.getString(R.string.information_contact_twitter_key)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				final Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(resources.getString(R.string.information_contact_twitter_data)));
				
				Preferences.this.startActivity(Intent.createChooser(intent, resources.getString(R.string.information_contact_twitter)));
				return true;
			}
		});
        
        //info web
        this.findPreference(resources.getString(R.string.information_contact_website_key)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				final Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(resources.getString(R.string.information_contact_website_data)));
				
				Preferences.this.startActivity(Intent.createChooser(intent, resources.getString(R.string.information_contact_website_data)));
				return true;
			}
		});
    }
}
