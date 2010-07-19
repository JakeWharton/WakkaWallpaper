package com.jakewharton.wakkawallpaper;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import com.jakewharton.wakkawallpaper.Game.Cell;

import android.content.SharedPreferences;
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
public class TheMan extends Entity implements SharedPreferences.OnSharedPreferenceChangeListener {
	private static final String TAG = "WakkaWallpaper.TheMan";
	private static final int CHOMP_ANGLE_COUNT = 4;
	private static final int[] CHOMP_ANGLES = new int[] { 90, 45, 0, 45 };

	private static final int DEFAULT_FOREGROUND_COLOR = 0xfffff000;
	
    private final Paint mForeground;
	private Direction mWantsToGo;
    
	/**
	 * Create instance of "The Man"
	 */
	public TheMan() {
		super();
        
        this.mForeground = new Paint();
        this.mForeground.setAntiAlias(true);
        this.mForeground.setStyle(Style.FILL_AND_STROKE);

        //Load all preferences or their defaults
        Wallpaper.PREFERENCES.registerOnSharedPreferenceChangeListener(this);
        this.onSharedPreferenceChanged(Wallpaper.PREFERENCES, null);
    	
    	this.mWantsToGo = null;
	}

    /**
     * Handle the changing of a preference.
     */
	public void onSharedPreferenceChanged(final SharedPreferences preferences, final String key) {
		if (Wallpaper.LOG_VERBOSE) {
			Log.v(TheMan.TAG, "> onSharedPreferenceChanged()");
		}
		
		final boolean all = (key == null);
		
		final String foregroundColor = Wallpaper.CONTEXT.getString(R.string.settings_color_theman_key);
		if (all || key.equals(foregroundColor)) {
			this.mForeground.setColor(Wallpaper.PREFERENCES.getInt(foregroundColor, TheMan.DEFAULT_FOREGROUND_COLOR));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(TheMan.TAG, "Foreground Color: " + Integer.toHexString(this.mForeground.getColor()));
			}
		}

		if (Wallpaper.LOG_VERBOSE) {
			Log.v(TheMan.TAG, "< onSharedPreferenceChanged()");
		}
	}

    /**
     * Specify a direction you would like "The Man" to travel in next (if possible).
     * 
     * @param direction Desired direction.
     */
    public void setWantsToGo(final Direction direction) {
    	this.mWantsToGo = direction;
    	
    	if (Wallpaper.LOG_DEBUG) {
    		Log.d(TheMan.TAG, "Wants To Go: " + direction.toString());
    	}
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
		
		//Breadth-first search for new next direction
		final Queue<Vector> queue = new LinkedList<Vector>();
		final HashSet<Integer> seen = new HashSet<Integer>();
		queue.add(new Vector(this.mPosition, this.mCurrentDirection));
		Vector current;
		
		while (!queue.isEmpty()) {
			current = queue.remove();
			seen.add(game.hashPosition(current.getPosition()));
			
			for (Vector next : current.getPossibleMoves()) {
				if (game.isValidPosition(next.getPosition()) && !seen.contains(game.hashPosition(next.getPosition())) && !game.isGhostAtPosition(next.getPosition())) {
					if (game.getCell(next.getPosition()) == Cell.DOT) {
						this.mNextDirection = next.getInitialDirection();
						return;
					} else {
						queue.add(next);
					}
				}
			}
		}
		
		if (Wallpaper.LOG_DEBUG) {
			Log.w(TheMan.TAG, "WARNING: No path found.");
		}
	}

    @Override
	public void draw(final Canvas c) {
		c.save();
		c.translate(this.mLocation.x - this.mCellWidthOverTwo, this.mLocation.y - this.mCellHeightOverTwo);
		
		float startingAngle = 0;
		int degrees = 360;
		if (this.mCurrentDirection != null) {
			final int angle = TheMan.CHOMP_ANGLES[this.mTickCount % TheMan.CHOMP_ANGLE_COUNT];
			startingAngle = this.mCurrentDirection.getAngle(this.mNextDirection) + (angle / 2.0f);
			degrees -= angle;
		}
		
		c.drawArc(new RectF(0, 0, this.mCellWidth, this.mCellHeight), startingAngle, degrees, true, this.mForeground);
		
		c.restore();
	}

	@Override
	protected void newLevel(final Game game) {
		if (Wallpaper.LOG_VERBOSE) {
			Log.v(TheMan.TAG, "> newLevel()");
		}
		
		//Get initial position
		this.setPosition(this.getInitialPosition(game));
		
		//Randomize the initial direction
		this.mCurrentDirection = Direction.values()[Game.RANDOM.nextInt(Direction.values().length)];
		//Starting using path finding logic
		this.determineNextDirection(game);
		
		if (Wallpaper.LOG_VERBOSE) {
			Log.v(TheMan.TAG, "< newLevel()");
		}
	}
	
	/**
	 * Get the position in the center-most region of the board.
	 * 
	 * @param game Game instance.
	 * @return Point
	 */
	public Point getInitialPosition(final Game game) {
		return new Point(game.getCellsWide() / 2, ((game.getIconRows() / 2) * (game.getCellRowSpacing() + 1)));
	}
}