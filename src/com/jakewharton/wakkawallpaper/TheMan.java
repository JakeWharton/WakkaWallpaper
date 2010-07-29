package com.jakewharton.wakkawallpaper;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.Log;

/**
 * TheMan class is the on screen representation of the player.
 * 
 * @author Jake Wharton
 */
public class TheMan extends Entity implements SharedPreferences.OnSharedPreferenceChangeListener {
	enum State { ALIVE, DEAD }
	enum Mode {
		A_STAR(0), NEAREST_DOT(1), RANDOM(2);
		
		public final int value;
		
		private Mode(final int value) {
			this.value = value;
		}
		
		public static TheMan.Mode parseInt(final int modeValue) {
			for (TheMan.Mode mode : TheMan.Mode.values()) {
				if (mode.value == modeValue) {
					return mode;
				}
			}
			throw new IllegalArgumentException("Unknown TheMan mode value: " + modeValue);
		}
	}
	
	private static final String TAG = "WakkaWallpaper.TheMan";
	private static final int CHOMP_ANGLE_COUNT = 4;
	private static final int DEATH_ANGLE_GROWTH = 30;
	private static final int[] CHOMP_ANGLES = new int[] { 90, 45, 0, 45 };
	
	private TheMan.State mState;
	private TheMan.Mode mMode;
	private int mStateTicker;
    private final Paint mForeground;
	private Direction mWantsToGo;
    
	/**
	 * Create instance of "The Man"
	 */
	public TheMan() {
		super();
        
        this.mForeground = new Paint(Paint.ANTI_ALIAS_FLAG);

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
		final Resources resources = Wallpaper.CONTEXT.getResources();
		
		final String foregroundColor = Wallpaper.CONTEXT.getString(R.string.settings_color_theman_key);
		if (all || key.equals(foregroundColor)) {
			this.mForeground.setColor(Wallpaper.PREFERENCES.getInt(foregroundColor, resources.getInteger(R.integer.color_theman_default)));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(TheMan.TAG, "Foreground Color: #" + Integer.toHexString(this.mForeground.getColor()));
			}
		}
		
		final String mode = Wallpaper.CONTEXT.getString(R.string.settings_game_themanmode_key);
		if (all || key.equals(mode)) {
			this.mMode = TheMan.Mode.parseInt(Wallpaper.PREFERENCES.getInt(mode, resources.getInteger(R.integer.game_themanmode_default)));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(TheMan.TAG, "Mode: " + this.mMode);
			}
		}
		
		final String color_style = Wallpaper.CONTEXT.getString(R.string.settings_color_entitystyle_key);
		if (all || key.equals(color_style)) {
			final Entity.Style style = Entity.Style.parseInt(Wallpaper.PREFERENCES.getInt(color_style, resources.getInteger(R.integer.color_entitystyle_default)));
			this.mForeground.setStyle(style.style);
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(TheMan.TAG, "Drawing Style: " + style);
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
	public void tick(Game game) {
		//Only tick if we are alive
		if (this.mState == TheMan.State.ALIVE) {
			super.tick(game);
		} else {
			//for death animation
			this.mStateTicker += 1;
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
	 * Change the state of "The Man".
	 * 
	 * @param state New state.
	 */
	public void setState(TheMan.State state) {
		this.mState = state;
		this.mStateTicker = 0;
	}
	
	/**
	 * Get the current state of The Man
	 * @return
	 */
	public TheMan.State getState() {
		return this.mState;
	}
	
	/**
	 * Determine our next direction based on the mode.
	 * 
	 * @param game Game instance.
	 */
	private void determineNextDirection(final Game game) {
		//Try the user direction first
		if ((this.mWantsToGo != null) && game.isValidPosition(Entity.move(this.mPosition, this.mWantsToGo))) {
			this.mDirectionNext = this.mWantsToGo;
			return;
		}
		
		//Use logic based on mode
		switch (this.mMode) {
			case A_STAR:
				this.determineNextDirectionByAI(game);
				break;
				
			case NEAREST_DOT:
				this.determineNextDirectionByNearestDot(game);
				break;
				
			case RANDOM:
				this.determineNextDirectionByRandom(game);
				break;
		}
		
		//If the wants-to-go direction exists and the AI forced us to change direction then wants-to-go direction
		//is impossible and should be cleared
		if ((this.mWantsToGo != null) && (this.mDirectionNext != this.mDirectionCurrent)) {
			this.mWantsToGo = null;
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(TheMan.TAG, "Clearing wants-to-go direction via AI.");
			}
		}
	}
	
	/**
	 * Determine our next direction based on a breadth-first search.
	 * 
	 * @param game Game instance.
	 */
	private void determineNextDirectionByNearestDot(final Game game) {
		//Breadth-first search for new next direction
		final Queue<Vector> queue = new LinkedList<Vector>();
		final HashSet<Integer> seen = new HashSet<Integer>();
		queue.add(new Vector(this.mPosition, this.mDirectionCurrent));
		Vector current;
		
		while (!queue.isEmpty()) {
			current = queue.remove();
			seen.add(game.hashPosition(current.position));
			
			if (Wallpaper.LOG_VERBOSE) {
				Log.v(TheMan.TAG, "With Current: (" + current.position.x + "," + current.position.y + ") " + current.direction);
			}
			
			for (Vector next : current.getPossibleMoves()) {
				if (Wallpaper.LOG_VERBOSE) {
					Log.v(TheMan.TAG, "- Checking: (" + next.position.x + "," + next.position.y + ") " + next.direction);
				}
				
				if (game.isValidPosition(next.position) && !seen.contains(game.hashPosition(next.position)) && !game.isGhostAtPosition(next.position)) {
					if (Wallpaper.LOG_VERBOSE) {
						Log.v(TheMan.TAG, "-- Valid");
					}
					
					if (game.getCell(next.position) != Game.Cell.BLANK) {
						if (Wallpaper.LOG_VERBOSE) {
							Log.v(TheMan.TAG, "-- Has Dot");
						}
						
						this.mDirectionNext = next.initialDirection;
						queue.clear(); //break out of while
						break; //break out of for
					} else {
						if (Wallpaper.LOG_VERBOSE) {
							Log.v(TheMan.TAG, "-- Empty, Queued");
						}
						
						queue.add(next);
					}
				}
			}
		}
		
		//Temporary last ditch effort: pick a random direction
		if (this.mDirectionNext == null) {
			while (true) {
				Direction direction = Direction.values()[Game.RANDOM.nextInt(Direction.values().length)];
				if (game.isValidPosition(Entity.move(this.mPosition, direction))) {
					this.mDirectionNext = direction;
					break;
				}
			}
		}
	}

	/**
	 * Determine next direction based on a simple random number generator.
	 * 
	 * @param game Game instance.
	 */
	private void determineNextDirectionByRandom(final Game game) {
		if (game.isIntersection(this.mPosition)) {
			while (true) {
				this.mDirectionNext = Entity.Direction.values()[Game.RANDOM.nextInt(Entity.Direction.values().length)];
				
				if (game.isValidPosition(Entity.move(this.mPosition, this.mDirectionNext)) && ((this.mDirectionCurrent == null) || (this.mDirectionNext != this.mDirectionCurrent.getOpposite()))) {
					break;
				}
			}
		} else {
			//Not at intersection, go straight
			this.mDirectionNext = this.mDirectionCurrent;
		}
	}
	
	/**
	 * Determine next direction based on advanced AI.
	 * 
	 * @param game Game instance.
	 */
	private void determineNextDirectionByAI(final Game game) {
		throw new IllegalArgumentException();
	}
    @Override
	public void draw(final Canvas c, final boolean isLandscape) {
		c.save();
		c.translate(this.mLocation.x - this.mCellWidthOverTwo, this.mLocation.y - this.mCellHeightOverTwo);
		
		float startingAngle = 270;
		int degrees = 360;
		if (this.mState == TheMan.State.ALIVE) {
			if (this.mDirectionCurrent != null) {
				final int angle = TheMan.CHOMP_ANGLES[this.mTickCount % TheMan.CHOMP_ANGLE_COUNT];
				startingAngle = this.mDirectionCurrent.getAngle(this.mDirectionNext) + (angle / 2.0f);
				degrees -= angle;
			}
		} else {
			if (isLandscape) {
				c.rotate(90, this.mCellWidthOverTwo, this.mCellHeightOverTwo);
			}
			final int delta = this.mStateTicker * TheMan.DEATH_ANGLE_GROWTH;
			startingAngle += delta / 2.0f;
			degrees -= delta;
		}
		
		if (degrees > 0) {
			c.drawArc(new RectF(0, 0, this.mCellWidth, this.mCellHeight), startingAngle, degrees, true, this.mForeground);
		}
		
		c.restore();
	}

	@Override
	protected void newLevel(final Game game) {
		this.newLife(game);
	}
	
	@Override
	public void newLife(final Game game) {
		//Get initial position
		this.setPosition(this.getInitialPosition(game));
		
		//Breath some life
		this.mState = TheMan.State.ALIVE;
		
		//Current direction is stopped
		this.mDirectionCurrent = null;
		
		//No influence on direction
		this.mWantsToGo = null;
		
		//Randomize next direction
		boolean valid = false;
		while (!valid) {
			this.mDirectionNext = Direction.values()[Game.RANDOM.nextInt(Direction.values().length)];
			valid = game.isValidPosition(Entity.move(this.mPosition, this.mDirectionNext));
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