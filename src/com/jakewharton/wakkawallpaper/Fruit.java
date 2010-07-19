package com.jakewharton.wakkawallpaper;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.util.Log;

/**
 * The Fruit class is a special reward entity that appears only at specific times.
 * 
 * @author Jake Wharton
 */
public class Fruit extends Entity implements SharedPreferences.OnSharedPreferenceChangeListener {
	enum Type {
		CHERRY(100), STRAWBERRY(300), PEACH(500), APPLE(700), GRAPES(1000), GALAXIAN(2000), BELL(3000), KEY(5000);
		
		public final int points;
		
		private Type(int points) {
			this.points = points;
		}
	}
	
	private static final String TAG = "WakkaWallpaper.Fruit";
	
    private static final int DEFAULT_THRESHOLD_FIRST = 70;
    private static final int DEFAULT_THRESHOLD_SECOND = 170;
    private static final int DEFAULT_VISIBLE_LOWER = 9000;
    private static final int DEFAULT_VISIBLE_UPPER = 10000;
	
	private Type mType;
	private long mCreated;
	private boolean mVisible;
	private int mVisibleLength;
	private int mVisibleLower;
	private int mVisibleUpper;
	private int mThresholdFirst;
	private int mThresholdSecond;
	
	/**
	 * Initialize a new fruit adhering to the parameters.
	 * 
	 * @param startingPositionX X coordinate of the position of the fruit.
	 * @param startingPositionY Y coordinate of the position of the fruit.
	 * @param type Type value representing the type of fruit.
	 * @param visible The length (in milliseconds) that the fruit will be visible on screen.
	 */
	public Fruit() {
		super();

        //Load all preferences or their defaults
        Wallpaper.PREFERENCES.registerOnSharedPreferenceChangeListener(this);
        this.onSharedPreferenceChanged(Wallpaper.PREFERENCES, null);
	}

    /**
     * Handle the changing of a preference.
     */
	public void onSharedPreferenceChanged(final SharedPreferences preferences, final String key) {
		if (Wallpaper.LOG_VERBOSE) {
			Log.v(Fruit.TAG, "> onSharedPreferenceChanged()");
		}
		
		final boolean all = (key == null);
		
		boolean changed = false;
		
		final String thresholdFirst = Wallpaper.CONTEXT.getString(R.string.settings_game_fruitonethreshold_key);
		if (all || key.equals(thresholdFirst)) {
			this.mThresholdFirst = Wallpaper.PREFERENCES.getInt(key, Fruit.DEFAULT_THRESHOLD_FIRST);
			changed = true;
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Fruit.TAG, "First Threshold: " + this.mThresholdFirst);
			}
		}

		final String thresholdSecond = Wallpaper.CONTEXT.getString(R.string.settings_game_fruittwothreshold_key);
		if (all || key.equals(thresholdSecond)) {
			this.mThresholdSecond = Wallpaper.PREFERENCES.getInt(key, Fruit.DEFAULT_THRESHOLD_SECOND);
			changed = true;
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Fruit.TAG, "Second Threshold: " + this.mThresholdSecond);
			}
		}
		
		final String visibleLower = Wallpaper.CONTEXT.getString(R.string.settings_game_fruitvisiblelower_key);
		if (all || key.equals(visibleLower)) {
			this.mVisibleLower = Wallpaper.PREFERENCES.getInt(key, Fruit.DEFAULT_VISIBLE_LOWER);
			changed = true;
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Fruit.TAG, "Visible Lower: " + this.mVisibleLower);
			}
		}
		
		final String visibleUpper = Wallpaper.CONTEXT.getString(R.string.settings_game_fruitvisibleupper_key);
		if (all || key.equals(visibleUpper)) {
			this.mVisibleUpper = Wallpaper.PREFERENCES.getInt(key, Fruit.DEFAULT_VISIBLE_UPPER);
			changed = true;
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Fruit.TAG, "Visible Upper: " + this.mVisibleUpper);
			}
		}
		
		
		if (changed) {
			this.hide();
		}
        
		if (Wallpaper.LOG_VERBOSE) {
			Log.v(Fruit.TAG, "> onSharedPreferenceChanged()");
		}
	}
	
	/**
	 * Return the number of points the current fruit is worth and hide from the screen.
	 * 
	 * @return Integer point value.
	 */
	public int eat() {
		this.hide();
		return this.mType.points;
	}
	
	/**
	 * Move off screen and make invisible.
	 */
	private void hide() {
		this.mVisible = false;
		this.mPosition.set(-1, -1);
	}
	
	@Override
	public void tick(Game game) {
		if (this.mVisible) {
			if ((System.currentTimeMillis() - this.mCreated) > this.mVisibleLength) {
				this.newLevel(game);
			}
		} else {
			final int dotsEaten = game.getDotsEaten();
			if ((dotsEaten > this.mThresholdFirst) || (dotsEaten > this.mThresholdSecond)) {
				this.mVisible = true; 
				this.mVisibleLength = Game.RANDOM.nextInt(this.mVisibleUpper - this.mVisibleLower + 1) + this.mVisibleLower;
				this.setPosition(game.getTheMan().getInitialPosition(game));
				this.mCreated = System.currentTimeMillis();
			}
		}
	}

	@Override
	public void draw(Canvas c) {
		if (this.mType != null) {
			c.save();
			c.translate(this.mLocation.x - this.mCellWidthOverTwo, this.mLocation.y - this.mCellHeightOverTwo);
			
			switch (this.mType) {
				case CHERRY:
					break;
					
				case STRAWBERRY:
					break;
					
				case PEACH:
					break;
					
				case APPLE:
					break;
					
				case GRAPES:
					break;
					
				case GALAXIAN:
					break;
					
				case BELL:
					break;
					
				case KEY:
					break;
			}
			
			c.restore();
		}
	}

	@Override
	protected void moved(Game game) {
		//We do not move
	}

	@Override
	protected void newLevel(Game game) {
		this.hide();
		this.mType = Fruit.getForLevel(game.getLevel());
	}
	

	/**
	 * Return which type of fruit should appear on which level.
	 * 
	 * @param level The level you wish to get fruit for.
	 * @return The Type of fruit for the level.
	 */
	private static Type getForLevel(int level) {
		if (level <= 0) {
			throw new IllegalArgumentException("Level number must be greater than zero.");
		}
		
		switch (level) {
			case 1:
				return Type.CHERRY;
			case 2:
				return Type.STRAWBERRY;
			case 3:
			case 4:
				return Type.PEACH;
			case 5:
			case 6:
				return Type.APPLE;
			case 7:
			case 8:
				return Type.GRAPES;
			case 9:
			case 10:
				return Type.GALAXIAN;
			case 11:
			case 12:
				return Type.BELL;
			default:
				return Type.KEY;
		}
	}
}
