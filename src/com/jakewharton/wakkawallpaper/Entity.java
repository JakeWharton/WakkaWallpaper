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
		
		public final int angle;
		
		private Direction(int angle) {
			this.angle = angle;
		}
		
		public Direction getOpposite() {
			switch (this) {
				case NORTH:
					return SOUTH;
				case SOUTH:
					return NORTH;
				case EAST:
					return WEST;
				case WEST:
					return EAST;
				default:
					return this;
			}
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
	protected Entity(int startingPositionX, int startingPositionY, Direction startingDirection) {
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
	public void setPosition(int x, int y) {
		this.mPosition.set(x, y);
		this.mDeltaX = 0;
		this.mDeltaY = 0;
	}
	
	/**
	 * Set the current direction of the entity.
	 * 
	 * @param direction New Direction.
	 */
	public void setDirection(Direction direction) {
		this.mDirection = direction;
	}
	
	/**
	 * Resize the entity to fit within the specified dimensions.
	 * 
	 * @param width New width.
	 * @param height New height.
	 */
	public void performResize(float width, float height) {
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
	public void tick(Game game) {
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
	public abstract void draw(Canvas c);
	
	/**
	 * Update the point one step in the direction specified.
	 * 
	 * @param point Point of original coordinates.
	 * @param direction Direction in which to move the point.
	 * @return New point coordinates.
	 */
	protected static Point move(Point point, Direction direction) {
    	Point newPoint = new Point(point);
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
