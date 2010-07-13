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
    
	public TheMan() {
		super(0, 0, Direction.EAST);
        
        this.mForeground = new Paint();
        this.mForeground.setColor(TheMan.DEFAULT_FOREGROUND_COLOR);
        this.mForeground.setAntiAlias(true);
        this.mForeground.setStyle(Style.FILL_AND_STROKE);
    	
    	this.mWantsToGo = null;
	}
	
	public void setForeground(int color) {
		this.mForeground.setColor(color);
	}
    public void setWantsToGo(Direction direction) {
    	Log.d(TheMan.TAG, "Wants to go " + direction.toString());
    	this.mWantsToGo = direction;
    }
	
    @Override
	public void tick(Game game) {
		super.tick(game);
		
		if (this.mMovedThisTick || (this.mWantsToGo != null)) {
			boolean success = false;
			Direction nextDirection = null;
			while (!success) {
				if (this.mWantsToGo != null) {
					nextDirection = this.mWantsToGo;
					this.mWantsToGo = null;
				} else {
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
							if (this.mDirection != Direction.STOPPED) {
								nextDirection = this.mDirection;
							}
							break;
					}
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
		}
	}
	
	public void draw(Canvas c) {
		c.save();
		c.translate(this.getLocationX(), this.getLocationY());
		
		//keep us low
		this.mTickCount %= TheMan.CHOMP_ANGLE_COUNT;
		
		int angle = TheMan.CHOMP_ANGLES[this.mTickCount];
		float startingAngle = this.mDirection.angle + (angle / 2.0f);
		int degrees = 360;
		if (this.mDirection != Direction.STOPPED) {
			degrees -= angle;
		}
		
		c.drawArc(new RectF(0, 0, this.mCellWidth, this.mCellHeight), startingAngle, degrees, true, this.mForeground);
		
		c.restore();
	}
}