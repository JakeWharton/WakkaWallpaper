package com.jakewharton.wakkawallpaper;

import android.content.Intent;
import android.content.SharedPreferences;
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
        this.findPreference(resources.getString(R.string.settings_display_reset_key)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				final SharedPreferences.Editor editor = Preferences.this.getPreferenceManager().getSharedPreferences().edit();
				
				//fps
				editor.putInt(resources.getString(R.string.settings_display_fps_key), resources.getInteger(R.integer.display_fps_default));
				//show hud
				editor.putBoolean(resources.getString(R.string.settings_display_showhud_key), resources.getBoolean(R.bool.display_showhud_default));
				//icon rows
				editor.putInt(resources.getString(R.string.settings_display_iconrows_key), resources.getInteger(R.integer.display_iconrows_default));
				//icon cols
				editor.putInt(resources.getString(R.string.settings_display_iconcols_key), resources.getInteger(R.integer.display_iconcols_default));
				//icon row spacing
				editor.putInt(resources.getString(R.string.settings_display_rowspacing_key), resources.getInteger(R.integer.display_rowspacing_default));
				//icon col spacing
				editor.putInt(resources.getString(R.string.settings_display_colspacing_key), resources.getInteger(R.integer.display_colspacing_default));
				//padding top
				editor.putInt(resources.getString(R.string.settings_display_padding_top_key), resources.getInteger(R.integer.display_padding_top_default));
				//padding bottom
				editor.putInt(resources.getString(R.string.settings_display_padding_bottom_key), resources.getInteger(R.integer.display_padding_bottom_default));
				//padding left
				editor.putInt(resources.getString(R.string.settings_display_padding_left_key), resources.getInteger(R.integer.display_padding_left_default));
				//padding right
				editor.putInt(resources.getString(R.string.settings_display_padding_right_key), resources.getInteger(R.integer.display_padding_right_default));

				editor.commit();
				return true;
			}
		});
        
        //reset game
        this.findPreference(resources.getString(R.string.settings_game_reset_key)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				final SharedPreferences.Editor editor = Preferences.this.getPreferenceManager().getSharedPreferences().edit();
				
				//display kill screen
				editor.putBoolean(resources.getString(R.string.settings_game_killscreen_key), resources.getBoolean(R.bool.game_killscreen_default));
				//ghosts are deadly
				editor.putBoolean(resources.getString(R.string.settings_game_deadlyghosts_key), resources.getBoolean(R.bool.game_deadlyghosts_default));
				//ghost count
				editor.putInt(resources.getString(R.string.settings_game_ghostcount_key), resources.getInteger(R.integer.game_ghostcount_default));
				//bonus allowed
				editor.putBoolean(resources.getString(R.string.settings_game_bonuslife_key), resources.getBoolean(R.bool.game_bonuslife_default));
				//bonus threshold
				editor.putInt(resources.getString(R.string.settings_game_bonuslifethreshold_key), resources.getInteger(R.integer.game_bonuslifethreshold_default));
				//fruit enabled
				editor.putBoolean(resources.getString(R.string.settings_game_fruitenabled_key), resources.getBoolean(R.bool.game_fruitenabled_default));
				//fruit one threshold
				editor.putInt(resources.getString(R.string.settings_game_fruitonethreshold_key), resources.getInteger(R.integer.game_fruitonethreshold_default));
				//fruit two threshold
				editor.putInt(resources.getString(R.string.settings_game_fruittwothreshold_key), resources.getInteger(R.integer.game_fruittwothreshold_default));
				//fruit visible lower
				editor.putInt(resources.getString(R.string.settings_game_fruitvisiblelower_key), resources.getInteger(R.integer.game_fruitvisiblelower_default));
				//fruit visible upper
				editor.putInt(resources.getString(R.string.settings_game_fruitvisibleupper_key), resources.getInteger(R.integer.game_fruitvisibleupper_default));

				editor.commit();
				return true;
			}
		});
        
        //reset colors
        this.findPreference(resources.getString(R.string.settings_color_reset_key)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				final SharedPreferences.Editor editor = Preferences.this.getPreferenceManager().getSharedPreferences().edit();
				
				//background
				editor.putInt(resources.getString(R.string.settings_color_game_background_key), resources.getInteger(R.integer.color_game_background_default));
				//dots
				editor.putInt(resources.getString(R.string.settings_color_game_dot_key), resources.getInteger(R.integer.color_game_dot_default));
				//hud foreground
				editor.putInt(resources.getString(R.string.settings_color_game_hudfg_key), resources.getInteger(R.integer.color_game_hudfg_default));
				//hud background
				editor.putInt(resources.getString(R.string.settings_color_game_hudbg_key), resources.getInteger(R.integer.color_game_hudbg_default));
				//"The Man"
				editor.putInt(resources.getString(R.string.settings_color_theman_key), resources.getInteger(R.integer.color_theman_default));
				//eye background
				editor.putInt(resources.getString(R.string.settings_color_ghost_eyebg_key), resources.getInteger(R.integer.color_ghost_eyebg_default));
				//eye foreground
				editor.putInt(resources.getString(R.string.settings_color_ghost_eyefg_key), resources.getInteger(R.integer.color_ghost_eyefg_default));
				//scared body
				editor.putInt(resources.getString(R.string.settings_color_ghost_scaredbg_key), resources.getInteger(R.integer.color_ghost_scaredbg_default));
				//scared eyes
				editor.putInt(resources.getString(R.string.settings_color_ghost_scaredfg_key), resources.getInteger(R.integer.color_ghost_scaredfg_default));
				//scared blinking body
				editor.putInt(resources.getString(R.string.settings_color_ghost_scaredblinkbg_key), resources.getInteger(R.integer.color_ghost_scaredblinkbg_default));
				//scared blinking eyes
				editor.putInt(resources.getString(R.string.settings_color_ghost_scaredblinkfg_key), resources.getInteger(R.integer.color_ghost_scaredblinkfg_default));
				
				editor.commit();
				return true;
			}
		});
        
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
