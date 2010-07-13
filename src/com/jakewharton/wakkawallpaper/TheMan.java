package com.jakewharton.wakkawallpaper;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.util.Log;

public class TheMan extends Entity {
	private static final String TAG = "TheMan";
	private static final int DEFAULT_FOREGROUND_COLOR = 0xfffff000;
	private static final int CHOMP_ANGLE_COUNT = 4;
	private static final int[] CHOMP_ANGLES = new int[] { 90, 45, 0, 45 };
	
    private final Paint mForeground;
	private Direction mWantsToGo;
	private int mTickCount;
    
	public TheMan() {
		super(0, 0, Direction.EAST);
        
        this.mForeground = new Paint();
        this.mForeground.setColor(TheMan.DEFAULT_FOREGROUND_COLOR);
        this.mForeground.setStyle(Style.FILL_AND_STROKE);
    	
    	this.mWantsToGo = null;
    	this.mTickCount = 0;
	}
	
	public void setForeground(int color) {
		this.mForeground.setColor(color);
	}
    public void setWantsToGo(Direction direction) {
    	Log.d(TheMan.TAG, "Wants to go " + direction.toString());
    	this.mWantsToGo = direction;
    }
	
	public void tick(Game game) {
		this.mTickCount += 1;
		
		switch (this.mDirection) {
			case NORTH:
				if (this.mDeltaY > 0) {
					this.mDeltaY -= 1;
				}
				if (this.mDeltaX > 0) {
					this.mDeltaX -= 1;
				} else if (this.mDeltaX < 0) {
					this.mDeltaX += 1;
				}
				break;
			case SOUTH:
				if (this.mDeltaY < 0) {
					this.mDeltaY += 1;
				}
				if (this.mDeltaX > 0) {
					this.mDeltaX -= 1;
				} else if (this.mDeltaX < 0) {
					this.mDeltaX += 1;
				}
				break;
			case EAST:
				if (this.mDeltaX < 0) {
					this.mDeltaX += 1;
				}
				if (this.mDeltaY > 0) {
					this.mDeltaY -= 1;
				} else if (this.mDeltaY < 0) {
					this.mDeltaY += 1;
				}
				break;
			case WEST:
				if (this.mDeltaX > 0) {
					this.mDeltaX -= 1;
				}
				if (this.mDeltaY > 0) {
					this.mDeltaY -= 1;
				} else if (this.mDeltaY < 0) {
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
		boolean moved = false;
		if (this.mDeltaX > this.mGranularity) {
			this.mPosition.x += 1;
			this.mDeltaX = -this.mGranularity;
			moved = true;
		} else if (this.mDeltaX < -this.mGranularity) {
			this.mPosition.x -= 1;
			this.mDeltaX = this.mGranularity;
			moved = true;
		}
		if (this.mDeltaY > this.mGranularity) {
			this.mPosition.y += 1;
			this.mDeltaY = -this.mGranularity;
			moved = true;
		} else if (this.mDeltaY < -this.mGranularity) {
			this.mPosition.y -= 1;
			this.mDeltaY = this.mGranularity;
			moved = true;
		}
		
		if (moved || (this.mDirection == Direction.STOPPED)) {
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
					default:
						nextDirection = this.mDirection;
						break;
				}
				
				success = game.isValidPosition(Entity.move(this.mPosition, nextDirection));
			}
			
			Log.d(TheMan.TAG, "Changing direction to " + nextDirection.toString());
			this.mDirection = this.mNextDirection;
			this.mNextDirection = nextDirection;
		}
	}
	
	public void draw(Canvas c) {
		c.save();
		c.translate(this.getLocationX(), this.getLocationY());
		
		//keep us low
		this.mTickCount %= TheMan.CHOMP_ANGLE_COUNT;
		
		int angle = TheMan.CHOMP_ANGLES[this.mTickCount];
		float startingAngle = this.mDirection.getAngle() + (angle / 2.0f);
		int degrees = 360;
		if (this.mDirection != Direction.STOPPED) {
			degrees -= angle;
		}
		
		c.drawArc(new RectF(0, 0, this.mCellWidth, this.mCellHeight), startingAngle, degrees, true, this.mForeground);
		
		c.restore();
	}
}