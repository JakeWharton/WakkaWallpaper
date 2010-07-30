package com.jakewharton.wakkawallpaper;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
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
	enum Mode {
		CHASE_AND_SCATTER(0), CHASE_ONLY(1), SCATTER_ONLY(2), RANDOM_TURNS(3);
		
		public final int value;
		
		private Mode(final int value) {
			this.value = value;
		}
		
		public static Ghost.Mode parseInt(final int modeValue) {
			for (Ghost.Mode mode : Ghost.Mode.values()) {
				if (mode.value == modeValue) {
					return mode;
				}
			}
			throw new IllegalArgumentException("Unknown Ghost mode value: " + modeValue);
		}
	}

	private static final String TAG = "WakkaWallpaper.Ghost";
	private static final int FRIGHTENED_LENGTH_BLINK = 5000;
	private static final int FRIGHTENED_LENGTH = 7000;
	private static final int[][] CHASE_AND_SCATTER_TIMES = new int[][] {
		new int[] { -7000, 20000, -7000, 20000, -5000, 20000, -5000, 0 },
		new int[] { -7000, 20000, -7000, 20000, -5000, 1033000, -17, 0 },
		new int[] { -7000, 20000, -7000, 20000, -5000, 1033000, -17, 0 },
		new int[] { -7000, 20000, -7000, 20000, -5000, 1033000, -17, 0 },
		new int[] { -5000, 20000, -5000, 20000, -5000, 1037000, -17, 0 },
	};
	
	protected Ghost.State mState;
	protected Ghost.Strategy mStrategy;
	protected Ghost.Mode mMode;
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
	private long mStateTimer;
	private int mModePointer;
	private long mModeTimer;
	private long mModeLastTime;
	
    /**
     * Create a new ghost.
     * 
     * @param backgroundColor Primary color of the ghost.
     */
	protected Ghost() {
		super();
		
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
			this.mEyeBackground.setColor(Wallpaper.PREFERENCES.getInt(eyeBg, resources.getInteger(R.integer.color_ghost_eyebg_default)));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Ghost.TAG, "Eye Background: #" + Integer.toHexString(this.mEyeBackground.getColor()));
			}
		}
		
		final String eyeFg = Wallpaper.CONTEXT.getString(R.string.settings_color_ghost_eyefg_key);
		if (all || key.equals(eyeFg)) {
			this.mEyeForeground.setColor(Wallpaper.PREFERENCES.getInt(eyeFg, resources.getInteger(R.integer.color_ghost_eyefg_default)));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Ghost.TAG, "Eye Foreground: #" + Integer.toHexString(this.mEyeForeground.getColor()));
			}
		}
		
		final String scaredBg = Wallpaper.CONTEXT.getString(R.string.settings_color_ghost_scaredbg_key);
		if (all || key.equals(scaredBg)) {
			this.mScaredBackground.setColor(Wallpaper.PREFERENCES.getInt(scaredBg, resources.getInteger(R.integer.color_ghost_scaredbg_default)));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Ghost.TAG, "Scared Background: #" + Integer.toHexString(this.mScaredBackground.getColor()));
			}
		}
		
		final String scaredFg = Wallpaper.CONTEXT.getString(R.string.settings_color_ghost_scaredfg_key);
		if (all || key.equals(scaredFg)) {
			final int color = Wallpaper.PREFERENCES.getInt(scaredFg, resources.getInteger(R.integer.color_ghost_scaredfg_default));
			this.mScaredMouthForeground.setColor(color);
			this.mScaredEyeForeground.setColor(color);
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Ghost.TAG, "Scared Foreground: #" + Integer.toHexString(color));
			}
		}
		
		final String scaredBlinkBg = Wallpaper.CONTEXT.getString(R.string.settings_color_ghost_scaredblinkbg_key);
		if (all || key.equals(scaredBlinkBg)) {
			this.mScaredBlinkBackground.setColor(Wallpaper.PREFERENCES.getInt(scaredBlinkBg, resources.getInteger(R.integer.color_ghost_scaredblinkbg_default)));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Ghost.TAG, "Scared Blink Background: #" + Integer.toHexString(this.mScaredBlinkBackground.getColor()));
			}
		}
		
		final String scaredBlinkFg = Wallpaper.CONTEXT.getString(R.string.settings_color_ghost_scaredblinkfg_key);
		if (all || key.equals(scaredBlinkFg)) {
			final int color = Wallpaper.PREFERENCES.getInt(scaredBlinkFg, resources.getInteger(R.integer.color_ghost_scaredblinkfg_default));
			this.mScaredBlinkMouthForeground.setColor(color);
			this.mScaredBlinkEyeForeground.setColor(color);
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Ghost.TAG, "Scared Blink Foreground: #" + Integer.toHexString(color));
			}
		}
		
		final String ghostMode = Wallpaper.CONTEXT.getString(R.string.settings_game_ghostmode_key);
		if (all || key.equals(ghostMode)) {
			this.mMode = Ghost.Mode.parseInt(Wallpaper.PREFERENCES.getInt(ghostMode, resources.getInteger(R.integer.game_ghostmode_default)));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Ghost.TAG, "Mode: " + this.mMode);
			}
		}
		
		final String color_style = Wallpaper.CONTEXT.getString(R.string.settings_color_entitystyle_key);
		if (all || key.equals(color_style)) {
			final Entity.Style style = Entity.Style.parseInt(Wallpaper.PREFERENCES.getInt(color_style, resources.getInteger(R.integer.color_entitystyle_default)));
			//The eyes and mouth are always fill_and_stroke and stroke, respectively. We only change the body background rendering.
			this.mBodyBackground.setStyle(style.style);
			this.mScaredBackground.setStyle(style.style);
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Ghost.TAG, "Drawing Style: " + style);
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
    	if ((this.mState == Ghost.State.FRIGHTENED) && ((System.currentTimeMillis() - this.mStateTimer) > Ghost.FRIGHTENED_LENGTH)) {
    		this.setState(game, Ghost.State.HUNTING);
    	}
    	
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
        		
        		this.mStrategy = (value < 0) ? Ghost.Strategy.SCATTER : Ghost.Strategy.CHASE;
        		this.mModeTimer = Math.abs(value);
        		
        		if (Wallpaper.LOG_DEBUG) {
        			Log.d(Ghost.TAG, "Switching ghosts strategy to " + this.mStrategy + " for " + this.mModeTimer + "ms");
        		}
    		} else {
    			//tick mode timer
    			final long timer = System.currentTimeMillis();
    			this.mModeTimer -= timer - this.mModeLastTime;
    			this.mModeLastTime = timer;
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
	public void draw(final Canvas c, final boolean isLandscape) {
		c.save();
		c.translate(this.mLocation.x - this.mCellWidthOverTwo, this.mLocation.y - this.mCellHeightOverTwo);
		
		if (isLandscape) {
			c.rotate(90, this.mCellWidthOverTwo, this.mCellHeightOverTwo);
		}
		
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
				final long timeDiff = System.currentTimeMillis() - this.mStateTimer;
				if ((timeDiff < Ghost.FRIGHTENED_LENGTH_BLINK) || (this.mTickCount % 2 == 0)) {
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
		
		c.restore();
	}

	/**
	 * Get ghost state.
	 * 
	 * @return State
	 */
	public State getState() {
		return this.mState;
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
		
		this.mState = state;
		
		if (state == Ghost.State.FRIGHTENED) {
			//reverse direction immediately if frightened
			this.mDirectionNext = this.mDirectionCurrent.getOpposite();
		} else {
			//otherwise get new next direction
			this.determineNextDirection(game, true);
		}
		
		//Set the timestamp for timed states
		this.mStateTimer = System.currentTimeMillis();
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
				this.mStrategy = Ghost.Strategy.CHASE;
				break;

			case CHASE_AND_SCATTER:
			case SCATTER_ONLY:
				this.mStrategy = Ghost.Strategy.SCATTER;
				break;
				
			case RANDOM_TURNS:
				this.mStrategy = Ghost.Strategy.RANDOM;
				break;
				
			default:
				throw new IllegalArgumentException("Unknown ghost mode: " + this.mMode);
		}
		
		//Breathe life into ghost
		this.setState(game, Ghost.State.HUNTING);
		
		//Since mDirectionNext will have been set by the state change, copy it to mDirectionCurrent in case of an immediate state change
		this.mDirectionCurrent = this.mDirectionNext;
		
		if (this.mMode == Ghost.Mode.CHASE_AND_SCATTER) {
			//This will be bumped up to zero based on the timer being less than zero
			this.mModePointer = -1;
			this.mModeTimer = -1;
		}
	}
	
	@Override
	protected void moved(final Game game) {
		game.checkGhosts();
		this.determineNextDirection(game, false);
	}
	
	/**
	 * Determine the next direction to travel in.
	 * 
	 * @param game Game instance
	 * @param isStateChange Whether or not this change is the result of a state change
	 */
	protected void determineNextDirection(final Game game, boolean isStateChange) {
		switch (this.mState) {
			case HUNTING:
				switch (this.mStrategy) {
					case CHASE:
						this.determineNextDirectionByLineOfSight(game, this.getChasingTarget(game), isStateChange);
						break;
						
					case SCATTER:
						this.determineNextDirectionByLineOfSight(game, this.getScatterTarget(game), isStateChange);
						break;
						
					case RANDOM:
						this.determineNextDirectionByRandomness(game, isStateChange);
						break;
						
					default:
						throw new IllegalArgumentException("Unknown Ghost strategy: " + this.mStrategy);
				}
				break;
				
			case EATEN:
				final Point initialPosition = this.getInitialPosition(game);
				if ((this.mPosition.x == initialPosition.x) && (this.mPosition.y == initialPosition.y)) {
					if (Wallpaper.LOG_DEBUG) {
						Log.d(Ghost.TAG, this.getClass().getSimpleName() + " has reached initial position. Reverting to initial ghost state.");
					}
					
					this.setState(game, Ghost.State.HUNTING);
				} else {
					this.determineNextDirectionByLineOfSight(game, initialPosition, isStateChange);
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
			Direction nextDirection = Direction.values()[Game.RANDOM.nextInt(Direction.values().length)];
			
			if (!game.isValidPosition(Entity.move(this.mPosition, nextDirection)) || (nextDirection == this.mDirectionCurrent.getOpposite())) {
				//If the random direction was not valid, iterate over all possible directions looking for a valid one
				for (Direction direction : Direction.values()) {
					//See if the direction is a valid position and not the opposite of our current direction
					if (game.isValidPosition(Entity.move(this.mPosition, direction)) && (direction != this.mDirectionCurrent.getOpposite())) {
						//Save and exit the loop
						nextDirection = direction;
						break;
					}
				}
			}
			
			//Store new direction
			this.mDirectionNext = nextDirection;
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
	 * @param isStateChange Whether or not this is occurring because of a state change
	 */
	protected void determineNextDirectionByLineOfSight(final Game game, final Point target, final boolean isStateChange) {
		Point nextPoint;
		Direction nextDirection = null;
		double nextDistance;
		double shortestDistance = Double.MAX_VALUE;
		
		for (Direction direction : Direction.values()) {
			if (isStateChange || (this.mDirectionCurrent == null) || (direction != this.mDirectionCurrent.getOpposite())) {
				nextPoint = Entity.move(this.mPosition, direction);
				nextDistance = Math.sqrt(Math.pow(nextPoint.x - target.x, 2) + Math.pow(nextPoint.y - target.y, 2));
				
				if (game.isValidPosition(nextPoint) && (nextDistance < shortestDistance)) {
					nextDirection = direction;
					shortestDistance = nextDistance; 
				}
			}
		}
		
		if (Wallpaper.LOG_DEBUG) {
			if (nextDirection == null) {
				Log.w(Ghost.TAG, this.getClass().getSimpleName() + "'s next direction is null. This will result in a fatal error.");
				Log.w(Ghost.TAG, "Target: (" + target.x + ", " + target.y + ")");
				Log.w(Ghost.TAG, "State Changed: " + isStateChange);
				Log.w(Ghost.TAG, "State: " + this.mState);
				Log.w(Ghost.TAG, "Mode: " + this.mMode);
			}
		}
		
		this.mDirectionNext = nextDirection;
	}
	
	/**
	 * Pick a next direction at random when at an intersection.
	 * 
	 * @param game Game state.
	 * @param isStateChange Whether or not this is occurring because of a state change
	 */
	protected void determineNextDirectionByRandomness(final Game game, final boolean isStateChange) {
		if (game.isIntersection(this.mPosition)) {
			while (true) {
				this.mDirectionNext = Entity.Direction.values()[Game.RANDOM.nextInt(Entity.Direction.values().length)];
				
				if (game.isValidPosition(Entity.move(this.mPosition, this.mDirectionNext)) && ((this.mDirectionCurrent == null) || isStateChange || (this.mDirectionNext != this.mDirectionCurrent.getOpposite()))) {
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
		private static final int LEADING_FACTOR = 4;

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
		private static final int LEADING_FACTOR = 2;
		private static final int BLINKY_INDEX = 0; //always the first ghost initialized

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
		private static final int PROXIMITY_THRESHOLD = 8;

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
