package com.jakewharton.wakkawallpaper;

import android.graphics.Canvas;
import android.graphics.Point;

public abstract class Entity {
	enum Direction {
		NORTH(0), SOUTH(180), EAST(90), WEST(270), STOPPED(-1);
		
		private int angle;
		public final int COUNT = 4;
		
		private Direction(int angle) {
			this.angle = angle;
		}
		
		public int getAngle() {
			return this.angle;
		}
	}
	
	protected Point mPosition;
	protected Direction mDirection;
	protected float mCellWidth;
	protected float mCellHeight;
	
	protected Entity(int startingPositionX, int startingPositionY, Direction startingDirection) {
		this.mPosition = new Point(startingPositionX, startingPositionY);
		this.mDirection = startingDirection;
		this.mCellWidth = 0;
		this.mCellHeight = 0;
	}
	
	public void setPosition(int x, int y) {
		this.mPosition.set(x, y);
	}
	public void setDirection(Direction direction) {
		this.mDirection = direction;
	}
	protected boolean tryMove(Game game, Direction direction) {
    	Point newPoint = Entity.move(this.mPosition, direction);
    	if (game.isValidPosition(newPoint)) {
    		this.mPosition = newPoint;
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
		return this.mPosition.x * this.mCellWidth;
	}
	public float getLocationY() {
		return this.mPosition.y * this.mCellHeight;
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
