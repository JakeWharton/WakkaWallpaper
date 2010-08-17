package com.jakewharton.wakkawallpaper;

import android.graphics.Canvas;
import android.graphics.Paint;
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
		public int getAngle(final Entity.Direction nextDirection) {
			if ((nextDirection == null) || (this == nextDirection) || (this.getOpposite() == nextDirection)) {
				return this.angle;
			} else {
				int angle1 = this.angle;
				int angle2 = nextDirection.angle;
				
				//Special case NORTH and EAST since (270+0)/2 is not what we want
				if ((this == Entity.Direction.NORTH) && (nextDirection == Entity.Direction.EAST)) {
					angle2 += Entity.Direction.DEGREES_IN_CIRCLE;
				} else if ((this == Entity.Direction.EAST) && (nextDirection == Entity.Direction.NORTH)) {
					angle1 += Entity.Direction.DEGREES_IN_CIRCLE;
				}
				
				return (angle1 + angle2) / 2;
			}
		}
		
		/**
		 * Get the direction that is the opposite of this one.
		 * 
		 * @return Opposite direction.
		 */
		public Entity.Direction getOpposite() {
			switch (this) {
				case NORTH:
					return Entity.Direction.SOUTH;
				case SOUTH:
					return Entity.Direction.NORTH;
				case EAST:
					return Entity.Direction.WEST;
				case WEST:
					return Entity.Direction.EAST;
				default:
					throw new IllegalStateException("This is impossible.");
			}
		}
	}
	enum Style {
		FILL(0, Paint.Style.FILL_AND_STROKE),
		STROKE(1, Paint.Style.STROKE);
		
		public final int value;
		public final Paint.Style style;
		
		private Style(final int value, final Paint.Style style) {
			this.value = value;
			this.style = style;
		}
		
		public static Entity.Style parseInt(final int stateValue) {
			for (final Entity.Style state : Entity.Style.values()) {
				if (state.value == stateValue) {
					return state;
				}
			}
			throw new IllegalArgumentException("Unknown Entity state value: " + stateValue);
		}
	}
	
	private static final String TAG = "WakkaWallpaper.Entity";
	/*package*/static final Paint SPRITE_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
	/*package*/static final int SPRITE_WIDTH = 100;
	/*package*/static final int SPRITE_HEIGHT = 100;
	
	protected final Point mPosition;
	protected final PointF mLocation;
	protected Entity.Direction mDirectionLast;
	protected Entity.Direction mDirectionCurrent;
	protected Entity.Direction mDirectionNext;
	protected float mSpeed;
	protected float mCellWidth;
	protected float mCellHeight;
	protected float mCellWidthOverTwo;
	protected float mCellHeightOverTwo;
	protected int mTickCount;
	protected boolean mIsWrapping;
	
	/**
	 * Create a new entity.
	 */
	protected Entity() {
		this.mPosition = new Point();
		this.mLocation = new PointF();
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
	 * Get the current location of the entity.
	 * 
	 * @return Position.
	 */
	public PointF getLocation() {
		return this.mLocation;
	}
	
	/**
	 * Get the current direction of the entity.
	 * 
	 * @return Position.
	 */
	public Entity.Direction getDirection() {
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

		if (this.mDirectionNext == null) {
			Log.w(Entity.TAG, this.getClass().getSimpleName() + "'s next direction is null. This will result in a fatal error.");
			Log.w(Entity.TAG, "Position: (" + this.mPosition.x + ", " + this.mPosition.y + ")");
			Log.w(Entity.TAG, "Location: (" + this.mLocation.x + ", " + this.mLocation.y + ")");
			Log.w(Entity.TAG, "Direction Current: " + this.mDirectionCurrent);
			Log.w(Entity.TAG, "Direction Last: " + this.mDirectionLast);
			Log.w(Entity.TAG, "Speed: " + (this.mSpeed * 100) + "%");
			
			StringBuilder params = new StringBuilder('{');
			params.append("Position = (");
			params.append(this.mPosition.x);
			params.append(", ");
			params.append(this.mPosition.y);
			params.append("); Location = (");
			params.append(this.mLocation.x);
			params.append(", ");
			params.append(this.mLocation.y);
			params.append("); DirectionLast = ");
			params.append(this.mDirectionLast);
			params.append("; DirectionCurrent = ");
			params.append(this.mDirectionCurrent);
			params.append("; DirectionNext = null; Speed = ");
			params.append(this.mSpeed);
			params.append('}');
			
			//get this over with before the switch below throws it anyways
			throw new NullPointerException(this.getClass().getSimpleName() + "'s next direction is null. " + params.toString());
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
				this.mLocation.set((this.mPosition.x * this.mCellWidth) + this.mCellWidthOverTwo, ((this.mPosition.y - 1) * this.mCellHeight) + this.mCellHeightOverTwo);
				break;
			case SOUTH:
				this.mLocation.set((this.mPosition.x * this.mCellWidth) + this.mCellWidthOverTwo, ((this.mPosition.y + 1) * this.mCellHeight) + this.mCellHeightOverTwo);
				break;
			case EAST:
				this.mLocation.set(((this.mPosition.x + 1) * this.mCellWidth) + this.mCellWidthOverTwo, (this.mPosition.y * this.mCellHeight) + this.mCellHeightOverTwo);
				break;
			case WEST:
				this.mLocation.set(((this.mPosition.x - 1) * this.mCellWidth) + this.mCellWidthOverTwo, (this.mPosition.y * this.mCellHeight) + this.mCellHeightOverTwo);
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
			if (this.mIsWrapping) {
	    		//wrap past bounds positively and negatively
	    		if (this.mPosition.x < 0) {
	    			this.mPosition.x = game.getCellsWide() + this.mPosition.x;
	    		} else {
	    			this.mPosition.x %= game.getCellsWide();
	    		}
	    		if (this.mPosition.y < 0) {
	    			this.mPosition.y = game.getCellsTall() + this.mPosition.y;
	    		} else {
	    			this.mPosition.y %= game.getCellsTall();
	    		}
	    		//resets location coordinates
	    		this.setPosition(this.mPosition);
			}
			
			this.moved(game);
		}
		
		if (Wallpaper.LOG_VERBOSE) {
			Log.v(Entity.TAG, "Position: (" + this.mPosition.x + "," + this.mPosition.y + ");  Location: (" + this.mLocation.x + "," + this.mLocation.y + ");  Direction: " + this.mDirectionCurrent + ";  Next: " + this.mDirectionNext);
		}
	}

    /**
     * Render the entity on the Canvas.
     * 
     * @param game Game instance
     * @param c Canvas on which to draw.
     */
	public abstract void draw(final Game game, final Canvas c);
	
	/**
	 * Triggered when we have moved into a new cell.
	 * 
	 * @param game Game instance
	 */
	protected abstract void moved(final Game game);
	
	/**
	 * Triggered to reset to initial level state.
	 * 
	 * @param game Game instance
	 */
	protected abstract void newLevel(final Game game);
	
	/**
	 * Triggered to reset to initial level position.
	 * 
	 * @param game Game instance
	 */
	public abstract void newLife(final Game game);
	
	
	/**
	 * Update the point one step in the direction specified.
	 * 
	 * @param point Point of original coordinates.
	 * @param direction Direction in which to move the point.
	 * @return New point coordinates.
	 */
	protected static Point move(final Point point, final Entity.Direction direction) {
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
	protected static Point move(final Point point, final Entity.Direction direction, final int steps) {
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
