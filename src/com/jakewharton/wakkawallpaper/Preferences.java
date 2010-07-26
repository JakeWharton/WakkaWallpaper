package com.jakewharton.wakkawallpaper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import org.json.JSONObject;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * Settings activity for WakkaWallpaper
 * 
 * @author Jake Wharton
 */
public class Preferences extends PreferenceActivity {
	public static final String SHARED_NAME = "WakkaWallpaper";
	private static final String FILE_NAME = "settings.wakkawallpaper.json";
	private static final int IMPORT_JSON = 1;
	private static final String MIME_TYPE = "text/plain";
	
    @Override
    protected void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        
        this.getPreferenceManager().setSharedPreferencesName(Preferences.SHARED_NAME);
        this.addPreferencesFromResource(R.xml.preferences);
        
        final Resources resources = this.getResources();
        
        //reset display
        this.findPreference(resources.getString(R.string.settings_display_reset_key)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(final Preference preference) {
				(new AlertDialog.Builder(Preferences.this))
					.setMessage(resources.getString(R.string.reset_display))
					.setCancelable(false)
					.setPositiveButton(resources.getString(R.string.yes), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							Preferences.this.loadDisplayDefaults();
						}
					})
					.setNegativeButton(resources.getString(R.string.no), null)
					.show();
				return true;
			}
		});
        
        //reset game
        this.findPreference(resources.getString(R.string.settings_game_reset_key)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(final Preference preference) {
				(new AlertDialog.Builder(Preferences.this))
				.setMessage(resources.getString(R.string.reset_game))
				.setCancelable(false)
				.setPositiveButton(resources.getString(R.string.yes), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Preferences.this.loadGameDefaults();
					}
				})
				.setNegativeButton(resources.getString(R.string.no), null)
				.show();
				return true;
			}
		});
        
        //reset colors
        this.findPreference(resources.getString(R.string.settings_color_reset_key)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(final Preference preference) {
				(new AlertDialog.Builder(Preferences.this))
				.setMessage(resources.getString(R.string.reset_color))
				.setCancelable(false)
				.setPositiveButton(resources.getString(R.string.yes), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Preferences.this.loadColorDefaults();
					}
				})
				.setNegativeButton(resources.getString(R.string.no), null)
				.show();
				return true;
			}
		});
        
        //info email
        this.findPreference(resources.getString(R.string.information_contact_email_key)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(final Preference preference) {
				Preferences.this.infoEmail();
				return true;
			}
		});
        
        //info twitter
        this.findPreference(resources.getString(R.string.information_contact_twitter_key)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(final Preference preference) {
				Preferences.this.infoTwitter();
				return true;
			}
		});
        
        //info web
        this.findPreference(resources.getString(R.string.information_contact_website_key)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(final Preference preference) {
				Preferences.this.infoWeb();
				return true;
			}
		});
        
        //instructions
        this.findPreference(resources.getString(R.string.instructions_key)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				Preferences.this.startActivity(new Intent(Preferences.this, Instructions.class));
				return true;
			}
		});
        
        //change log
        this.findPreference(resources.getString(R.string.changelog_key)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				Preferences.this.startActivity(new Intent(Preferences.this, ChangeLog.class));
				return true;
			}
		});
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.getMenuInflater().inflate(R.menu.preferences, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final Resources resources = this.getResources();
		
		switch (item.getItemId()) {
			case R.id.menu_import:
				final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				intent.setType(Preferences.MIME_TYPE);
				this.startActivityForResult(Intent.createChooser(intent, resources.getString(R.string.menu_import)), Preferences.IMPORT_JSON);
				return true;
				
			case R.id.menu_export:
				(new AlertDialog.Builder(this))
					.setMessage(resources.getString(R.string.menu_export_prompt))
					.setCancelable(false)
					.setPositiveButton(resources.getString(R.string.yes), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							Preferences.this.jsonExport();
						}
					})
					.setNegativeButton(resources.getString(R.string.no), null)
					.show();
				return true;
				
			case R.id.menu_reset:
				(new AlertDialog.Builder(this))
					.setMessage(resources.getString(R.string.reset_all))
					.setCancelable(false)
					.setPositiveButton(resources.getString(R.string.yes), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							Preferences.this.loadDisplayDefaults();
							Preferences.this.loadGameDefaults();
							Preferences.this.loadColorDefaults();
						}
					})
					.setNegativeButton(resources.getString(R.string.no), null)
					.show();
				return true;
				
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		switch (requestCode) {
			case Preferences.IMPORT_JSON:
				if (resultCode == Activity.RESULT_OK) {
					final Resources resources = this.getResources();
					
					(new AlertDialog.Builder(this))
						.setMessage(resources.getString(R.string.menu_import_prompt))
						.setCancelable(false)
						.setPositiveButton(resources.getString(R.string.yes), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								Preferences.this.jsonImport(data.getData());
							}
						})
						.setNegativeButton(resources.getString(R.string.no), null)
						.show();
				}
			default:
				super.onActivityResult(requestCode, resultCode, data);
		}
	}

	private void infoEmail() {
        final Resources resources = this.getResources();
		final Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("plain/text");
		intent.putExtra(Intent.EXTRA_EMAIL, new String[] { resources.getString(R.string.information_contact_email_data) });
		intent.putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.title));
		
		Preferences.this.startActivity(Intent.createChooser(intent, resources.getString(R.string.information_contact_email)));
    }
    
    private void infoTwitter() {
        final Resources resources = this.getResources();
		final Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(resources.getString(R.string.information_contact_twitter_data)));
		
		Preferences.this.startActivity(Intent.createChooser(intent, resources.getString(R.string.information_contact_twitter)));
    }
    
    private void infoWeb() {
        final Resources resources = this.getResources();
    	final Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(resources.getString(R.string.information_contact_website_data)));
		
		Preferences.this.startActivity(Intent.createChooser(intent, resources.getString(R.string.information_contact_website_data)));
    }
    
    private void loadDisplayDefaults() {
        final Resources resources = this.getResources();
	    final SharedPreferences.Editor editor = Preferences.this.getPreferenceManager().getSharedPreferences().edit();
		
		//fps
		editor.putInt(resources.getString(R.string.settings_display_fps_key), resources.getInteger(R.integer.display_fps_default));
		//juggerdot blink
		editor.putInt(resources.getString(R.string.settings_display_juggerdotblink_key), resources.getInteger(R.integer.display_juggerdotblink_default));
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
		//hud offset
		editor.putInt(resources.getString(R.string.settings_display_hudoffset_key), resources.getInteger(R.integer.display_hudoffset_default));
		//padding top
		editor.putInt(resources.getString(R.string.settings_display_padding_top_key), resources.getInteger(R.integer.display_padding_top_default));
		//padding bottom
		editor.putInt(resources.getString(R.string.settings_display_padding_bottom_key), resources.getInteger(R.integer.display_padding_bottom_default));
		//padding left
		editor.putInt(resources.getString(R.string.settings_display_padding_left_key), resources.getInteger(R.integer.display_padding_left_default));
		//padding right
		editor.putInt(resources.getString(R.string.settings_display_padding_right_key), resources.getInteger(R.integer.display_padding_right_default));
	
		editor.commit();
	}
    
    private void loadGameDefaults() {
        final Resources resources = this.getResources();
		final SharedPreferences.Editor editor = Preferences.this.getPreferenceManager().getSharedPreferences().edit();
		
		//display kill screen
		editor.putBoolean(resources.getString(R.string.settings_game_killscreen_key), resources.getBoolean(R.bool.game_killscreen_default));
		//user controllable
		editor.putBoolean(resources.getString(R.string.settings_game_usercontrol_key), resources.getBoolean(R.bool.game_usercontrol_default));
		//ghost mode
		editor.putInt(resources.getString(R.string.settings_game_ghostmode_key), resources.getInteger(R.integer.game_ghostmode_default));
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
	}
    
    private void loadColorDefaults() {
        final Resources resources = this.getResources();
		final SharedPreferences.Editor editor = Preferences.this.getPreferenceManager().getSharedPreferences().edit();
		
		//background
		editor.putInt(resources.getString(R.string.settings_color_game_background_key), resources.getInteger(R.integer.color_game_background_default));
		//dots
		editor.putInt(resources.getString(R.string.settings_color_game_dot_key), resources.getInteger(R.integer.color_game_dot_default));
		//juggerdots
		editor.putInt(resources.getString(R.string.settings_color_game_juggerdot_key), resources.getInteger(R.integer.color_game_juggerdot_default));
		//hud foreground
		editor.putInt(resources.getString(R.string.settings_color_game_hudfg_key), resources.getInteger(R.integer.color_game_hudfg_default));
		//hud background
		editor.putInt(resources.getString(R.string.settings_color_game_hudbg_key), resources.getInteger(R.integer.color_game_hudbg_default));
		//ready color
		editor.putInt(resources.getString(R.string.settings_color_game_ready_key), resources.getInteger(R.integer.color_game_ready_default));
		//game over color
		editor.putInt(resources.getString(R.string.settings_color_game_gameover_key), resources.getInteger(R.integer.color_game_gameover_default));
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
		//blinky
		editor.putInt(resources.getString(R.string.settings_color_ghost_blinky_key), resources.getInteger(R.integer.color_ghost_blinky_default));
		//pinky
		editor.putInt(resources.getString(R.string.settings_color_ghost_pinky_key), resources.getInteger(R.integer.color_ghost_pinky_default));
		//inky
		editor.putInt(resources.getString(R.string.settings_color_ghost_inky_key), resources.getInteger(R.integer.color_ghost_inky_default));
		//clyde
		editor.putInt(resources.getString(R.string.settings_color_ghost_clyde_key), resources.getInteger(R.integer.color_ghost_clyde_default));
		
		editor.commit();
	}
    
    private void jsonImport(final Uri uri) {
    	final Resources resources = this.getResources();
    	final SharedPreferences.Editor editor = this.getPreferenceManager().getSharedPreferences().edit();
    	
    	try {
    		//Load entire file into JSONOBject
    		BufferedReader file = new BufferedReader(new InputStreamReader(this.getContentResolver().openInputStream(uri)));
    		StringBuilder json = new StringBuilder();
    		String data;
    		while ((data = file.readLine()) != null) {
    			json.append(data);
    		}
    		final JSONObject settings = new JSONObject(json.toString());
    		
    		//DISPLAY
    		final JSONObject display = settings.getJSONObject(resources.getString(R.string.settings_display_key));
    		//fps
	        final String display_fps = resources.getString(R.string.settings_display_fps_key);
    		editor.putInt(display_fps, display.getInt(display_fps));
    		//juggerdot blink
    		final String display_juggerdotblink = resources.getString(R.string.settings_display_juggerdotblink_key);
    		editor.putInt(display_juggerdotblink, display.getInt(display_juggerdotblink));
    		//show hud
			final String display_showhud = resources.getString(R.string.settings_display_showhud_key);
    		editor.putBoolean(display_showhud, display.getBoolean(display_showhud));
    		//icon rows
	        final String display_iconrows = resources.getString(R.string.settings_display_iconrows_key);
    		editor.putInt(display_iconrows, display.getInt(display_iconrows));
    		//icon cols
	        final String display_iconcols = resources.getString(R.string.settings_display_iconcols_key);
    		editor.putInt(display_iconcols, display.getInt(display_iconcols));
    		//icon row spacing
	        final String display_rowspacing = resources.getString(R.string.settings_display_rowspacing_key);
    		editor.putInt(display_rowspacing, display.getInt(display_rowspacing));
    		//icon col spacing
	        final String display_colspacing = resources.getString(R.string.settings_display_colspacing_key); 
    		editor.putInt(display_colspacing, display.getInt(display_colspacing));
    		//hud offset
    		final String display_hudoffset = resources.getString(R.string.settings_display_hudoffset_key);
    		editor.putInt(display_hudoffset, display.getInt(display_hudoffset));
    		//padding top
	        final String display_paddingtop = resources.getString(R.string.settings_display_padding_top_key);
    		editor.putInt(display_paddingtop, display.getInt(display_paddingtop));
    		//padding bottom
	        final String display_paddingbottom = resources.getString(R.string.settings_display_padding_bottom_key);
    		editor.putInt(display_paddingbottom, display.getInt(display_paddingbottom));
    		//padding left
	        final String display_paddingleft = resources.getString(R.string.settings_display_padding_left_key);
    		editor.putInt(display_paddingleft, display.getInt(display_paddingleft));
    		//padding right
	        final String display_paddingright = resources.getString(R.string.settings_display_padding_right_key);
    		editor.putInt(display_paddingright, display.getInt(display_paddingright));
    		
    		//GAME
    		final JSONObject game = settings.getJSONObject(resources.getString(R.string.settings_game_key));
    		//display kill screen
	    	final String game_killscreen = resources.getString(R.string.settings_game_killscreen_key);
    		editor.putBoolean(game_killscreen, game.getBoolean(game_killscreen));
    		//user controllable
    		final String game_usercontrol = resources.getString(R.string.settings_game_usercontrol_key);
    		editor.putBoolean(game_usercontrol, game.getBoolean(game_usercontrol));
    		//ghost mode
    		final String game_ghostmode = resources.getString(R.string.settings_game_ghostmode_key);
    		editor.putInt(game_ghostmode, game.getInt(game_ghostmode));
    		//ghosts are deadly
			final String game_deadlyghosts = resources.getString(R.string.settings_game_deadlyghosts_key);
    		editor.putBoolean(game_deadlyghosts, game.getBoolean(game_deadlyghosts));
    		//ghost count
			final String game_ghostcount = resources.getString(R.string.settings_game_ghostcount_key);
    		editor.putInt(game_ghostcount, game.getInt(game_ghostcount));
    		//bonus allowed
			final String game_bonuslife = resources.getString(R.string.settings_game_bonuslife_key);
    		editor.putBoolean(game_bonuslife, game.getBoolean(game_bonuslife));
    		//bonus threshold
			final String game_bonuslifethreshold = resources.getString(R.string.settings_game_bonuslifethreshold_key);
    		editor.putInt(game_bonuslifethreshold, game.getInt(game_bonuslifethreshold));
    		//fruit enabled
			final String game_fruitenabled = resources.getString(R.string.settings_game_fruitenabled_key);
    		editor.putBoolean(game_fruitenabled, game.getBoolean(game_fruitenabled));
    		//fruit one threshold
			final String game_fruitonethreshold = resources.getString(R.string.settings_game_fruitonethreshold_key);
    		editor.putInt(game_fruitonethreshold, game.getInt(game_fruitonethreshold));
    		//fruit two threshold
			final String game_fruittwothreshold = resources.getString(R.string.settings_game_fruittwothreshold_key); 
    		editor.putInt(game_fruittwothreshold, game.getInt(game_fruittwothreshold));
    		//fruit visible lower
			final String game_fruitvisiblelower = resources.getString(R.string.settings_game_fruitvisiblelower_key);
    		editor.putInt(game_fruitvisiblelower, game.getInt(game_fruitvisiblelower));
    		//fruit visible upper
			final String game_fruitvisibleupper = resources.getString(R.string.settings_game_fruitvisibleupper_key);
    		editor.putInt(game_fruitvisibleupper, game.getInt(game_fruitvisibleupper));
    		
    		//COLORS
    		final JSONObject color = settings.getJSONObject(resources.getString(R.string.settings_color_key));
    		//background
	    	final String color_game_background = resources.getString(R.string.settings_color_game_background_key);
    		editor.putInt(color_game_background, color.getInt(color_game_background));
    		//dots
	    	final String color_game_dot = resources.getString(R.string.settings_color_game_dot_key);
    		editor.putInt(color_game_dot, color.getInt(color_game_dot));
    		//juggerdots
	    	final String color_game_juggerdot = resources.getString(R.string.settings_color_game_juggerdot_key);
    		editor.putInt(color_game_juggerdot, color.getInt(color_game_juggerdot));
    		//hud foreground
	    	final String color_game_hudfg = resources.getString(R.string.settings_color_game_hudfg_key);
    		editor.putInt(color_game_hudfg, color.getInt(color_game_hudfg));
    		//hud background
	    	final String color_game_hudbg = resources.getString(R.string.settings_color_game_hudbg_key);
    		editor.putInt(color_game_hudbg, color.getInt(color_game_hudbg));
    		//ready color
	    	final String color_game_ready = resources.getString(R.string.settings_color_game_ready_key);
    		editor.putInt(color_game_ready, color.getInt(color_game_ready));
    		//game over color
	    	final String color_game_gameover = resources.getString(R.string.settings_color_game_gameover_key);
    		editor.putInt(color_game_gameover, color.getInt(color_game_gameover));
    		//"The Man"
	    	final String color_theman = resources.getString(R.string.settings_color_theman_key);
    		editor.putInt(color_theman, color.getInt(color_theman));
    		//eye background
	    	final String color_ghost_eyebg = resources.getString(R.string.settings_color_ghost_eyebg_key);
    		editor.putInt(color_ghost_eyebg, color.getInt(color_ghost_eyebg));
    		//eye foreground
	    	final String color_ghost_eyefg = resources.getString(R.string.settings_color_ghost_eyefg_key);
    		editor.putInt(color_ghost_eyefg, color.getInt(color_ghost_eyefg));
    		//scared body
	    	final String color_ghost_scaredbg = resources.getString(R.string.settings_color_ghost_scaredbg_key);
    		editor.putInt(color_ghost_scaredbg, color.getInt(color_ghost_scaredbg));
    		//scared eyes
	    	final String color_ghost_scaredfg = resources.getString(R.string.settings_color_ghost_scaredfg_key);
    		editor.putInt(color_ghost_scaredfg, color.getInt(color_ghost_scaredfg));
    		//scared blinking body
	    	final String color_ghost_scaredblinkbg = resources.getString(R.string.settings_color_ghost_scaredblinkbg_key);
    		editor.putInt(color_ghost_scaredblinkbg, color.getInt(color_ghost_scaredblinkbg));
    		//scared blinking eyes
	    	final String color_ghost_scaredblinkfg = resources.getString(R.string.settings_color_ghost_scaredblinkfg_key);
    		editor.putInt(color_ghost_scaredblinkfg, color.getInt(color_ghost_scaredblinkfg));
    		//blinky
    		final String color_ghost_blinky = resources.getString(R.string.settings_color_ghost_blinky_key);
    		editor.putInt(color_ghost_blinky, color.getInt(color_ghost_blinky));
    		//pinky
    		final String color_ghost_pinky = resources.getString(R.string.settings_color_ghost_pinky_key);
    		editor.putInt(color_ghost_pinky, color.getInt(color_ghost_pinky));
    		//inky
    		final String color_ghost_inky = resources.getString(R.string.settings_color_ghost_inky_key);
    		editor.putInt(color_ghost_inky, color.getInt(color_ghost_inky));
    		//clyde
    		final String color_ghost_clyde = resources.getString(R.string.settings_color_ghost_clyde_key);
    		editor.putInt(color_ghost_clyde, color.getInt(color_ghost_clyde));
    		
    		//save changes
    		editor.commit();
    		
    		//cheers
    		Toast.makeText(this, R.string.menu_import_success, Toast.LENGTH_SHORT).show();
    	} catch (Exception e) {
    		e.printStackTrace();
    		Toast.makeText(this, R.string.menu_import_failed, Toast.LENGTH_LONG).show();
    	}
    }
    
    private void jsonExport() {
        final Resources resources = this.getResources();
        final SharedPreferences preferences = this.getPreferenceManager().getSharedPreferences();

        try {
	    	//DISPLAY
	        final JSONObject display = new JSONObject();
			//fps
	        final String display_fps = resources.getString(R.string.settings_display_fps_key);
			display.put(display_fps, preferences.getInt(display_fps, resources.getInteger(R.integer.display_fps_default)));
    		//juggerdot blink
    		final String display_juggerdotblink = resources.getString(R.string.settings_display_juggerdotblink_key);
    		display.put(display_juggerdotblink, preferences.getInt(display_juggerdotblink, resources.getInteger(R.integer.display_juggerdotblink_default)));
			//show hud
			final String display_showhud = resources.getString(R.string.settings_display_showhud_key);
	        display.put(display_showhud, preferences.getBoolean(display_showhud, resources.getBoolean(R.bool.display_showhud_default)));
			//icon rows
	        final String display_iconrows = resources.getString(R.string.settings_display_iconrows_key);
	        display.put(display_iconrows, preferences.getInt(display_iconrows, resources.getInteger(R.integer.display_iconrows_default)));
			//icon cols
	        final String display_iconcols = resources.getString(R.string.settings_display_iconcols_key);
	        display.put(display_iconcols, preferences.getInt(display_iconcols, resources.getInteger(R.integer.display_iconcols_default)));
    		//hud offset
    		final String display_hudoffset = resources.getString(R.string.settings_display_hudoffset_key);
    		display.put(display_hudoffset, preferences.getInt(display_hudoffset, resources.getInteger(R.integer.display_hudoffset_default)));
			//icon row spacing
	        final String display_rowspacing = resources.getString(R.string.settings_display_rowspacing_key);
	        display.put(display_rowspacing, preferences.getInt(display_rowspacing, resources.getInteger(R.integer.display_rowspacing_default)));
			//icon col spacing
	        final String display_colspacing = resources.getString(R.string.settings_display_colspacing_key); 
	        display.put(display_colspacing, preferences.getInt(display_colspacing, resources.getInteger(R.integer.display_colspacing_default)));
			//padding top
	        final String display_paddingtop = resources.getString(R.string.settings_display_padding_top_key);
	        display.put(display_paddingtop, preferences.getInt(display_paddingtop, resources.getInteger(R.integer.display_padding_top_default)));
			//padding bottom
	        final String display_paddingbottom = resources.getString(R.string.settings_display_padding_bottom_key);
	        display.put(display_paddingbottom, preferences.getInt(display_paddingbottom, resources.getInteger(R.integer.display_padding_bottom_default)));
			//padding left
	        final String display_paddingleft = resources.getString(R.string.settings_display_padding_left_key);
	        display.put(display_paddingleft, preferences.getInt(display_paddingleft, resources.getInteger(R.integer.display_padding_left_default)));
			//padding right
	        final String display_paddingright = resources.getString(R.string.settings_display_padding_right_key);
	        display.put(display_paddingright, preferences.getInt(display_paddingright, resources.getInteger(R.integer.display_padding_right_default)));
	    	
	    	//GAME
	    	final JSONObject game = new JSONObject();
			//display kill screen
	    	final String game_killscreen = resources.getString(R.string.settings_game_killscreen_key);
	    	game.put(game_killscreen, preferences.getBoolean(game_killscreen, resources.getBoolean(R.bool.game_killscreen_default)));
	    	//user controllable
	    	final String game_usercontrol = resources.getString(R.string.settings_game_usercontrol_key);
	    	game.put(game_usercontrol, preferences.getBoolean(game_usercontrol, resources.getBoolean(R.bool.game_usercontrol_default)));
    		//ghost mode
    		final String game_ghostmode = resources.getString(R.string.settings_game_ghostmode_key);
    		game.put(game_ghostmode, preferences.getInt(game_ghostmode, resources.getInteger(R.integer.game_ghostmode_default)));
			//ghosts are deadly
			final String game_deadlyghosts = resources.getString(R.string.settings_game_deadlyghosts_key);
			game.put(game_deadlyghosts, preferences.getBoolean(game_deadlyghosts, resources.getBoolean(R.bool.game_deadlyghosts_default)));
			//ghost count
			final String game_ghostcount = resources.getString(R.string.settings_game_ghostcount_key);
			game.put(game_ghostcount, preferences.getInt(game_ghostcount, resources.getInteger(R.integer.game_ghostcount_default)));
			//bonus allowed
			final String game_bonuslife = resources.getString(R.string.settings_game_bonuslife_key);
			game.put(game_bonuslife, preferences.getBoolean(game_bonuslife, resources.getBoolean(R.bool.game_bonuslife_default)));
			//bonus threshold
			final String game_bonuslifethreshold = resources.getString(R.string.settings_game_bonuslifethreshold_key);
			game.put(game_bonuslifethreshold, preferences.getInt(game_bonuslifethreshold, resources.getInteger(R.integer.game_bonuslifethreshold_default)));
			//fruit enabled
			final String game_fruitenabled = resources.getString(R.string.settings_game_fruitenabled_key);
			game.put(game_fruitenabled, preferences.getBoolean(game_fruitenabled, resources.getBoolean(R.bool.game_fruitenabled_default)));
			//fruit one threshold
			final String game_fruitonethreshold = resources.getString(R.string.settings_game_fruitonethreshold_key);
			game.put(game_fruitonethreshold, preferences.getInt(game_fruitonethreshold, resources.getInteger(R.integer.game_fruitonethreshold_default)));
			//fruit two threshold
			final String game_fruittwothreshold = resources.getString(R.string.settings_game_fruittwothreshold_key); 
			game.put(game_fruittwothreshold, preferences.getInt(game_fruittwothreshold, resources.getInteger(R.integer.game_fruittwothreshold_default)));
			//fruit visible lower
			final String game_fruitvisiblelower = resources.getString(R.string.settings_game_fruitvisiblelower_key);
			game.put(game_fruitvisiblelower, preferences.getInt(game_fruitvisiblelower, resources.getInteger(R.integer.game_fruitvisiblelower_default)));
			//fruit visible upper
			final String game_fruitvisibleupper = resources.getString(R.string.settings_game_fruitvisibleupper_key);
			game.put(game_fruitvisibleupper, preferences.getInt(game_fruitvisibleupper, resources.getInteger(R.integer.game_fruitvisibleupper_default)));
	    	
	    	//COLORS
	    	final JSONObject color = new JSONObject();
			//background
	    	final String color_game_background = resources.getString(R.string.settings_color_game_background_key);
	    	color.put(color_game_background, preferences.getInt(color_game_background, resources.getInteger(R.integer.color_game_background_default)));
			//dots
	    	final String color_game_dot = resources.getString(R.string.settings_color_game_dot_key);
	    	color.put(color_game_dot, preferences.getInt(color_game_dot, resources.getInteger(R.integer.color_game_dot_default)));
			//jugger dots
	    	final String color_game_juggerdot = resources.getString(R.string.settings_color_game_juggerdot_key);
	    	color.put(color_game_juggerdot, preferences.getInt(color_game_juggerdot, resources.getInteger(R.integer.color_game_juggerdot_default)));
			//hud foreground
	    	final String color_game_hudfg = resources.getString(R.string.settings_color_game_hudfg_key);
	    	color.put(color_game_hudfg, preferences.getInt(color_game_hudfg, resources.getInteger(R.integer.color_game_hudfg_default)));
			//hud background
	    	final String color_game_hudbg = resources.getString(R.string.settings_color_game_hudbg_key);
	    	color.put(color_game_hudbg, preferences.getInt(color_game_hudbg, resources.getInteger(R.integer.color_game_hudbg_default)));
			//ready color
	    	final String color_game_ready = resources.getString(R.string.settings_color_game_ready_key);
	    	color.put(color_game_ready, preferences.getInt(color_game_ready, resources.getInteger(R.integer.color_game_ready_default)));
			//game over color
	    	final String color_game_gameover = resources.getString(R.string.settings_color_game_gameover_key);
	    	color.put(color_game_gameover, preferences.getInt(color_game_gameover, resources.getInteger(R.integer.color_game_gameover_default)));
			//"The Man"
	    	final String color_theman = resources.getString(R.string.settings_color_theman_key);
	    	color.put(color_theman, preferences.getInt(color_theman, resources.getInteger(R.integer.color_theman_default)));
			//eye background
	    	final String color_ghost_eyebg = resources.getString(R.string.settings_color_ghost_eyebg_key);
	    	color.put(color_ghost_eyebg, preferences.getInt(color_ghost_eyebg, resources.getInteger(R.integer.color_ghost_eyebg_default)));
			//eye foreground
	    	final String color_ghost_eyefg = resources.getString(R.string.settings_color_ghost_eyefg_key);
	    	color.put(color_ghost_eyefg, preferences.getInt(color_ghost_eyefg, resources.getInteger(R.integer.color_ghost_eyefg_default)));
			//scared body
	    	final String color_ghost_scaredbg = resources.getString(R.string.settings_color_ghost_scaredbg_key);
	    	color.put(color_ghost_scaredbg, preferences.getInt(color_ghost_scaredbg, resources.getInteger(R.integer.color_ghost_scaredbg_default)));
			//scared eyes
	    	final String color_ghost_scaredfg = resources.getString(R.string.settings_color_ghost_scaredfg_key);
	    	color.put(color_ghost_scaredfg, preferences.getInt(color_ghost_scaredfg, resources.getInteger(R.integer.color_ghost_scaredfg_default)));
			//scared blinking body
	    	final String color_ghost_scaredblinkbg = resources.getString(R.string.settings_color_ghost_scaredblinkbg_key);
	    	color.put(color_ghost_scaredblinkbg, preferences.getInt(color_ghost_scaredblinkbg, resources.getInteger(R.integer.color_ghost_scaredblinkbg_default)));
			//scared blinking eyes
	    	final String color_ghost_scaredblinkfg = resources.getString(R.string.settings_color_ghost_scaredblinkfg_key);
	    	color.put(color_ghost_scaredblinkfg, preferences.getInt(color_ghost_scaredblinkfg, resources.getInteger(R.integer.color_ghost_scaredblinkfg_default)));
	    	//blinky
	    	final String color_ghost_blinky = resources.getString(R.string.settings_color_ghost_blinky_key);
	    	color.put(color_ghost_blinky, preferences.getInt(color_ghost_blinky, resources.getInteger(R.integer.color_ghost_blinky_default)));
	    	//pinky
	    	final String color_ghost_pinky = resources.getString(R.string.settings_color_ghost_pinky_key);
	    	color.put(color_ghost_pinky, preferences.getInt(color_ghost_pinky, resources.getInteger(R.integer.color_ghost_pinky_default)));
	    	//inky
	    	final String color_ghost_inky = resources.getString(R.string.settings_color_ghost_inky_key);
	    	color.put(color_ghost_inky, preferences.getInt(color_ghost_inky, resources.getInteger(R.integer.color_ghost_inky_default)));
	    	//clyde
	    	final String color_ghost_clyde = resources.getString(R.string.settings_color_ghost_clyde_key);
	    	color.put(color_ghost_clyde, preferences.getInt(color_ghost_clyde, resources.getInteger(R.integer.color_ghost_clyde_default)));
	    	
	    	//ALL
	    	final JSONObject settings = new JSONObject();
	    	settings.put(resources.getString(R.string.settings_display_key), display);
	    	settings.put(resources.getString(R.string.settings_game_key), game);
	    	settings.put(resources.getString(R.string.settings_color_key), color);
	    	
	    	//write to disk
	    	PrintWriter file = new PrintWriter(new FileOutputStream(new File(Environment.getExternalStorageDirectory(), Preferences.FILE_NAME)));
	    	file.write(settings.toString());
	    	file.close();
	    	
	    	//cheers
	    	Toast.makeText(this, R.string.menu_export_success, Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, R.string.menu_export_failed, Toast.LENGTH_LONG).show();
		}
    }
}
