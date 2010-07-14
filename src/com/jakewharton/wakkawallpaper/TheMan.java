package com.jakewharton.wakkawallpaper;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import com.jakewharton.wakkawallpaper.Game.Cell;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.util.Log;

/**
 * TheMan class is the on screen representation of the player.
 * 
 * @author Jake Wharton
 */
public class TheMan extends Entity {
	private static final String TAG = "WakkaWallpaper.TheMan";
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

    /**
     * Specify a direction you would like "The Man" to travel in next (if possible).
     * 
     * @param direction Desired direction.
     */
    public void setWantsToGo(Direction direction) {
    	Log.d(TheMan.TAG, "Wants to go " + direction.toString());
    	this.mWantsToGo = direction;
    }

    /**
     * Iterate the entity one step.
     * 
     * @param game Game instance
     */
    @Override
	public void tick(Game game) {
		super.tick(game);
		
		if (this.mMovedThisTick || (this.mWantsToGo != null)) {
			//Promote next direction to current
			this.mDirection = this.mNextDirection;
			this.mNextDirection = Direction.STOPPED; //fallback
			
			//Breadth-first search for new next direction
			Queue<Entity.Position> queue = new LinkedList<Entity.Position>();
			HashSet<Integer> seen = new HashSet<Integer>();
			queue.add(new Entity.Position(this.mPosition, this.mDirection));
			Entity.Position current;
			
			//Log.d(TheMan.TAG, "--------------");
			//Log.d(TheMan.TAG, "Current position: (" + this.mPosition.x + "," + this.mPosition.y + ")");
			
			while (!queue.isEmpty()) {
				current = queue.remove();
				seen.add(game.hashPosition(current.getPosition()));
				
				for (Entity.Position next : current.getPossibleMoves()) {
					//Log.d(TheMan.TAG, "Checking " + next.getDirection() + " of (" + current.getPositionX() + "," + current.getPositionY() + ") to (" + next.getPositionX() + "," + next.getPositionY() +")");
					if (game.isValidPosition(next.getPosition()) && !seen.contains(game.hashPosition(next.getPosition()))) {
						if (game.getCell(next.getPositionX(), next.getPositionY()) == Cell.DOT) {
							//Log.d(TheMan.TAG, "Going " + next.getInitialDirection() + " toward (" + next.getPositionX() + "," + next.getPositionY() + ")");
							this.mNextDirection = next.getInitialDirection();
							queue.clear(); //exit while
							break; //exit for
						} else {
							//Log.d(TheMan.TAG, "Pushing " + next.getDirection() + " from (" + next.getPositionX() + "," + next.getPositionY() + ")");
							queue.add(next);
						}
					}
				}
			}
		}
	}

    /**
     * Render the entity on the Canvas.
     * 
     * @param c Canvas to draw on.
     */
    @Override
	public void draw(Canvas c) {
		c.save();
		c.translate(this.getLocationX(), this.getLocationY());
		
		//keep us low
		this.mTickCount %= TheMan.CHOMP_ANGLE_COUNT;
		
		int angle = TheMan.CHOMP_ANGLES[this.mTickCount];
		float startingAngle = this.mDirection.getAngle(this.mNextDirection) + (angle / 2.0f);
		int degrees = 360;
		if (this.mDirection != Direction.STOPPED) {
			degrees -= angle;
		}
		
		c.drawArc(new RectF(0, 0, this.mCellWidth, this.mCellHeight), startingAngle, degrees, true, this.mForeground);
		
		c.restore();
	}
}