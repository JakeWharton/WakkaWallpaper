package com.jakewharton.wakkawallpaper;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.Log;

/**
 * The Entity class represents an object that can move within the game board.
 * 
 * @author Jake Wharton
 */
public abstract class Entity {
	enum Direction {
		NORTH(270), SOUTH(90), EAST(0), WEST(180);
		
		private static final int DEGREES_IN_CIRCLE = 360;
		
		protected final int angle;
		
		/**
		 * Create a direction with specified angle.
		 * 
		 * @param angle Angle in degrees.
		 */
		private Direction(final int angle) {
			this.angle = angle;
		}
		
		/**
		 * Get the angle of the direction taking into account the next direction.
		 * 
		 * @param nextDirection The next direction.
		 * @return Angle.
		 */
		public int getAngle(final Direction nextDirection) {
			if ((nextDirection == null) || (this == nextDirection) || (this.getOpposite() == nextDirection)) {
				return this.angle;
			} else {
				int angle1 = this.angle;
				int angle2 = nextDirection.angle;
				
				//Special case NORTH and EAST since (270+0)/2 is not what we want
				if ((this == Direction.NORTH) && (nextDirection == Direction.EAST)) {
					angle2 += Direction.DEGREES_IN_CIRCLE;
				} else if ((this == Direction.EAST) && (nextDirection == Direction.NORTH)) {
					angle1 += Direction.DEGREES_IN_CIRCLE;
				}
				
				return (angle1 + angle2) / 2;
			}
		}
		
		/**
		 * Get the direction that is the opposite of this one.
		 * 
		 * @return Opposite direction.
		 */
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
					return null;
			}
		}
	}
	
	private static final String TAG = "WakkaWallpaper.Entity";
	
	protected final Point mPosition;
	protected final PointF mLocation;
	protected Direction mDirectionLast;
	protected Direction mDirectionCurrent;
	protected Direction mDirectionNext;
	protected float mSpeed;
	protected float mCellWidth;
	protected float mCellHeight;
	protected float mCellWidthOverTwo;
	protected float mCellHeightOverTwo;
	protected int mTickCount;
	
	/**
	 * Create a new entity.
	 */
	protected Entity() {
		this.mPosition = new Point();
		this.mLocation = new PointF();
		this.mDirectionCurrent = null;
		this.mDirectionNext = null;
		this.mSpeed = 1.0f; //100%
		this.mCellWidth = 0;
		this.mCellHeight = 0;
    	this.mTickCount = 0;
	}
	
	/**
	 * Resize the entity to fit within the specified dimensions.
	 * 
	 * @param width New width.
	 * @param height New height.
	 */
	public void performResize(final Game game) {
		if (Wallpaper.LOG_VERBOSE) {
			Log.v(Entity.TAG, "> performResize()");
		}
		
		this.mCellWidth = game.getCellWidth();
		this.mCellHeight = game.getCellHeight();
		this.mCellWidthOverTwo = this.mCellWidth / 2.0f;
		this.mCellHeightOverTwo = this.mCellHeight / 2.0f;
		
		//Reset position to update location
		this.setPosition(this.mPosition);

		if (Wallpaper.LOG_VERBOSE) {
			Log.v(Entity.TAG, "< performResize()");
		}
	}
	
	/**
	 * Get the current position of the entity.
	 * 
	 * @return Position.
	 */
	public Point getPosition() {
		return this.mPosition;
	}
	
	/**
	 * Get the current direction of the entity.
	 * 
	 * @return Position.
	 */
	public Direction getDirection() {
		return this.mDirectionCurrent;
	}
	
	/**
	 * Set the board position and location.
	 * 
	 * @param x X coordinate.
	 * @param y Y coordinate.
	 */
	public void setPosition(final Point position) {
		this.mPosition.set(position.x, position.y);
		this.mLocation.set((position.x * this.mCellWidth) + this.mCellWidthOverTwo, (position.y * this.mCellHeight) + this.mCellHeightOverTwo);
	}
	
	/**
	 * Test if this entity is occupying the same cell as another.
	 * 
	 * @param other Other entity.
	 * @return Whether or not they are occupying the same cell.
	 */
	public boolean isCollidingWith(final Entity other) {
		return ((this.mPosition.x == other.getPosition().x) && (this.mPosition.y == other.getPosition().y));
	}

    /**
     * Iterate the entity one step.
     * 
     * @param game Game instance
     */
	public void tick(final Game game) {
		this.mTickCount += 1;

		if (Wallpaper.LOG_DEBUG) {
			if (this.mDirectionNext == null) {
				Log.w(Entity.TAG, this.getClass().getSimpleName() + "'s next direction is null. This will result in a fatal error.");
				Log.w(Entity.TAG, "Position: (" + this.mPosition.x + ", " + this.mPosition.y + ")");
				Log.w(Entity.TAG, "Location: (" + this.mLocation.x + ", " + this.mLocation.y + ")");
				Log.w(Entity.TAG, "Direction Current: " + this.mDirectionCurrent);
				Log.w(Entity.TAG, "Direction Last: " + this.mDirectionLast);
				Log.w(Entity.TAG, "Speed: " + (this.mSpeed * 100) + "%");
			}
		}
		
		//Promote current direction to last
		this.mDirectionLast = this.mDirectionCurrent;
		//Promote next direction to current
		this.mDirectionCurrent = this.mDirectionNext;
		//Next direction fallback. Will be set by implementing moved() method call.
		this.mDirectionNext = null;
		
		//TODO: move this.mLocation based on this.mSpeed and this.mDirectionCurrent
		switch (this.mDirectionCurrent) {
			case NORTH:
				this.mLocation.set((this.mPosition.x * this.mCellWidth) + this.mCellWidthOverTwo, (this.mPosition.y - 1 * this.mCellHeight) + this.mCellHeightOverTwo);
				break;
			case SOUTH:
				this.mLocation.set((this.mPosition.x * this.mCellWidth) + this.mCellWidthOverTwo, (this.mPosition.y + 1 * this.mCellHeight) + this.mCellHeightOverTwo);
				break;
			case EAST:
				this.mLocation.set((this.mPosition.x + 1 * this.mCellWidth) + this.mCellWidthOverTwo, (this.mPosition.y * this.mCellHeight) + this.mCellHeightOverTwo);
				break;
			case WEST:
				this.mLocation.set((this.mPosition.x - 1 * this.mCellWidth) + this.mCellWidthOverTwo, (this.mPosition.y * this.mCellHeight) + this.mCellHeightOverTwo);
				break;
		}
		
		//Move to next space if we are far enough
		boolean moved = false;
		if (this.mLocation.x >= ((this.mPosition.x + 1) * this.mCellWidth)) {
			this.mPosition.x += 1;
			moved = true;
		} else if (this.mLocation.x < (this.mPosition.x * this.mCellWidth)) {
			this.mPosition.x -= 1;
			moved = true;
		} else if (this.mLocation.y >= ((this.mPosition.y + 1) * this.mCellHeight)) {
			this.mPosition.y += 1;
			moved = true;
		} else if (this.mLocation.y < (this.mPosition.y * this.mCellHeight)) {
			this.mPosition.y -= 1;
			moved = true;
		}
		
		if (moved) {
			this.moved(game);
		}
		
		if (Wallpaper.LOG_VERBOSE) {
			Log.v(Entity.TAG, "Position: (" + this.mPosition.x + "," + this.mPosition.y + ");  Location: (" + this.mLocation.x + "," + this.mLocation.y + ");  Direction: " + this.mDirectionCurrent + ";  Next: " + this.mDirectionNext);
		}
	}

    /**
     * Render the entity on the Canvas.
     * 
     * @param c Canvas on which to draw.
     */
	public abstract void draw(final Canvas c);
	
	/**
	 * Triggered when we have moved into a new cell.
	 * 
	 * @param game Game instance
	 */
	protected abstract void moved(final Game game);
	
	/**
	 * Triggered to reset to initial game position.
	 * 
	 * @param game Game instance
	 */
	protected abstract void newLevel(final Game game);
	
	
	/**
	 * Update the point one step in the direction specified.
	 * 
	 * @param point Point of original coordinates.
	 * @param direction Direction in which to move the point.
	 * @return New point coordinates.
	 */
	protected static Point move(final Point point, final Direction direction) {
		return Entity.move(point, direction, 1);
	}
	
	/**
	 * Update the point in the direction specified by a specific number of steps.
	 * 
	 * @param point Point of original coordinates.
	 * @param direction Direction in which to move the point.
	 * @param setps Number of steps to move point.
	 * @return New point coordinates.
	 */
	protected static Point move(final Point point, final Direction direction, final int steps) {
    	final Point newPoint = new Point(point);
    	if (direction != null) {
	    	switch (direction) {
	    		case NORTH:
	    			newPoint.y -= steps;
					break;
					
	    		case SOUTH:
	    			newPoint.y += steps;
					break;
					
	    		case WEST:
	    			newPoint.x -= steps;
					break;
					
	    		case EAST:
	    			newPoint.x += steps;
					break;
	    	}
    	}
    	return newPoint;
    }
}
