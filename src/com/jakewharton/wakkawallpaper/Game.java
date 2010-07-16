package com.jakewharton.wakkawallpaper;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Random;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.Log;

import com.jakewharton.wakkawallpaper.Entity.Direction;

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
	private static final int POINTS_DOT = 10;
	private static final int POINTS_JUGGERDOT = 50;
	
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
	private Cell[][] mBoard;
	private Entity[] mEntities;
	private TheMan mTheMan;
	private Fruit mFruit;
	private int mDotsRemaining;
	private int mDotsEaten;
	private int mLives;
	private int mScore;
	private int mLevel;
    private boolean mBonusLifeAllowed;
    private boolean mBonusLifeGiven;
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
    	//Create entities
    	this.mTheMan = new TheMan();
    	this.mFruit = new Fruit();
    	this.mEntities = new Entity[6];
    	this.mEntities[0] = this.mTheMan;
    	this.mEntities[1] = new Ghost.Blinky();
    	this.mEntities[2] = new Ghost.Pinky();
    	this.mEntities[3] = new Ghost.Inky();
    	this.mEntities[4] = new Ghost.Clyde();
    	this.mEntities[5] = this.mFruit;
    	
    	//Screen and grid data
        this.mDotGridPaddingLeft = -5;
        this.mDotGridPaddingRight = -5;
        this.mDotGridPaddingTop = 35;
        this.mDotGridPaddingBottom = 75;
    	this.mIconRows = iconRows;
    	
    	Log.v(Game.TAG, "Icon Rows: " + iconRows);
    	this.mIconCols = iconCols;
    	Log.v(Game.TAG, "Icon Cols: " + iconCols);
    	this.performResize(screenWidth, screenHeight);
    	
    	//Create playing board
        this.mBoard = new Cell[this.mCellsTall][this.mCellsWide];
    	
        this.mBonusLifeAllowed = Game.DEFAULT_BONUS_ALLOWED;
        this.mBonusLifeThreshold = Game.DEFAULT_BONUS_THRESHOLD;
        
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
     * Specify a direction you would like "The Man" to travel in next (if possible).
     * 
     * @param direction Desired direction.
     */
    public void setWantsToGo(final Direction direction) {
    	this.mTheMan.setWantsToGo(direction);
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
    
    public TheMan getTheMan() {
    	return this.mTheMan;
    }
    
    public int getLevel() {
    	return this.mLevel;
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
    
    public int hashPosition(final Point position) {
    	return (position.y * this.mCellsWide) + position.x;
    }
    
    public void checkForDot() {
    	if (this.mBoard[this.mTheMan.getPositionY()][this.mTheMan.getPositionX()] == Cell.DOT) {
    		this.mDotsEaten += 1;
    		this.mDotsRemaining -= 1;
    		this.mScore += Game.POINTS_DOT;
    	} else if (this.mBoard[this.mTheMan.getPositionY()][this.mTheMan.getPositionX()] == Cell.JUGGERDOT) {
    		this.mScore += Game.POINTS_JUGGERDOT;
    	}
    }
    
    public void checkForGhost() {
    	//TODO: check ghost collisions
    }
    
    /**
     * Reset the game state to that of first initialization.
     */
    public void newGame() {
    	//Game level values
		this.mLives = 3;
		this.mScore = 0;
		this.mLevel = 1;
		this.mBonusLifeGiven = false;
    	
    	//Reset board
    	this.reset();
    }
    
    /**
     * Reset the board state to that of a level's first initialization.
     */
    private void reset() {
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
    	
    	//Initialize juggerdots
    	this.mBoard[this.mCellRowSpacing + 1][0] = Cell.JUGGERDOT;
    	this.mBoard[0][this.mCellsWide - this.mCellColumnSpacing - 2] = Cell.JUGGERDOT;
    	this.mBoard[this.mCellsTall - this.mCellRowSpacing - 2][this.mCellsWide - 1] = Cell.JUGGERDOT;
    	this.mBoard[this.mCellsTall - 1][this.mCellColumnSpacing + 1] = Cell.JUGGERDOT;
    	this.mDotsRemaining -= 4;
    	
    	//Initialize "The Man"
    	for (Entity entity : this.mEntities) {
    		entity.reset(this);
    	}
    }
    
    /**
     * Iterate all entities one step.
     */
    public void tick() {
    	for (Entity entity : this.mEntities) {
    		entity.tick(this);
    	}
    	
    	//Check bonus life
    	if (this.mBonusLifeAllowed && !this.mBonusLifeGiven && (this.mScore > this.mBonusLifeThreshold)) {
    		this.mBonusLifeGiven = true;
    		this.mLives += 1;
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
    		entity.performResize(screenWidth, screenHeight);
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
        
        c.restore();
    }
}
