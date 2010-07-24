package com.jakewharton.wakkawallpaper;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
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
	
	private Type mType;
	private long mCreated;
	private boolean mIsVisible;
	private int mVisibleLength;
	private int mVisibleLower;
	private int mVisibleUpper;
	private int mThresholdFirst;
	private int mThresholdSecond;
	private int mNumberDisplayed;
	private final Paint mTempColor;
	
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
		
		this.mTempColor = new Paint();
		this.mTempColor.setAntiAlias(true);
		this.mTempColor.setColor(0xffff6666);

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
		final Resources resources = Wallpaper.CONTEXT.getResources();
		
		boolean changed = false;
		
		final String thresholdFirst = Wallpaper.CONTEXT.getString(R.string.settings_game_fruitonethreshold_key);
		if (all || key.equals(thresholdFirst)) {
			this.mThresholdFirst = Wallpaper.PREFERENCES.getInt(key, resources.getInteger(R.integer.game_fruitonethreshold_default));
			changed = true;
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Fruit.TAG, "First Threshold: " + this.mThresholdFirst);
			}
		}

		final String thresholdSecond = Wallpaper.CONTEXT.getString(R.string.settings_game_fruittwothreshold_key);
		if (all || key.equals(thresholdSecond)) {
			this.mThresholdSecond = Wallpaper.PREFERENCES.getInt(key, resources.getInteger(R.integer.game_fruittwothreshold_default));
			changed = true;
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Fruit.TAG, "Second Threshold: " + this.mThresholdSecond);
			}
		}
		
		final String visibleLower = Wallpaper.CONTEXT.getString(R.string.settings_game_fruitvisiblelower_key);
		if (all || key.equals(visibleLower)) {
			this.mVisibleLower = Wallpaper.PREFERENCES.getInt(key, resources.getInteger(R.integer.game_fruitvisiblelower_default));
			changed = true;
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Fruit.TAG, "Visible Lower: " + this.mVisibleLower);
			}
		}
		
		final String visibleUpper = Wallpaper.CONTEXT.getString(R.string.settings_game_fruitvisibleupper_key);
		if (all || key.equals(visibleUpper)) {
			this.mVisibleUpper = Wallpaper.PREFERENCES.getInt(key, resources.getInteger(R.integer.game_fruitvisibleupper_default));
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
		this.mIsVisible = false;
		this.mPosition.set(-1, -1);
	}
	
	/**
	 * Whether or not the fruit is on the board.
	 * 
	 * @return Boolean.
	 */
	public boolean isVisible() {
		return this.mIsVisible;
	}
	
	@Override
	public void tick(final Game game) {
		if (this.mIsVisible) {
			if ((System.currentTimeMillis() - this.mCreated) > this.mVisibleLength) {
				this.hide();
			}
		} else {
			final int dotsEaten = game.getDotsEaten();
			if (((dotsEaten > this.mThresholdFirst) && (this.mNumberDisplayed == 0)) || ((dotsEaten > this.mThresholdSecond) && (this.mNumberDisplayed == 1))) {
				this.mIsVisible = true;
				this.mNumberDisplayed += 1;
				this.mVisibleLength = Game.RANDOM.nextInt(this.mVisibleUpper - this.mVisibleLower + 1) + this.mVisibleLower;
				this.setPosition(game.getTheMan().getInitialPosition(game));
				this.mCreated = System.currentTimeMillis();
			}
		}
	}

	@Override
	public void draw(final Canvas c) {
		if (this.mIsVisible) {
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
			
			c.drawCircle(this.mCellWidthOverTwo, this.mCellHeightOverTwo, this.mCellWidthOverTwo, this.mTempColor);
			
			c.restore();
		}
	}

	@Override
	protected void moved(final Game game) {
		//We do not move
	}

	@Override
	protected void newLevel(final Game game) {
		this.hide();
		this.mNumberDisplayed = 0;
		this.mType = Fruit.getForLevel(game.getLevel());
	}
	
	@Override
	public void newLife(final Game game) {
		//We do not move
	}
	

	/**
	 * Return which type of fruit should appear on which level.
	 * 
	 * @param level The level you wish to get fruit for.
	 * @return The Type of fruit for the level.
	 */
	private static Type getForLevel(final int level) {
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
