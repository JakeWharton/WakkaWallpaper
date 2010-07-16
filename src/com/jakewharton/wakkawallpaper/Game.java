package com.jakewharton.wakkawallpaper;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Random;

import com.jakewharton.wakkawallpaper.Entity.Direction;
import com.jakewharton.wakkawallpaper.Ghost.State;

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
public class Game {
	enum Cell { BLANK, WALL, DOT, JUGGERDOT }

	public static final Random RANDOM = new Random();
	private static final String TAG = "WakkaWallpaper.Game";
	private static final NumberFormat SCORE_FORMAT = new DecimalFormat("000000");
	private static final boolean DEFAULT_BONUS_ALLOWED = true;
	private static final int DEFAULT_BONUS_THRESHOLD = 10000;
	private static final int DEFAULT_DOT_FOREGROUND = 0xff6161a1;
	private static final int DEFAULT_GAME_BACKGROUND = 0xff000040;
    private static final int DEFAULT_HUD_FOREGROUND = 0xff8181c1;
    private static final int DEFAULT_HUD_BACKGROUND = 0xff000000;
    private static final boolean DEFAULT_KILL_SCREEN_ENABLED = true;
    private static final int DEFAULT_GHOST_COUNT = 4;
	private static final int POINTS_DOT = 10;
	private static final int POINTS_JUGGERDOT = 50;
	private static final int[] POINTS_FLEEING_GHOSTS = new int[] { 200, 400, 800, 1600 };
	private static final int POINTS_ALL_FLEEDING_GHOSTS = 12000;
	private static final int NUMBER_OF_JUGGERDOTS = 4;
	private static final int KILL_SCREEN_LEVEL = 256;
	
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
	private int mFleeingGhostsEaten;
	private int mAllFleedingGhostsEaten;
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
    private final Paint mHUDForeground;
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
    public Game(final int iconRows, final int iconCols, final int screenWidth, final int screenHeight) {
    	this.mGhostCount = Game.DEFAULT_GHOST_COUNT;
    	
    	//Create entities
    	this.mTheMan = new TheMan();
    	this.mFruit = new Fruit();
    	this.mGhosts = new Ghost[this.mGhostCount];
    	if (this.mGhostCount > 0) { this.mGhosts[0] = new Ghost.Blinky(); }
    	if (this.mGhostCount > 1) { this.mGhosts[1] = new Ghost.Pinky(); }
    	if (this.mGhostCount > 2) { this.mGhosts[2] = new Ghost.Inky(); }
    	if (this.mGhostCount > 3) { this.mGhosts[3] = new Ghost.Clyde(); }
    	
    	int i = 0;
    	this.mEntities = new Entity[2 + this.mGhostCount]; //TheMan, Fruit, and Ghosts
    	this.mEntities[i++] = this.mTheMan;
    	this.mEntities[i++] = this.mFruit;
    	for (Ghost ghost : this.mGhosts) {
    		this.mEntities[i++] = ghost;
    	}
    	
    	//Screen and grid data
        this.mDotGridPaddingLeft = -5;
        this.mDotGridPaddingRight = -5;
        this.mDotGridPaddingTop = 35;
        this.mDotGridPaddingBottom = 75;
    	this.mIconRows = iconRows;
    	this.mIconCols = iconCols;
    	Log.v(Game.TAG, "Icon Rows: " + iconRows);
    	Log.v(Game.TAG, "Icon Cols: " + iconCols);
    	this.performResize(screenWidth, screenHeight);
    	
    	//Create playing board
        this.mBoard = new Cell[this.mCellsTall][this.mCellsWide];
    	
        this.mIsBonusLifeAllowed = Game.DEFAULT_BONUS_ALLOWED;
        this.mBonusLifeThreshold = Game.DEFAULT_BONUS_THRESHOLD;
        this.mIsOnKillScreen = false;
        this.mIsKillScreenEnabled = Game.DEFAULT_KILL_SCREEN_ENABLED;
        
        this.mDotForeground = new Paint();
        this.mDotForeground.setColor(Game.DEFAULT_DOT_FOREGROUND);
        this.mDotForeground.setAntiAlias(true);
        this.mGameBackground = Game.DEFAULT_GAME_BACKGROUND;
    	
        this.mHUDForeground = new Paint();
        this.mHUDForeground.setColor(Game.DEFAULT_HUD_FOREGROUND);
        this.mHUDForeground.setAntiAlias(true);
        this.mHUDForeground.setTextSize(20f);
        this.mHUDForeground.setShadowLayer(1, -1, 1, Game.DEFAULT_HUD_BACKGROUND);
        
        this.newGame();
    }
    
    /**
     * Get the Cell value for a specific coordinate.
     * 
     * @param x Horizontal coordinate.
     * @param y Vertical coordinate.
     * @return Cell value.
     */
    public Cell getCell(final int x, final int y) {
    	return this.mBoard[y][x];
    }
    
    /**
     * Get the game's instance of The Man.
     * @return The Man instance.
     */
    public TheMan getTheMan() {
    	return this.mTheMan;
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
    	for (Direction direction : Direction.movingValues()) {
    		if (this.isValidPosition(Entity.move(position, direction))) {
    			directions += 1;
    		}
    	}
    	
    	return (directions > 1);
    }
    
    /**
     * Get a unique integer hash for the position on the board.
     * @param position Position on the board.
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
    	if (this.mBoard[this.mTheMan.getPositionY()][this.mTheMan.getPositionX()] == Cell.DOT) {
    		this.mDotsEaten += 1;
    		this.mDotsRemaining -= 1;
    		this.addToScore(Game.POINTS_DOT);
        	
        	//Check for level complete
        	if (this.mDotsRemaining <= 0) {
        		this.newLevel();
        	}
    	} else if (this.mBoard[this.mTheMan.getPositionY()][this.mTheMan.getPositionX()] == Cell.JUGGERDOT) {
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
						//TODO: Kill pacman
						break;
	
					case FRIGHTENED:
						//Eat ghost
						this.mFleeingGhostsEaten += 1;
						this.addToScore(Game.POINTS_FLEEING_GHOSTS[this.mFleeingGhostsEaten]);
						ghost.setState(this, State.EATEN);
						
						//See if we have eaten all the ghosts for this juggerdot
						if (this.mFleeingGhostsEaten == this.mGhostCount) {
							this.mAllFleedingGhostsEaten += 1;
							
							//See if we have eaten all the ghosts for every juggerdot
							if (this.mAllFleedingGhostsEaten == Game.NUMBER_OF_JUGGERDOTS) {
								this.addToScore(Game.POINTS_ALL_FLEEDING_GHOSTS);
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
    	//Game level values
		this.mLives = 3;
		this.mScore = 0;
		this.mLevel = 0; //changed to 1 in newLevel
		this.mIsBonusLifeGiven = false;
    	
    	//Reset board
    	this.newLevel();
    }
    
    /**
     * Reset the board state to that of a level's first initialization.
     */
    private void newLevel() {
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
    	
    	this.mAllFleedingGhostsEaten = 0;
    	
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
    	if (screenWidth > screenHeight) {
    		this.mIsLandscape = true;
    		final int temp = screenHeight;
    		screenHeight = screenWidth;
    		screenWidth = temp;
    	} else {
    		this.mIsLandscape = false;
    	}
    	
    	this.mScreenWidth = screenWidth;
    	Log.v(Game.TAG, "Screen Width: " + screenWidth);
    	this.mScreenHeight = screenHeight;
    	Log.v(Game.TAG, "Screen Height: " + screenHeight);
    	this.mCellColumnSpacing = 4; //TODO: calculate this from width and left/right padding
    	Log.v(Game.TAG, "Cell Column Spacing: " + this.mCellColumnSpacing);
    	this.mCellRowSpacing = 6; //TODO: calculate this from height and top/bottom padding
    	Log.v(Game.TAG, "Cell Row Spacing: " + this.mCellRowSpacing);
    	this.mCellsWide = (this.mIconCols * (mCellColumnSpacing + 1)) + 1;
    	Log.v(Game.TAG, "Cells Wide: " + this.mCellsWide);
    	this.mCellsTall = (this.mIconRows * (mCellRowSpacing + 1)) + 1;
    	Log.v(Game.TAG, "Cells Tall: " + this.mCellsTall);
    	
    	if (this.mIsLandscape) {
    		this.mCellWidth = (screenWidth - this.mDotGridPaddingTop) / (this.mCellsWide * 1.0f);
    		this.mCellHeight = (screenHeight - (this.mDotGridPaddingBottom + this.mDotGridPaddingLeft + this.mDotGridPaddingRight)) / (this.mCellsTall * 1.0f);
    	} else {
    		this.mCellWidth = (screenWidth - (this.mDotGridPaddingLeft + this.mDotGridPaddingRight)) / (this.mCellsWide * 1.0f);
    		this.mCellHeight = (screenHeight - (this.mDotGridPaddingTop + this.mDotGridPaddingBottom)) / (this.mCellsTall * 1.0f);
    	}
    	Log.v(Game.TAG, "Cell Width: " + this.mCellWidth);
    	Log.v(Game.TAG, "Cell Height: " + this.mCellHeight);
    	
    	for (Entity entity : this.mEntities) {
    		entity.performResize(this);
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
    	
        //Lives and score
        final float textY = this.mScreenHeight - this.mDotGridPaddingBottom + 15;
        final String score = String.valueOf(Game.SCORE_FORMAT.format(this.mScore));
        c.drawText(this.mLives + "UP", 10, textY, this.mHUDForeground);
        c.drawText(score, this.mScreenWidth - this.mHUDForeground.measureText(score) - 10, textY, this.mHUDForeground);
        
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
