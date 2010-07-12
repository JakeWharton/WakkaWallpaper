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

public class Game {
	enum Cell { BLANK, WALL, DOT, JUGGERDOT }

	public static final Random RANDOM = new Random();
	private static final String TAG = "Game";
	private static final int NUMBER_OF_GHOSTS = 4;
	private static final int POINTS_DOT = 10;
	private static final int POINTS_JUGGERDOT = 50;
	private static final NumberFormat SCORE_FORMAT = new DecimalFormat("000000");
	private static final boolean DEFAULT_BONUS_ALLOWED = true;
	private static final int DEFAULT_BONUS_THRESHOLD = 10000;
	private static final int DEFAULT_DOT_FOREGROUND = 0xff6161a1;
	private static final int DEFAULT_GAME_BACKGROUND = 0xff000040;
    private static final int DEFAULT_HUD_FOREGROUND = 0xff8181c1;
    private static final int DEFUALT_HUD_BACKGROUND = 0xff000000;
	
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
	private Ghost[] mGhosts;
	private TheMan mTheMan;
	private Fruit mFruit;
	private int mDotsRemaining;
	private int mLives;
	private int mScore;
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
    
    public Game(int iconRows, int iconCols, int screenWidth, int screenHeight) {
    	//Create entities
    	this.mGhosts = new Ghost[Game.NUMBER_OF_GHOSTS];
    	this.mGhosts[0] = new Ghost.Blinky();
    	this.mGhosts[1] = new Ghost.Pinky();
    	this.mGhosts[2] = new Ghost.Inky();
    	this.mGhosts[3] = new Ghost.Clyde();
    	this.mTheMan = new TheMan();
    	
    	//Screen and grid data
        this.mDotGridPaddingLeft = -5;
        this.mDotGridPaddingRight = -5;
        this.mDotGridPaddingTop = 35;
        this.mDotGridPaddingBottom = 75;
    	this.mIconRows = iconRows;
    	Log.d(Game.TAG, "Icon Rows: " + iconRows);
    	this.mIconCols = iconCols;
    	Log.d(Game.TAG, "Icon Cols: " + iconCols);
    	this.performResize(screenWidth, screenHeight);
    	
    	//Create playing board
        this.mBoard = new Cell[this.mCellsTall][this.mCellsWide];
    	
        this.mBonusLifeAllowed = Game.DEFAULT_BONUS_ALLOWED;
        this.mBonusLifeThreshold = Game.DEFAULT_BONUS_THRESHOLD;
        
        this.mDotForeground = new Paint();
        this.mDotForeground.setColor(Game.DEFAULT_DOT_FOREGROUND);
        this.mGameBackground = Game.DEFAULT_GAME_BACKGROUND;
    	
        this.mHUDForeground = new Paint();
        this.mHUDForeground.setColor(Game.DEFAULT_HUD_FOREGROUND);
        this.mHUDForeground.setAntiAlias(true);
        this.mHUDForeground.setTextSize(20f);
        this.mHUDForeground.setShadowLayer(1, -1, 1, Game.DEFUALT_HUD_BACKGROUND);
        
        this.reset();
    }
    
    public void setWantsToGo(Direction direction) {
    	this.mTheMan.setWantsToGo(direction);
    }
    public Cell getCell(int x, int y) {
    	return this.mBoard[y][x];
    }
    public void addScore(int value) {
    	this.mScore += value;
    }
    
    public void reset() {
    	//Game level values
		this.mLives = 3;
		this.mScore = 0;
		this.mBonusLifeGiven = false;
    	
    	//Reset board
    	this.boardReset();
    }
    private void boardReset() {
    	//Initialize dots
    	this.mDotsRemaining = 0;
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
    	this.mTheMan.setPosition(5, 7);
    	this.mTheMan.setDirection(Direction.STOPPED);
    	
    	//Initialize ghosts
    	this.mGhosts[0].setPosition(this.mCellColumnSpacing + 1, 0);
    	this.mGhosts[1].setPosition(this.mCellsWide - 1, this.mCellRowSpacing + 1);
    	this.mGhosts[2].setPosition(this.mCellsWide - this.mCellColumnSpacing - 2, this.mCellsTall - 1);
    	this.mGhosts[3].setPosition(0, this.mCellsTall - this.mCellRowSpacing - 2);
    	
    	this.mFruit = null;
    }
    public void tick() {
    	if (this.mFruit != null) {
    		this.mFruit.tick(this);
    	}
    	
    	this.mTheMan.tick(this);
    	
    	//XXX: temporary dot checking
    	//TODO: move this somewhere else logical
    	if (this.mBoard[this.mTheMan.getPositionY()][this.mTheMan.getPositionX()] == Cell.DOT) {
    		this.mScore += Game.POINTS_DOT;
    		this.mBoard[this.mTheMan.getPositionY()][this.mTheMan.getPositionX()] = Cell.BLANK;
    		this.mDotsRemaining -= 1;
    		
    		if (this.mDotsRemaining == 0) {
    			this.boardReset();
    		}
    		if (this.mBonusLifeAllowed && !this.mBonusLifeGiven && this.mScore >= this.mBonusLifeThreshold) {
    			this.mLives += 1;
    			this.mBonusLifeGiven = true;
    		}
    	} else if (this.mBoard[this.mTheMan.getPositionY()][this.mTheMan.getPositionX()] == Cell.JUGGERDOT) {
    		this.mScore += Game.POINTS_JUGGERDOT;
    		this.mBoard[this.mTheMan.getPositionY()][this.mTheMan.getPositionX()] = Cell.BLANK;
    	}
    	
    	for (Ghost ghost : this.mGhosts) {
    		ghost.tick(this);
    	}
    }

    public void performResize(int screenWidth, int screenHeight) {
    	if (screenWidth > screenHeight) {
    		this.mIsLandscape = true;
    		int temp = screenHeight;
    		screenHeight = screenWidth;
    		screenWidth = temp;
    	} else {
    		this.mIsLandscape = false;
    	}
    	
    	this.mScreenWidth = screenWidth;
    	Log.d(Game.TAG, "Screen Width: " + screenWidth);
    	this.mScreenHeight = screenHeight;
    	Log.d(Game.TAG, "Screen Height: " + screenHeight);
    	this.mCellColumnSpacing = 4; //TODO: calculate this from width and left/right padding
    	Log.d(Game.TAG, "Cell Column Spacing: " + this.mCellColumnSpacing);
    	this.mCellRowSpacing = 6; //TODO: calculate this from height and top/bottom padding
    	Log.d(Game.TAG, "Cell Row Spacing: " + this.mCellRowSpacing);
    	this.mCellsWide = (this.mIconCols * (mCellColumnSpacing + 1)) + 1;
    	Log.d(Game.TAG, "Cells Wide: " + this.mCellsWide);
    	this.mCellsTall = (this.mIconRows * (mCellRowSpacing + 1)) + 1;
    	Log.d(Game.TAG, "Cells Tall: " + this.mCellsTall);
    	
    	if (this.mIsLandscape) {
    		this.mCellWidth = (screenWidth - this.mDotGridPaddingTop) / (this.mCellsWide * 1.0f);
    		this.mCellHeight = (screenHeight - (this.mDotGridPaddingBottom + this.mDotGridPaddingLeft + this.mDotGridPaddingRight)) / (this.mCellsTall * 1.0f);
    	} else {
    		this.mCellWidth = (screenWidth - (this.mDotGridPaddingLeft + this.mDotGridPaddingRight)) / (this.mCellsWide * 1.0f);
    		this.mCellHeight = (screenHeight - (this.mDotGridPaddingTop + this.mDotGridPaddingBottom)) / (this.mCellsTall * 1.0f);
    	}
    	Log.d(Game.TAG, "Cell Width: " + this.mCellWidth);
    	Log.d(Game.TAG, "Cell Height: " + this.mCellHeight);
    	
    	if (this.mFruit != null) {
    		this.mFruit.performResize(this.mCellWidth, this.mCellHeight);
    	}
    	this.mTheMan.performResize(this.mCellWidth, this.mCellHeight);
    	for (Ghost ghost : this.mGhosts) {
    		ghost.performResize(this.mCellWidth, this.mCellHeight);
    	}
    }
    public void draw(Canvas c) {
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
            		float left = (x * this.mCellWidth) + ((this.mCellWidth * 0.75f) / 2);
            		float top = (y * this.mCellHeight) + ((this.mCellHeight * 0.75f) / 2);
            		float right = left + (this.mCellWidth * 0.25f);
            		float bottom = top + (this.mCellHeight * 0.25f);
            		
            		c.drawOval(new RectF(left, top, right, bottom), this.mDotForeground);
        		} else if (this.mBoard[y][x] == Cell.JUGGERDOT) {
            		float left = (x * this.mCellWidth) + ((this.mCellWidth * 0.25f) / 2);
            		float top = (y * this.mCellHeight) + ((this.mCellHeight * 0.25f) / 2);
            		float right = left + (this.mCellWidth * 0.75f);
            		float bottom = top + (this.mCellHeight * 0.75f);

            		c.drawOval(new RectF(left, top, right, bottom), this.mDotForeground);
        		}
        	}
        }
        
        //Draw the entities
        if (this.mFruit != null) {
        	this.mFruit.draw(c);
        }
        this.mTheMan.draw(c);
        for (Ghost ghost : this.mGhosts) {
        	ghost.draw(c);
        }
        
        c.restore();
    }

    public boolean isValidPosition(Point position) {
    	return ((position.x >= 0) && (position.x < this.mCellsWide)
    		    && (position.y >= 0) && (position.y < this.mCellsTall)
    		    && (this.mBoard[position.y][position.x] != Cell.WALL));
    }
}
