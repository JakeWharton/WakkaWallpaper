package com.jakewharton.wakkawallpaper;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PointF;

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
			
			if (this.mDirection != Direction.STOPPED) {
				//favor the same direction
				moves[i++] = new Position(Entity.move(this.mPosition, this.mDirection), this.mDirection, (this.mInitialDirection == null) ? this.mDirection : this.mInitialDirection);
			}
			
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
	protected final PointF mLocation;
	protected Direction mDirection;
	protected Direction mNextDirection;
	protected float mSpeed;
	protected float mCellWidth;
	protected float mCellHeight;
	protected int mTickCount;
	
	/**
	 * Create a new entity.
	 */
	protected Entity() {
		this.mPosition = new Point();
		this.mLocation = new PointF();
		this.mDirection = null;
		this.mNextDirection = null;
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
	public void performResize(final float width, final float height) {
		this.mCellWidth = width;
		this.mCellHeight = height;
	}

    /**
     * Iterate the entity one step.
     * 
     * @param game Game instance
     */
	public void tick(final Game game) {
		this.mTickCount += 1;
		
		//TODO: move this.mLocation based on this.mSpeed and this.mDirection
		
		//Move to next space if we are far enough
		if (this.mLocation.x >= ((this.mPosition.x + 1) * this.mCellWidth)) {
			this.mPosition.x += 1;
			this.moved(game);
		} else if (this.mLocation.x < (this.mPosition.x * this.mCellWidth)) {
			this.mPosition.x -= 1;
			this.moved(game);
		} else if (this.mLocation.y >= ((this.mPosition.y + 1) * this.mCellHeight)) {
			this.mPosition.y += 1;
			this.moved(game);
		} else if (this.mLocation.y < (this.mPosition.y * this.mCellHeight)) {
			this.mPosition.y -= 1;
			this.moved(game);
		}
	}

    /**
     * Render the entity on the Canvas.
     * 
     * @param c Canvas to draw on.
     */
	public abstract void draw(final Canvas c);
	
	/**
	 * Callback when we have moved into a new cell.
	 * @param game Game instance
	 */
	protected abstract void moved(final Game game);
	
	/**
	 * Callback to reset to initial game position
	 * @param game Game instance
	 */
	protected abstract void reset(final Game game);
	
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
