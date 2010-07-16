package com.jakewharton.wakkawallpaper;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import com.jakewharton.wakkawallpaper.Game.Cell;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
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
    
	/**
	 * Create instance of "The Man"
	 */
	public TheMan() {
		super();
        
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
    public void setWantsToGo(final Direction direction) {
    	Log.d(TheMan.TAG, "Wants to go " + direction.toString());
    	this.mWantsToGo = direction;
    }

	@Override
    protected void moved(final Game game) {
		game.checkDots();
		game.checkFruit();
		game.checkGhosts();
		this.determineNextDirection(game);
    }
	
	/**
	 * Determine our next direction based on a breadth-first search.
	 * 
	 * @param game Game instance.
	 */
	private void determineNextDirection(final Game game) {
		//TODO: account for this.mWantsToGo
		
		//Promote next direction to current
		this.mDirection = this.mNextDirection;
		this.mNextDirection = null; //fallback to stopped
		
		//Breadth-first search for new next direction
		final Queue<Vector> queue = new LinkedList<Vector>();
		final HashSet<Integer> seen = new HashSet<Integer>();
		queue.add(new Vector(this.mPosition, this.mDirection));
		Vector current;
		
		while (!queue.isEmpty()) {
			current = queue.remove();
			seen.add(game.hashPosition(current.getPosition()));
			
			for (Vector next : current.getPossibleMoves()) {
				if (game.isValidPosition(next.getPosition()) && !seen.contains(game.hashPosition(next.getPosition())) && !game.isGhostAtPosition(next.getPosition())) {
					if (game.getCell(next.getPosition()) == Cell.DOT) {
						this.mNextDirection = next.getInitialDirection();
						queue.clear(); //exit while
						break; //exit for
					} else {
						queue.add(next);
					}
				}
			}
		}
	}

    @Override
	public void draw(final Canvas c) {
		c.save();
		c.translate(this.mLocation.x - this.mCellWidthOverTwo, this.mLocation.y - this.mCellHeightOverTwo);
		
		float startingAngle = 0;
		int degrees = 360;
		if (this.mDirection != null) {
			final int angle = TheMan.CHOMP_ANGLES[this.mTickCount % TheMan.CHOMP_ANGLE_COUNT];
			startingAngle = this.mDirection.getAngle(this.mNextDirection) + (angle / 2.0f);
			degrees -= angle;
		}
		
		c.drawArc(new RectF(0, 0, this.mCellWidth, this.mCellHeight), startingAngle, degrees, true, this.mForeground);
		
		c.restore();
	}

	@Override
	protected void newLevel(Game game) {
		//Position in the center-most region of the board.
		this.setPosition(new Point(game.getCellsWide() / 2, ((game.getIconRows() / 2) * (game.getCellRowSpacing() + 1))));
		
		this.mDirection = null;
		this.mNextDirection = null;
		this.determineNextDirection(game);
	}
}