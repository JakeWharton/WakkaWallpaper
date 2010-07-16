package com.jakewharton.wakkawallpaper;

import android.graphics.Canvas;
import android.graphics.Point;

/**
 * The Entity class represents an object that can move within the game board.
 * 
 * @author Jake Wharton
 */
public abstract class Entity {
	enum Direction {
		NORTH(270), SOUTH(90), EAST(0), WEST(180), STOPPED(-1);
		
		private static final int DEGREES_IN_CIRCLE = 360;
		
		private final int angle;
		
		private Direction(final int angle) {
			this.angle = angle;
		}
		
		public int getAngle() {
			return this.angle;
		}
		public int getAngle(final Direction nextDirection) {
			if ((nextDirection == null) || (nextDirection == Direction.STOPPED) || (this == nextDirection) || (this.getOpposite() == nextDirection)) {
				return this.angle;
			} else {
				int angle1 = this.angle;
				int angle2 = nextDirection.getAngle();
				
				//Special case NORTH and EAST since (270+0)/2 is not what we want
				if ((this == Direction.NORTH) && (nextDirection == Direction.EAST)) {
					angle2 += Direction.DEGREES_IN_CIRCLE;
				} else if ((this == Direction.EAST) && (nextDirection == Direction.NORTH)) {
					angle1 += Direction.DEGREES_IN_CIRCLE;
				}
				
				return (angle1 + angle2) / 2;
			}
		}
		
		public Direction getOpposite() {
			switch (this) {
				case NORTH:
					return Direction.SOUTH;
				case SOUTH:
					return Direction.NORTH;
				case EAST:
					return Direction.WEST;
				case WEST:
					return Direction.EAST;
				default:
					return this;
			}
		}
		
		public static Direction[] movingValues() {
			return new Direction[] { NORTH, EAST, SOUTH, WEST };
		}
	}
	public static class Position {
		private final Point mPosition;
		private final Direction mDirection;
		private final Direction mInitialDirection;
		
		public Position(final Point position, final Direction direction) {
			this(position, direction, null);
		}
		private Position(final Point position, final Direction direction, final Direction initialDirection) {
			this.mPosition = position;
			this.mDirection = direction;
			this.mInitialDirection = initialDirection;
		}
		
		public Point getPosition() {
			return this.mPosition;
		}
		public int getPositionX() {
			return this.mPosition.x;
		}
		public int getPositionY() {
			return this.mPosition.y;
		}
		public Direction getDirection() {
			return this.mDirection;
		}
		public Direction getInitialDirection() {
			return this.mInitialDirection;
		}
		public Position[] getPossibleMoves() {
			final Position[] moves = new Position[4];
			int i = 0;
			
			//favor the same direction
			moves[i++] = new Position(Entity.move(this.mPosition, this.mDirection), this.mDirection, (this.mInitialDirection == null) ? this.mDirection : this.mInitialDirection);
			
			//add other three directions
			for (Direction direction : Direction.movingValues()) {
				if (direction != this.mDirection) {
					moves[i++] = new Position(Entity.move(this.mPosition, direction), direction, (this.mInitialDirection == null) ? direction : this.mInitialDirection);
				}
			}
			
			return moves;
		}
	}
	
	protected final Point mPosition;
	protected Direction mDirection;
	protected Direction mNextDirection;
	protected int mDeltaX;
	protected int mDeltaY;
	protected int mGranularity;
	protected float mCellWidth;
	protected float mCellHeight;
	protected int mTickCount;
	protected boolean mMovedThisTick;
	
	/**
	 * Create a new entity adhering to the parameters.
	 * 
	 * @param startingPositionX X coordinate of initial starting position.
	 * @param startingPositionY Y coordinate of initial starting position.
	 * @param startingDirection Initial direction to travel in.
	 */
	protected Entity(final int startingPositionX, final int startingPositionY, final Direction startingDirection) {
		this.mPosition = new Point();
		this.setPosition(startingPositionX, startingPositionY);
		this.mDirection = Direction.STOPPED;
		this.mNextDirection = startingDirection;
		this.mCellWidth = 0;
		this.mCellHeight = 0;
		this.mGranularity = 2;
    	this.mTickCount = 0;
    	this.mMovedThisTick = false;
	}
	
	/**
	 * Set the current position of the entity.
	 * 
	 * @param x X coordinate.
	 * @param y Y coordinate.
	 */
	public void setPosition(final int x, final int y) {
		this.mPosition.set(x, y);
		this.mDeltaX = 0;
		this.mDeltaY = 0;
	}
	
	/**
	 * Set the current direction of the entity.
	 * 
	 * @param direction New Direction.
	 */
	public void setDirection(final Direction direction) {
		this.mDirection = direction;
	}
	
	/**
	 * Resize the entity to fit within the specified dimensions.
	 * 
	 * @param width New width.
	 * @param height New height.
	 */
	public void performResize(final float width, final float height) {
		this.mCellWidth = width;
		this.mCellHeight = height;
	}
	
	/**
	 * Return only the X coordinate of the current location on the screen.
	 * 
	 * @return Float corresponding to the coordinate value.
	 */
	public float getLocationX() {
		return (this.mPosition.x * this.mCellWidth) + ((this.mDeltaX / ((this.mGranularity + 1) * 2.0f)) * this.mCellWidth);
	}
	
	/**
	 * Return only the Y coordinate of the current location on the screen.
	 * 
	 * @return Float corresponding to the coordinate value.
	 */
	public float getLocationY() {
		return (this.mPosition.y * this.mCellHeight) + ((this.mDeltaY / ((this.mGranularity + 1) * 2.0f)) * this.mCellHeight);
	}
	
	/**
	 * Return only the X coordinate of the current position on the board.
	 * 
	 * @return Integer coordinate.
	 */
	public int getPositionX() {
		return this.mPosition.x;
	}
	
	/**
	 * Return only the Y coordinate of the current position on the board.
	 * 
	 * @return Integer coordinate.
	 */
	public int getPositionY() {
		return this.mPosition.y;
	}

    /**
     * Iterate the entity one step.
     * 
     * @param game Game instance
     */
	public void tick(final Game game) {
		this.mTickCount += 1;
		this.mMovedThisTick = false;
		
		switch (this.mDirection) {
			case NORTH:
				if (this.mDeltaY > 0) {
					this.mDeltaY -= 1;
				}
				if ((this.mDeltaX > 0) && (this.mNextDirection != Direction.EAST)) {
					this.mDeltaX -= 1;
				} else if ((this.mDeltaX < 0) && (this.mNextDirection != Direction.WEST)) {
					this.mDeltaX += 1;
				}
				break;
			case SOUTH:
				if (this.mDeltaY < 0) {
					this.mDeltaY += 1;
				}
				if ((this.mDeltaX > 0) && (this.mNextDirection != Direction.EAST)) {
					this.mDeltaX -= 1;
				} else if ((this.mDeltaX < 0) && (this.mNextDirection != Direction.WEST)) {
					this.mDeltaX += 1;
				}
				break;
			case EAST:
				if (this.mDeltaX < 0) {
					this.mDeltaX += 1;
				}
				if ((this.mDeltaY > 0) && (this.mNextDirection != Direction.SOUTH)) {
					this.mDeltaY -= 1;
				} else if ((this.mDeltaY < 0) && (this.mNextDirection != Direction.NORTH)) {
					this.mDeltaY += 1;
				}
				break;
			case WEST:
				if (this.mDeltaX > 0) {
					this.mDeltaX -= 1;
				}
				if ((this.mDeltaY > 0) && (this.mNextDirection != Direction.SOUTH)) {
					this.mDeltaY -= 1;
				} else if ((this.mDeltaY < 0) && (this.mNextDirection != Direction.NORTH)) {
					this.mDeltaY += 1;
				}
				break;
		}
		
		switch (this.mNextDirection) {
			case NORTH:
				if (this.mDeltaY <= 0) {
					this.mDeltaY -= 1;
				}
				break;
			case SOUTH:
				if (this.mDeltaY >= 0) {
					this.mDeltaY += 1;
				}
				break;
			case EAST:
				if (this.mDeltaX >= 0) {
					this.mDeltaX += 1;
				}
				break;
			case WEST:
				if (this.mDeltaX <= 0) {
					this.mDeltaX -= 1;
				}
				break;
		}
		
		//Move to next space if we are far enough
		if (this.mDeltaX > this.mGranularity) {
			this.mPosition.x += 1;
			this.mDeltaX = -this.mGranularity;
			this.mMovedThisTick = true;
		} else if (this.mDeltaX < -this.mGranularity) {
			this.mPosition.x -= 1;
			this.mDeltaX = this.mGranularity;
			this.mMovedThisTick = true;
		} else if (this.mDeltaY > this.mGranularity) {
			this.mPosition.y += 1;
			this.mDeltaY = -this.mGranularity;
			this.mMovedThisTick = true;
		} else if (this.mDeltaY < -this.mGranularity) {
			this.mPosition.y -= 1;
			this.mDeltaY = this.mGranularity;
			this.mMovedThisTick = true;
		}
	}

    /**
     * Render the entity on the Canvas.
     * 
     * @param c Canvas to draw on.
     */
	public abstract void draw(final Canvas c);
	
	/**
	 * Update the point one step in the direction specified.
	 * 
	 * @param point Point of original coordinates.
	 * @param direction Direction in which to move the point.
	 * @return New point coordinates.
	 */
	protected static Point move(final Point point, final Direction direction) {
    	final Point newPoint = new Point(point);
    	switch (direction) {
    		case NORTH:
    			newPoint.y -= 1;
				break;
				
    		case SOUTH:
    			newPoint.y += 1;
				break;
				
    		case WEST:
    			newPoint.x -= 1;
				break;
				
    		case EAST:
    			newPoint.x += 1;
				break;
    	}
    	return newPoint;
    }
}
