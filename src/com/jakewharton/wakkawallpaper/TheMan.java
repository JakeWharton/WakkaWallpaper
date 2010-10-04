package com.jakewharton.wakkawallpaper;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
	/**
	 * Living state of The Man.
	 * 
	 * @author Jake Wharton
	 */
	enum State { ALIVE, DEAD }
	
	/**
	 * Current character representing The Man.
	 * 
	 * @author Jake Wharton
	 */
	enum Character { THEMAN, /*MRS_THEMAN,*/ ANDY, GOOGOL, THEMANDROID }
	
	/**
	 * Current AI mode.
	 * 
	 * @author Jake Wharton
	 */
	enum Mode {
		/*AI(0),*/ NEAREST_DOT(1), RANDOM(2);
		
		/**
		 * Persisted unique value.
		 */
		public final int value;
		
		
		
		private Mode(final int value) {
			this.value = value;
		}
		
		
		
		/**
		 * Parse an integer that corresponds to a mode.
		 * 
		 * @param modeValue Value to parse.
		 * @return Mode.
		 */
		public static TheMan.Mode parseInt(final int modeValue) {
			for (TheMan.Mode mode : TheMan.Mode.values()) {
				if (mode.value == modeValue) {
					return mode;
				}
			}
			throw new IllegalArgumentException("Unknown TheMan mode value: " + modeValue);
		}
	}
	
	
	
	/**
	 * Tag used for logging purposes.
	 */
	private static final String TAG = "WakkaWallpaper.TheMan";
	
	/**
	 * Angles iterated over when moving to create the chomping action.
	 */
	private static final int[] CHOMP_ANGLES = new int[] { 90, 45, 0, 45 };
	
	/**
	 * Angle by which the death animation grows per frame.
	 */
	private static final int DEATH_ANGLE_GROWTH = 30;
	
	/**
	 * The maximum timespan to follow a user's directional guidance.
	 */
	private static final int WANTS_TO_GO_MAX_LENGTH = 5000;
	
	/**
	 * The foreground color used when we are "The Mandroid" character.
	 */
	/*package*/static final int THE_MANDROID_FOREGROUND = 0xffa4c639;
	
	
	
	/**
	 * Current living state.
	 */
	private TheMan.State mState;
	
	/**
	 * Current AI mode.
	 */
	private TheMan.Mode mMode;
	
	/**
	 * Current playing character.
	 */
	private TheMan.Character mCharacter;
	
	/**
	 * Timer used for holding states in animation.
	 */
	private int mStateTicker;
	
	/**
	 * The foreground color used to paint.
	 */
    private final Paint mForeground;
    
    /**
     * The next direction the user wants us to take. 
     */
	private Entity.Direction mWantsToGo;
	
	/**
	 * Timer for the wants to go value to timeout.
	 */
	private long mWantsToGoTimer;
	
	/**
	 * Whether or not the Andy trophy is enabled.
	 */
	private boolean mIsTrophyAndyEnabled;
	
	/**
	 * Whether or not the Googol trophy is enabled.
	 */
	private boolean mIsTrophyGoogolEnabled;
	
	/**
	 * Whether or not The Mandroid trophy is enabled.
	 */
	private boolean mIsTrophyTheMandroidEnabled;
	
	/**
	 * Whether or not the Ego trophy is enabled.
	 */
    private boolean mIsTrophyEgoEnabled;
    
    /**
     * The image of sprites for the current character (if any).1
     */
	private Bitmap mSprite;
    
	
	
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
			this.mForeground.setColor(preferences.getInt(foregroundColor, resources.getInteger(R.integer.color_theman_default)));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(TheMan.TAG, "Foreground Color: #" + Integer.toHexString(this.mForeground.getColor()));
			}
		}
		
		final String mode = Wallpaper.CONTEXT.getString(R.string.settings_game_themanmode_key);
		if (all || key.equals(mode)) {
			this.mMode = TheMan.Mode.parseInt(preferences.getInt(mode, resources.getInteger(R.integer.game_themanmode_default)));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(TheMan.TAG, "Mode: " + this.mMode);
			}
		}
		
		final String color_style = Wallpaper.CONTEXT.getString(R.string.settings_color_entitystyle_key);
		if (all || key.equals(color_style)) {
			final Entity.Style style = Entity.Style.parseInt(preferences.getInt(color_style, resources.getInteger(R.integer.color_entitystyle_default)));
			this.mForeground.setStyle(style.style);
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(TheMan.TAG, "Drawing Style: " + style);
			}
		}
		
		final String wrapping = resources.getString(R.string.settings_game_wrappingtheman_key);
		if (all || key.equals(wrapping)) {
			this.mIsWrapping = preferences.getBoolean(wrapping, resources.getBoolean(R.bool.game_wrappingtheman_default));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(TheMan.TAG, "Is Wrapping: " + this.mIsWrapping);
			}
		}
		
		final String trophyAndy = resources.getString(R.string.trophy_andy_key);
		if (all || key.equals(trophyAndy)) {
			this.mIsTrophyAndyEnabled = preferences.getBoolean(trophyAndy, resources.getBoolean(R.bool.trophy_andy_default));
			
			if (this.mIsTrophyAndyEnabled) {
				this.mCharacter = TheMan.Character.ANDY;

				//Load the Andy sprites
				final BitmapFactory.Options options = new BitmapFactory.Options();
				options.inScaled = false;
				this.mSprite = BitmapFactory.decodeResource(Wallpaper.CONTEXT.getResources(), R.drawable.andy, options);
			} else if (!this.mIsTrophyGoogolEnabled && !this.mIsTrophyTheMandroidEnabled) {
				//TODO: parseValue of character when the Mrs. is implemented
				this.mCharacter = TheMan.Character.THEMAN;
				this.mForeground.setColor(preferences.getInt(resources.getString(R.string.settings_color_theman_key), resources.getInteger(R.integer.color_theman_default)));
				
				this.mSprite = null;
			}
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(TheMan.TAG, "Is Trophy Andy Enabled: " + this.mIsTrophyAndyEnabled);
			}
		}
		
		final String trophyGoogol = resources.getString(R.string.trophy_googol_key);
		if (all || key.equals(trophyGoogol)) {
			this.mIsTrophyGoogolEnabled = preferences.getBoolean(trophyGoogol, resources.getBoolean(R.bool.trophy_googol_default));
			
			if (this.mIsTrophyGoogolEnabled) {
				this.mCharacter = TheMan.Character.GOOGOL;

				//Load the Andy sprites
				final BitmapFactory.Options options = new BitmapFactory.Options();
				options.inScaled = false;
				this.mSprite = BitmapFactory.decodeResource(Wallpaper.CONTEXT.getResources(), R.drawable.googol_theman, options);
			} else if (!this.mIsTrophyAndyEnabled && !this.mIsTrophyTheMandroidEnabled) {
				//TODO: parseValue of character when the Mrs. is implemented
				this.mCharacter = TheMan.Character.THEMAN;
				this.mForeground.setColor(preferences.getInt(resources.getString(R.string.settings_color_theman_key), resources.getInteger(R.integer.color_theman_default)));
				
				this.mSprite = null;
			}
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(TheMan.TAG, "Is Trophy Andy Enabled: " + this.mIsTrophyAndyEnabled);
			}
		}
		
		final String trophyTheMandroid = resources.getString(R.string.trophy_themandroid_key);
		if (all || key.equals(trophyTheMandroid)) {
			this.mIsTrophyTheMandroidEnabled = preferences.getBoolean(trophyTheMandroid, resources.getBoolean(R.bool.trophy_themandroid_default));
			
			if (this.mIsTrophyTheMandroidEnabled) {
				this.mCharacter = TheMan.Character.THEMANDROID;
				this.mForeground.setColor(TheMan.THE_MANDROID_FOREGROUND);
			} else if (!this.mIsTrophyAndyEnabled && !this.mIsTrophyGoogolEnabled) {
				//TODO: parseValue of character when the Mrs. is implemented
				this.mCharacter = TheMan.Character.THEMAN;
				this.mForeground.setColor(preferences.getInt(resources.getString(R.string.settings_color_theman_key), resources.getInteger(R.integer.color_theman_default)));
				
				this.mSprite = null;
			}
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(TheMan.TAG, "Is Trophy The Mandroid Enabled: " + this.mIsTrophyTheMandroidEnabled);
			}
		}
		
		final String trophyEgo = resources.getString(R.string.trophy_ego_key);
		if (all || key.equals(trophyEgo)) {
			this.mIsTrophyEgoEnabled = preferences.getBoolean(trophyEgo, resources.getBoolean(R.bool.trophy_ego_default));
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
    public void setWantsToGo(final Entity.Direction direction) {
    	this.mWantsToGo = direction;
    	this.mWantsToGoTimer = System.currentTimeMillis();
    	
    	if (Wallpaper.LOG_DEBUG) {
    		Log.d(TheMan.TAG, "Wants To Go: " + direction.toString());
    	}
    }

	@Override
	public void tick(final Game game) {
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
	public void setState(final TheMan.State state) {
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
	 * Get the current character of The Man.
	 * 
	 * @return TheMan.Character
	 */
	public TheMan.Character getCharacter() {
		return this.mCharacter;
	}
	
	/**
	 * Determine our next direction based on the mode.
	 * 
	 * @param game Game instance.
	 */
	private void determineNextDirection(final Game game) {
		//Try the user direction first
		if ((this.mWantsToGo != null) && game.isValidPosition(this, Entity.move(this.mPosition, this.mWantsToGo))) {
			if (this.mIsWrapping && ((System.currentTimeMillis() - this.mWantsToGoTimer) > TheMan.WANTS_TO_GO_MAX_LENGTH)) {
				//When wrapping, only allow The Man to follow the user direction for a maximum amount of time
				this.mWantsToGo = null;
			} else {
				//Follow user direction and GTFO
				this.mDirectionNext = this.mWantsToGo;
				return;
			}
		}
		
		//Use logic based on mode
		switch (this.mMode) {
			/*case AI:
				this.determineNextDirectionByAI(game);
				break;*/
				
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
			
			for (final Vector next : current.getPossibleMoves()) {
				if (Wallpaper.LOG_VERBOSE) {
					Log.v(TheMan.TAG, "- Checking: (" + next.position.x + "," + next.position.y + ") " + next.direction);
				}
				
				if (game.isValidPosition(this, next.position) && !seen.contains(game.hashPosition(next.position))) {
					if (Wallpaper.LOG_VERBOSE) {
						Log.v(TheMan.TAG, "-- Valid");
					}
					
					final Ghost ghostA = game.getGhostAtPosition(next.position);
					final Ghost ghostB = game.getGhostAtPosition(Entity.move(next.position, next.direction));
					if (((ghostA != null) && (ghostA.getState() == Ghost.State.HUNTING)) || ((ghostB != null) && (ghostB.getState() == Ghost.State.HUNTING))) {
						//If there's a hunting ghost in the next position or the one after, immediately disgard
						continue;
					}
					
					if ((game.getCell(next.position) != Game.Cell.BLANK) || (game.getFruitAtPosition(next.position) != null)) {
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
				final Entity.Direction direction = Entity.Direction.values()[Game.RANDOM.nextInt(Entity.Direction.values().length)];
				if (game.isValidPosition(this, Entity.move(this.mPosition, direction))) {
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
				
				if (game.isValidPosition(this, Entity.move(this.mPosition, this.mDirectionNext)) && ((this.mDirectionCurrent == null) || (this.mDirectionNext != this.mDirectionCurrent.getOpposite()))) {
					break;
				}
			}
		} else {
			//Not at intersection, go straight
			this.mDirectionNext = this.mDirectionCurrent;
		}
	}
	
	/*private void determineNextDirectionByAI(final Game game) {
		final HashSet<Integer> seen = new HashSet<Integer>();
		final Queue<Vector> queue = new LinkedList<Vector>();
		queue.add(new Vector(this.mPosition, this.mDirectionCurrent));
		final Map<Entity.Direction, Integer> weights = new HashMap<Entity.Direction, Integer>();
		Vector current;
		
		//Initialize direction scores
		weights.put(Entity.Direction.NORTH, 0);
		weights.put(Entity.Direction.SOUTH, 0);
		weights.put(Entity.Direction.EAST, 0);
		weights.put(Entity.Direction.WEST, 0);
		
		//BFS to a max depth of 10
		while (!queue.isEmpty()) {
			current = queue.remove();
			seen.add(game.hashPosition(current.position));
			
			if (Wallpaper.LOG_VERBOSE) {
				Log.v(TheMan.TAG, "With Current: (" + current.position.x + "," + current.position.y + ") " + current.direction);
			}
			
			//Do not go past 10 steps into the future.
			if (current.step == 10) {
				continue;
			}
			
			for (final Vector next : current.getPossibleMoves()) {
				if (Wallpaper.LOG_VERBOSE) {
					Log.v(TheMan.TAG, "- Checking: (" + next.position.x + "," + next.position.y + ") " + next.direction);
				}
				
				if (game.isValidPosition(next.position) && !seen.contains(game.hashPosition(next.position))) {
					if (Wallpaper.LOG_VERBOSE) {
						Log.v(TheMan.TAG, "-- Valid");
					}
					
					int weight = weights.get(next.initialDirection);
					
					final Game.Cell cell = game.getCell(next.position);
					if (cell != Game.Cell.BLANK) {
						weight += cell.value;
					}
					
					final Ghost ghost = game.getGhostAtPosition(next.position); 
					if (ghost != null) {
						if (ghost.getState() == Ghost.State.HUNTING) {
							weight -= 500; //Fear ghosts
						} else if (ghost.getState() == Ghost.State.FRIGHTENED) {
							weight += 200;
						}
					}
					
					final Fruit fruit = game.getFruitAtPosition(next.position);
					if (fruit != null) {
						weight += fruit.getValue();
					}
					
					weights.put(next.initialDirection, weight);
					queue.add(next);
				}
			}
		}
		
		this.mDirectionNext = null;
		while (this.mDirectionNext == null) {
			int best = Integer.MIN_VALUE;
			Entity.Direction direction = null;
			for (Map.Entry<Entity.Direction, Integer> entry : weights.entrySet()) {
				if (entry.getValue() > best) {
					direction = entry.getKey();
				}
			}
			
			if (game.isValidPosition(Entity.move(this.mPosition, direction))) {
				this.mDirectionNext = direction;
			} else {
				weights.remove(direction);
			}
		}
	}*/
	
    @Override
	public void draw(final Game game, final Canvas c) {
		c.save();
		
		if (this.mIsTrophyEgoEnabled) {
			c.translate(this.mLocation.x - this.mCellWidth, this.mLocation.y - this.mCellHeight);
			c.scale(2, 2);
		} else {
			c.translate(this.mLocation.x - this.mCellWidthOverTwo, this.mLocation.y - this.mCellHeightOverTwo);
		}
		
		switch (this.mCharacter) {
			case THEMAN:
			case THEMANDROID:
				this.drawTheMan(game, c);
				break;
				
			/*case MRS_THEMAN:
				this.drawMrsTheMan(c, isLandscape);
				break;*/
				
			case ANDY:
			case GOOGOL:
		        c.setDrawFilter(Game.FILTER_SET);
				this.drawStaticSprite(game, c);
		    	c.setDrawFilter(Game.FILTER_REMOVE);
				break;
		}
		
		c.restore();
	}
    
    /**
     * Draw The Man.
     * 
     * @param c Canvas to draw on.
     * @param isLandscape Whether or not the display is in landscape mode.
     */
    private void drawTheMan(final Game game, final Canvas c) {
		float startingAngle = 270;
		int degrees = 360;
		if (this.mState == TheMan.State.ALIVE) {
			if (this.mDirectionCurrent != null) {
				final int angle = TheMan.CHOMP_ANGLES[this.mTickCount % TheMan.CHOMP_ANGLES.length];
				startingAngle = this.mDirectionCurrent.getAngle(this.mDirectionNext) + (angle / 2.0f);
				degrees -= angle;
			}
		} else {
			final int delta = this.mStateTicker * TheMan.DEATH_ANGLE_GROWTH;
			startingAngle += delta / 2.0f;
			degrees -= delta;
		}
		
		if (degrees > 0) {
			c.drawArc(new RectF(0, 0, this.mCellWidth, this.mCellHeight), startingAngle, degrees, true, this.mForeground);
		}
		
		if ((this.mState == TheMan.State.ALIVE) && (this.mCharacter == TheMan.Character.THEMANDROID)) {
			//TODO: draw eye
			//TODO: draw antenna
		}
    }

    /*private void drawMrsTheMan(final Canvas c, final boolean isLandscape) {
    	//TODO: draw Mrs. The Man
    }*/
    
    /**
     * Draw sprite.
     * 
     * @param c Canvas to draw on.
     * @param isLandscape Whether or not the display is in landscape mode.
     */
    private void drawStaticSprite(final Game game, final Canvas c) {
    	c.drawBitmap(this.mSprite, null, game.getCellSize(), Entity.SPRITE_PAINT);
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
			this.mDirectionNext = Entity.Direction.values()[Game.RANDOM.nextInt(Entity.Direction.values().length)];
			valid = game.isValidPosition(this, Entity.move(this.mPosition, this.mDirectionNext));
		}
	}
	
	/**
	 * Get the position in the center-most region of the board.
	 * 
	 * @param game Game instance.
	 * @return Point
	 */
	public Point getInitialPosition(final Game game) {
		final Point center = new Point(game.getCellsWide() / 2, game.getCellsTall() / 2);
		Point position = new Point(center);
		
		int i = Entity.Direction.values().length; //This allows the moving distance to start a 1 and not 0
		while (!game.isValidBoardPosition(position)) {
			position = Entity.move(center, Entity.Direction.values()[i % Entity.Direction.values().length], i / Entity.Direction.values().length);
			i += 1;
		}
		
		return position;
	}
}
