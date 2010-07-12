package com.jakewharton.wakkawallpaper;

import com.jakewharton.wakkawallpaper.Game.Cell;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
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
		super(0, 0, Direction.STOPPED);
        
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
		
    	//TODO: Use AI logic to determine best direction to proceed
    	boolean success = false;
    	while (!success) {
    		if (this.mWantsToGo != null) {
    			success = this.tryMove(game, this.mWantsToGo);
    			this.mWantsToGo = null;
    		} else {
    			Point next = Entity.move(this.mPosition, this.mDirection);
    			if (game.isValidPosition(next) && ((game.getCell(next.x, next.y) == Cell.DOT) || (game.getCell(next.x, next.y) == Cell.JUGGERDOT))) {
    				this.mPosition = next;
    				success = true;
    			} else {
    				//TODO: AI for nearest dot
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
						default:
							success = this.tryMove(game, this.mDirection);
							break;
    				}
    			}
    		}
        }
	}
	
	public void draw(Canvas c) {
		c.save();
		c.translate(this.getLocationX(), this.getLocationY());
		
		int angle = TheMan.CHOMP_ANGLES[this.mTickCount % TheMan.CHOMP_ANGLE_COUNT];
		float startingAngle = this.mDirection.getAngle() + (angle / 2.0f);
		int degrees = 360 - angle;
		
		c.drawArc(new RectF(0, 0, this.mCellWidth, this.mCellHeight), startingAngle, degrees, true, this.mForeground);
		
		c.restore();
	}
}