package com.jakewharton.wakkawallpaper;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;

/**
 * The Ghost class represents an enemy on the board.
 * 
 * @author Jake Wharton
 */
public abstract class Ghost extends Entity {
	enum State { CHASE, SCATTER, FRIGHTENED, EATEN }
	
	private static final int FLEE_LENGTH = 7000;
	private static final int FLEE_BLINK_THRESHOLD = 2000;
	private static final int FLEE_BLINK_INTERVAL = 200;
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
	private final Paint mScaredForeground;
	private final Paint mScaredBlinkBackground;
	private final Paint mScaredBlinkForeground;
	private final Path[] mBodyPaths;
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
		this.mFleeLength = Ghost.FLEE_LENGTH;
		
		this.mBodyBackground = new Paint();
		this.mBodyBackground.setColor(backgroundColor);
		this.mBodyBackground.setAntiAlias(true);
		this.mEyeBackground = new Paint();
		this.mEyeBackground.setColor(Ghost.DEFAULT_EYE_BACKGROUND);
		this.mEyeBackground.setAntiAlias(true);
		this.mEyeForeground = new Paint();
		this.mEyeForeground.setColor(Ghost.DEFAULT_EYE_FOREGROUND);
		this.mEyeForeground.setAntiAlias(true);
		this.mScaredBackground = new Paint();
		this.mScaredBackground.setColor(Ghost.DEFAULT_SCARED_BACKGROUND);
		this.mScaredBackground.setAntiAlias(true);
		this.mScaredForeground = new Paint();
		this.mScaredForeground.setColor(Ghost.DEFAULT_SCARED_FOREGROUND);
		this.mScaredForeground.setAntiAlias(true);
		this.mScaredBlinkBackground = new Paint();
		this.mScaredBlinkBackground.setColor(Ghost.DEFAULT_SCARED_BLINK_BACKGROUND);
		this.mScaredBlinkBackground.setAntiAlias(true);
		this.mScaredBlinkForeground = new Paint();
		this.mScaredBlinkForeground.setColor(Ghost.DEFAULT_SCARED_BLINK_FOREGROUND);
		this.mScaredBlinkForeground.setAntiAlias(true);
		
		this.mBodyPaths = new Path[2];
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
		
		this.mBodyPaths[0] = new Path();
		this.mBodyPaths[0].moveTo(0, 0.75f * this.mCellHeight);
		this.mBodyPaths[0].lineTo(0, 0.9f * this.mCellHeight);
		this.mBodyPaths[0].lineTo(0.1f * this.mCellWidth, this.mCellHeight);
		this.mBodyPaths[0].lineTo(0.3f * this.mCellWidth, 0.8f * this.mCellHeight);
		this.mBodyPaths[0].lineTo(0.5f * this.mCellWidth, this.mCellHeight);
		this.mBodyPaths[0].lineTo(0.7f * this.mCellWidth, 0.8f * this.mCellHeight);
		this.mBodyPaths[0].lineTo(0.9f * this.mCellWidth, this.mCellHeight);
		this.mBodyPaths[0].lineTo(this.mCellWidth, 0.9f * this.mCellHeight);
		this.mBodyPaths[0].arcTo(new RectF(0, 0, this.mCellWidth, 0.75f * this.mCellHeight), 0, -180);

		final float widthOverSix = this.mCellWidth / 6.0f;
		this.mBodyPaths[1] = new Path();
		this.mBodyPaths[1].moveTo(0, 0.75f * this.mCellHeight);
		this.mBodyPaths[1].lineTo(0, this.mCellHeight);
		this.mBodyPaths[1].lineTo(1 * widthOverSix, 0.8f * this.mCellHeight);
		this.mBodyPaths[1].lineTo(2 * widthOverSix, this.mCellHeight);
		this.mBodyPaths[1].lineTo(3 * widthOverSix, 0.8f * this.mCellHeight);
		this.mBodyPaths[1].lineTo(4 * widthOverSix, this.mCellHeight);
		this.mBodyPaths[1].lineTo(5 * widthOverSix, 0.8f * this.mCellHeight);
		this.mBodyPaths[1].lineTo(this.mCellWidth, this.mCellHeight);
		this.mBodyPaths[1].arcTo(new RectF(0, 0, this.mCellWidth, 0.75f * this.mCellHeight), 0, -180);
		
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
				c.drawPath(this.mBodyPaths[this.mTickCount % this.mBodyPaths.length], this.mBodyBackground);
				
				//fall through to eyes only case
			case EATEN:
				Point eyeOffset = Entity.move(new Point(0, 0), this.mDirection);
				
				c.drawCircle(this.mCellWidthOverThree, this.mCellHeightOverThree, this.mCellWidthOverSeven, this.mEyeBackground);
				c.drawCircle(2.0f * this.mCellWidthOverThree, this.mCellHeightOverThree, this.mCellWidthOverSeven, this.mEyeBackground);
				c.drawCircle(this.mCellWidthOverThree + (eyeOffset.x * this.mCellWidthOverFourteen), this.mCellHeightOverThree + (eyeOffset.y * this.mCellWidthOverFourteen), this.mCellWidthOverFourteen, this.mEyeForeground);
				c.drawCircle((2.0f * this.mCellWidthOverThree) + (eyeOffset.x * this.mCellWidthOverFourteen), this.mCellHeightOverThree + (eyeOffset.y * this.mCellWidthOverFourteen), this.mCellWidthOverFourteen, this.mEyeForeground);
				break;
				
			case FRIGHTENED:
				if ((this.mFleeLength <= Ghost.FLEE_BLINK_THRESHOLD) && ((this.mFleeLength / Ghost.FLEE_BLINK_INTERVAL) % 2 == 0)) {
					//draw scared blink
					c.drawPath(this.mBodyPaths[this.mTickCount % this.mBodyPaths.length], this.mScaredBlinkBackground);
				} else {
					//draw normal scared
					c.drawPath(this.mBodyPaths[this.mTickCount % this.mBodyPaths.length], this.mScaredBackground);
				}
				break;
		}
		
		c.restore();
	}

	/**
	 * Get ghost state.
	 * @return State
	 */
	public State getState() {
		return this.mState;
	}
	
	/**
	 * Change ghost state. This will trigger the locating of a new next direction.
	 * @param game Game instance
	 * @param state Next ghost state
	 */
	public void setState(final Game game, final State state) {
		this.mState = state;
		
		if (state == State.FRIGHTENED) {
			//reverse direction immediately if frightened
			this.mNextDirection = this.mDirection.getOpposite();
		} else {
			//otherwise get new next direction
			this.determineNextDirection(game, true);
		}
	}
	
	@Override
	protected void newLevel(final Game game) {
		this.mDirection = null;
		this.mNextDirection = null;
		this.setPosition(this.getInitialPositionX(game), this.getInitialPositionY(game));
	}
	
	@Override
	protected void moved(final Game game) {
		game.checkGhosts();
		this.determineNextDirection(game, false);
	}
	
	/**
	 * Determine the next direction to travel in.
	 * @param game Game instance
	 * @param isStateChange Whether or not this change is the result of a state change
	 */
	protected void determineNextDirection(final Game game, final boolean isStateChange) {
		switch (this.mState) {
			case CHASE:
				this.determindNextDirectionWhenChasing(game, isStateChange);				
				break;
				
			case SCATTER:
				this.determineNextDirectionByLineOfSight(game, this.getInitialCorner(game), isStateChange);
				break;
				
			case EATEN:
				this.determineNextDirectionByLineOfSight(game, new Point(this.getInitialPositionX(game), this.getInitialPositionY(game)), isStateChange);
				break;
				
			case FRIGHTENED:
				this.determineNextFrightenedDirection(game);
				break;
		}
	}
	
	/**
	 * Determine next direction based on a simple random number generator.
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
					if (game.isValidPosition(Entity.move(this.mPosition, direction)) && (direction != this.mDirection.getOpposite())) {
						//Save and exit the loop
						nextDirection = direction;
						break;
					}
				}
			}
			
			//Store new direction
			this.mNextDirection = nextDirection;
		} else {
			//Not at intersection, go straight
			this.mNextDirection = this.mDirection;
		}
	}
	
	/**
	 * Use line-of-sight distance to a target point to determine the next direction
	 * @param game Game instance
	 * @param target Target Point
	 * @param isStateChange Whether or not this is occurring because of a state change
	 */
	protected void determineNextDirectionByLineOfSight(final Game game, final Point target, final boolean isStateChange) {
		Point nextPoint;
		double nextDistance;
		Direction nextDirection = null;
		double shortestDistance = Double.MAX_VALUE;
		
		for (Direction direction : Direction.values()) {
			if (isStateChange || (direction != this.mDirection.getOpposite())) {
				nextPoint = Entity.move(this.mPosition, direction);
				nextDistance = Math.sqrt(Math.pow(nextPoint.x - target.x, 2) + Math.pow(nextPoint.y - target.y, 2));
				if (nextDistance < shortestDistance) {
					nextDirection = direction;
					shortestDistance = nextDistance; 
				}
			}
		}
		
		this.mNextDirection = nextDirection;
	}
	
	/**
	 * X coordinate of initial starting position
	 * @param game Game instance
	 * @return X coordinate
	 */
	protected abstract int getInitialPositionX(final Game game);
	
	/**
	 * Y coordinate of initial starting position
	 * @param game Game instance
	 * @return Y coordinate
	 */
	protected abstract int getInitialPositionY(final Game game);
	
	/**
	 * Point off of the board of the initial starting corner
	 * @param game Game instance
	 * @return Point
	 */
	protected abstract Point getInitialCorner(final Game game);
	
	/**
	 * Determine the next location to travel in when chasing
	 * @param game Game instance
	 * @param isStateChange Whether or not this new direction is occuring from a state change
	 */
	protected abstract void determindNextDirectionWhenChasing(final Game game, final boolean isStateChange);

	
	
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
		protected int getInitialPositionX(final Game game) {
			//last column
			return game.getCellsWide() - 1;
		}

		@Override
		protected int getInitialPositionY(final Game game) {
			//second row
			return game.getCellRowSpacing() + 1;
		}

		@Override
		protected Point getInitialCorner(final Game game) {
			//upper right
			return new Point(game.getCellsWide(), 0);
		}

		@Override
		protected void determindNextDirectionWhenChasing(final Game game, final boolean isStateChange) {
			// TODO Auto-generated method stub
		}
	}
	
	/**
	 * The ghost Pinky (Speedy).
	 * 
	 * @author Jake Wharton
	 */
	public static class Pinky extends Ghost {
		private static final int BACKGROUND_COLOR = 0xffff00f0;
		
		/**
		 * Create a new instance of the ghost Pinky (Speedy).
		 */
		public Pinky() {
			super(Pinky.BACKGROUND_COLOR);
		}

		@Override
		protected int getInitialPositionX(final Game game) {
			//second column
			return game.getCellColumnSpacing() + 1;
		}

		@Override
		protected int getInitialPositionY(final Game game) {
			//first row
			return 0;
		}

		@Override
		protected Point getInitialCorner(final Game game) {
			//upper left
			return new Point(0, 0);
		}

		@Override
		protected void determindNextDirectionWhenChasing(final Game game, final boolean isStateChange) {
			// TODO Auto-generated method stub
		}
	}
	
	/**
	 * The ghost Inky (Bashful).
	 * 
	 * @author Jake Wharton
	 */
	public static class Inky extends Ghost {
		private static final int BACKGROUND_COLOR = 0xff01d8ff;
		
		/**
		 * Create a new instance of the ghost Inky (Bashful).
		 */
		public Inky() {
			super(Inky.BACKGROUND_COLOR);
		}

		@Override
		protected int getInitialPositionX(final Game game) {
			//second to last column
			return game.getCellsWide() - game.getCellColumnSpacing() - 2;
		}

		@Override
		protected int getInitialPositionY(final Game game) {
			//last row
			return game.getCellsTall() - 1;
		}

		@Override
		protected Point getInitialCorner(final Game game) {
			//lower right
			return new Point(game.getCellsWide(), game.getCellsTall());
		}

		@Override
		protected void determindNextDirectionWhenChasing(final Game game, final boolean isStateChange) {
			// TODO Auto-generated method stub
		}
	}
	
	/**
	 * The ghost Clyde (Pokey).
	 * 
	 * @author Jake Wharton
	 */
	public static class Clyde extends Ghost {
		private static final int BACKGROUND_COLOR = 0xffff8401;
		
		/**
		 * Create a new instance of the ghost Clyde (Pokey).
		 */
		public Clyde() {
			super(Clyde.BACKGROUND_COLOR);
		}

		@Override
		protected int getInitialPositionX(final Game game) {
			//first column
			return 0;
		}

		@Override
		protected int getInitialPositionY(final Game game) {
			//second to last row
			return game.getCellsTall() - game.getCellRowSpacing() - 2;
		}

		@Override
		protected Point getInitialCorner(final Game game) {
			//lower left
			return new Point(0, game.getCellsTall());
		}

		@Override
		protected void determindNextDirectionWhenChasing(final Game game, final boolean isStateChange) {
			// TODO Auto-generated method stub
		}
	}
}
