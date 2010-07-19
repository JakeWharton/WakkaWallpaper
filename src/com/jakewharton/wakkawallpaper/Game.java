package com.jakewharton.wakkawallpaper;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Random;

import com.jakewharton.wakkawallpaper.Entity.Direction;
import com.jakewharton.wakkawallpaper.Ghost.State;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.Log;

/**
 * The Game class manages the playing board and all of the entities contained within.
 * 
 * @author Jake Wharton
 */
public class Game implements SharedPreferences.OnSharedPreferenceChangeListener {
	enum Cell { BLANK, WALL, DOT, JUGGERDOT }

	public static final Random RANDOM = new Random();
	private static final String TAG = "WakkaWallpaper.Game";
	private static final NumberFormat SCORE_FORMAT = new DecimalFormat("000000");
	private static final int POINTS_DOT = 10;
	private static final int POINTS_JUGGERDOT = 50;
	private static final int[] POINTS_FLEEING_GHOSTS = new int[] { 200, 400, 800, 1600 };
	private static final int POINTS_ALL_FLEEING_GHOSTS = 12000;
	private static final float HUD_TEXT_SIZE = 20;
	private static final int NUMBER_OF_JUGGERDOTS = 4;
	private static final int KILL_SCREEN_LEVEL = 256;

	private static final boolean DEFAULT_BONUS_ALLOWED = true;
	private static final int DEFAULT_BONUS_THRESHOLD = 10000;
	private static final int DEFAULT_DOT_FOREGROUND = 0xff6161a1;
	private static final int DEFAULT_GAME_BACKGROUND = 0xff000040;
    private static final int DEFAULT_HUD_FOREGROUND = 0xff8181c1;
    private static final int DEFAULT_HUD_BACKGROUND = 0xff000000;
    private static final int DEFAULT_GRID_PADDING_LEFT = -5;
    private static final int DEFAULT_GRID_PADDING_RIGHT = -5;
    private static final int DEFAULT_GRID_PADDING_TOP = 35;
    private static final int DEFAULT_GRID_PADDING_BOTTOM = 75;
    private static final boolean DEFAULT_DISPLAY_HUD = true;
    private static final boolean DEFAULT_KILL_SCREEN_ENABLED = true;
    private static final int DEFAULT_ICON_ROWS = 4;
    private static final int DEFAULT_ICON_COLS = 4;
    private static final int DEFAULT_CELL_SPACING_ROWS = 6;
    private static final int DEFAULT_CELL_SPACING_COLUMNS = 4;
    private static final int DEFAULT_GHOST_COUNT = 4;
    private static final boolean DEFAULT_GHOST_DEADLY = true;
	
	private int mCellsWide;
	private int mCellsTall;
	private int mCellColumnSpacing;
	private int mCellRowSpacing;
	private float mCellWidth;
	private float mCellHeight;
    private int mScreenHeight;
    private int mScreenWidth;
    private boolean mIsLandscape;
    private int mIconRows;
    private int mIconCols;
    private boolean mIsOnKillScreen;
    private boolean mIsKillScreenEnabled;
	private Cell[][] mBoard;
	private Entity[] mEntities;
	private TheMan mTheMan;
	private Fruit mFruit;
	private Ghost[] mGhosts;
	private int mGhostCount;
	private boolean mIsGhostDeadly;
	private int mFleeingGhostsEaten;
	private int mAllFleeingGhostsEaten;
	private int mDotsRemaining;
	private int mDotsEaten;
	private int mLives;
	private int mScore;
	private int mLevel;
    private boolean mIsBonusLifeAllowed;
    private boolean mIsBonusLifeGiven;
    private final Paint mDotForeground;
    private int mGameBackground;
    private int mBonusLifeThreshold;
    private boolean mIsDisplayingHud;
    private final Paint mHudForeground;
    private float mDotGridPaddingTop;
    private float mDotGridPaddingLeft;
    private float mDotGridPaddingBottom;
    private float mDotGridPaddingRight;
    
    /**
     * Create a new game adhering to the specified parameters.
     * 
     * @param iconRows Number of rows of icons on the launcher.
     * @param iconCols Number of columns of icons on the launcher.
     * @param screenWidth Width in pixels of the screen.
     * @param screenHeight Height in pixels of the screen.
     */
    public Game() {
    	if (Wallpaper.LOG_VERBOSE) {
    		Log.v(Game.TAG, "> Game()");
    	}
    	
        //Create Paints
        this.mDotForeground = new Paint(); 
        this.mDotForeground.setAntiAlias(true);
        this.mHudForeground = new Paint();
        this.mHudForeground.setAntiAlias(true);
        this.mHudForeground.setTextSize(Game.HUD_TEXT_SIZE);
        
        //Create "The Man" and fruit
    	this.mTheMan = new TheMan();
    	this.mFruit = new Fruit();
        
        //Load all preferences or their defaults
        Wallpaper.PREFERENCES.registerOnSharedPreferenceChangeListener(this);
        this.onSharedPreferenceChanged(Wallpaper.PREFERENCES, null);

    	if (Wallpaper.LOG_VERBOSE) {
    		Log.v(Game.TAG, "< Game()");
    	}
    }
    
	public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
    	if (Wallpaper.LOG_VERBOSE) {
    		Log.v(Game.TAG, "> onSharedPreferenceChanged()");
    	}
    	
		final boolean all = (key == null);

		boolean hasBonusChanged = false;
        boolean hasGhostCountChanged = false;
		boolean hasLayoutChanged = false;

		
		// GENERAL //
		
		final String bonusAllowed = Wallpaper.CONTEXT.getString(R.string.settings_game_bonuslife_key);
		if (all || key.equals(bonusAllowed)) {
			this.mIsBonusLifeAllowed = Wallpaper.PREFERENCES.getBoolean(bonusAllowed, Game.DEFAULT_BONUS_ALLOWED);
			hasBonusChanged = true;
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Bonus Allowed: " + this.mIsBonusLifeAllowed);
			}
		}
        
		final String bonusThreshold = Wallpaper.CONTEXT.getString(R.string.settings_game_bonuslifethreshold_key);
		if (all || key.equals(bonusThreshold)) {
			this.mBonusLifeThreshold = Wallpaper.PREFERENCES.getInt(key, Game.DEFAULT_BONUS_THRESHOLD);
			hasBonusChanged = true;
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Bonus Threshold: " + this.mBonusLifeThreshold);
			}
		}
        
        final String killScreen = Wallpaper.CONTEXT.getString(R.string.settings_game_killscreen_key);
        if (all || key.equals(killScreen)) {
        	this.mIsKillScreenEnabled = Wallpaper.PREFERENCES.getBoolean(killScreen, Game.DEFAULT_KILL_SCREEN_ENABLED);
        	
        	if (Wallpaper.LOG_DEBUG) {
        		Log.d(Game.TAG, "Is Kill Screen Enabled: " + this.mIsKillScreenEnabled);
        	}
        }
        
        final String ghostsDeadly = Wallpaper.CONTEXT.getString(R.string.settings_game_deadlyghosts_key);
        if (all || key.equals(ghostsDeadly)) {
        	this.mIsGhostDeadly = Wallpaper.PREFERENCES.getBoolean(ghostsDeadly, Game.DEFAULT_GHOST_DEADLY);
        	
        	if (Wallpaper.LOG_DEBUG) {
        		Log.d(Game.TAG, "Is Ghost Deadly: " + this.mIsGhostDeadly);
        	}
        }
        
        final String ghostCount = Wallpaper.CONTEXT.getString(R.string.settings_game_ghostcount_key);
        if (all || key.equals(ghostCount)) {
        	this.mGhostCount = Wallpaper.PREFERENCES.getInt(ghostCount, Game.DEFAULT_GHOST_COUNT);
        	hasGhostCountChanged = true;
        	
        	if (Wallpaper.LOG_DEBUG) {
        		Log.d(Game.TAG, "Ghost Count: " + this.mGhostCount);
        	}
        }

    	if (hasGhostCountChanged) {
	    	this.mGhosts = new Ghost[this.mGhostCount];
	    	int i = 0;
	    	if (this.mGhostCount > i) { this.mGhosts[i++] = new Ghost.Blinky(); }
	    	if (this.mGhostCount > i) { this.mGhosts[i++] = new Ghost.Pinky(); }
	    	if (this.mGhostCount > i) { this.mGhosts[i++] = new Ghost.Inky(); }
	    	if (this.mGhostCount > i) { this.mGhosts[i++] = new Ghost.Clyde(); }
	    	
	    	i = 0;
	    	this.mEntities = new Entity[2 + this.mGhostCount]; //The Man, Fruit, and Ghosts
	    	this.mEntities[i++] = this.mTheMan;
	    	this.mEntities[i++] = this.mFruit;
	    	for (Ghost ghost : this.mGhosts) {
	    		this.mEntities[i++] = ghost;
	    	}
    	}
		
		final String displayHud = Wallpaper.CONTEXT.getString(R.string.settings_display_showhud_key);
		if (all || key.equals(displayHud)) {
			this.mIsDisplayingHud = Wallpaper.PREFERENCES.getBoolean(key, Game.DEFAULT_DISPLAY_HUD);
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Is Displaying HUD: " + this.mIsDisplayingHud);
			}
		}
		
		
		// COLORS //
        
		final String gameBackground = Wallpaper.CONTEXT.getString(R.string.settings_color_game_background_key);
		if (all || key.equals(gameBackground)) {
			this.mGameBackground = Wallpaper.PREFERENCES.getInt(gameBackground, Game.DEFAULT_GAME_BACKGROUND);
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Game Background: " + Integer.toHexString(this.mGameBackground));
			}
		}
        
		final String dot = Wallpaper.CONTEXT.getString(R.string.settings_color_game_dot_key);
		if (all || key.equals(dot)) {
			this.mDotForeground.setColor(Wallpaper.PREFERENCES.getInt(dot, Game.DEFAULT_DOT_FOREGROUND));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Dot Foreground: " + Integer.toHexString(this.mDotForeground.getColor()));
			}
		}
		
		final String hudFg = Wallpaper.CONTEXT.getString(R.string.settings_color_game_hudfg_key);
		if (all || key.equals(hudFg)) {
			this.mHudForeground.setColor(Wallpaper.PREFERENCES.getInt(key, Game.DEFAULT_HUD_FOREGROUND));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "HUD Foreground: " + Integer.toHexString(this.mHudForeground.getColor()));
			}
		}
        
		final String hudBg = Wallpaper.CONTEXT.getString(R.string.settings_color_game_hudbg_key);
		if (all || key.equals(hudBg)) {
			final int hudBgColor = Wallpaper.PREFERENCES.getInt(key, Game.DEFAULT_HUD_BACKGROUND);
			this.mHudForeground.setShadowLayer(1, -1, 1, hudBgColor);
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "HUD Background: " + Integer.toHexString(hudBgColor));
			}
		}
    	
        
		// GRID //
		
		final String dotGridPaddingLeft = Wallpaper.CONTEXT.getString(R.string.settings_display_padding_left_key);
		if (all || key.equals(dotGridPaddingLeft)) {
			this.mDotGridPaddingLeft = Wallpaper.PREFERENCES.getInt(dotGridPaddingLeft, Game.DEFAULT_GRID_PADDING_LEFT);
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Dot Grid Padding Left: " + this.mDotGridPaddingLeft);
			}
		}

		final String dotGridPaddingRight = Wallpaper.CONTEXT.getString(R.string.settings_display_padding_right_key);
		if (all || key.equals(dotGridPaddingRight)) {
			this.mDotGridPaddingRight = Wallpaper.PREFERENCES.getInt(dotGridPaddingRight, Game.DEFAULT_GRID_PADDING_RIGHT);
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Dot Grid Padding Right: " + this.mDotGridPaddingRight);
			}
		}

		final String dotGridPaddingTop = Wallpaper.CONTEXT.getString(R.string.settings_display_padding_top_key);
		if (all || key.equals(dotGridPaddingTop)) {
			this.mDotGridPaddingTop = Wallpaper.PREFERENCES.getInt(dotGridPaddingTop, Game.DEFAULT_GRID_PADDING_TOP);
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Dot Grid Padding Top: " + this.mDotGridPaddingTop);
			}
		}

		final String dotGridPaddingBottom = Wallpaper.CONTEXT.getString(R.string.settings_display_padding_bottom_key);
		if (all || key.equals(dotGridPaddingBottom)) {
			this.mDotGridPaddingBottom = Wallpaper.PREFERENCES.getInt(dotGridPaddingBottom, Game.DEFAULT_GRID_PADDING_BOTTOM);
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Dot Grid Padding Bottom: " + this.mDotGridPaddingBottom);
			}
		}
		
		
		// CELLS //
		
		final String iconRows = Wallpaper.CONTEXT.getString(R.string.settings_display_iconrows_key);
		if (all || key.equals(iconRows)) {
			this.mIconRows = Wallpaper.PREFERENCES.getInt(iconRows, Game.DEFAULT_ICON_ROWS);
			hasLayoutChanged = true;
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Icon Rows: " + this.mIconRows);
			}
		}
		
		final String iconCols = Wallpaper.CONTEXT.getString(R.string.settings_display_iconcols_key);
		if (all || key.equals(iconCols)) {
			this.mIconCols = Wallpaper.PREFERENCES.getInt(iconCols, Game.DEFAULT_ICON_COLS);
			hasLayoutChanged = true;
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Icon Cols: " + this.mIconCols);
			}
		}
		
		final String cellSpacingRow = Wallpaper.CONTEXT.getString(R.string.settings_display_rowspacing_key);
		if (all || key.equals(cellSpacingRow)) {
			this.mCellRowSpacing = Wallpaper.PREFERENCES.getInt(cellSpacingRow, Game.DEFAULT_CELL_SPACING_ROWS);
			hasLayoutChanged = true;
			
			if (Wallpaper.LOG_DEBUG) {
		    	Log.d(Game.TAG, "Cell Row Spacing: " + this.mCellRowSpacing);
			}
		}
		
		final String cellSpacingCol = Wallpaper.CONTEXT.getString(R.string.settings_display_colspacing_key);
		if (all || key.equals(cellSpacingCol)) {
			this.mCellColumnSpacing = Wallpaper.PREFERENCES.getInt(cellSpacingCol, Game.DEFAULT_CELL_SPACING_COLUMNS);
			hasLayoutChanged = true;
			
			if (Wallpaper.LOG_DEBUG) {
		    	Log.d(Game.TAG, "Cell Column Spacing: " + this.mCellColumnSpacing);
			}
		}
		
		if (hasLayoutChanged) {
	    	this.mCellsWide = (this.mIconCols * (mCellColumnSpacing + 1)) + 1;
	    	Log.v(Game.TAG, "Cells Wide: " + this.mCellsWide);
	    	this.mCellsTall = (this.mIconRows * (mCellRowSpacing + 1)) + 1;
	    	Log.v(Game.TAG, "Cells Tall: " + this.mCellsTall);
	    	
	    	//Create playing board
	        this.mBoard = new Cell[this.mCellsTall][this.mCellsWide];
		}
		
		
		//Check to see if we need a new game
		if (hasBonusChanged || hasGhostCountChanged || hasLayoutChanged) {
	    	this.newGame();
		}

    	if (Wallpaper.LOG_VERBOSE) {
    		Log.v(Game.TAG, "< onSharedPreferenceChanged()");
    	}
	}
    
    /**
     * Get the Cell value for a specific coordinate.
     * 
     * @param x Horizontal coordinate.
     * @param y Vertical coordinate.
     * @return Cell value.
     */
    public Cell getCell(final Point position) {
    	return this.mBoard[position.y][position.x];
    }
    
    /**
     * Sets the cell value for a specific position.
     * @param position Position to change.
     * @param newCell New cell value.
     */
    public void setCell(final Point position, Cell newCell) {
    	this.mBoard[position.y][position.x] = newCell; 
    }
    
    /**
     * Get the game's instance of The Man.
     * @return The Man instance.
     */
    public TheMan getTheMan() {
    	return this.mTheMan;
    }
    
    /**
     * Get the ghost at specified index.
     * 
     * @param index Index of ghost.
     * @return Ghost instance.
     */
    public Ghost getGhost(final int index) {
    	//WARNING: unchecked
    	return this.mGhosts[index];
    }
    
    /**
     * Get the number of dots eaten this level.
     * @return Number of dots eaten.
     */
    public int getDotsEaten() {
    	return this.mDotsEaten;
    }
    
    /**
     * Get the current level number.
     * @return Level number.
     */
    public int getLevel() {
    	return this.mLevel;
    }
    
    /**
     * Get the width of a cell in pixels.
     * @return Cell width.
     */
    public float getCellWidth() {
    	return this.mCellWidth;
    }
    
    /**
     * Get the height of a cell in pixels.
     * @return Cell height.
     */
    public float getCellHeight() {
    	return this.mCellHeight;
    }
    
    /**
     * Get the board's number of cells horizontally.
     * @return Number of cells.
     */
    public int getCellsWide() {
    	return this.mCellsWide;
    }
    
    /**
     * Get the board's number of cells vertically.
     * @return Number of cells.
     */
    public int getCellsTall() {
    	return this.mCellsTall;
    }
    
    /**
     * Get the board's number of cells between two columns.
     * @return Number of cells.
     */
    public int getCellColumnSpacing() {
    	return this.mCellColumnSpacing;
    }
    
    /**
     * Get the board's number of cells between two rows.
     * @return Number of cells.
     */
    public int getCellRowSpacing() {
    	return this.mCellRowSpacing;
    }
    
    /**
     * Get the number of icon rows on the home screen.
     * @return Number of icons.
     */
    public int getIconRows() {
    	return this.mIconRows;
    }
    
    /**
     * Get the number of icon columns on the home screen.
     * @return Number of icons.
     */
    public int getIconCols() {
    	return this.mIconCols;
    }
    
    /**
     * Test if a Point is a valid coordinate on the game board.
     * 
     * @param position Point representing coordinate.
     * @return Boolean indicating whether or not the position is valid.
     */
    public boolean isValidPosition(final Point position) {
    	return ((position.x >= 0) && (position.x < this.mCellsWide)
    		    && (position.y >= 0) && (position.y < this.mCellsTall)
    		    && (this.mBoard[position.y][position.x] != Cell.WALL));
    }
    
    /**
     * Test if a Point is an intersection on the game board.
     * @param position Point representing coordinate.
     * @return Boolean indicating whether or not the position is in an intersection.
     */
    public boolean isIntersection(final Point position) {
    	int directions = 0;
    	for (Direction direction : Direction.values()) {
    		if (this.isValidPosition(Entity.move(position, direction))) {
    			directions += 1;
    		}
    	}
    	
    	return (directions > 1);
    }
    
    /**
     * Test if a Point has a ghost on it.
     * 
     * @param position Position to check.
     * @return Whether or not a ghost exists on the position.
     */
    public boolean isGhostAtPosition(final Point position) {
    	for (Ghost ghost : this.mGhosts) {
    		if ((ghost.getPosition().x == position.x) && (ghost.getPosition().y == position.y)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    /**
     * Get a unique integer hash for the position on the board.
     * 
     * @param position Point position.
     * @return Integer hash.
     */
    public int hashPosition(final Point position) {
    	return (position.y * this.mCellsWide) + position.x;
    }
    
    /**
     * Add an amount to the player's score.
     * @param amount Amount to add.
     */
    private void addToScore(final int amount) {
    	this.mScore += amount;
    	
    	//Check bonus life
    	if (this.mIsBonusLifeAllowed && !this.mIsBonusLifeGiven && (this.mScore > this.mBonusLifeThreshold)) {
    		this.mIsBonusLifeGiven = true;
    		this.mLives += 1;
    	}
    }
    
    /**
     * Check to see if The Man has eaten a dot or juggerdot.
     */
    public void checkDots() {
    	if (this.getCell(this.mTheMan.getPosition()) == Cell.DOT) {
    		this.mDotsEaten += 1;
    		this.mDotsRemaining -= 1;
    		this.addToScore(Game.POINTS_DOT);
    		
    		//Blank cell since we've eaten the dot
    		this.setCell(this.mTheMan.getPosition(), Cell.BLANK);
        	
        	//Check for level complete
        	if (this.mDotsRemaining <= 0) {
        		this.newLevel();
        	}
    	} else if (this.getCell(this.mTheMan.getPosition()) == Cell.JUGGERDOT) {
    		this.addToScore(Game.POINTS_JUGGERDOT);
    		this.switchGhostsState(Ghost.State.FRIGHTENED);
    	}
    }
    
    /**
     * Check to see if The Man has eaten the fruit.
     */
    public void checkFruit() {
    	if (this.mTheMan.isCollidingWith(this.mFruit)) {
    		//eat the fruit
    		this.addToScore(this.mFruit.eat());
    	}
    }
    
    /**
     * Check to see if The Man has collided with a ghost.
     */
    public void checkGhosts() {
    	for (Ghost ghost : this.mGhosts) {
    		if (this.mTheMan.isCollidingWith(ghost)) {
    			switch (ghost.getState()) {
					case CHASE:
					case SCATTER:
				    	if (this.mIsGhostDeadly) {
				    		//TODO: Kill pacman
				    	}
						break;
	
					case FRIGHTENED:
						//Eat ghost
						this.mFleeingGhostsEaten += 1;
						this.addToScore(Game.POINTS_FLEEING_GHOSTS[this.mFleeingGhostsEaten]);
						ghost.setState(this, State.EATEN);
						
						//See if we have eaten all the ghosts for this juggerdot
						if (this.mFleeingGhostsEaten == this.mGhostCount) {
							this.mAllFleeingGhostsEaten += 1;
							
							//See if we have eaten all the ghosts for every juggerdot
							if (this.mAllFleeingGhostsEaten == Game.NUMBER_OF_JUGGERDOTS) {
								this.addToScore(Game.POINTS_ALL_FLEEING_GHOSTS);
							}
						}
						
						break;
				}
    		}
    	}
    }
    
    /**
     * Switch the current state of the ghosts.
     * @param state New state.
     */
    private void switchGhostsState(final State state) {
    	for (Ghost ghost : this.mGhosts) {
    		ghost.setState(this, state);
    	}
    	if (state == State.FRIGHTENED) {
    		this.mFleeingGhostsEaten = 0;
    	}
    }
    
    /**
     * Reset the game state to that of first initialization.
     */
    public void newGame() {
    	if (Wallpaper.LOG_VERBOSE) {
    		Log.v(Game.TAG, "> newGame()");
    	}
    	
    	//Game level values
		this.mLives = 3;
		this.mScore = 0;
		this.mLevel = 0; //changed to 1 in newLevel
		this.mIsBonusLifeGiven = false;
        this.mIsOnKillScreen = false;
    	
    	//Reset board
    	this.newLevel();
    	
    	if (Wallpaper.LOG_VERBOSE) {
    		Log.v(Game.TAG, "< newGame()");
    	}
    }
    
    /**
     * Reset the board state to that of a level's first initialization.
     */
    private void newLevel() {
    	if (Wallpaper.LOG_VERBOSE) {
    		Log.v(Game.TAG, "> newLevel()");
    	}
    	
    	this.mLevel += 1;
    	
    	//Kill screen is shown randomly one out of 256 levels as long as we are not on level one
    	if ((this.mLevel > 1) && (Game.RANDOM.nextInt(Game.KILL_SCREEN_LEVEL) == 0) && this.mIsKillScreenEnabled) {
    		this.mIsOnKillScreen = true;
    	}
    	
    	//Initialize dots
    	this.mDotsRemaining = 0;
    	this.mDotsEaten = 0;
    	for (int y = 0; y < this.mCellsTall; y++) {
    		for (int x = 0; x < this.mCellsWide; x++) {
    			if ((x % (this.mCellColumnSpacing + 1) == 0) || (y % (this.mCellRowSpacing + 1) == 0)) {
    				this.mBoard[y][x] = Cell.DOT;
    				this.mDotsRemaining += 1;
    			} else {
    				this.mBoard[y][x] = Cell.WALL;
    			}
    		}
    	}
    	
    	this.mAllFleeingGhostsEaten = 0;
    	
    	//Initialize juggerdots
    	this.mBoard[this.mCellRowSpacing + 1][0] = Cell.JUGGERDOT;
    	this.mBoard[0][this.mCellsWide - this.mCellColumnSpacing - 2] = Cell.JUGGERDOT;
    	this.mBoard[this.mCellsTall - this.mCellRowSpacing - 2][this.mCellsWide - 1] = Cell.JUGGERDOT;
    	this.mBoard[this.mCellsTall - 1][this.mCellColumnSpacing + 1] = Cell.JUGGERDOT;
    	this.mDotsRemaining -= Game.NUMBER_OF_JUGGERDOTS;
    	
    	//Initialize entities
    	for (Entity entity : this.mEntities) {
    		entity.newLevel(this);
    	}
    	
    	if (Wallpaper.LOG_VERBOSE) {
    		Log.v(Game.TAG, "< newLevel()");
    	}
    }
    
    /**
     * Iterate all entities one step.
     */
    public void tick() {
    	for (Entity entity : this.mEntities) {
    		entity.tick(this);
    	}
    }

    /**
     * Resize the game board and all entities according to a new width and height.
     * 
     * @param screenWidth New width.
     * @param screenHeight New height.
     */
    public void performResize(int screenWidth, int screenHeight) {
    	if (Wallpaper.LOG_VERBOSE) {
    		Log.v(Game.TAG, "> performResize(width = " + screenWidth + ", height = " + screenHeight + ")");
    	}
    	
    	if (screenWidth > screenHeight) {
    		this.mIsLandscape = true;
    		final int temp = screenHeight;
    		screenHeight = screenWidth;
    		screenWidth = temp;
    	} else {
    		this.mIsLandscape = false;
    	}
    	
    	this.mScreenWidth = screenWidth;
    	this.mScreenHeight = screenHeight;
    	
    	if (this.mIsLandscape) {
    		this.mCellWidth = (screenWidth - this.mDotGridPaddingTop) / (this.mCellsWide * 1.0f);
    		this.mCellHeight = (screenHeight - (this.mDotGridPaddingBottom + this.mDotGridPaddingLeft + this.mDotGridPaddingRight)) / (this.mCellsTall * 1.0f);
    	} else {
    		this.mCellWidth = (screenWidth - (this.mDotGridPaddingLeft + this.mDotGridPaddingRight)) / (this.mCellsWide * 1.0f);
    		this.mCellHeight = (screenHeight - (this.mDotGridPaddingTop + this.mDotGridPaddingBottom)) / (this.mCellsTall * 1.0f);
    	}
    	
    	if (Wallpaper.LOG_DEBUG) {
    		Log.d(Game.TAG, "Is Landscape: " + this.mIsLandscape);
    		Log.d(Game.TAG, "Screen Width: " + screenWidth);
    		Log.d(Game.TAG, "Screen Height: " + screenHeight);
    		Log.d(Game.TAG, "Cell Width: " + this.mCellWidth);
    		Log.d(Game.TAG, "Cell Height: " + this.mCellHeight);
    	}
    	
    	for (Entity entity : this.mEntities) {
    		entity.performResize(this);
    	}

    	if (Wallpaper.LOG_VERBOSE) {
    		Log.v(Game.TAG, "< performResize()");
    	}
    }
    
    /**
     * Render the board and all entities on a Canvas.
     * 
     * @param c Canvas to draw on.
     */
    public void draw(final Canvas c) {
    	c.save();
    	
    	//Background
    	c.drawColor(this.mGameBackground);
    	
    	if (this.mIsDisplayingHud) {
	        //Lives and score
	        final float textY = this.mScreenHeight - this.mDotGridPaddingBottom + 15;
	        final String score = String.valueOf(Game.SCORE_FORMAT.format(this.mScore));
	        c.drawText(this.mLives + "UP", 10, textY, this.mHudForeground);
	        c.drawText(score, this.mScreenWidth - this.mHudForeground.measureText(score) - 10, textY, this.mHudForeground);
    	}
        
        if (this.mIsLandscape) {
        	c.rotate(-90, this.mScreenWidth / 2.0f, this.mScreenWidth / 2.0f);
        	c.translate(0, this.mDotGridPaddingLeft);
        } else {
        	c.translate(this.mDotGridPaddingLeft, this.mDotGridPaddingTop);
        }
        
        for (int y = 0; y < this.mCellsTall; y++) {
        	for (int x = 0; x < this.mCellsWide; x++) {
        		if (this.mBoard[y][x] == Cell.DOT) {
            		final float left = (x * this.mCellWidth) + ((this.mCellWidth * 0.75f) / 2);
            		final float top = (y * this.mCellHeight) + ((this.mCellHeight * 0.75f) / 2);
            		final float right = left + (this.mCellWidth * 0.25f);
            		final float bottom = top + (this.mCellHeight * 0.25f);
            		
            		c.drawOval(new RectF(left, top, right, bottom), this.mDotForeground);
        		} else if (this.mBoard[y][x] == Cell.JUGGERDOT) {
            		final float left = (x * this.mCellWidth) + ((this.mCellWidth * 0.25f) / 2);
            		final float top = (y * this.mCellHeight) + ((this.mCellHeight * 0.25f) / 2);
            		final float right = left + (this.mCellWidth * 0.75f);
            		final float bottom = top + (this.mCellHeight * 0.75f);

            		c.drawOval(new RectF(left, top, right, bottom), this.mDotForeground);
        		}
        	}
        }
        
        //Draw the entities
        for (Entity entity : this.mEntities) {
        	entity.draw(c);
        }
        
        if (this.mIsOnKillScreen) {
        	//TODO: draw garbled text on right half of screen using dot, entity, and HUD colors
        }
        
        c.restore();
    }
}
