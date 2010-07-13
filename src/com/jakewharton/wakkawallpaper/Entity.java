package com.jakewharton.wakkawallpaper;

import android.graphics.Canvas;
import android.graphics.Point;

public abstract class Entity {
	enum Direction {
		NORTH(270), SOUTH(90), EAST(0), WEST(180), STOPPED(-1);
		
		private int angle;
		public final int COUNT = 4;
		
		private Direction(int angle) {
			this.angle = angle;
		}
		
		public int getAngle() {
			return this.angle;
		}
		
		public static Direction getOpposite(Direction direction) {
			switch (direction) {
				case NORTH:
					return Direction.SOUTH;
				case SOUTH:
					return Direction.NORTH;
				case EAST:
					return Direction.WEST;
				case WEST:
					return Direction.EAST;
				default:
					throw new IllegalArgumentException("Must be called with explicit direction.");
					
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
	
	protected Entity(int startingPositionX, int startingPositionY, Direction startingDirection) {
		this.mPosition = new Point();
		this.setPosition(startingPositionX, startingPositionY);
		this.mDirection = Direction.STOPPED;
		this.mNextDirection = startingDirection;
		this.mCellWidth = 0;
		this.mCellHeight = 0;
		this.mGranularity = 2;
	}
	
	public void setPosition(int x, int y) {
		this.mPosition.set(x, y);
		this.mDeltaX = 0;
		this.mDeltaY = 0;
	}
	public void setDirection(Direction direction) {
		this.mDirection = direction;
	}
	protected boolean tryMove(Game game, Direction direction) {
    	Point newPoint = Entity.move(this.mPosition, direction);
    	if (game.isValidPosition(newPoint)) {
    		this.mPosition.set(newPoint.x, newPoint.y);
    		this.mDirection = direction;
    		return true;
    	} else {
    		return false;
    	}
    }
	public void performResize(float width, float height) {
		this.mCellWidth = width;
		this.mCellHeight = height;
	}
	public float getLocationX() {
		return (this.mPosition.x * this.mCellWidth) + ((this.mDeltaX / ((this.mGranularity + 1) * 2.0f)) * this.mCellWidth);
	}
	public float getLocationY() {
		return (this.mPosition.y * this.mCellHeight) + ((this.mDeltaY / ((this.mGranularity + 1) * 2.0f)) * this.mCellHeight);
	}
	public int getPositionX() {
		return this.mPosition.x;
	}
	public int getPositionY() {
		return this.mPosition.y;
	}
	
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
	
	public abstract void tick(Game game);
	public abstract void draw(Canvas c);
}
