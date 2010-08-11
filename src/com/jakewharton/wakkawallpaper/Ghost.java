package com.jakewharton.wakkawallpaper;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

/**
 * The Ghost class represents an enemy on the board.
 * 
 * @author Jake Wharton
 */
public abstract class Ghost extends Entity implements SharedPreferences.OnSharedPreferenceChangeListener {
	enum State { HUNTING, FRIGHTENED, EATEN }
	enum Strategy { CHASE, SCATTER, RANDOM }
	enum Character { GHOST, SPRITES }
	enum Mode {
		CHASE_AND_SCATTER(0), CHASE_ONLY(1), SCATTER_ONLY(2), RANDOM_TURNS(3);
		
		public final int value;
		
		private Mode(final int value) {
			this.value = value;
		}
		
		public static Ghost.Mode parseInt(final int modeValue) {
			for (final Ghost.Mode mode : Ghost.Mode.values()) {
				if (mode.value == modeValue) {
					return mode;
				}
			}
			throw new IllegalArgumentException("Unknown Ghost mode value: " + modeValue);
		}
	}

	private static final String TAG = "WakkaWallpaper.Ghost";
	private static final int FRIGHTENED_LENGTH_BLINK = 1000;
	private static final int SPRITE_OFFSET_HUNTING = 0;
	private static final int SPRITE_OFFSET_FRIGHTENED = 1;
	private static final int SPRITE_OFFSET_FRIGHTENED_BLINK = 2;
	private static final int SPRITE_OFFSET_EATEN = 3;
	private static final int[] FRIGHTENED_LENGTH = new int[] {
		/* Level 1   */ 6000,
		/* Level 2   */ 5000,
		/* Level 3   */ 4000,
		/* Level 4   */ 3000,
		/* Level 5   */ 2000,
		/* Level 6   */ 5000,
		/* Level 7   */ 2000,
		/* Level 8   */ 2000,
		/* Level 9   */ 1000,
		/* Level 10  */ 5000,
		/* Level 11  */ 2000,
		/* Level 12  */ 1000,
		/* Level 13  */ 1000,
		/* Level 14  */ 3000,
		/* Level 15  */ 1000,
		/* Level 16  */ 1000,
		/* Level 17  */ 0,
		/* Level 18  */ 1000,
		/* Level 19+ */ 0,
	};
	private static final int[][] CHASE_AND_SCATTER_TIMES = new int[][] {
		/* Level 1  */ new int[] { -7000, 20000, -7000, 20000, -5000, 20000, -5000, 0 },
		/* Level 2  */ new int[] { -7000, 20000, -7000, 20000, -5000, 1033000, -17, 0 },
		/* Level 3  */ new int[] { -7000, 20000, -7000, 20000, -5000, 1033000, -17, 0 },
		/* Level 4  */ new int[] { -7000, 20000, -7000, 20000, -5000, 1033000, -17, 0 },
		/* Level 5+ */ new int[] { -5000, 20000, -5000, 20000, -5000, 1037000, -17, 0 },
	};
	
	protected Ghost.State mState;
	protected Ghost.Strategy mStrategyCurrent;
	protected Ghost.Strategy mStrategyLast;
	protected Ghost.Mode mMode;
	private Ghost.Character mCharacter;
	protected final Paint mBodyBackground;
	private final Paint mEyeBackground;
	private final Paint mEyeForeground;
	private final Paint mScaredBackground;
	private final Paint mScaredMouthForeground;
	private final Paint mScaredEyeForeground;
	private final Paint mScaredBlinkBackground;
	private final Paint mScaredBlinkMouthForeground;
	private final Paint mScaredBlinkEyeForeground;
	private final Path[] mBody;
	private Path mScaredMouth;
	private float mCellWidthOverThree;
	private float mCellHeightOverThree;
	private float mCellWidthOverSeven;
	private float mCellWidthOverFourteen;
	private int mStateTimer;
	private long mStateLastTime;
	private int mModePointer;
	private int mModeTimer;
	private long mModeLastTime;
	private boolean mIsTrophyLogosEnabled;
	private boolean mIsTrophyCeosEnabled;
	private boolean mIsTrophyGoogolEnabled;
	private boolean mIsTrophyDessertsEnabled;
	private Bitmap mSprites;
	private final int mSpriteIndex;
	
    /**
     * Create a new ghost.
     * 
     * @param backgroundColor Primary color of the ghost.
     */
	protected Ghost(final int spriteIndex) {
		super();
		
		this.mSpriteIndex = spriteIndex;
		
		this.mBodyBackground = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.mEyeBackground = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.mEyeBackground.setStyle(Paint.Style.FILL_AND_STROKE);
		this.mEyeForeground = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.mEyeForeground.setStyle(Paint.Style.FILL_AND_STROKE);
		this.mScaredBackground = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.mScaredMouthForeground = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.mScaredMouthForeground.setStyle(Paint.Style.STROKE);
		this.mScaredEyeForeground = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.mScaredEyeForeground.setStyle(Paint.Style.FILL_AND_STROKE);
		this.mScaredBlinkBackground = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.mScaredBlinkMouthForeground = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.mScaredBlinkMouthForeground.setStyle(Paint.Style.STROKE);
		this.mScaredBlinkEyeForeground = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.mScaredBlinkEyeForeground.setStyle(Paint.Style.FILL_AND_STROKE);
		
		this.mBody = new Path[2];

        //Load all preferences or their defaults
        Wallpaper.PREFERENCES.registerOnSharedPreferenceChangeListener(this);
        this.onSharedPreferenceChanged(Wallpaper.PREFERENCES, null);
	}

    /**
     * Handle the changing of a preference.
     */
	public void onSharedPreferenceChanged(final SharedPreferences preferences, final String key) {
    	if (Wallpaper.LOG_VERBOSE) {
    		Log.v(Ghost.TAG, "> onSharedPreferenceChanged()");
    	}
    	
		final boolean all = (key == null);
		final Resources resources = Wallpaper.CONTEXT.getResources();
		
		final String eyeBg = Wallpaper.CONTEXT.getString(R.string.settings_color_ghost_eyebg_key);
		if (all || key.equals(eyeBg)) {
			this.mEyeBackground.setColor(preferences.getInt(eyeBg, resources.getInteger(R.integer.color_ghost_eyebg_default)));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Ghost.TAG, "Eye Background: #" + Integer.toHexString(this.mEyeBackground.getColor()));
			}
		}
		
		final String eyeFg = Wallpaper.CONTEXT.getString(R.string.settings_color_ghost_eyefg_key);
		if (all || key.equals(eyeFg)) {
			this.mEyeForeground.setColor(preferences.getInt(eyeFg, resources.getInteger(R.integer.color_ghost_eyefg_default)));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Ghost.TAG, "Eye Foreground: #" + Integer.toHexString(this.mEyeForeground.getColor()));
			}
		}
		
		final String scaredBg = Wallpaper.CONTEXT.getString(R.string.settings_color_ghost_scaredbg_key);
		if (all || key.equals(scaredBg)) {
			this.mScaredBackground.setColor(preferences.getInt(scaredBg, resources.getInteger(R.integer.color_ghost_scaredbg_default)));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Ghost.TAG, "Scared Background: #" + Integer.toHexString(this.mScaredBackground.getColor()));
			}
		}
		
		final String scaredFg = Wallpaper.CONTEXT.getString(R.string.settings_color_ghost_scaredfg_key);
		if (all || key.equals(scaredFg)) {
			final int color = preferences.getInt(scaredFg, resources.getInteger(R.integer.color_ghost_scaredfg_default));
			this.mScaredMouthForeground.setColor(color);
			this.mScaredEyeForeground.setColor(color);
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Ghost.TAG, "Scared Foreground: #" + Integer.toHexString(color));
			}
		}
		
		final String scaredBlinkBg = Wallpaper.CONTEXT.getString(R.string.settings_color_ghost_scaredblinkbg_key);
		if (all || key.equals(scaredBlinkBg)) {
			this.mScaredBlinkBackground.setColor(preferences.getInt(scaredBlinkBg, resources.getInteger(R.integer.color_ghost_scaredblinkbg_default)));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Ghost.TAG, "Scared Blink Background: #" + Integer.toHexString(this.mScaredBlinkBackground.getColor()));
			}
		}
		
		final String scaredBlinkFg = Wallpaper.CONTEXT.getString(R.string.settings_color_ghost_scaredblinkfg_key);
		if (all || key.equals(scaredBlinkFg)) {
			final int color = preferences.getInt(scaredBlinkFg, resources.getInteger(R.integer.color_ghost_scaredblinkfg_default));
			this.mScaredBlinkMouthForeground.setColor(color);
			this.mScaredBlinkEyeForeground.setColor(color);
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Ghost.TAG, "Scared Blink Foreground: #" + Integer.toHexString(color));
			}
		}
		
		final String ghostMode = Wallpaper.CONTEXT.getString(R.string.settings_game_ghostmode_key);
		if (all || key.equals(ghostMode)) {
			this.mMode = Ghost.Mode.parseInt(preferences.getInt(ghostMode, resources.getInteger(R.integer.game_ghostmode_default)));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Ghost.TAG, "Mode: " + this.mMode);
			}
		}
		
		final String color_style = Wallpaper.CONTEXT.getString(R.string.settings_color_entitystyle_key);
		if (all || key.equals(color_style)) {
			final Entity.Style style = Entity.Style.parseInt(preferences.getInt(color_style, resources.getInteger(R.integer.color_entitystyle_default)));
			//The eyes and mouth are always fill_and_stroke and stroke, respectively. We only change the body background rendering.
			this.mBodyBackground.setStyle(style.style);
			this.mScaredBackground.setStyle(style.style);
			this.mScaredBlinkBackground.setStyle(style.style);
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Ghost.TAG, "Drawing Style: " + style);
			}
		}
		
		final String wrapping = resources.getString(R.string.settings_game_wrappingghosts_key);
		if (all || key.equals(wrapping)) {
			this.mIsWrapping = preferences.getBoolean(wrapping, resources.getBoolean(R.bool.game_wrappingghosts_default));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Ghost.TAG, "Is Wrapping: " + this.mIsWrapping);
			}
		}
		
		final String trophyLogos = resources.getString(R.string.trophy_logos_key);
		if (all || key.equals(trophyLogos)) {
			this.mIsTrophyLogosEnabled = preferences.getBoolean(trophyLogos, resources.getBoolean(R.bool.trophy_logos_default));
			
			if (this.mIsTrophyLogosEnabled) {
				this.mCharacter = Ghost.Character.SPRITES;

				//Load the Logos sprites
				final BitmapFactory.Options options = new BitmapFactory.Options();
				options.inScaled = false;
				this.mSprites = BitmapFactory.decodeResource(Wallpaper.CONTEXT.getResources(), R.drawable.logos, options);
			} else if (!this.mIsTrophyCeosEnabled && !this.mIsTrophyGoogolEnabled && !this.mIsTrophyDessertsEnabled) {
				this.mCharacter = Ghost.Character.GHOST;
				this.mSprites = null;
			}
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Ghost.TAG, "Is Trophy Logos Enabled: " + this.mIsTrophyLogosEnabled);
			}
		}
		
		final String trophyCeos = resources.getString(R.string.trophy_ceos_key);
		if (all || key.equals(trophyCeos)) {
			this.mIsTrophyCeosEnabled = preferences.getBoolean(trophyCeos, resources.getBoolean(R.bool.trophy_ceos_default));
			
			if (this.mIsTrophyCeosEnabled) {
				this.mCharacter = Ghost.Character.SPRITES;

				//Load the Logos sprites
				final BitmapFactory.Options options = new BitmapFactory.Options();
				options.inScaled = false;
				this.mSprites = BitmapFactory.decodeResource(Wallpaper.CONTEXT.getResources(), R.drawable.ceos, options);
			} else if (!this.mIsTrophyLogosEnabled && !this.mIsTrophyGoogolEnabled && !this.mIsTrophyDessertsEnabled) {
				this.mCharacter = Ghost.Character.GHOST;
				this.mSprites = null;
			}
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Ghost.TAG, "Is Trophy CEOs Enabled: " + this.mIsTrophyCeosEnabled);
			}
		}
		
		final String trophyGoogol = resources.getString(R.string.trophy_googol_key);
		if (all || key.equals(trophyGoogol)) {
			this.mIsTrophyGoogolEnabled = preferences.getBoolean(trophyGoogol, resources.getBoolean(R.bool.trophy_googol_default));
			
			if (this.mIsTrophyGoogolEnabled) {
				this.mCharacter = Ghost.Character.SPRITES;

				//Load the Logos sprites
				final BitmapFactory.Options options = new BitmapFactory.Options();
				options.inScaled = false;
				this.mSprites = BitmapFactory.decodeResource(Wallpaper.CONTEXT.getResources(), R.drawable.googol, options);
			} else if (!this.mIsTrophyLogosEnabled && !this.mIsTrophyCeosEnabled && !this.mIsTrophyDessertsEnabled) {
				this.mCharacter = Ghost.Character.GHOST;
				this.mSprites = null;
			}
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Ghost.TAG, "Is Trophy Googol Enabled: " + this.mIsTrophyGoogolEnabled);
			}
		}
		
		final String trophyDesserts = resources.getString(R.string.trophy_desserts_key);
		if (all || key.equals(trophyDesserts)) {
			this.mIsTrophyDessertsEnabled = preferences.getBoolean(trophyDesserts, resources.getBoolean(R.bool.trophy_desserts_default));
			
			if (this.mIsTrophyDessertsEnabled) {
				this.mCharacter = Ghost.Character.SPRITES;

				//Load the Dessers sprites
				final BitmapFactory.Options options = new BitmapFactory.Options();
				options.inScaled = false;
				this.mSprites = BitmapFactory.decodeResource(Wallpaper.CONTEXT.getResources(), R.drawable.desserts, options);
			} else if (!this.mIsTrophyCeosEnabled && !this.mIsTrophyGoogolEnabled && !this.mIsTrophyLogosEnabled) {
				this.mCharacter = Ghost.Character.GHOST;
				this.mSprites = null;
			}
		}
		
    	if (Wallpaper.LOG_VERBOSE) {
    		Log.v(Ghost.TAG, "< onSharedPreferenceChanged()");
    	}
	}

    @Override
	public void performResize(final Game game) {
		super.performResize(game);
		
		final Path shapeOne = new Path();
		shapeOne.moveTo(0, 0.75f * this.mCellHeight);
		shapeOne.lineTo(0, 0.9f * this.mCellHeight);
		shapeOne.lineTo(0.1f * this.mCellWidth, this.mCellHeight);
		shapeOne.lineTo(0.3f * this.mCellWidth, 0.8f * this.mCellHeight);
		shapeOne.lineTo(0.5f * this.mCellWidth, this.mCellHeight);
		shapeOne.lineTo(0.7f * this.mCellWidth, 0.8f * this.mCellHeight);
		shapeOne.lineTo(0.9f * this.mCellWidth, this.mCellHeight);
		shapeOne.lineTo(this.mCellWidth, 0.9f * this.mCellHeight);
		shapeOne.arcTo(new RectF(0, 0, this.mCellWidth, 0.75f * this.mCellHeight), 0, -180);
		shapeOne.lineTo(0, 0.75f * this.mCellHeight);
		this.mBody[0] = shapeOne;

		final float widthOverSix = this.mCellWidth / 6.0f;
		final Path shapeTwo = new Path();
		shapeTwo.moveTo(0, 0.75f * this.mCellHeight);
		shapeTwo.lineTo(0, this.mCellHeight);
		shapeTwo.lineTo(1 * widthOverSix, 0.8f * this.mCellHeight);
		shapeTwo.lineTo(2 * widthOverSix, this.mCellHeight);
		shapeTwo.lineTo(3 * widthOverSix, 0.8f * this.mCellHeight);
		shapeTwo.lineTo(4 * widthOverSix, this.mCellHeight);
		shapeTwo.lineTo(5 * widthOverSix, 0.8f * this.mCellHeight);
		shapeTwo.lineTo(this.mCellWidth, this.mCellHeight);
		shapeTwo.arcTo(new RectF(0, 0, this.mCellWidth, 0.75f * this.mCellHeight), 0, -180);
		shapeTwo.lineTo(0, 0.75f * this.mCellHeight);
		this.mBody[1] = shapeTwo;
		
		final float widthOverEight = this.mCellWidth / 8.0f;
		final float mouthHeightLower = 6 * this.mCellHeight / 8.0f;
		final float mouthHeightUpper = 5 * this.mCellHeight / 8.0f;
		final Path scaredMouth = new Path();
		scaredMouth.moveTo(1 * widthOverEight, mouthHeightLower);
		scaredMouth.lineTo(2 * widthOverEight, mouthHeightUpper);
		scaredMouth.lineTo(3 * widthOverEight, mouthHeightLower);
		scaredMouth.lineTo(4 * widthOverEight, mouthHeightUpper);
		scaredMouth.lineTo(5 * widthOverEight, mouthHeightLower);
		scaredMouth.lineTo(6 * widthOverEight, mouthHeightUpper);
		scaredMouth.lineTo(7 * widthOverEight, mouthHeightLower);
		this.mScaredMouth = scaredMouth;
		
		this.mCellWidthOverThree = this.mCellWidth / 3.0f;
		this.mCellHeightOverThree = this.mCellHeight / 3.0f;
		this.mCellWidthOverSeven = this.mCellWidth / 7.0f;
		this.mCellWidthOverFourteen = this.mCellWidthOverSeven / 2.0f;
	}

    @Override
	public void tick(Game game) {
    	if (this.mState == Ghost.State.FRIGHTENED) {
    		if (this.mStateTimer <= 0) {
    			this.setState(game, Ghost.State.HUNTING);
    		} else {
    			final long time = System.currentTimeMillis();
    			this.mStateTimer -= time - this.mStateLastTime;
    			this.mStateLastTime = time;
    		}
    	}

		this.mStrategyLast = this.mStrategyCurrent;
		
    	if ((this.mMode == Ghost.Mode.CHASE_AND_SCATTER) && (this.mState == Ghost.State.HUNTING)) {
    		if (this.mModeTimer <= 0) {
        		
        		int levelPointer = game.getLevel() - 1;
        		if (levelPointer >= Ghost.CHASE_AND_SCATTER_TIMES.length) {
        			levelPointer = Ghost.CHASE_AND_SCATTER_TIMES.length - 1;
        		}
        		final int[] levelTimes = Ghost.CHASE_AND_SCATTER_TIMES[levelPointer];

        		this.mModePointer += 1;
        		if (this.mModePointer >= levelTimes.length) {
        			this.mModePointer = levelTimes.length - 1;
        		}
        		final int value = levelTimes[this.mModePointer];
        		
        		this.mStrategyCurrent = (value < 0) ? Ghost.Strategy.SCATTER : Ghost.Strategy.CHASE;
        		this.mModeTimer = Math.abs(value);
        		
        		if (Wallpaper.LOG_DEBUG) {
        			Log.d(Ghost.TAG, "Switching " + this.getClass().getSimpleName() +" strategy to " + this.mStrategyCurrent + " for " + this.mModeTimer + "ms");
        		}
    		} else {
    			//tick mode timer
    			final long time = System.currentTimeMillis();
    			this.mModeTimer -= time - this.mModeLastTime;
    			this.mModeLastTime = time;
    		}
    	}
    	
    	final Ghost.State stateBefore = this.mState;

		super.tick(game);
		
		if ((stateBefore == Ghost.State.EATEN) && (this.mState == Ghost.State.EATEN)) {
			//tick twice!
			super.tick(game);
		}
	}

	/**
     * Render the entity on the Canvas.
     * 
     * @param c Canvas to draw on.
     */
	@Override
	public void draw(final Game game, final Canvas c) {
		c.save();
		c.translate(this.mLocation.x - this.mCellWidthOverTwo, this.mLocation.y - this.mCellHeightOverTwo);
		
		if (game.getIsLandscape()) {
			c.rotate(90, this.mCellWidthOverTwo, this.mCellHeightOverTwo);
		}
		
		switch (this.mCharacter) {
			case GHOST:
				this.drawGhost(game, c);
				break;
				
			case SPRITES:
		        c.setDrawFilter(Game.FILTER_SET);
				this.drawSprites(game, c);
		    	c.setDrawFilter(Game.FILTER_REMOVE);
				break;
		}
		
		c.restore();
	}
	
	/**
	 * Draw a normal ghost rendering.
	 * 
	 * @param game Game instance.
	 * @param c Canvas to draw on.
	 */
	private void drawGhost(final Game game, final Canvas c) {
		switch (this.mState) {
			case HUNTING:
				c.drawPath(this.mBody[this.mTickCount % this.mBody.length], this.mBodyBackground);
				
				//fall through to eyes only case
			case EATEN:
				final Point eyeOffset = Entity.move(new Point(0, 0), this.mDirectionCurrent);
				
				c.drawCircle(this.mCellWidthOverThree, this.mCellHeightOverThree, this.mCellWidthOverSeven, this.mEyeBackground);
				c.drawCircle(2.0f * this.mCellWidthOverThree, this.mCellHeightOverThree, this.mCellWidthOverSeven, this.mEyeBackground);
				c.drawCircle(this.mCellWidthOverThree + (eyeOffset.x * this.mCellWidthOverFourteen), this.mCellHeightOverThree + (eyeOffset.y * this.mCellWidthOverFourteen), this.mCellWidthOverFourteen, this.mEyeForeground);
				c.drawCircle((2.0f * this.mCellWidthOverThree) + (eyeOffset.x * this.mCellWidthOverFourteen), this.mCellHeightOverThree + (eyeOffset.y * this.mCellWidthOverFourteen), this.mCellWidthOverFourteen, this.mEyeForeground);
				break;
				
			case FRIGHTENED:
				if ((this.mStateTimer > Ghost.FRIGHTENED_LENGTH_BLINK) || (this.mTickCount % 2 == 0)) {
					//draw normal scared
					c.drawPath(this.mBody[this.mTickCount % this.mBody.length], this.mScaredBackground);
					c.drawPath(this.mScaredMouth, this.mScaredMouthForeground);
					c.drawCircle(this.mCellWidthOverThree, this.mCellHeightOverThree, this.mCellWidthOverFourteen, this.mScaredEyeForeground);
					c.drawCircle(2.0f * this.mCellWidthOverThree, this.mCellHeightOverThree, this.mCellWidthOverFourteen, this.mScaredEyeForeground);
				} else {
					//draw scared blink
					c.drawPath(this.mBody[this.mTickCount % this.mBody.length], this.mScaredBlinkBackground);
					c.drawPath(this.mScaredMouth, this.mScaredBlinkMouthForeground);
					c.drawCircle(this.mCellWidthOverThree, this.mCellHeightOverThree, this.mCellWidthOverFourteen, this.mScaredBlinkEyeForeground);
					c.drawCircle(2.0f * this.mCellWidthOverThree, this.mCellHeightOverThree, this.mCellWidthOverFourteen, this.mScaredBlinkEyeForeground);
				}
				break;
		}
	}
	
	/**
	 * Draw the ghosts as competitor logos.
	 * 
	 * @param game Game instance.
	 * @param c Canvas to draw on.
	 */
	private void drawSprites(final Game game, final Canvas c) {
		final Rect src = new Rect(this.mSpriteIndex * Entity.SPRITE_WIDTH, 0, (this.mSpriteIndex + 1) * Entity.SPRITE_WIDTH, 0);
		
		switch (this.mState) {
			case HUNTING:
				src.top = Ghost.SPRITE_OFFSET_HUNTING * Entity.SPRITE_HEIGHT;
				break;
				
			case EATEN:
				src.top = Ghost.SPRITE_OFFSET_EATEN * Entity.SPRITE_HEIGHT;
				break;
				
			case FRIGHTENED:
				if ((this.mStateTimer > Ghost.FRIGHTENED_LENGTH_BLINK) || (this.mTickCount % 2 == 0)) {
					//draw normal scared
					src.top = Ghost.SPRITE_OFFSET_FRIGHTENED * Entity.SPRITE_HEIGHT;
				} else {
					//draw scared blink
					src.top = Ghost.SPRITE_OFFSET_FRIGHTENED_BLINK * Entity.SPRITE_HEIGHT;
				}
				break;
		}
		
		src.bottom = src.top + Entity.SPRITE_HEIGHT;
		c.drawBitmap(this.mSprites, src, game.getCellSize(), Entity.SPRITE_PAINT);
	}

	/**
	 * Get ghost state.
	 * 
	 * @return State
	 */
	public State getState() {
		if (this.mIsTrophyDessertsEnabled && (this.mState == Ghost.State.HUNTING)) {
			//Always edible when this trophy is enabled
			return Ghost.State.FRIGHTENED;
		} else {
			return this.mState;
		}
	}
	
	/**
	 * Change ghost state. This will trigger the locating of a new next direction.
	 * 
	 * @param game Game instance
	 * @param state Next ghost state
	 */
	public void setState(final Game game, final Ghost.State state) {
		//We cannot be re-eaten or frightened when eaten
		if ((this.mState == Ghost.State.EATEN) && ((state == Ghost.State.EATEN) || (state == Ghost.State.FRIGHTENED))) {
			return;
		}
		
		//This takes care of the mode timer when switching between states
		if (this.mMode == Ghost.Mode.CHASE_AND_SCATTER) {
			if (state == Ghost.State.FRIGHTENED) {
				//Going in to frightened mode so remove the time spend from the last tick until the state change
				this.mModeTimer -= System.currentTimeMillis() - this.mModeLastTime;
			} else if (state == Ghost.State.HUNTING) {
				//Going in to hunting mode so reset the last timer to right now
				this.mModeLastTime = System.currentTimeMillis();
			}
		}
		
		this.mState = state;
		
		if (Wallpaper.LOG_DEBUG) {
			Log.d(Ghost.TAG, "Switching " + this.getClass().getSimpleName() + " state to " + state);
		}
		
		if (state == Ghost.State.FRIGHTENED) {
			//reverse direction immediately if frightened
			this.mDirectionNext = this.mDirectionCurrent.getOpposite();
			
			int levelPointer = game.getLevel() - 1;
			if (levelPointer >= Ghost.FRIGHTENED_LENGTH.length) {
				levelPointer = Ghost.FRIGHTENED_LENGTH.length - 1;
			}
			this.mStateTimer = Ghost.FRIGHTENED_LENGTH[levelPointer];
			this.mStateLastTime = System.currentTimeMillis();
		} else {
			//otherwise get new next direction
			this.determineNextDirection(game);
		}
	}
	
	@Override
	protected void newLevel(final Game game) {
		this.newLife(game);
	}
	
	@Override
	public void newLife(final Game game) {
		//Set initial ghost position
		this.setPosition(this.getInitialPosition(game));
		
		//Set initial strategy based on mode
		switch (this.mMode) {
			case CHASE_ONLY:
				this.mStrategyCurrent = Ghost.Strategy.CHASE;
				break;

			case CHASE_AND_SCATTER:
			case SCATTER_ONLY:
				this.mStrategyCurrent = Ghost.Strategy.SCATTER;
				break;
				
			case RANDOM_TURNS:
				this.mStrategyCurrent = Ghost.Strategy.RANDOM;
				break;
				
			default:
				throw new IllegalArgumentException("Unknown ghost mode: " + this.mMode);
		}
		
		//Breathe life into ghost
		this.setState(game, Ghost.State.HUNTING);
		
		//Since mDirectionNext will have been set by the state change, copy it to mDirectionCurrent in case of an immediate state change
		this.mDirectionCurrent = this.mDirectionNext;
		
		if (this.mMode == Ghost.Mode.CHASE_AND_SCATTER) {
			//This will be bumped up to zero based on the timer being expired
			this.mModePointer = -1;
			this.mModeTimer = 0;
		}
	}
	
	@Override
	protected void moved(final Game game) {
		game.checkGhosts();
		
		if (this.mStrategyCurrent != this.mStrategyLast) {
			this.mDirectionNext = this.mDirectionCurrent.getOpposite();
		} else {
			this.determineNextDirection(game);
		}
	}
	
	/**
	 * Determine the next direction to travel in.
	 * 
	 * @param game Game instance
	 */
	protected void determineNextDirection(final Game game) {
		switch (this.mState) {
			case HUNTING:
				switch (this.mStrategyCurrent) {
					case CHASE:
						this.determineNextDirectionByLineOfSight(game, this.getChasingTarget(game));
						break;
						
					case SCATTER:
						this.determineNextDirectionByLineOfSight(game, this.getScatterTarget(game));
						break;
						
					case RANDOM:
						this.determineNextDirectionByRandomness(game);
						break;
						
					default:
						throw new IllegalArgumentException("Unknown Ghost strategy: " + this.mStrategyCurrent);
				}
				break;
				
			case EATEN:
				final Point initialPosition = this.getInitialPosition(game);
				if ((this.mPosition.x == initialPosition.x) && (this.mPosition.y == initialPosition.y)) {
					if (Wallpaper.LOG_DEBUG) {
						Log.d(Ghost.TAG, this.getClass().getSimpleName() + " has reached initial position. Going on the hunt.");
					}
					
					this.setState(game, Ghost.State.HUNTING);
				} else {
					this.determineNextDirectionByLineOfSight(game, initialPosition);
				}
				break;
				
			case FRIGHTENED:
				this.determineNextFrightenedDirection(game);
				break;
		}
	}
	
	/**
	 * Determine next direction based on a simple random number generator.
	 * 
	 * @param game Game instance
	 */
	protected void determineNextFrightenedDirection(final Game game) {
		if (game.isIntersection(this.mPosition)) {
			//Try a random direction
			this.mDirectionNext = Direction.values()[Game.RANDOM.nextInt(Direction.values().length)];
			
			if (!game.isValidPosition(this, Entity.move(this.mPosition, this.mDirectionNext)) || (this.mDirectionNext == this.mDirectionCurrent.getOpposite())) {
				//If the random direction was not valid, iterate over all possible directions looking for a valid one
				for (final Direction direction : Direction.values()) {
					//See if the direction is a valid position and not the opposite of our current direction
					if (game.isValidPosition(this, Entity.move(this.mPosition, direction)) && (direction != this.mDirectionCurrent.getOpposite())) {
						//Exit the loop
						this.mDirectionNext = direction;
						break;
					}
				}
			}
		} else {
			//Not at intersection, go straight
			this.mDirectionNext = this.mDirectionCurrent;
		}
	}
	
	/**
	 * Use line-of-sight distance to a target point to determine the next direction.
	 * 
	 * @param game Game instance
	 * @param target Target Point
	 */
	protected void determineNextDirectionByLineOfSight(final Game game, final Point target) {
		Point nextPoint;
		double nextDistance;
		double shortestDistance = Double.MAX_VALUE;
		this.mDirectionNext = null;
		
		for (final Direction direction : Direction.values()) {
			if ((this.mDirectionCurrent == null) || (direction != this.mDirectionCurrent.getOpposite())) {
				nextPoint = Entity.move(this.mPosition, direction);
				nextDistance = Math.sqrt(Math.pow(nextPoint.x - target.x, 2) + Math.pow(nextPoint.y - target.y, 2));
				
				if (game.isValidPosition(this, nextPoint) && (nextDistance < shortestDistance)) {
					this.mDirectionNext = direction;
					shortestDistance = nextDistance; 
				}
			}
		}
		
		if (Wallpaper.LOG_DEBUG) {
			if (this.mDirectionNext == null) {
				Log.w(Ghost.TAG, this.getClass().getSimpleName() + "'s next direction is null. This will result in a fatal error.");
				Log.w(Ghost.TAG, "Target: (" + target.x + ", " + target.y + ")");
				Log.w(Ghost.TAG, "State: " + this.mState);
				Log.w(Ghost.TAG, "Mode: " + this.mMode);
			}
		}
	}
	
	/**
	 * Pick a next direction at random when at an intersection.
	 * 
	 * @param game Game state.
	 */
	protected void determineNextDirectionByRandomness(final Game game) {
		if (game.isIntersection(this.mPosition)) {
			while (true) {
				this.mDirectionNext = Entity.Direction.values()[Game.RANDOM.nextInt(Entity.Direction.values().length)];
				
				if (game.isValidPosition(this, Entity.move(this.mPosition, this.mDirectionNext)) && ((this.mDirectionCurrent == null) || (this.mDirectionNext != this.mDirectionCurrent.getOpposite()))) {
					break;
				}
			}
		} else {
			//Not at intersection, go straight
			this.mDirectionNext = this.mDirectionCurrent;
		}
	}
	
	/**
	 * Initial starting position.
	 * 
	 * @param game Game instance
	 * @return Point
	 */
	protected abstract Point getInitialPosition(final Game game);
	
	/**
	 * Point off of the board of the initial starting corner.
	 * 
	 * @param game Game instance
	 * @return Point
	 */
	protected abstract Point getScatterTarget(final Game game);
	
	/**
	 * Point to use when chasing down The Man.
	 * 
	 * @param game Game instance.
	 * @return Point
	 */
	protected abstract Point getChasingTarget(final Game game);

	
	
	/**
	 * The ghost Blinky (Shadow).
	 * 
	 * @author Jake Wharton
	 */
	public static class Blinky extends Ghost implements SharedPreferences.OnSharedPreferenceChangeListener {
		private static final String TAG = Ghost.TAG + ".Blinky";
		private static final int INDEX = 0;
		
		public Blinky() {
			super(Blinky.INDEX);
		}
		
	    /**
	     * Handle the changing of a preference.
	     */
		public void onSharedPreferenceChanged(final SharedPreferences preferences, final String key) {
			final boolean all = (key == null);
			final Resources resources = Wallpaper.CONTEXT.getResources();
			
			final String color = Wallpaper.CONTEXT.getString(R.string.settings_color_ghost_blinky_key);
			if (all || key.equals(color)) {
				this.mBodyBackground.setColor(preferences.getInt(color, resources.getInteger(R.integer.color_ghost_blinky_default)));
				
				if (Wallpaper.LOG_DEBUG) {
					Log.d(Blinky.TAG, "Color: #" + Integer.toHexString(this.mBodyBackground.getColor()));
				}
			}
			
			super.onSharedPreferenceChanged(preferences, key);
		}
		
		@Override
		protected Point getInitialPosition(final Game game) {
			//last column, second row
			return new Point(game.getCellsWide() - 1, game.getCellRowSpacing() + 1);
		}

		@Override
		protected Point getScatterTarget(final Game game) {
			//upper right
			return new Point(game.getCellsWide(), -1);
		}

		@Override
		protected Point getChasingTarget(final Game game) {
			//use The Man's position as a target
			return new Point(game.getTheMan().getPosition());
		}
	}
	
	/**
	 * The ghost Pinky (Speedy).
	 * 
	 * @author Jake Wharton
	 */
	public static class Pinky extends Ghost implements SharedPreferences.OnSharedPreferenceChangeListener {
		private static final String TAG = Ghost.TAG + ".Pinky";
		private static final int INDEX = 1;
		private static final int LEADING_FACTOR = 4;
		
		public Pinky() {
			super(Pinky.INDEX);
		}

	    /**
	     * Handle the changing of a preference.
	     */
		public void onSharedPreferenceChanged(final SharedPreferences preferences, final String key) {
			final boolean all = (key == null);
			final Resources resources = Wallpaper.CONTEXT.getResources();
			
			final String color = Wallpaper.CONTEXT.getString(R.string.settings_color_ghost_pinky_key);
			if (all || key.equals(color)) {
				this.mBodyBackground.setColor(preferences.getInt(color, resources.getInteger(R.integer.color_ghost_pinky_default)));
				
				if (Wallpaper.LOG_DEBUG) {
					Log.d(Pinky.TAG, "Color: #" + Integer.toHexString(this.mBodyBackground.getColor()));
				}
			}
			
			super.onSharedPreferenceChanged(preferences, key);
		}

		@Override
		protected Point getInitialPosition(final Game game) {
			//second column, first row
			return new Point(game.getCellColumnSpacing() + 1, 0);
		}

		@Override
		protected Point getScatterTarget(final Game game) {
			//upper left
			return new Point(-1, -1);
		}

		@Override
		protected Point getChasingTarget(final Game game) {
			//the target is 4 tiles in front of The Man, unless he is going upward, then it is up 4 and left 4
			Point position = Entity.move(game.getTheMan().getPosition(), game.getTheMan().getDirection(), Pinky.LEADING_FACTOR);
			if (game.getTheMan().getDirection() == Direction.NORTH) {
				position.x -= Pinky.LEADING_FACTOR;
			}
			return position;
		}
	}
	
	/**
	 * The ghost Inky (Bashful).
	 * 
	 * @author Jake Wharton
	 */
	public static class Inky extends Ghost implements SharedPreferences.OnSharedPreferenceChangeListener {
		private static final String TAG = Ghost.TAG + ".Inky";
		private static final int INDEX = 2;
		private static final int LEADING_FACTOR = 2;
		private static final int BLINKY_INDEX = 0; //always the first ghost initialized
		
		public Inky() {
			super(Inky.INDEX);
		}

	    /**
	     * Handle the changing of a preference.
	     */
		public void onSharedPreferenceChanged(final SharedPreferences preferences, final String key) {
			final boolean all = (key == null);
			final Resources resources = Wallpaper.CONTEXT.getResources();
			
			final String color = Wallpaper.CONTEXT.getString(R.string.settings_color_ghost_inky_key);
			if (all || key.equals(color)) {
				this.mBodyBackground.setColor(preferences.getInt(color, resources.getInteger(R.integer.color_ghost_inky_default)));
				
				if (Wallpaper.LOG_DEBUG) {
					Log.d(Inky.TAG, "Color: #" + Integer.toHexString(this.mBodyBackground.getColor()));
				}
			}
			
			super.onSharedPreferenceChanged(preferences, key);
		}

		@Override
		protected Point getInitialPosition(final Game game) {
			//second to last column, last row
			return new Point(game.getCellsWide() - game.getCellColumnSpacing() - 2, game.getCellsTall() - 1);
		}

		@Override
		protected Point getScatterTarget(final Game game) {
			//lower right
			return new Point(game.getCellsWide(), game.getCellsTall());
		}

		@Override
		protected Point getChasingTarget(final Game game) {
			//the leading position is 2 tiles in front of The Man, unless he is going upward, then it is up 2 and left 2
			Point position = Entity.move(game.getTheMan().getPosition(), game.getTheMan().getDirection(), Inky.LEADING_FACTOR);
			if (game.getTheMan().getDirection() == Direction.NORTH) {
				position.x -= Inky.LEADING_FACTOR;
			}
			//add the offset between the leading position and blinky to the leading position for our target
			position.x += game.getGhost(Inky.BLINKY_INDEX).getPosition().x - position.x;
			position.y += game.getGhost(Inky.BLINKY_INDEX).getPosition().y - position.y;
			return position;
		}
	}
	
	/**
	 * The ghost Clyde (Pokey).
	 * 
	 * @author Jake Wharton
	 */
	public static class Clyde extends Ghost implements SharedPreferences.OnSharedPreferenceChangeListener {
		private static final String TAG = Ghost.TAG + ".Clyde";
		private static final int INDEX = 3;
		private static final int PROXIMITY_THRESHOLD = 8;
		
		public Clyde() {
			super(Clyde.INDEX);
		}

	    /**
	     * Handle the changing of a preference.
	     */
		public void onSharedPreferenceChanged(final SharedPreferences preferences, final String key) {
			final boolean all = (key == null);
			final Resources resources = Wallpaper.CONTEXT.getResources();
			
			final String color = Wallpaper.CONTEXT.getString(R.string.settings_color_ghost_clyde_key);
			if (all || key.equals(color)) {
				this.mBodyBackground.setColor(preferences.getInt(color, resources.getInteger(R.integer.color_ghost_clyde_default)));
				
				if (Wallpaper.LOG_DEBUG) {
					Log.d(Clyde.TAG, "Color: #" + Integer.toHexString(this.mBodyBackground.getColor()));
				}
			}
			
			super.onSharedPreferenceChanged(preferences, key);
		}

		@Override
		protected Point getInitialPosition(final Game game) {
			//first column, second to last row
			return new Point(0, game.getCellsTall() - game.getCellRowSpacing() - 2);
		}

		@Override
		protected Point getScatterTarget(final Game game) {
			//lower left
			return new Point(-1, game.getCellsTall());
		}

		@Override
		protected Point getChasingTarget(final Game game) {
			double distance = Math.sqrt(Math.pow(this.mPosition.x - game.getTheMan().getPosition().x, 2) + Math.pow(this.mPosition.y - game.getTheMan().getPosition().y, 2));
			if (distance > Clyde.PROXIMITY_THRESHOLD) {
				//outside proximity zone, The Man's position is the target
				return new Point(game.getTheMan().getPosition());
			} else {
				//inside proximity zone, scatter position is the target
				return this.getScatterTarget(game);
			}
		}
	}
}
