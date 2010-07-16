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
	enum State { HUNT, FLEE, EYES_ONLY }
	
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

		this.mState = State.HUNT;
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
		c.translate(this.mLocation.x, this.mLocation.y);
		
		switch (this.mState) {
			case HUNT:
				c.drawPath(this.mBodyPaths[this.mTickCount % this.mBodyPaths.length], this.mBodyBackground);
				
				//fall through to eyes only case
			case EYES_ONLY:
				Point eyeOffset = Entity.move(new Point(0, 0), this.mDirection);
				
				c.drawCircle(this.mCellWidthOverThree, this.mCellHeightOverThree, this.mCellWidthOverSeven, this.mEyeBackground);
				c.drawCircle(2.0f * this.mCellWidthOverThree, this.mCellHeightOverThree, this.mCellWidthOverSeven, this.mEyeBackground);
				c.drawCircle(this.mCellWidthOverThree + (eyeOffset.x * this.mCellWidthOverFourteen), this.mCellHeightOverThree + (eyeOffset.y * this.mCellWidthOverFourteen), this.mCellWidthOverFourteen, this.mEyeForeground);
				c.drawCircle((2.0f * this.mCellWidthOverThree) + (eyeOffset.x * this.mCellWidthOverFourteen), this.mCellHeightOverThree + (eyeOffset.y * this.mCellWidthOverFourteen), this.mCellWidthOverFourteen, this.mEyeForeground);
				break;
				
			case FLEE:
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
	
	@Override
	protected void reset(final Game game) {
		this.mDirection = Direction.STOPPED;
		this.mNextDirection = Direction.STOPPED;
	}
	

	
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
		protected void moved(Game game) {
			//TODO: use real logic, per ghost
			switch (this.mState) {
				case HUNT:
			    	boolean success = false;
			    	Direction nextDirection = null;
			    	while (!success) {
		    			switch (Game.RANDOM.nextInt(10)) {
		    				case 0:
		        				nextDirection = Direction.NORTH;
		        				break;
		        			case 1:
		        				nextDirection = Direction.SOUTH;
		        				break;
		        			case 2:
		        				nextDirection = Direction.EAST;
		        				break;
		        			case 3:
		        				nextDirection = Direction.WEST;
		        				break;
		        			default: //4-9, most of the time stay straight (if possible)
		        				if (this.mDirection != null) {
		        					nextDirection = this.mDirection;
		        				}
		        				break;
		    			}
	
						if (nextDirection != null) {
							if (nextDirection == this.mNextDirection.getOpposite()) {
								success = false;
							} else {
								success = game.isValidPosition(Entity.move(this.mPosition, nextDirection));
							}
						}
		    		}
			    	
			    	this.mDirection = this.mNextDirection;
			    	this.mNextDirection = nextDirection;
					break;
					
				case EYES_ONLY:
					break;
					
				case FLEE:
					break;
			}
		}

		@Override
		protected void reset(Game game) {
			super.reset(game);
			
			//TODO: move to position
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
		protected void moved(Game game) {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void reset(Game game) {
			super.reset(game);
			
			//TODO: move to position
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
		protected void moved(Game game) {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void reset(Game game) {
			super.reset(game);
			
			//TODO: move to position
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
		protected void moved(Game game) {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void reset(Game game) {
			super.reset(game);
			
			//TODO: move to position
		}
	}
}
