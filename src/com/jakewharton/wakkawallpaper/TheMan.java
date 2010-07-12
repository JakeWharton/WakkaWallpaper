package com.jakewharton.wakkawallpaper;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.util.Log;

public class TheMan extends Entity {
	private static final String TAG = "TheMan";
	private static final int DEFAULT_FOREGROUND_COLOR = 0xfffff000;
	
    private final Paint mForeground;
	private Direction mWantsToGo;
    
	public TheMan() {
		super(0, 0, Direction.STOPPED);
        
        this.mForeground = new Paint();
        this.mForeground.setColor(TheMan.DEFAULT_FOREGROUND_COLOR);
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
	
	public void tick(Game game) {
    	//TODO: Use AI logic to determine best direction to proceed
    	boolean success = false;
    	while (!success) {
    		if (this.mWantsToGo != null) {
    			success = this.tryMove(game, this.mWantsToGo);
    			this.mWantsToGo = null;
    		} else {
    			switch (Game.RANDOM.nextInt(10)) {
    				case 0:
        				success = this.tryMove(game, Direction.NORTH);
        				break;
        			case 1:
        				success = this.tryMove(game, Direction.SOUTH);
        				break;
        			case 2:
        				success = this.tryMove(game, Direction.EAST);
        				break;
        			case 3:
        				success = this.tryMove(game, Direction.WEST);
        				break;
        			default: //4-9, most of the time stay straight (if possible)
        				success = this.tryMove(game, this.mDirection);
        				break;
    			}
    		}
        }
	}
	
	public void draw(Canvas c) {
		c.save();
		c.translate(this.getLocationX(), this.getLocationY());
		
		int startingAngle = 0;
		int degrees = 360;
		
		switch (this.mDirection) {
			case EAST:
			case WEST:
			case NORTH:
			case SOUTH:
				startingAngle = this.mDirection.getAngle();
				startingAngle -= 45;
				degrees -= 90;
				break;
		}
		
		c.drawArc(new RectF(0, 0, this.mCellWidth, this.mCellHeight), startingAngle, degrees, true, this.mForeground);
		
		c.restore();
	}
}