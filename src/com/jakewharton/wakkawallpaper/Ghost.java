package com.jakewharton.wakkawallpaper;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.util.Log;

/**
 * The Ghost class represents an enemy on the board.
 * 
 * @author Jake Wharton
 */
public abstract class Ghost extends Entity implements SharedPreferences.OnSharedPreferenceChangeListener {
	enum State { CHASE, SCATTER, FRIGHTENED, EATEN }

	private static final String TAG = "WakkaWallpaper.Ghost";
	private static final int FLEE_BLINK_INTERVAL = 200;
	
	private static final int DEFAULT_FLEE_LENGTH = 7000;
	private static final int DEFAULT_FLEE_BLINK_THRESHOLD = 2000;
	private static final int DEFAULT_EYE_BACKGROUND = 0xffffffff;
	private static final int DEFAULT_EYE_FOREGROUND = 0xff000000;
	private static final int DEFAULT_SCARED_BACKGROUND = 0xff0033ff;
	private static final int DEFAULT_SCARED_FOREGROUND = 0xffffcc33;
	private static final int DEFAULT_SCARED_BLINK_BACKGROUND = 0xffff0000;
	private static final int DEFAULT_SCARED_BLINK_FOREGROUND = 0xfffafafa;
	
	protected State mState;
	private final Paint mBodyBackground;
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
	private int mFleeLength;
	
    /**
     * Create a new ghost.
     * 
     * @param backgroundColor Primary color of the ghost.
     */
	protected Ghost(final int backgroundColor) {
		super();

		this.mState = State.CHASE;
		this.mFleeLength = Ghost.DEFAULT_FLEE_LENGTH;
		
		this.mBodyBackground = new Paint();
		this.mBodyBackground.setAntiAlias(true);
		this.mBodyBackground.setColor(backgroundColor);
		this.mEyeBackground = new Paint();
		this.mEyeBackground.setAntiAlias(true);
		this.mEyeForeground = new Paint();
		this.mEyeForeground.setAntiAlias(true);
		this.mScaredBackground = new Paint();
		this.mScaredBackground.setAntiAlias(true);
		this.mScaredMouthForeground = new Paint();
		this.mScaredMouthForeground.setAntiAlias(true);
		this.mScaredMouthForeground.setStyle(Style.STROKE);
		this.mScaredEyeForeground = new Paint();
		this.mScaredEyeForeground.setAntiAlias(true);
		this.mScaredEyeForeground.setStyle(Style.FILL_AND_STROKE);
		this.mScaredBlinkBackground = new Paint();
		this.mScaredBlinkBackground.setAntiAlias(true);
		this.mScaredBlinkMouthForeground = new Paint();
		this.mScaredBlinkMouthForeground.setAntiAlias(true);
		this.mScaredBlinkMouthForeground.setStyle(Style.STROKE);
		this.mScaredBlinkEyeForeground = new Paint();
		this.mScaredBlinkEyeForeground.setAntiAlias(true);
		this.mScaredBlinkEyeForeground.setStyle(Style.FILL_AND_STROKE);

        //Load all preferences or their defaults
        Wallpaper.PREFERENCES.registerOnSharedPreferenceChangeListener(this);
        this.onSharedPreferenceChanged(Wallpaper.PREFERENCES, null);
		
		this.mBody = new Path[2];
	}

    /**
     * Handle the changing of a preference.
     */
	public void onSharedPreferenceChanged(final SharedPreferences preferences, final String key) {
    	if (Wallpaper.LOG_VERBOSE) {
    		Log.v(Ghost.TAG, "> onSharedPreferenceChanged()");
    	}
    	
		final boolean all = (key == null);
		
		final String eyeBg = Wallpaper.CONTEXT.getString(R.string.settings_color_ghost_eyebg_key);
		if (all || key.equals(eyeBg)) {
			this.mEyeBackground.setColor(Wallpaper.PREFERENCES.getInt(eyeBg, Ghost.DEFAULT_EYE_BACKGROUND));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Ghost.TAG, "Eye Background: " + Integer.toHexString(this.mEyeBackground.getColor()));
			}
		}
		
		final String eyeFg = Wallpaper.CONTEXT.getString(R.string.settings_color_ghost_eyefg_key);
		if (all || key.equals(eyeFg)) {
			this.mEyeForeground.setColor(Wallpaper.PREFERENCES.getInt(eyeFg, Ghost.DEFAULT_EYE_FOREGROUND));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Ghost.TAG, "Eye Foreground: " + Integer.toHexString(this.mEyeForeground.getColor()));
			}
		}
		
		final String scaredBg = Wallpaper.CONTEXT.getString(R.string.settings_color_ghost_scaredbg_key);
		if (all || key.equals(scaredBg)) {
			this.mScaredBackground.setColor(Wallpaper.PREFERENCES.getInt(scaredBg, Ghost.DEFAULT_SCARED_BACKGROUND));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Ghost.TAG, "Scared Background: " + Integer.toHexString(this.mScaredBackground.getColor()));
			}
		}
		
		final String scaredFg = Wallpaper.CONTEXT.getString(R.string.settings_color_ghost_scaredfg_key);
		if (all || key.equals(scaredFg)) {
			final int color = Wallpaper.PREFERENCES.getInt(scaredFg, Ghost.DEFAULT_SCARED_FOREGROUND);
			this.mScaredMouthForeground.setColor(color);
			this.mScaredEyeForeground.setColor(color);
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Ghost.TAG, "Scared Foreground: " + Integer.toHexString(color));
			}
		}
		
		final String scaredBlinkBg = Wallpaper.CONTEXT.getString(R.string.settings_color_ghost_scaredblinkbg_key);
		if (all || key.equals(scaredBlinkBg)) {
			this.mScaredBlinkBackground.setColor(Wallpaper.PREFERENCES.getInt(scaredBlinkBg, Ghost.DEFAULT_SCARED_BLINK_BACKGROUND));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Ghost.TAG, "Scared Blink Background: " + Integer.toHexString(this.mScaredBlinkBackground.getColor()));
			}
		}
		
		final String scaredBlinkFg = Wallpaper.CONTEXT.getString(R.string.settings_color_ghost_scaredblinkfg_key);
		if (all || key.equals(scaredBlinkFg)) {
			final int color = Wallpaper.PREFERENCES.getInt(scaredBlinkFg, Ghost.DEFAULT_SCARED_BLINK_FOREGROUND);
			this.mScaredBlinkMouthForeground.setColor(color);
			this.mScaredBlinkEyeForeground.setColor(color);
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Ghost.TAG, "Scared Blink Foreground: " + Integer.toHexString(color));
			}
		}
		

    	if (Wallpaper.LOG_VERBOSE) {
    		Log.v(Ghost.TAG, "< onSharedPreferenceChanged()");
    	}
	}

	/**
	 * Resize the entity to fit within the specified dimensions.
	 * 
	 * @param width New width.
	 * @param height New height.
	 */
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

    /**
     * Render the entity on the Canvas.
     * 
     * @param c Canvas to draw on.
     */
	@Override
	public void draw(final Canvas c) {
		c.save();
		c.translate(this.mLocation.x - this.mCellWidthOverTwo, this.mLocation.y - this.mCellHeightOverTwo);
		
		switch (this.mState) {
			case CHASE:
			case SCATTER:
				c.drawPath(this.mBody[this.mTickCount % this.mBody.length], this.mBodyBackground);
				
				//fall through to eyes only case
			case EATEN:
				Point eyeOffset = Entity.move(new Point(0, 0), this.mDirectionCurrent);
				
				c.drawCircle(this.mCellWidthOverThree, this.mCellHeightOverThree, this.mCellWidthOverSeven, this.mEyeBackground);
				c.drawCircle(2.0f * this.mCellWidthOverThree, this.mCellHeightOverThree, this.mCellWidthOverSeven, this.mEyeBackground);
				c.drawCircle(this.mCellWidthOverThree + (eyeOffset.x * this.mCellWidthOverFourteen), this.mCellHeightOverThree + (eyeOffset.y * this.mCellWidthOverFourteen), this.mCellWidthOverFourteen, this.mEyeForeground);
				c.drawCircle((2.0f * this.mCellWidthOverThree) + (eyeOffset.x * this.mCellWidthOverFourteen), this.mCellHeightOverThree + (eyeOffset.y * this.mCellWidthOverFourteen), this.mCellWidthOverFourteen, this.mEyeForeground);
				break;
				
			case FRIGHTENED:
				if ((this.mFleeLength <= Ghost.DEFAULT_FLEE_BLINK_THRESHOLD) && ((this.mFleeLength / Ghost.FLEE_BLINK_INTERVAL) % 2 == 0)) {
					//draw scared blink
					c.drawPath(this.mBody[this.mTickCount % this.mBody.length], this.mScaredBlinkBackground);
					c.drawPath(this.mScaredMouth, this.mScaredBlinkMouthForeground);
					c.drawCircle(this.mCellWidthOverThree, this.mCellHeightOverThree, this.mCellWidthOverFourteen, this.mScaredBlinkEyeForeground);
					c.drawCircle(2.0f * this.mCellWidthOverThree, this.mCellHeightOverThree, this.mCellWidthOverFourteen, this.mScaredBlinkEyeForeground);
				} else {
					//draw normal scared
					c.drawPath(this.mBody[this.mTickCount % this.mBody.length], this.mScaredBackground);
					c.drawPath(this.mScaredMouth, this.mScaredMouthForeground);
					c.drawCircle(this.mCellWidthOverThree, this.mCellHeightOverThree, this.mCellWidthOverFourteen, this.mScaredEyeForeground);
					c.drawCircle(2.0f * this.mCellWidthOverThree, this.mCellHeightOverThree, this.mCellWidthOverFourteen, this.mScaredEyeForeground);
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
	public void setState(final Game game, final State state) {
		if (this.mState != State.EATEN) {
			this.mState = state;
			
			if (state == State.FRIGHTENED) {
				//reverse direction immediately if frightened
				this.mDirectionNext = this.mDirectionCurrent.getOpposite();
			} else {
				//otherwise get new next direction
				this.determineNextDirection(game, true);
			}
		}
	}
	
	@Override
	protected void newLevel(final Game game) {
		//Set initial ghost position
		this.setPosition(this.getInitialPosition(game));
		
		//Initial direction is stopped
		this.mDirectionCurrent = null;
		//Begin TheMan-seeking logic
		this.determineNextDirection(game, false);
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
	protected void determineNextDirection(final Game game, final boolean isStateChange) {
		switch (this.mState) {
			case EATEN:
				final Point initialPosition = this.getInitialPosition(game);
				if ((this.mPosition.x == initialPosition.x) && (this.mPosition.y == initialPosition.y)) {
					this.mState = State.CHASE;
					//fall through to next case
				} else {
					this.determineNextDirectionByLineOfSight(game, initialPosition, isStateChange);
					break;
				}
			
			case CHASE:
				this.determineNextDirectionByLineOfSight(game, this.getChasingPosition(game), isStateChange);				
				break;
				
			case SCATTER:
				this.determineNextDirectionByLineOfSight(game, this.getScatterPosition(game), isStateChange);
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
			
			if (!game.isValidPosition(Entity.move(this.mPosition, nextDirection))) {
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
		
		this.mDirectionNext = nextDirection;
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
	protected abstract Point getScatterPosition(final Game game);
	
	/**
	 * Point to use when chasing down The Man.
	 * 
	 * @param game Game instance.
	 * @return Point
	 */
	protected abstract Point getChasingPosition(final Game game);

	
	
	/**
	 * The ghost Blinky (Shadow).
	 * 
	 * @author Jake Wharton
	 */
	public static class Blinky extends Ghost {
		private static final int BACKGROUND_COLOR = 0xfff00000;
		
		/**
		 * Create a new instance of the ghost Blinky (Shadow).
		 */
		public Blinky() {
			super(Blinky.BACKGROUND_COLOR);
		}
		
		@Override
		protected Point getInitialPosition(final Game game) {
			//last column, second row
			return new Point(game.getCellsWide() - 1, game.getCellRowSpacing() + 1);
		}

		@Override
		protected Point getScatterPosition(final Game game) {
			//upper right
			return new Point(game.getCellsWide(), -1);
		}

		@Override
		protected Point getChasingPosition(final Game game) {
			//use The Man's position as a target
			return new Point(game.getTheMan().getPosition());
		}
	}
	
	/**
	 * The ghost Pinky (Speedy).
	 * 
	 * @author Jake Wharton
	 */
	public static class Pinky extends Ghost {
		private static final int BACKGROUND_COLOR = 0xffff00f0;
		private static final int LEADING_FACTOR = 4;
		
		/**
		 * Create a new instance of the ghost Pinky (Speedy).
		 */
		public Pinky() {
			super(Pinky.BACKGROUND_COLOR);
		}

		@Override
		protected Point getInitialPosition(final Game game) {
			//second column, first row
			return new Point(game.getCellColumnSpacing() + 1, 0);
		}

		@Override
		protected Point getScatterPosition(final Game game) {
			//upper left
			return new Point(-1, -1);
		}

		@Override
		protected Point getChasingPosition(final Game game) {
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
	public static class Inky extends Ghost {
		private static final int BACKGROUND_COLOR = 0xff01d8ff;
		private static final int LEADING_FACTOR = 2;
		private static final int BLINKY_INDEX = 0; //always the first ghost initialized
		
		/**
		 * Create a new instance of the ghost Inky (Bashful).
		 */
		public Inky() {
			super(Inky.BACKGROUND_COLOR);
		}

		@Override
		protected Point getInitialPosition(final Game game) {
			//second to last column, last row
			return new Point(game.getCellsWide() - game.getCellColumnSpacing() - 2, game.getCellsTall() - 1);
		}

		@Override
		protected Point getScatterPosition(final Game game) {
			//lower right
			return new Point(game.getCellsWide(), game.getCellsTall());
		}

		@Override
		protected Point getChasingPosition(final Game game) {
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
	public static class Clyde extends Ghost {
		private static final int BACKGROUND_COLOR = 0xffff8401;
		private static final int PROXIMITY_THRESHOLD = 8;
		
		/**
		 * Create a new instance of the ghost Clyde (Pokey).
		 */
		public Clyde() {
			super(Clyde.BACKGROUND_COLOR);
		}

		@Override
		protected Point getInitialPosition(final Game game) {
			//first column, second to last row
			return new Point(0, game.getCellsTall() - game.getCellRowSpacing() - 2);
		}

		@Override
		protected Point getScatterPosition(final Game game) {
			//lower left
			return new Point(-1, game.getCellsTall());
		}

		@Override
		protected Point getChasingPosition(final Game game) {
			double distance = Math.sqrt(Math.pow(this.mPosition.x - game.getTheMan().getPosition().x, 2) + Math.pow(this.mPosition.y - game.getTheMan().getPosition().y, 2));
			if (distance > Clyde.PROXIMITY_THRESHOLD) {
				//outside proximity zone, The Man's position is the target
				return new Point(game.getTheMan().getPosition());
			} else {
				//inside proximity zone, scatter position is the target
				return this.getScatterPosition(game);
			}
		}
	}
}
