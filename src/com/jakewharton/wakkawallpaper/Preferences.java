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
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * Settings activity for WakkaWallpaper
 * 
 * @author Jake Wharton
 */
public class Preferences extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
	public static final String SHARED_NAME = "WakkaWallpaper";
	private static final String FILE_NAME = "settings.wakkawallpaper.json";
	private static final String MIME_TYPE = "text/plain";
	private static final int IMPORT_JSON = 1;
	private static final int SELECT_BACKGROUND = 2;
	private static final String FILENAME_CHANGELOG = "changelog.html";
	private static final String FILENAME_CREDITS = "credits.html";
	private static final String FILENAME_INSTRUCTIONS = "instructions.html";
	private static final String FILENAME_TODO = "todo.html";
	private static final String FILENAME_AI = "ai.html";
	
    @Override
    protected void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        
        final PreferenceManager manager = this.getPreferenceManager();
        manager.setSharedPreferencesName(Preferences.SHARED_NAME);
        this.addPreferencesFromResource(R.xml.preferences);
        
        final SharedPreferences preferences = manager.getSharedPreferences();
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
							
							Toast.makeText(Preferences.this, resources.getString(R.string.reset_display_toast), Toast.LENGTH_LONG).show();
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
							
							Toast.makeText(Preferences.this, resources.getString(R.string.reset_game_toast), Toast.LENGTH_LONG).show();
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
							
							Toast.makeText(Preferences.this, resources.getString(R.string.reset_color_toast), Toast.LENGTH_LONG).show();
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
				Preferences.this.viewInstructions();
				return true;
			}
		});
        
        //change log
        this.findPreference(resources.getString(R.string.changelog_key)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				Preferences.this.viewChangelog();
				return true;
			}
		});
        
        //credits
        this.findPreference(resources.getString(R.string.credits_key)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				Preferences.this.viewCredits();
				return true;
			}
		});
        
        //todo
        this.findPreference(resources.getString(R.string.todo_key)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				Preferences.this.viewTodo();
				return true;
			}
		});
        
        //ai
        this.findPreference(resources.getString(R.string.ai_key)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				Preferences.this.viewAI();
				return true;
			}
		});
        
        
        //background image
        this.findPreference(resources.getString(R.string.settings_color_game_bgimage_key)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				Preferences.this.startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI), Preferences.SELECT_BACKGROUND);
				return true;
			}
		});
        
        //clear background image
        this.findPreference(resources.getString(R.string.settings_color_game_bgimageclear_key)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				Preferences.this.getPreferenceManager().getSharedPreferences().edit().putString(resources.getString(R.string.settings_color_game_bgimage_key), null).commit();
				Toast.makeText(Preferences.this, R.string.settings_color_game_bgimageclear_toast, Toast.LENGTH_SHORT).show();
				return true;
			}
		});

        //Register as a preference change listener
        Wallpaper.PREFERENCES.registerOnSharedPreferenceChangeListener(this);
        this.onSharedPreferenceChanged(Wallpaper.PREFERENCES, null);
        
        //Check previously installed version
        final int thisVersion = resources.getInteger(R.integer.version_code);
        final int defaultVersion = resources.getInteger(R.integer.version_code_default);
        final int previousVersion = preferences.getInt(resources.getString(R.string.version_code_key), defaultVersion);
        if (previousVersion == defaultVersion) {
        	//First install
        	
        	//Store this version
        	this.getPreferenceManager().getSharedPreferences().edit().putInt(resources.getString(R.string.version_code_key), thisVersion).commit();
        	//Show hello
        	(new AlertDialog.Builder(this))
        		.setTitle(resources.getString(R.string.welcome))
        		.setMessage(resources.getString(R.string.welcome_firstrun))
        		.setCancelable(true)
        		.setPositiveButton(resources.getString(R.string.yes), new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Preferences.this.viewInstructions();
					}
				})
				.setNegativeButton(resources.getString(R.string.no), null)
				.show();
        } else if (previousVersion < thisVersion) {
        	//First run after upgrade
        	
        	//Store this version
        	this.getPreferenceManager().getSharedPreferences().edit().putInt(resources.getString(R.string.version_code_key), thisVersion).commit();
        	//Show hello
        	(new AlertDialog.Builder(this))
        		.setTitle(resources.getString(R.string.welcome))
        		.setMessage(resources.getString(R.string.welcome_upgrade))
        		.setCancelable(true)
        		.setPositiveButton(resources.getString(R.string.yes), new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Preferences.this.viewChangelog();
					}
				})
				.setNegativeButton(resources.getString(R.string.no), null)
				.show();
        }
    }
    
    /**
     * Open change log.
     */
    private void viewChangelog() {
		final Intent intent = new Intent(this, About.class);
		intent.putExtra(About.EXTRA_FILENAME, Preferences.FILENAME_CHANGELOG);
		intent.putExtra(About.EXTRA_TITLE, this.getResources().getString(R.string.changelog_title));
		this.startActivity(intent);
    }
    
    /**
     * Open instructions.
     */
    private void viewInstructions() {
		final Intent intent = new Intent(this, About.class);
		intent.putExtra(About.EXTRA_FILENAME, Preferences.FILENAME_INSTRUCTIONS);
		intent.putExtra(About.EXTRA_TITLE, this.getResources().getString(R.string.instructions_title));
		this.startActivity(intent);
    }
    
    /**
     * Open credits
     */
    private void viewCredits() {
		final Intent intent = new Intent(this, About.class);
		intent.putExtra(About.EXTRA_FILENAME, Preferences.FILENAME_CREDITS);
		intent.putExtra(About.EXTRA_TITLE, this.getResources().getString(R.string.credits_title));
		this.startActivity(intent);
    }
    
    /**
     * Open todo
     */
    private void viewTodo() {
		final Intent intent = new Intent(this, About.class);
		intent.putExtra(About.EXTRA_FILENAME, Preferences.FILENAME_TODO);
		intent.putExtra(About.EXTRA_TITLE, this.getResources().getString(R.string.todo_title));
		this.startActivity(intent);
    }
    
    /**
     * Open ai
     */
    private void viewAI() {
		final Intent intent = new Intent(this, About.class);
		intent.putExtra(About.EXTRA_FILENAME, Preferences.FILENAME_AI);
		intent.putExtra(About.EXTRA_TITLE, this.getResources().getString(R.string.ai_title));
		this.startActivity(intent);
    }

    /**
     * Handle the changing of a preference.
     */
	public void onSharedPreferenceChanged(final SharedPreferences preferences, final String key) {
		final boolean all = (key == null);
		final Resources resources = this.getResources();
		
		final String mode = resources.getString(R.string.settings_game_mode_key);
		if (all || key.equals(mode)) {
			boolean enableEndless = (Game.Mode.parseInt(preferences.getInt(mode, resources.getInteger(R.integer.game_mode_default))) == Game.Mode.ENDLESS);
			
			this.findPreference(resources.getString(R.string.settings_game_endlessdotregen_key)).setEnabled(enableEndless);
			this.findPreference(resources.getString(R.string.settings_game_endlessjuggerdotregen_key)).setEnabled(enableEndless);
		}
		
		final String bgimage = resources.getString(R.string.settings_color_game_bgimage_key);
		if (all || key.equals(bgimage)) {
			boolean imageEnabled = (preferences.getString(bgimage, null) != null);
			
			this.findPreference(resources.getString(R.string.settings_color_game_background_key)).setEnabled(!imageEnabled);
			this.findPreference(resources.getString(R.string.settings_color_game_bgimageclear_key)).setEnabled(imageEnabled);
		}
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
							
							Toast.makeText(Preferences.this, resources.getString(R.string.reset_all_toast), Toast.LENGTH_LONG).show();
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
		if (resultCode == Activity.RESULT_OK) {
			final Resources resources = this.getResources();
			
			switch (requestCode) {
				case Preferences.IMPORT_JSON:
					
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
					break;
					
				case Preferences.SELECT_BACKGROUND:
					//Store the string value of the background image
				    this.getPreferenceManager().getSharedPreferences().edit().putString(resources.getString(R.string.settings_color_game_bgimage_key), data.getDataString()).commit();
				    Toast.makeText(this, R.string.settings_color_game_bgimage_toast, Toast.LENGTH_SHORT).show();
					break;
					
				default:
					super.onActivityResult(requestCode, resultCode, data);
			}
		}
	}

	private void infoEmail() {
        final Resources resources = this.getResources();
		final Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("plain/text");
		intent.putExtra(Intent.EXTRA_EMAIL, new String[] { resources.getString(R.string.information_contact_email_data) });
		intent.putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.title));
		
		Preferences.this.startActivity(intent);
    }
    
    private void infoTwitter() {
        final Resources resources = this.getResources();
		final Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(resources.getString(R.string.information_contact_twitter_data)));
		
		Preferences.this.startActivity(intent);
    }
    
    private void infoWeb() {
        final Resources resources = this.getResources();
    	final Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(resources.getString(R.string.information_contact_website_data)));
		
		Preferences.this.startActivity(intent);
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
		//hud offset
		editor.putInt(resources.getString(R.string.settings_display_hudoffset_key), resources.getInteger(R.integer.display_hudoffset_default));
		//show walls
		editor.putBoolean(resources.getString(R.string.settings_display_showwalls_key), resources.getBoolean(R.bool.display_showwalls_default));
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
	}
    
    private void loadGameDefaults() {
        final Resources resources = this.getResources();
		final SharedPreferences.Editor editor = Preferences.this.getPreferenceManager().getSharedPreferences().edit();
		
		//mode
		editor.putInt(resources.getString(R.string.settings_game_mode_key), resources.getInteger(R.integer.game_mode_default));
		//endless dot threshold
		editor.putInt(resources.getString(R.string.settings_game_endlessdotregen_key), resources.getInteger(R.integer.game_endlessdotregen_default));
		//endless jugger threshold
		editor.putInt(resources.getString(R.string.settings_game_endlessjuggerdotregen_key), resources.getInteger(R.integer.game_endlessjuggerdotregen_default));
		//display kill screen
		editor.putBoolean(resources.getString(R.string.settings_game_killscreen_key), resources.getBoolean(R.bool.game_killscreen_default));
		//theman mode
		editor.putInt(resources.getString(R.string.settings_game_themanmode_key), resources.getInteger(R.integer.game_themanmode_default));
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
		//walls
		editor.putInt(resources.getString(R.string.settings_color_game_walls_key), resources.getInteger(R.integer.color_game_walls_default));
		//background image
		editor.putString(resources.getString(R.string.settings_color_game_bgimage_key), null);
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
		//entity style
		editor.putInt(resources.getString(R.string.settings_color_entitystyle_key), resources.getInteger(R.integer.color_entitystyle_default));
		//dot style
		editor.putInt(resources.getString(R.string.settings_color_dotstyle_key), resources.getInteger(R.integer.color_dotstyle_default));
		
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
    		final String display_key = resources.getString(R.string.settings_display_key);
    		if (settings.has(display_key)) {
	    		final JSONObject display = settings.getJSONObject(display_key);
	    		
	    		//fps
		        final String display_fps = resources.getString(R.string.settings_display_fps_key);
		        if (display.has(display_fps)) {
		        	editor.putInt(display_fps, display.getInt(display_fps));
		        }
	    		//juggerdot blink
	    		final String display_juggerdotblink = resources.getString(R.string.settings_display_juggerdotblink_key);
	    		if (display.has(display_juggerdotblink)) {
	    			editor.putInt(display_juggerdotblink, display.getInt(display_juggerdotblink));
	    		}
	    		//show hud
				final String display_showhud = resources.getString(R.string.settings_display_showhud_key);
				if (display.has(display_showhud)) {
					editor.putBoolean(display_showhud, display.getBoolean(display_showhud));
				}
	    		//hud offset
	    		final String display_hudoffset = resources.getString(R.string.settings_display_hudoffset_key);
	    		if (display.has(display_hudoffset)) {
	    			editor.putInt(display_hudoffset, display.getInt(display_hudoffset));
	    		}
	    		//show walls
	    		final String display_showwalls = resources.getString(R.string.settings_display_showwalls_key);
	    		if (display.has(display_showwalls)) {
	    			editor.putBoolean(display_showwalls, display.getBoolean(display_showwalls));
	    		}
	    		//icon rows
		        final String display_iconrows = resources.getString(R.string.settings_display_iconrows_key);
		        if (display.has(display_iconrows)) {
		        	editor.putInt(display_iconrows, display.getInt(display_iconrows));
		        }
	    		//icon cols
		        final String display_iconcols = resources.getString(R.string.settings_display_iconcols_key);
		        if (display.has(display_iconcols)) {
		        	editor.putInt(display_iconcols, display.getInt(display_iconcols));
		        }
	    		//icon row spacing
		        final String display_rowspacing = resources.getString(R.string.settings_display_rowspacing_key);
		        if (display.has(display_rowspacing)) {
		        	editor.putInt(display_rowspacing, display.getInt(display_rowspacing));
		        }
	    		//icon col spacing
		        final String display_colspacing = resources.getString(R.string.settings_display_colspacing_key);
		        if (display.has(display_colspacing)) {
		        	editor.putInt(display_colspacing, display.getInt(display_colspacing));
		        }
	    		//padding top
		        final String display_paddingtop = resources.getString(R.string.settings_display_padding_top_key);
		        if (display.has(display_paddingtop)) {
		        	editor.putInt(display_paddingtop, display.getInt(display_paddingtop));
		        }
	    		//padding bottom
		        final String display_paddingbottom = resources.getString(R.string.settings_display_padding_bottom_key);
		        if (display.has(display_paddingbottom)) {
		        	editor.putInt(display_paddingbottom, display.getInt(display_paddingbottom));
		        }
	    		//padding left
		        final String display_paddingleft = resources.getString(R.string.settings_display_padding_left_key);
		        if (display.has(display_paddingleft)) {
		        	editor.putInt(display_paddingleft, display.getInt(display_paddingleft));
		        }
	    		//padding right
		        final String display_paddingright = resources.getString(R.string.settings_display_padding_right_key);
		        if (display.has(display_paddingright)) {
		        	editor.putInt(display_paddingright, display.getInt(display_paddingright));
		        }
    		}
    		
    		//GAME
    		final String game_key = resources.getString(R.string.settings_game_key);
    		if (settings.has(game_key)) {
	    		final JSONObject game = settings.getJSONObject(game_key);
	    		
	    		//mode
	    		final String game_mode = resources.getString(R.string.settings_game_mode_key);
	    		if (game.has(game_mode)) {
	    			editor.putInt(game_mode, game.getInt(game_mode));
	    		}
	    		//endless dot threshold
	    		final String game_endlessdotregen = resources.getString(R.string.settings_game_endlessdotregen_key);
	    		if (game.has(game_endlessdotregen)) {
	    			editor.putInt(game_endlessdotregen, game.getInt(game_endlessdotregen));
	    		}
	    		//endless jugger threshold
	    		final String game_endlessjuggerdotregen = resources.getString(R.string.settings_game_endlessjuggerdotregen_key);
	    		if (game.has(game_endlessjuggerdotregen)) {
	    			editor.putInt(game_endlessjuggerdotregen, game.getInt(game_endlessjuggerdotregen));
	    		}
	    		//display kill screen
		    	final String game_killscreen = resources.getString(R.string.settings_game_killscreen_key);
		    	if (game.has(game_killscreen)) {
		    		editor.putBoolean(game_killscreen, game.getBoolean(game_killscreen));
		    	}
	    		//theman mode
	    		final String game_themanmode = resources.getString(R.string.settings_game_themanmode_key);
	    		if (game.has(game_themanmode)) {
	    			editor.putInt(game_themanmode, game.getInt(game_themanmode));
	    		}
	    		//user controllable
	    		final String game_usercontrol = resources.getString(R.string.settings_game_usercontrol_key);
	    		if (game.has(game_usercontrol)) {
	    			editor.putBoolean(game_usercontrol, game.getBoolean(game_usercontrol));
	    		}
	    		//ghost mode
	    		final String game_ghostmode = resources.getString(R.string.settings_game_ghostmode_key);
	    		if (game.has(game_ghostmode)) {
	    			editor.putInt(game_ghostmode, game.getInt(game_ghostmode));
	    		}
	    		//ghosts are deadly
				final String game_deadlyghosts = resources.getString(R.string.settings_game_deadlyghosts_key);
				if (game.has(game_deadlyghosts)) {
					editor.putBoolean(game_deadlyghosts, game.getBoolean(game_deadlyghosts));
				}
	    		//ghost count
				final String game_ghostcount = resources.getString(R.string.settings_game_ghostcount_key);
				if (game.has(game_ghostcount)) {
					editor.putInt(game_ghostcount, game.getInt(game_ghostcount));
				}
	    		//bonus allowed
				final String game_bonuslife = resources.getString(R.string.settings_game_bonuslife_key);
				if (game.has(game_bonuslife)) {
					editor.putBoolean(game_bonuslife, game.getBoolean(game_bonuslife));
				}
	    		//bonus threshold
				final String game_bonuslifethreshold = resources.getString(R.string.settings_game_bonuslifethreshold_key);
				if (game.has(game_bonuslifethreshold)) {
					editor.putInt(game_bonuslifethreshold, game.getInt(game_bonuslifethreshold));
				}
	    		//fruit enabled
				final String game_fruitenabled = resources.getString(R.string.settings_game_fruitenabled_key);
				if (game.has(game_fruitenabled)) {
					editor.putBoolean(game_fruitenabled, game.getBoolean(game_fruitenabled));
				}
	    		//fruit one threshold
				final String game_fruitonethreshold = resources.getString(R.string.settings_game_fruitonethreshold_key);
				if (game.has(game_fruitonethreshold)) {
					editor.putInt(game_fruitonethreshold, game.getInt(game_fruitonethreshold));
				}
	    		//fruit two threshold
				final String game_fruittwothreshold = resources.getString(R.string.settings_game_fruittwothreshold_key);
				if (game.has(game_fruittwothreshold)) {
					editor.putInt(game_fruittwothreshold, game.getInt(game_fruittwothreshold));
				}
	    		//fruit visible lower
				final String game_fruitvisiblelower = resources.getString(R.string.settings_game_fruitvisiblelower_key);
				if (game.has(game_fruitvisiblelower)) {
					editor.putInt(game_fruitvisiblelower, game.getInt(game_fruitvisiblelower));
				}
	    		//fruit visible upper
				final String game_fruitvisibleupper = resources.getString(R.string.settings_game_fruitvisibleupper_key);
				if (game.has(game_fruitvisibleupper)) {
					editor.putInt(game_fruitvisibleupper, game.getInt(game_fruitvisibleupper));
				}
    		}
    		
    		//COLORS
    		final String color_key = resources.getString(R.string.settings_color_key);
    		if (settings.has(color_key)) {
	    		final JSONObject color = settings.getJSONObject(color_key);
	    		//background
		    	final String color_game_background = resources.getString(R.string.settings_color_game_background_key);
		    	if (color.has(color_game_background)) {
		    		editor.putInt(color_game_background, color.getInt(color_game_background));
		    	}
		    	//wall
		    	final String color_game_walls = resources.getString(R.string.settings_color_game_walls_key);
		    	if (color.has(color_game_walls)) {
		    		editor.putInt(color_game_walls, color.getInt(color_game_walls));
		    	}
	    		//background image
	    		final String color_game_bgimage = resources.getString(R.string.settings_color_game_bgimage_key);
	    		if (color.has(color_game_bgimage)) {
	    			editor.putString(color_game_bgimage, color.getString(color_game_bgimage));
	    		}
	    		//dots
		    	final String color_game_dot = resources.getString(R.string.settings_color_game_dot_key);
		    	if (color.has(color_game_dot)) {
		    		editor.putInt(color_game_dot, color.getInt(color_game_dot));
		    	}
	    		//juggerdots
		    	final String color_game_juggerdot = resources.getString(R.string.settings_color_game_juggerdot_key);
		    	if (color.has(color_game_juggerdot)) {
		    		editor.putInt(color_game_juggerdot, color.getInt(color_game_juggerdot));
		    	}
	    		//hud foreground
		    	final String color_game_hudfg = resources.getString(R.string.settings_color_game_hudfg_key);
		    	if (color.has(color_game_hudfg)) {
		    		editor.putInt(color_game_hudfg, color.getInt(color_game_hudfg));
		    	}
	    		//hud background
		    	final String color_game_hudbg = resources.getString(R.string.settings_color_game_hudbg_key);
		    	if (color.has(color_game_hudbg)) {
		    		editor.putInt(color_game_hudbg, color.getInt(color_game_hudbg));
		    	}
	    		//ready color
		    	final String color_game_ready = resources.getString(R.string.settings_color_game_ready_key);
		    	if (color.has(color_game_ready)) {
		    		editor.putInt(color_game_ready, color.getInt(color_game_ready));
		    	}
	    		//game over color
		    	final String color_game_gameover = resources.getString(R.string.settings_color_game_gameover_key);
		    	if (color.has(color_game_gameover)) {
		    		editor.putInt(color_game_gameover, color.getInt(color_game_gameover));
		    	}
	    		//"The Man"
		    	final String color_theman = resources.getString(R.string.settings_color_theman_key);
		    	if (color.has(color_theman)) {
		    		editor.putInt(color_theman, color.getInt(color_theman));
		    	}
	    		//eye background
		    	final String color_ghost_eyebg = resources.getString(R.string.settings_color_ghost_eyebg_key);
		    	if (color.has(color_ghost_eyebg)) {
		    		editor.putInt(color_ghost_eyebg, color.getInt(color_ghost_eyebg));
		    	}
	    		//eye foreground
		    	final String color_ghost_eyefg = resources.getString(R.string.settings_color_ghost_eyefg_key);
		    	if (color.has(color_ghost_eyefg)) {
		    		editor.putInt(color_ghost_eyefg, color.getInt(color_ghost_eyefg));
		    	}
	    		//scared body
		    	final String color_ghost_scaredbg = resources.getString(R.string.settings_color_ghost_scaredbg_key);
		    	if (color.has(color_ghost_scaredbg)) {
		    		editor.putInt(color_ghost_scaredbg, color.getInt(color_ghost_scaredbg));
		    	}
	    		//scared eyes
		    	final String color_ghost_scaredfg = resources.getString(R.string.settings_color_ghost_scaredfg_key);
		    	if (color.has(color_ghost_scaredfg)) {
		    		editor.putInt(color_ghost_scaredfg, color.getInt(color_ghost_scaredfg));
		    	}
	    		//scared blinking body
		    	final String color_ghost_scaredblinkbg = resources.getString(R.string.settings_color_ghost_scaredblinkbg_key);
		    	if (color.has(color_ghost_scaredblinkbg)) {
		    		editor.putInt(color_ghost_scaredblinkbg, color.getInt(color_ghost_scaredblinkbg));
		    	}
	    		//scared blinking eyes
		    	final String color_ghost_scaredblinkfg = resources.getString(R.string.settings_color_ghost_scaredblinkfg_key);
		    	if (color.has(color_ghost_scaredblinkfg)) {
		    		editor.putInt(color_ghost_scaredblinkfg, color.getInt(color_ghost_scaredblinkfg));
		    	}
	    		//blinky
	    		final String color_ghost_blinky = resources.getString(R.string.settings_color_ghost_blinky_key);
	    		if (color.has(color_ghost_blinky)) {
	    			editor.putInt(color_ghost_blinky, color.getInt(color_ghost_blinky));
	    		}
	    		//pinky
	    		final String color_ghost_pinky = resources.getString(R.string.settings_color_ghost_pinky_key);
	    		if (color.has(color_ghost_pinky)) {
	    			editor.putInt(color_ghost_pinky, color.getInt(color_ghost_pinky));
	    		}
	    		//inky
	    		final String color_ghost_inky = resources.getString(R.string.settings_color_ghost_inky_key);
	    		if (color.has(color_ghost_inky)) {
		    		editor.putInt(color_ghost_inky, color.getInt(color_ghost_inky));
	    		}
	    		//clyde
	    		final String color_ghost_clyde = resources.getString(R.string.settings_color_ghost_clyde_key);
	    		if (color.has(color_ghost_clyde)) {
	    			editor.putInt(color_ghost_clyde, color.getInt(color_ghost_clyde));
	    		}
	    		//entity style
	    		final String color_entitystyle = resources.getString(R.string.settings_color_entitystyle);
	    		if (color.has(color_entitystyle)) {
	    			editor.putInt(color_entitystyle, color.getInt(color_entitystyle));
	    		}
	    		//dot style
	    		final String color_dotstyle = resources.getString(R.string.settings_color_dotstyle);
	    		if (color.has(color_dotstyle)) {
	    			editor.putInt(color_dotstyle, color.getInt(color_dotstyle));
	    		}
    		}
    		
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
    		//hud offset
    		final String display_hudoffset = resources.getString(R.string.settings_display_hudoffset_key);
    		display.put(display_hudoffset, preferences.getInt(display_hudoffset, resources.getInteger(R.integer.display_hudoffset_default)));
    		//show walls
    		final String display_showwalls = resources.getString(R.string.settings_display_showwalls_key);
    		display.put(display_showwalls, preferences.getBoolean(display_showwalls, resources.getBoolean(R.bool.display_showwalls_default)));
			//icon rows
	        final String display_iconrows = resources.getString(R.string.settings_display_iconrows_key);
	        display.put(display_iconrows, preferences.getInt(display_iconrows, resources.getInteger(R.integer.display_iconrows_default)));
			//icon cols
	        final String display_iconcols = resources.getString(R.string.settings_display_iconcols_key);
	        display.put(display_iconcols, preferences.getInt(display_iconcols, resources.getInteger(R.integer.display_iconcols_default)));
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
    		//mode
    		final String game_mode = resources.getString(R.string.settings_game_mode_key);
    		game.put(game_mode, preferences.getInt(game_mode, resources.getInteger(R.integer.game_mode_default)));
    		//endless dot threshold
    		final String game_endlessdotregen = resources.getString(R.string.settings_game_endlessdotregen_key);
    		game.put(game_endlessdotregen, preferences.getInt(game_endlessdotregen, resources.getInteger(R.integer.game_endlessdotregen_default)));
    		//endless juggerdot threshold
    		final String game_endlessjuggerdotregen = resources.getString(R.string.settings_game_endlessjuggerdotregen_key);
    		game.put(game_endlessjuggerdotregen, preferences.getInt(game_endlessjuggerdotregen, resources.getInteger(R.integer.game_endlessjuggerdotregen_default)));
			//display kill screen
	    	final String game_killscreen = resources.getString(R.string.settings_game_killscreen_key);
	    	game.put(game_killscreen, preferences.getBoolean(game_killscreen, resources.getBoolean(R.bool.game_killscreen_default)));
    		//theman mode
    		final String game_themanmode = resources.getString(R.string.settings_game_themanmode_key);
    		game.put(game_themanmode, preferences.getInt(game_themanmode, resources.getInteger(R.integer.game_themanmode_default)));
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
	    	//walls
	    	final String color_game_walls = resources.getString(R.string.settings_color_game_walls_key);
	    	color.put(color_game_walls, preferences.getInt(color_game_walls, resources.getInteger(R.integer.color_game_walls_default)));
    		//background image
    		final String color_game_bgimage = resources.getString(R.string.settings_color_game_bgimage_key);
    		color.put(color_game_bgimage, preferences.getString(color_game_bgimage, null));
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
    		//entity style
    		final String color_entitystyle = resources.getString(R.string.settings_color_entitystyle);
    		color.put(color_entitystyle, preferences.getInt(color_entitystyle, resources.getInteger(R.integer.color_entitystyle_default)));
    		//dot style
    		final String color_dotstyle = resources.getString(R.string.settings_color_dotstyle);
    		color.put(color_dotstyle, preferences.getInt(color_dotstyle, resources.getInteger(R.integer.color_dotstyle_default)));
	    	
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
