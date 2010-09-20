package com.jakewharton.wakkawallpaper;

import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import com.jakewharton.utilities.WidgetLocationsPreference;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.util.Log;

/**
 * The Game class manages the playing board and all of the entities contained within.
 * 
 * @author Jake Wharton
 */
public class Game implements SharedPreferences.OnSharedPreferenceChangeListener {
	/**
	 * Represents a single cell on the game board.
	 * 
	 * @author Jake Wharton
	 */
	enum Cell {
		BLANK(0), WALL(-1), DOT(10), JUGGERDOT(50);
		
		
		
		/**
		 * Point value when eaten.
		 */
		public final int value;
		
		
		
		/**
		 * Create a new Cell instance.
		 * 
		 * @param value Point value.
		 */
		private Cell(final int value) {
			this.value = value;
		}
	}
	
	/**
	 * Gameplay mode.
	 * 
	 * @author Jake Wharton
	 */
	enum Mode {
		ARCADE(0), ENDLESS(1);
		
		
		
		/**
		 * Persisted value.
		 */
		public final int value;
		
		
		
		/**
		 * Create a new mode.
		 * @param value
		 */
		private Mode(final int value) {
			this.value = value;
		}
		
		
		
		/**
		 * Convert an integer to its corresponding Game.Mode.
		 * 
		 * @param modeValue Integer
		 * @return Game.Mode
		 */
		public static Game.Mode parseInt(final int modeValue) {
			for (final Game.Mode mode : Game.Mode.values()) {
				if (mode.value == modeValue) {
					return mode;
				}
			}
			throw new IllegalArgumentException("Unknown Game mode value: " + modeValue);
		}
	}
	
	/**
	 * Gameplay state.
	 * 
	 * @author Jake Wharton
	 */
	enum State {
		READY(3000), PLAYING(0), DYING(3000), LEVEL_COMPLETE(1500), GAME_OVER(3000);
		
		
		
		/**
		 * Length to remain in this state.
		 */
		public final int length;
		
		
		
		/**
		 * Create a new State.
		 * 
		 * @param ticks Length of state in ticks.
		 */
		private State(final int length) {
			this.length = length;
		}
	}
	
	/**
	 * Edge wrapping mode.
	 * 
	 * @author Jake Wharton
	 */
	enum Wrapping {
		ALL(0), CENTER_ROW(1);
		
		
		
		/**
		 * Persisted value.
		 */
		public final int value;
		
		
		
		/**
		 * Create a new Wrapping.
		 * @param value
		 */
		private Wrapping(final int value) {
			this.value = value;
		}
		
		
		
		/**
		 * Convert an integer to its corresponding Game.Wrapping.
		 * 
		 * @param wrappingValue Integer
		 * @return Game.Wrapping
		 */
		public static Game.Wrapping parseInt(final int wrappingValue) {
			for (final Game.Wrapping wrapping : Game.Wrapping.values()) {
				if (wrapping.value == wrappingValue) {
					return wrapping;
				}
			}
			throw new IllegalArgumentException("Unknown Game wrapping value: " + wrappingValue);
		}
	}
	enum Dots { CIRCLES, APPLES }
	
	
	
	/**
	 * Single random number generate for this wallpaper.
	 */
	/*package*/static final Random RANDOM = new Random();
	
	/**
	 * Filters used to smooth the drawing of sprites.
	 */
	/*package*/static final PaintFlagsDrawFilter FILTER_SET = new PaintFlagsDrawFilter(0, Paint.FILTER_BITMAP_FLAG);
	
	/**
	 * Filters used to undo the sprite smoothing.
	 */
	/*package*/static final PaintFlagsDrawFilter FILTER_REMOVE = new PaintFlagsDrawFilter(Paint.FILTER_BITMAP_FLAG, 0);
	
	/**
	 * Tag used for logging.
	 */
	private static final String TAG = "WakkaWallpaper.Game";
	
	/**
	 * The format to use when rendering the score in arcade mode.
	 */
	private static final NumberFormat SCORE_FORMAT = new DecimalFormat("000000");
	
	/**
	 * The value at which to flip the score in arcade mode.
	 */
	private static final int SCORE_FLIPPING = 1000000;
	
	/**
	 * Point values of fleeing ghosts.
	 */
	private static final int[] POINTS_FLEEING_GHOSTS = new int[] { 200, 400, 800, 1600 };
	
	/**
	 * Points when all of the ghosts are eaten on a single juggerdot.
	 */
	private static final int POINTS_ALL_FLEEING_GHOSTS = 12000;
	
	/**
	 * Padding (in pixels) from the screen edges for the HUD.
	 */
	private static final float HUD_PADDING = 3;
	
	/**
	 * Angle at which to draw The Man's lives in the HUD.
	 */
	private static final float HUD_THEMAN_ANGLE = 202.5f;
	
	/**
	 * Arc of The Man in the HUD.
	 */
	private static final float HUD_THEMAN_ARC = 315;
	
	/**
	 * Number of juggerdots on the board.
	 */
	private static final int NUMBER_OF_JUGGERDOTS = 4;
	
	/**
	 * Level at which to display the kill screen.
	 */
	private static final int KILL_SCREEN_LEVEL = 256;
	
	/**
	 * Number of lives to start a game with.
	 */
	private static final int INITIAL_LIVES = 3;
	
	/**
	 * Point threshold at which the Googol trophy is earned.
	 */
	private static final int TROPHY_GOOGOL_THRESHOLD = 1010100;
	
	/**
	 * Point threshold at which the Legend trophy is earned.
	 */
	private static final int TROPHY_LEGEND_THRESHOLD = 3333360;
	
	/**
	 * Number of ghosts that need to be eaten on a single level to earn the Logos trophy.
	 */
	private static final int TROPHY_LOGOS_THRESHOLD = 12;
	
	/**
	 * Level that needs to be reaches to earn the CEOs trophy.
	 */
	private static final int TROPHY_CEOS_THRESHOLD = 256;
	
	/**
	 * Probability of the kill screen appearing when the Legend trophy is enabled.
	 */
	private static final int KILL_SCREEN_TROPHY_PROBABILITY = 10;
	
	
	
	/**
	 * The current game state.
	 */
	private Game.State mState;
	
	/**
	 * The current gameplay mode.
	 */
	private Game.Mode mMode;
	
	/**
	 * The type of wrapping employed if it is enabled on an entity.
	 */
	private Game.Wrapping mWrapping;
	
	/**
	 * What to represent the dots with when rendering.
	 */
	private Game.Dots mDots;
	
	/**
	 * Whether or not The Man can wrap around the edges.
	 */
	private boolean mIsWrappingTheMan;
	
	/**
	 * Whether or not the ghosts can wrap around the edges.
	 */
	private boolean mIsWrappingGhosts;
	
	/**
	 * The system milliseconds at which the game state last changed.
	 */
	private long mStateTimestamp;
	
	/**
	 * Number of cells on the board horizontally.
	 */
	private int mCellsWide;
	
	/**
	 * Number of cells on the board vertically.
	 */
	private int mCellsTall;
	
	/**
	 * Number of cells horizontally between the columns.
	 */
	private int mCellColumnSpacing;
	
	/**
	 * Number of cells vertically between the rows.
	 */
	private int mCellRowSpacing;
	
	/**
	 * Width (in pixels) of a single cell.
	 */
	private float mCellWidth;
	
	/**
	 * Height (in pixels) of a single cell.
	 */
	private float mCellHeight;
	
	/**
	 * Height (in pixels) of the screen.
	 */
    private int mScreenHeight;
    
    /**
     * Width (in pixels) of the screen.
     */
    private int mScreenWidth;
    
    /**
     * Whether or not the screen is currently in landscape mode.
     */
    private boolean mIsLandscape;
    
    /**
     * Number of icon rows on the launcher.
     */
    private int mIconRows;
    
    /**
     * Number of icon columns on the launcher.
     */
    private int mIconCols;
    
    /**
     * Whether or not the current level features the kill screen.
     */
    private boolean mIsOnKillScreen;
    
    /**
     * Whether or not the kill screen is enabled for level 256 and therefore causing level looping.
     */
    private boolean mIsKillScreenEnabled;
    
    /**
     * 2-dimensional array of the board's cells.
     */
	private Game.Cell[][] mBoard;
	
	/**
	 * The Man instance.
	 */
	private TheMan mTheMan;
	
	/**
	 * Fruit instance.
	 */
	private Fruit mFruit;
	
	/**
	 * Ghost instances (if any)
	 */
	private Ghost[] mGhosts;
	
	/**
	 * Whether or not running into a ghost is deadly.
	 */
	private boolean mIsGhostDeadly;
	
	/**
	 * Whether or not fruits are enabled.
	 */
	private boolean mIsFruitEnabled;
	
	/**
	 * Number of fleeing ghosts eaten this juggerdot.
	 */
	private int mFleeingGhostsEaten;
	
	/**
	 * Number of times all of the fleeing ghosts were eaten on a juggerdot this level.
	 */
	private int mAllFleeingGhostsEaten;
	
	/**
	 * Number of ghosts eaten this level.
	 */
	private int mGhostEatenThisLevel;
	
	/**
	 * Number of dots remaining this level.
	 */
	private int mDotsRemaining;
	
	/**
	 * Number of dots total on this level.
	 */
	private int mDotsTotal;
	
	/**
	 * Number of lives remaining.
	 */
	private int mLives;
	
	/**
	 * Current score.
	 */
	private int mScore;
	
	/**
	 * Current level.
	 */
	private int mLevel;
	
	/**
	 * Whether or not a bonus life is allowed in the game.
	 */
    private boolean mIsBonusLifeAllowed;
    
    /**
     * Whether or not the bonus life has been given.
     */
    private boolean mIsBonusLifeGiven;
    
    /**
     * Dot foreground color
     */
    private final Paint mDotForeground;
    
    /**
     * Juggerdot foreground color.
     */
    private final Paint mJuggerdotForeground;
    
    /**
     * Color of the background.
     */
    private int mGameBackground;
    
    /**
     * Walls forground color.
     */
    private final Paint mWallsForeground;
    
    /**
     * Whether or not we are displaying icon walls
     */
    private boolean mIsDisplayingWalls;
    
    /**
     * Number of points at which the bonus life is given.
     */
    private int mBonusLifeThreshold;
    
    /**
     * Whether or not we are displaying the HUD.
     */
    private boolean mIsDisplayingHud;
    
    /**
     * Foreground HUD color.
     */
    private final Paint mHudForeground;
    
    /**
     * Foreground The Man color.
     */
    private final Paint mTheManForeground;
    
    /**
     * "Ready" text color.
     */
    private final Paint mReadyForeground;
    
    /**
     * "Game Over" text color.
     */
    private final Paint mGameOverForeground;
    
    /**
     * Top padding (in pixels) of the grid from the screen top.
     */
    private float mDotGridPaddingTop;
    
    /**
     * Left padding (in pixels) of the grid from the screen left.
     */
    private float mDotGridPaddingLeft;
    
    /**
     * Bottom padding (in pixels) of the grid from the screen bottom.
     */
    private float mDotGridPaddingBottom;
    
    /**
     * Right padding (in pixels) of the grid from the screen right.
     */
    private float mDotGridPaddingRight;
    
    /**
     * "Ready" localized string.
     */
    private final String mTextReady;
    
    /**
     * "Game Over" localized string.
     */
    private final String mTextGameOver;
    
    /**
     * Location of the "Ready" and "Game Over" text.
     */
    private final PointF mTextLocation;
    
    /**
     * Offset (in pixels) of the HUD from the bottom of the screen.
     */
    private int mHudOffset;
    
    /**
     * Number of ticks calculated so far.
     */
    private long mTickCount;
    
    /**
     * Number of frames to hold a juggerdot blink state before toggling it.
     */
    private int mJuggerdotBlinkInterval;
    
    /**
     * Length of the blink cycle of the juggerdots.
     */
    private int mJuggerdotBlinkLength;
    
    /**
     * Number of juggerdots remaining on the level.
     */
    private int mJuggerdotsRemaining;
    
    /**
     * Percentage of dots at which to create more for endless mode.
     */
    private int mEndlessDotThresholdPercent;
    
    /**
     * Number of juggerdots left at which to create more for endless mode.
     */
    private int mEndlessJuggerdotThreshold;
    
    /**
     * Path to the user background image (if any).
     */
    private String mBackgroundPath;
    
    /**
     * The user background image (if any).
     */
    private Bitmap mBackground;
    
    /**
     * The size (in pixels) of a single cell.
     */
    private final RectF mCellSize;
    
    /**
     * Bitmap of Andy.
     */
    private Bitmap mAndy;
    
    /**
     * Bitmap of the kill screen.
     */
    private Bitmap mKillScreen;
    
    /**
     * Set of fruits eaten this game.
     */
    private final HashSet<Fruit.Type> mFruitsEaten;
    
    /**
     * Bitmap sprites used for the dots.
     */
    private Bitmap mDotSprite;
    
    /**
     * Whether or not the Legend trophy is enabled.
     */
    private boolean mIsTrophyLegendEnabled;
    
    /**
     * Whether or not the Desserts trophy is enabled.
     */
    private boolean mIsTrophyDessertsEnabled;
    
    /**
     * The locations of widgets on the launcher.
     */
    private List<Rect> mWidgetLocations;
    
    /**
     * Precalculated wall postions;
     */
    private final List<RectF> mWalls;
    
    /**
     * Paint to draw the background color.
     */
    private final Paint mBackgroundPaint;
    
    /**
     * Size (in pixels) of the HUD.
     */
    private int mHudSize;
    
    
    
    /**
     * Create a new game.
     */
    public Game() {
    	if (Wallpaper.LOG_VERBOSE) {
    		Log.v(Game.TAG, "> Game()");
    	}
    	
    	final Resources resources = Wallpaper.CONTEXT.getResources();
    	
        //Create Paints
    	this.mWallsForeground = new Paint(Paint.ANTI_ALIAS_FLAG);
    	this.mWallsForeground.setStyle(Paint.Style.STROKE);
        this.mDotForeground = new Paint(Paint.ANTI_ALIAS_FLAG); 
        this.mJuggerdotForeground = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.mHudForeground = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.mTheManForeground = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.mReadyForeground = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.mGameOverForeground = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.mBackgroundPaint = new Paint();
        
        this.mTextReady = resources.getString(R.string.ready);
        this.mTextGameOver = resources.getString(R.string.gameover);
        this.mTextLocation = new PointF();
        
        this.mCellSize = new RectF(0, 0, 0, 0);
        
        this.mFruitsEaten = new HashSet<Fruit.Type>();
        this.mWidgetLocations = new LinkedList<Rect>();
        this.mWalls = new LinkedList<RectF>();
        
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

    
    
    /**
     * Handle the changing of a preference.
     */
	public void onSharedPreferenceChanged(final SharedPreferences preferences, final String key) {
    	if (Wallpaper.LOG_VERBOSE) {
    		Log.v(Game.TAG, "> onSharedPreferenceChanged()");
    	}
    	
		final boolean all = (key == null);
		final Resources resources = Wallpaper.CONTEXT.getResources();

		boolean hasBonusChanged = false;
        boolean hasGhostCountChanged = false;
		boolean hasLayoutChanged = false;
		boolean hasGraphicsChanged = false;
		boolean hasModeChanged = false;

		
		// GENERAL //
		
		final String mode = resources.getString(R.string.settings_game_mode_key);
		if (all || key.equals(mode)) {
			this.mMode = Game.Mode.parseInt(preferences.getInt(mode, resources.getInteger(R.integer.game_mode_default)));
			hasModeChanged = true;
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Mode: " + this.mMode);
			}
		}
		
		final String wrappingMode = resources.getString(R.string.settings_game_wrappingmode_key);
		if (all || key.equals(wrappingMode)) {
			this.mWrapping = Game.Wrapping.parseInt(preferences.getInt(wrappingMode, resources.getInteger(R.integer.game_wrappingmode_default)));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Wrapping: " + this.mWrapping);
			}
		}
		
		final String wrappingTheMan = resources.getString(R.string.settings_game_wrappingtheman_key);
		if (all || key.equals(wrappingTheMan)) {
			this.mIsWrappingTheMan = preferences.getBoolean(wrappingTheMan, resources.getBoolean(R.bool.game_wrappingtheman_default));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Is Wrapping The Man: " + this.mIsWrappingTheMan);
			}
		}
		
		final String wrappingGhosts = resources.getString(R.string.settings_game_wrappingghosts_key);
		if (all || key.equals(wrappingGhosts)) {
			this.mIsWrappingGhosts = preferences.getBoolean(wrappingGhosts, resources.getBoolean(R.bool.game_wrappingghosts_default));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Is Wrapping Ghosts: " + this.mIsWrappingGhosts);
			}
		}
		
		final String endlessDotThreshold = resources.getString(R.string.settings_game_endlessdotregen_key);
		if (all || key.equals(endlessDotThreshold)) {
			this.mEndlessDotThresholdPercent = preferences.getInt(endlessDotThreshold, resources.getInteger(R.integer.game_endlessdotregen_default));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Endless Dot Threshold (%): " + this.mEndlessDotThresholdPercent);
			}
		}
		
		final String endlessJuggerdotThreshold = resources.getString(R.string.settings_game_endlessjuggerdotregen_key);
		if (all || key.equals(endlessJuggerdotThreshold)) {
			this.mEndlessJuggerdotThreshold = preferences.getInt(endlessJuggerdotThreshold, resources.getInteger(R.integer.game_endlessjuggerdotregen_default));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Endless Juggerdot Threshold: " + this.mEndlessJuggerdotThreshold);
			}
		}
		
		final String juggerdotBlink = resources.getString(R.string.settings_display_juggerdotblink_key);
		if (all || key.equals(juggerdotBlink)) {
			this.mJuggerdotBlinkInterval = preferences.getInt(juggerdotBlink, resources.getInteger(R.integer.display_juggerdotblink_default));
			this.mJuggerdotBlinkLength = this.mJuggerdotBlinkInterval * 2;
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Juggerdot Blink: " + this.mJuggerdotBlinkInterval);
			}
		}
		
		final String showWalls = resources.getString(R.string.settings_display_showwalls_key);
		if (all || key.equals(showWalls)) {
			this.mIsDisplayingWalls = preferences.getBoolean(showWalls, resources.getBoolean(R.bool.display_showwalls_default));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Is Displaying Walls: " + this.mIsDisplayingWalls);
			}
		}
		
		final String bonusAllowed = resources.getString(R.string.settings_game_bonuslife_key);
		if (all || key.equals(bonusAllowed)) {
			this.mIsBonusLifeAllowed = preferences.getBoolean(bonusAllowed, resources.getBoolean(R.bool.game_bonuslife_default));
			hasBonusChanged = true;
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Bonus Allowed: " + this.mIsBonusLifeAllowed);
			}
		}
        
		final String bonusThreshold = resources.getString(R.string.settings_game_bonuslifethreshold_key);
		if (all || key.equals(bonusThreshold)) {
			this.mBonusLifeThreshold = preferences.getInt(bonusThreshold, resources.getInteger(R.integer.game_bonuslifethreshold_default));
			hasBonusChanged = true;
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Bonus Threshold: " + this.mBonusLifeThreshold);
			}
		}
		
		final String fruitEnabled = resources.getString(R.string.settings_game_fruitenabled_key);
		if (all || key.equals(fruitEnabled)) {
			this.mIsFruitEnabled = preferences.getBoolean(fruitEnabled, resources.getBoolean(R.bool.game_fruitenabled_default));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Is Fruit Enabled: " + this.mIsFruitEnabled);
			}
		}
        
        final String killScreen = resources.getString(R.string.settings_game_killscreen_key);
        if (all || key.equals(killScreen)) {
        	this.mIsKillScreenEnabled = preferences.getBoolean(killScreen, resources.getBoolean(R.bool.game_killscreen_default));
        	
        	if (Wallpaper.LOG_DEBUG) {
        		Log.d(Game.TAG, "Is Kill Screen Enabled: " + this.mIsKillScreenEnabled);
        	}
        }
        
        final String ghostsDeadly = resources.getString(R.string.settings_game_deadlyghosts_key);
        if (all || key.equals(ghostsDeadly)) {
        	this.mIsGhostDeadly = preferences.getBoolean(ghostsDeadly, resources.getBoolean(R.bool.game_deadlyghosts_default));
        	
        	if (Wallpaper.LOG_DEBUG) {
        		Log.d(Game.TAG, "Is Ghost Deadly: " + this.mIsGhostDeadly);
        	}
        }
        
        int ghosts = 0;
        final String ghostCount = resources.getString(R.string.settings_game_ghostcount_key);
        if (all || key.equals(ghostCount)) {
        	ghosts = preferences.getInt(ghostCount, resources.getInteger(R.integer.game_ghostcount_default));
        	hasGhostCountChanged = true;
        	
        	if (Wallpaper.LOG_DEBUG) {
        		Log.d(Game.TAG, "Ghost Count: " + ghosts);
        	}
        }

    	if (hasGhostCountChanged) {
	    	this.mGhosts = new Ghost[ghosts];
	    	int i = 0;
	    	if (ghosts > i) { this.mGhosts[i++] = new Ghost.Blinky(); } //Blink MUST be first for Inky to properly calculate moves
	    	if (ghosts > i) { this.mGhosts[i++] = new Ghost.Clyde(); }
	    	if (ghosts > i) { this.mGhosts[i++] = new Ghost.Pinky(); }
	    	if (ghosts > i) { this.mGhosts[i++] = new Ghost.Inky(); }
	    	
	    	for (final Ghost ghost : this.mGhosts) {
	    		ghost.performResize(this);
	    	}
    	}
		
		final String displayHud = resources.getString(R.string.settings_display_showhud_key);
		if (all || key.equals(displayHud)) {
			this.mIsDisplayingHud = preferences.getBoolean(displayHud, resources.getBoolean(R.bool.display_showhud_default));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Is Displaying HUD: " + this.mIsDisplayingHud);
			}
		}
		
		final String hudSize = resources.getString(R.string.settings_display_hudsize_key);
		if (all || key.equals(hudSize)) {
			this.mHudSize = preferences.getInt(hudSize, resources.getInteger(R.integer.display_hudsize_default));
			this.mHudForeground.setTextSize(this.mHudSize);
			this.mReadyForeground.setTextSize(this.mHudSize);
			this.mGameOverForeground.setTextSize(this.mHudSize);
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "HUD Size: " + this.mHudSize);
			}
		}
		
		final String hudOffset = resources.getString(R.string.settings_display_hudoffset_key);
		if (all || key.equals(hudOffset)) {
			this.mHudOffset = preferences.getInt(hudOffset, resources.getInteger(R.integer.display_hudoffset_default));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "HUD Offset: " + this.mHudOffset);
			}
		}
		
		final String widgetLocations = resources.getString(R.string.settings_display_widgetlocations_key);
		if (all || key.equals(widgetLocations)) {
			this.mWidgetLocations = WidgetLocationsPreference.convertStringToWidgetList(preferences.getString(widgetLocations, resources.getString(R.string.display_widgetlocations_default)));
			hasLayoutChanged = true;
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Widget Locations: " + (this.mWidgetLocations.size() / 4));
			}
		}
		
		
		// COLORS //
        
		final String gameBackground = resources.getString(R.string.settings_color_game_background_key);
		if (all || key.equals(gameBackground)) {
			this.mGameBackground = preferences.getInt(gameBackground, resources.getInteger(R.integer.color_game_background_default));
	        
	        //Add background to text overlays after background is loaded
	        this.mReadyForeground.setShadowLayer(2, 0, 0, this.mGameBackground);
	        this.mGameOverForeground.setShadowLayer(2, 0, 0, this.mGameBackground);
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Background: #" + Integer.toHexString(this.mGameBackground));
			}
		}
		
		final String wallsForeground = resources.getString(R.string.settings_color_game_walls_key);
		if (all || key.equals(wallsForeground)) {
			this.mWallsForeground.setColor(preferences.getInt(wallsForeground, resources.getInteger(R.integer.color_game_walls_default)));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Walls Foreground: #" + Integer.toHexString(this.mWallsForeground.getColor()));
			}
		}
		
		final String backgroundImage = resources.getString(R.string.settings_color_game_bgimage_key);
		if (all || key.equals(backgroundImage)) {
			this.mBackgroundPath = preferences.getString(backgroundImage, null);
			
			if (this.mBackgroundPath != null) {			
				if (Wallpaper.LOG_DEBUG) {
					Log.d(Game.TAG, "Background Image: " + this.mBackgroundPath);
				}
				
				//Trigger performResize
				hasGraphicsChanged = true;
			} else {
				this.mBackground = null;
			}
		}
		
		final String backgroundOpacity = resources.getString(R.string.settings_color_game_bgopacity_key);
		if (all || key.equals(backgroundOpacity)) {
			this.mBackgroundPaint.setAlpha(preferences.getInt(backgroundOpacity, resources.getInteger(R.integer.color_game_bgopacity_default)));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Background Image Opacity: " + this.mBackgroundPaint.getAlpha());
			}
		}
        
		final String dot = resources.getString(R.string.settings_color_game_dot_key);
		if (all || key.equals(dot)) {
			this.mDotForeground.setColor(preferences.getInt(dot, resources.getInteger(R.integer.color_game_dot_default)));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Dot Foreground: #" + Integer.toHexString(this.mDotForeground.getColor()));
			}
		}
        
		final String juggerdot = resources.getString(R.string.settings_color_game_juggerdot_key);
		if (all || key.equals(juggerdot)) {
			this.mJuggerdotForeground.setColor(preferences.getInt(juggerdot, resources.getInteger(R.integer.color_game_juggerdot_default)));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Juggerdot Foreground: #" + Integer.toHexString(this.mJuggerdotForeground.getColor()));
			}
		}
		
		final String hudFg = resources.getString(R.string.settings_color_game_hudfg_key);
		if (all || key.equals(hudFg)) {
			this.mHudForeground.setColor(preferences.getInt(hudFg, resources.getInteger(R.integer.color_game_hudfg_default)));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "HUD Foreground: #" + Integer.toHexString(this.mHudForeground.getColor()));
			}
		}
        
		final String hudBg = resources.getString(R.string.settings_color_game_hudbg_key);
		if (all || key.equals(hudBg)) {
			final int hudBgColor = preferences.getInt(hudBg, resources.getInteger(R.integer.color_game_hudbg_default));
			this.mHudForeground.setShadowLayer(1, -1, 1, hudBgColor);
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "HUD Background: #" + Integer.toHexString(hudBgColor));
			}
		}
		
		final String foregroundColor = resources.getString(R.string.settings_color_theman_key);
		if (all || key.equals(foregroundColor)) {
			this.mTheManForeground.setColor(preferences.getInt(foregroundColor, resources.getInteger(R.integer.color_theman_default)));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "TheMan Color: #" + Integer.toHexString(this.mTheManForeground.getColor()));
			}
		}
		
		final String ready = resources.getString(R.string.settings_color_game_ready_key);
		if (all || key.equals(ready)) {
			this.mReadyForeground.setColor(preferences.getInt(ready, resources.getInteger(R.integer.color_game_ready_default)));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Ready Color: #" + Integer.toHexString(this.mReadyForeground.getColor()));
			}
		}
		
		final String gameOver = resources.getString(R.string.settings_color_game_gameover_key);
		if (all || key.equals(gameOver)) {
			this.mGameOverForeground.setColor(preferences.getInt(gameOver, resources.getInteger(R.integer.color_game_gameover_default)));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Ready Color: #" + Integer.toHexString(this.mGameOverForeground.getColor()));
			}
		}
		
		final String color_style = Wallpaper.CONTEXT.getString(R.string.settings_color_dotstyle_key);
		if (all || key.equals(color_style)) {
			final Entity.Style style = Entity.Style.parseInt(preferences.getInt(color_style, resources.getInteger(R.integer.color_dotstyle_default)));
			this.mDotForeground.setStyle(style.style);
			this.mJuggerdotForeground.setStyle(style.style);
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Drawing Style: " + style);
			}
		}
		final String entityStyle = Wallpaper.CONTEXT.getString(R.string.settings_color_entitystyle_key);
		if (all || key.equals(entityStyle)) {
			final Entity.Style style = Entity.Style.parseInt(preferences.getInt(entityStyle, resources.getInteger(R.integer.color_entitystyle_default)));
			
			this.mTheManForeground.setStyle(style.style);
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "TheMan HUD Style: " + style);
			}
		}
		
		
		// TROPHY //
		
		final String trophyAppleDots = resources.getString(R.string.trophy_appledots_key);
		if (all || key.equals(trophyAppleDots)) {
			if (preferences.getBoolean(trophyAppleDots, resources.getBoolean(R.bool.trophy_appledots_default))) {
				this.mDots = Game.Dots.APPLES;
				
				//Load the apple sprite
				final BitmapFactory.Options options = new BitmapFactory.Options();
				options.inScaled = false;
				this.mDotSprite = BitmapFactory.decodeResource(Wallpaper.CONTEXT.getResources(), R.drawable.apple, options);
			} else {
				this.mDots = Game.Dots.CIRCLES;
				this.mDotSprite = null;
			}
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Dot Drawing Mode: " + this.mDots);
			}
		}
		
		final String trophyAndy = resources.getString(R.string.trophy_andy_key);
		if (all || key.equals(trophyAndy)) {
			if (preferences.getBoolean(trophyAndy, resources.getBoolean(R.bool.trophy_andy_default))) {
				//Load the Andy sprite
				final BitmapFactory.Options options = new BitmapFactory.Options();
				options.inScaled = false;
				this.mAndy = BitmapFactory.decodeResource(Wallpaper.CONTEXT.getResources(), R.drawable.andy, options);
			} else {
				this.mAndy = null;
			}
		}
		
		final String trophyTheMandroid = resources.getString(R.string.trophy_themandroid_key);
		if (all || key.equals(trophyTheMandroid)) {
			if (preferences.getBoolean(trophyTheMandroid, resources.getBoolean(R.bool.trophy_themandroid_default))) {
				this.mTheManForeground.setColor(TheMan.THE_MANDROID_FOREGROUND);
			} else {
				this.mTheManForeground.setColor(preferences.getInt(resources.getString(R.string.settings_color_theman_key), resources.getInteger(R.integer.color_theman_default)));
			}
		}
		
		final String trophyLegend = resources.getString(R.string.trophy_legend_key);
		if (all || key.equals(trophyLegend)) {
			this.mIsTrophyLegendEnabled = preferences.getBoolean(trophyLegend, resources.getBoolean(R.bool.trophy_legend_default));
		}
		
		final String trophyDesserts = resources.getString(R.string.trophy_desserts_key);
		if (all || key.equals(trophyDesserts)) {
			this.mIsTrophyDessertsEnabled = preferences.getBoolean(trophyDesserts, resources.getBoolean(R.bool.trophy_desserts_default));
		}
    	
        
		// GRID //
		
		final String dotGridPaddingLeft = resources.getString(R.string.settings_display_padding_left_key);
		if (all || key.equals(dotGridPaddingLeft)) {
			this.mDotGridPaddingLeft = preferences.getInt(dotGridPaddingLeft, resources.getInteger(R.integer.display_padding_left_default));
			hasGraphicsChanged = true;
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Dot Grid Padding Left: " + this.mDotGridPaddingLeft);
			}
		}

		final String dotGridPaddingRight = resources.getString(R.string.settings_display_padding_right_key);
		if (all || key.equals(dotGridPaddingRight)) {
			this.mDotGridPaddingRight = preferences.getInt(dotGridPaddingRight, resources.getInteger(R.integer.display_padding_right_default));
			hasGraphicsChanged = true;
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Dot Grid Padding Right: " + this.mDotGridPaddingRight);
			}
		}

		final String dotGridPaddingTop = resources.getString(R.string.settings_display_padding_top_key);
		if (all || key.equals(dotGridPaddingTop)) {
			this.mDotGridPaddingTop = preferences.getInt(dotGridPaddingTop, resources.getInteger(R.integer.display_padding_top_default));
			hasGraphicsChanged = true;
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Dot Grid Padding Top: " + this.mDotGridPaddingTop);
			}
		}

		final String dotGridPaddingBottom = resources.getString(R.string.settings_display_padding_bottom_key);
		if (all || key.equals(dotGridPaddingBottom)) {
			this.mDotGridPaddingBottom = preferences.getInt(dotGridPaddingBottom, resources.getInteger(R.integer.display_padding_bottom_default));
			hasGraphicsChanged = true;
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Dot Grid Padding Bottom: " + this.mDotGridPaddingBottom);
			}
		}
		
		
		// CELLS //
		
		final String iconRows = resources.getString(R.string.settings_display_iconrows_key);
		if (all || key.equals(iconRows)) {
			this.mIconRows = preferences.getInt(iconRows, resources.getInteger(R.integer.display_iconrows_default));
			hasLayoutChanged = true;
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Icon Rows: " + this.mIconRows);
			}
		}
		
		final String iconCols = resources.getString(R.string.settings_display_iconcols_key);
		if (all || key.equals(iconCols)) {
			this.mIconCols = preferences.getInt(iconCols, resources.getInteger(R.integer.display_iconcols_default));
			hasLayoutChanged = true;
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Icon Cols: " + this.mIconCols);
			}
		}
		
		final String cellSpacingRow = resources.getString(R.string.settings_display_rowspacing_key);
		if (all || key.equals(cellSpacingRow)) {
			this.mCellRowSpacing = preferences.getInt(cellSpacingRow, resources.getInteger(R.integer.display_rowspacing_default));
			hasLayoutChanged = true;
			
			if (Wallpaper.LOG_DEBUG) {
		    	Log.d(Game.TAG, "Cell Row Spacing: " + this.mCellRowSpacing);
			}
		}
		
		final String cellSpacingCol = resources.getString(R.string.settings_display_colspacing_key);
		if (all || key.equals(cellSpacingCol)) {
			this.mCellColumnSpacing = preferences.getInt(cellSpacingCol, resources.getInteger(R.integer.display_colspacing_default));
			hasLayoutChanged = true;
			
			if (Wallpaper.LOG_DEBUG) {
		    	Log.d(Game.TAG, "Cell Column Spacing: " + this.mCellColumnSpacing);
			}
		}
		
		if (hasLayoutChanged) {
	    	this.mCellsWide = (this.mIconCols * (mCellColumnSpacing + 1)) + 1;
	    	this.mCellsTall = (this.mIconRows * (mCellRowSpacing + 1)) + 1;
	    	
	    	if (Wallpaper.LOG_DEBUG) {
	    		Log.d(Game.TAG, "Cells Wide: " + this.mCellsWide);
	    		Log.d(Game.TAG, "Cells Tall: " + this.mCellsTall);
	    	}
	    	
	    	//Create playing board
	        this.mBoard = new Cell[this.mCellsTall][this.mCellsWide];
		}
		if ((hasLayoutChanged || hasGraphicsChanged) && (this.mScreenWidth > 0) && (this.mScreenHeight > 0)) {
	        //Resize everything to fit
	        this.performResize(this.mScreenWidth, this.mScreenHeight);
		}
		
		
		//Check to see if we need a new game
		if (hasBonusChanged || hasGhostCountChanged || hasLayoutChanged || hasModeChanged) {
	    	this.newGame();
		}

    	if (Wallpaper.LOG_VERBOSE) {
    		Log.v(Game.TAG, "< onSharedPreferenceChanged()");
    	}
	}
    
    /**
     * Get the Cell value for a specific coordinate.
     * 
     * @param position Point
     * @return Cell value.
     */
    public Game.Cell getCell(final Point position) {
    	return this.mBoard[position.y][position.x];
    }
    
    /**
     * Sets the cell value for a specific position.
     * 
     * @param position Position to change.
     * @param newCell New cell value.
     */
    public void setCell(final Point position, final Game.Cell newCell) {
    	this.mBoard[position.y][position.x] = newCell; 
    }
    
    /**
     * Get the game's instance of The Man.
     * 
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
     * 
     * @return Number of dots eaten.
     */
    public int getDotsEaten() {
    	return this.mDotsTotal - this.mDotsRemaining;
    }
    
    /**
     * Get the current level number.
     * 
     * @return Level number.
     */
    public int getLevel() {
    	return this.mLevel;
    }
    
    /**
     * Get the width of a cell in pixels.
     * 
     * @return Cell width.
     */
    public float getCellWidth() {
    	return this.mCellWidth;
    }
    
    /**
     * Get the height of a cell in pixels.
     * 
     * @return Cell height.
     */
    public float getCellHeight() {
    	return this.mCellHeight;
    }
    
    /**
     * Get the board's number of cells horizontally.
     * 
     * @return Number of cells.
     */
    public int getCellsWide() {
    	return this.mCellsWide;
    }
    
    /**
     * Get the board's number of cells vertically.
     * 
     * @return Number of cells.
     */
    public int getCellsTall() {
    	return this.mCellsTall;
    }
    
    /**
     * Get the board's number of cells between two columns.
     * 
     * @return Number of cells.
     */
    public int getCellColumnSpacing() {
    	return this.mCellColumnSpacing;
    }
    
    /**
     * Get the board's number of cells between two rows.
     * 
     * @return Number of cells.
     */
    public int getCellRowSpacing() {
    	return this.mCellRowSpacing;
    }
    
    /**
     * Get the number of icon rows on the home screen.
     * 
     * @return Number of icons.
     */
    public int getIconRows() {
    	return this.mIconRows;
    }
    
    /**
     * Get the number of icon columns on the home screen.
     * 
     * @return Number of icons.
     */
    public int getIconCols() {
    	return this.mIconCols;
    }
    
    /**
     * Get whether or not the device is in landscape mode.
     * @return Boolean
     */
    public boolean getIsLandscape() {
    	return this.mIsLandscape;
    }
    
    /**
     * Get a RectF which represents the current cell size of the board.
     * @return RectF of cell size.
     */
    public RectF getCellSize() {
    	return this.mCellSize;
    }
    
    /**
     * Test if a Point is a valid coordinate on the game board for an entity.
     * 
     * @param entity Entity for whom to check.
     * @param position Point representing coordinate.
     * @return Boolean indicating whether or not the position is valid.
     */
    public boolean isValidPosition(final Entity entity, final Point position) {
    	if (((entity instanceof TheMan) && this.mIsWrappingTheMan) || ((entity instanceof Ghost) && this.mIsWrappingGhosts)) {
    		//wrap past bounds positively and negatively
    		if ((this.mWrapping == Game.Wrapping.ALL) || (position.y == ((this.mIconRows / 2) * (this.mCellRowSpacing + 1)))) {
	    		if (position.x < 0) {
	    			position.x = this.mCellsWide + position.x;
	    		} else {
	    			position.x %= this.mCellsWide;
	    		}
    		}
    		if (this.mWrapping == Game.Wrapping.ALL) {
	    		if (position.y < 0) {
	    			position.y = this.mCellsTall + position.y;
	    		} else {
	    			position.y %= this.mCellsTall;
	    		}
    		}
    	}
    	
    	return this.isValidBoardPosition(position);
    }
    
    /**
     * Test if a Point is a valid coordinate on the game board.
     * 
     * @param position Point representing coordinate.
     * @return Boolean indicating whether or not the position is valid.
     */
    public boolean isValidBoardPosition(final Point position) {
    	return ((position.x >= 0) && (position.x < this.mCellsWide)
    		    && (position.y >= 0) && (position.y < this.mCellsTall)
    		    && (this.mBoard[position.y][position.x] != Cell.WALL));
    }
    
    /**
     * Test if a Point is an intersection on the game board.
     * 
     * @param position Point representing coordinate.
     * @return Boolean indicating whether or not the position is in an intersection.
     */
    public boolean isIntersection(final Point position) {
    	int directions = 0;
    	for (final Entity.Direction direction : Entity.Direction.values()) {
    		if (this.isValidBoardPosition(Entity.move(position, direction))) {
    			directions += 1;
    		}
    	}
    	
    	//TODO: this is wrong. should be >2 but then the nextRandomDirection methods fail on a corner
    	return (directions > 1);
    }
    
    /**
     * Get a ghost at a certain position or null.
     * 
     * @param position Position to check.
     * @return The ghost at that position or null.
     */
    public Ghost getGhostAtPosition(final Point position) {
    	for (final Ghost ghost : this.mGhosts) {
    		if ((ghost.getPosition().x == position.x) && (ghost.getPosition().y == position.y)) {
    			return ghost;
    		}
    	}
    	return null;
    }
    
    /**
     * Return a fruit at the specified position or null.
     * 
     * @param position Position to check.
     * @return The fruit or null.
     */
    public Fruit getFruitAtPosition(final Point position) {
    	if ((this.mFruit.getPosition().x == position.x) && (this.mFruit.getPosition().y == position.y) && this.mFruit.isVisible()) {
    		return this.mFruit;
    	}
    	return null;
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
     * 
     * @param amount Amount to add.
     */
    private void addToScore(final int amount) {
    	this.mScore += amount;
    	
    	//Check bonus life
    	if (this.mIsBonusLifeAllowed && !this.mIsBonusLifeGiven && (this.mScore > this.mBonusLifeThreshold)) {
    		this.mIsBonusLifeGiven = true;
    		this.mLives += 1;
    	}
    	
    	//Check for Googol trophy
    	if (this.mScore >= Game.TROPHY_GOOGOL_THRESHOLD) {
    		this.earnTrophyGoogol();
    	}
    	
    	//Check for Legend trophy
    	if (this.mScore >= Game.TROPHY_LEGEND_THRESHOLD) {
    		this.earnTrophyLegend();
    	}
    }
    
    /**
     * Display a standard notification when a trophy has been earned
     * 
     * @param trophyTitle Title of the trophy.
     */
    private void showNotification(final String trophyTitle) {
    	final Resources resources = Wallpaper.CONTEXT.getResources();
    	final String text = resources.getString(R.string.trophy_notification_text);
    	final String title = resources.getString(R.string.trophy_notification_title) + ": " + trophyTitle;
    	final NotificationManager manager = (NotificationManager)Wallpaper.CONTEXT.getSystemService(Context.NOTIFICATION_SERVICE);
    	
    	final Notification notification = new Notification(R.drawable.notification, title, System.currentTimeMillis());
    	notification.flags |= Notification.FLAG_AUTO_CANCEL;
    	
    	final Intent intent = new Intent(Wallpaper.CONTEXT, Preferences.class);
    	intent.putExtra(Preferences.EXTRA_TROPHY, true);
    	final PendingIntent pendingIntent = PendingIntent.getActivity(Wallpaper.CONTEXT, 0, intent, 0);
    	
    	notification.setLatestEventInfo(Wallpaper.CONTEXT, title, text, pendingIntent);
    	manager.notify(0, notification);
    }
    
    /**
     * Check if a trophy has been earned
     * 
     * @param persistString Resource for the preference key that persists the boolean of whether or not the trophy has been earned
     * @param defaultBool Resource for the default boolean for the trophy
     * @param titleString Resource for the title of the trophy
     */
    private void earnTrophy(final int persistString, final int defaultBool, final int titleString) {
    	final Resources resources = Wallpaper.CONTEXT.getResources();
    	final String key = resources.getString(persistString);
    	
    	if (!Wallpaper.PREFERENCES.getBoolean(key, resources.getBoolean(defaultBool))) {
    		//If we haven't earned this yet, store it
    		Wallpaper.PREFERENCES.edit().putBoolean(key, true).commit();
    		this.showNotification(resources.getString(titleString));
    		
    		//Check if all trophies have been earned
    		final boolean trophyAndy = Wallpaper.PREFERENCES.getBoolean(resources.getString(R.string.trophy_andy_persist), resources.getBoolean(R.bool.trophy_andy_default));
    		final boolean trophyTheMandroid = Wallpaper.PREFERENCES.getBoolean(resources.getString(R.string.trophy_themandroid_persist), resources.getBoolean(R.bool.trophy_themandroid_default));
    		final boolean trophyLogos = Wallpaper.PREFERENCES.getBoolean(resources.getString(R.string.trophy_logos_persist), resources.getBoolean(R.bool.trophy_logos_default));
    		final boolean trophyCeos = Wallpaper.PREFERENCES.getBoolean(resources.getString(R.string.trophy_ceos_persist), resources.getBoolean(R.bool.trophy_ceos_default));
    		final boolean trophyDesserts = Wallpaper.PREFERENCES.getBoolean(resources.getString(R.string.trophy_desserts_persist), resources.getBoolean(R.bool.trophy_desserts_default));
    		final boolean trophyAppleDots = Wallpaper.PREFERENCES.getBoolean(resources.getString(R.string.trophy_appledots_persist), resources.getBoolean(R.bool.trophy_appledots_default));
    		final boolean trophyEden = Wallpaper.PREFERENCES.getBoolean(resources.getString(R.string.trophy_eden_persist), resources.getBoolean(R.bool.trophy_eden_default));
    		final boolean trophyGoogol = Wallpaper.PREFERENCES.getBoolean(resources.getString(R.string.trophy_googol_persist), resources.getBoolean(R.bool.trophy_googol_default));
    		final boolean trophyLegend = Wallpaper.PREFERENCES.getBoolean(resources.getString(R.string.trophy_legend_persist), resources.getBoolean(R.bool.trophy_legend_default));
    		if (trophyAndy && trophyTheMandroid && trophyLogos && trophyCeos && trophyDesserts && trophyAppleDots && trophyEden && trophyGoogol && trophyLegend) {
    			Wallpaper.PREFERENCES.edit().putBoolean(resources.getString(R.string.trophy_ego_persist), true).commit();
    			this.showNotification(resources.getString(R.string.trophy_ego));
    		}
    	}
    }
    
    /**
     * Mark the Andy trophy as earned
     */
    private void earnTrophyAndy() {
    	this.earnTrophy(R.string.trophy_andy_persist, R.bool.trophy_andy_default, R.string.trophy_andy);
    }
    
    /**
     * Mark the The Mandroid trophy as earned
     */
    private void earnTrophyTheMandroid() {
    	this.earnTrophy(R.string.trophy_themandroid_persist, R.bool.trophy_themandroid_default, R.string.trophy_themandroid);
    }
    
    /**
     * Mark the Logos trophy as earned
     */
    private void earnTrophyLogos() {
    	this.earnTrophy(R.string.trophy_logos_persist, R.bool.trophy_logos_default, R.string.trophy_logos);
    }
    
    /**
     * Mark the CEOs trophy as earned
     */
    private void earnTrophyCeos() {
    	this.earnTrophy(R.string.trophy_ceos_persist, R.bool.trophy_ceos_default, R.string.trophy_ceos);
    }
    
    /**
     * Mark the Desserts trophy as earned
     */
    private void earnTrophyDesserts() {
    	this.earnTrophy(R.string.trophy_desserts_persist, R.bool.trophy_desserts_default, R.string.trophy_desserts);
    }
    
    /**
     * Mark the Apple Dots trophy as earned
     */
    private void earnTrophyAppleDots() {
    	this.earnTrophy(R.string.trophy_appledots_persist, R.bool.trophy_appledots_default, R.string.trophy_appledots);
    }
    
    /**
     * Mark the Eden trophy as earned
     */
    private void earnTrophyEden() {
    	this.earnTrophy(R.string.trophy_eden_persist, R.bool.trophy_eden_default, R.string.trophy_eden);
    }
    
    /**
     * Mark the Googol trophy as earned
     */
    private void earnTrophyGoogol() {
    	this.earnTrophy(R.string.trophy_googol_persist, R.bool.trophy_googol_default, R.string.trophy_googol);
    }
    
    /**
     * Mark the Legend trophy as earned
     */
    private void earnTrophyLegend() {
    	this.earnTrophy(R.string.trophy_legend_persist, R.bool.trophy_legend_default, R.string.trophy_legend);
    }
    
    /**
     * See if the player has lost a life.
     * 
     * @return Boolean.
     */
    private boolean hasNotLostLife() {
    	return (!this.mIsBonusLifeGiven && (this.mLives == Game.INITIAL_LIVES)) || (this.mIsBonusLifeGiven && (this.mLives == (Game.INITIAL_LIVES + 1)));
    }
    
    /**
     * Check to see if The Man has eaten a dot or juggerdot.
     */
    public void checkDots() {
    	final Game.Cell cell = this.getCell(this.mTheMan.getPosition());
    	if (cell == Cell.DOT) {
    		this.mDotsRemaining -= 1;
    		this.addToScore(cell.value);
    		
    		//Blank cell since we've eaten the dot
    		this.setCell(this.mTheMan.getPosition(), Cell.BLANK);
        	
        	if ((this.mMode == Game.Mode.ENDLESS) && (this.mDotsRemaining < (this.mDotsTotal * this.mEndlessDotThresholdPercent / 100.0f))) {
        		//regen dot randomly
        		this.setCell(this.getRandomBlankCell(), Game.Cell.DOT);
        		this.mDotsRemaining += 1;
        	}
    	} else if (cell == Cell.JUGGERDOT) {
    		this.mJuggerdotsRemaining -= 1;
    		this.addToScore(cell.value);
    		this.switchGhostsState(Ghost.State.FRIGHTENED);
    		
    		//Blank cell since we've eaten the dot
    		this.setCell(this.mTheMan.getPosition(), Cell.BLANK);
        	
        	if ((this.mMode == Game.Mode.ENDLESS) && (this.mJuggerdotsRemaining < this.mEndlessJuggerdotThreshold)) {
        		//regen juggerdot randomly
        		this.setCell(this.getRandomBlankCell(), Game.Cell.JUGGERDOT);
        		this.mJuggerdotsRemaining += 1;
        	}
    	}
    }
    
    /**
     * Find a random blank and valid cell position on the board
     * 
     * @return Blank cell position
     */
    private Point getRandomBlankCell() {
    	while (true) {
    		final Point cell = new Point(Game.RANDOM.nextInt(this.mCellsWide), Game.RANDOM.nextInt(this.mCellsTall));
    		if (this.isValidBoardPosition(cell) && (this.getCell(cell) == Game.Cell.BLANK)) {
				return cell;
			}
    	}
    }
    
    /**
     * Check to see if The Man has eaten the fruit.
     */
    public void checkFruit() {
    	if (this.mTheMan.isCollidingWith(this.mFruit) && this.mFruit.isVisible()) {
    		//Check for Apple Dots trophy
    		if (this.mFruit.getType() == Fruit.Type.APPLE) {
    			this.earnTrophyAppleDots();
    		}
    		
    		//Check for Eden trophy
    		if (this.mFruitsEaten.add(this.mFruit.getType()) && (this.mFruitsEaten.size() == Fruit.Type.values().length)) {
    			this.earnTrophyEden();
    		}
    		
    		//Check for Desserts trophy
    		if (this.mFleeingGhostsEaten > 0) {
    			//we've eaten at least one ghost on the last juggerdot
    			for (final Ghost ghost : this.mGhosts) {
    				if (ghost.getState() == Ghost.State.FRIGHTENED) {
    					//there is still at least one frightened ghost
        				this.earnTrophyDesserts();
        				break;
    				}
    			}
    		}
    		
    		//eat the fruit
    		this.addToScore(this.mFruit.eat());
    	}
    }
    
    /**
     * Check to see if The Man has collided with a ghost.
     */
    public void checkGhosts() {
    	for (final Ghost ghost : this.mGhosts) {
    		if (this.mTheMan.isCollidingWith(ghost)) {
    			switch (ghost.getState()) {
					case HUNTING:
				    	if (this.mIsGhostDeadly && (this.mTheMan.getState() == TheMan.State.ALIVE)) {
				    		//Kill "The Man"
				    		this.mLives -= 1;
				    		this.mTheMan.setState(TheMan.State.DEAD);
				    		this.setState(Game.State.DYING);
				    	}
						break;
	
					case FRIGHTENED:
						//Eat ghost
						if (!this.mIsTrophyDessertsEnabled) {
							this.addToScore(Game.POINTS_FLEEING_GHOSTS[this.mFleeingGhostsEaten]);
							this.mFleeingGhostsEaten += 1;
							this.mGhostEatenThisLevel += 1;
						}
						
						ghost.setState(this, Ghost.State.EATEN);
						
						//See if we have eaten all the ghosts for this juggerdot
						if (this.mFleeingGhostsEaten == this.mGhosts.length) {
							this.mAllFleeingGhostsEaten += 1;
							
							//Check for Andy trophy
							if (this.hasNotLostLife()) {
								this.earnTrophyAndy();
							}
							
							//See if we have eaten all the ghosts for every juggerdot
							if (this.mAllFleeingGhostsEaten == Game.NUMBER_OF_JUGGERDOTS) {
								this.addToScore(Game.POINTS_ALL_FLEEING_GHOSTS);
							}
						}
						
						//Check for Logos trophy
						if ((this.mGhostEatenThisLevel >= Game.TROPHY_LOGOS_THRESHOLD) && this.hasNotLostLife()) {
							this.earnTrophyLogos();
						}
						
						break;
				}
    		}
    	}
    }
    
    /**
     * Switch the current state of the ghosts.
     * 
     * @param state New state.
     */
    private void switchGhostsState(final Ghost.State state) {
    	for (final Ghost ghost : this.mGhosts) {
    		ghost.setState(this, state);
    	}
    	if (state == Ghost.State.FRIGHTENED) {
    		this.mFleeingGhostsEaten = 0;
    	}
    }
    
    /**
     * Switch the current state of the game.
     * 
     * @param state New state.
     */
    private void setState(final Game.State state) {
    	this.mState = state;
    	this.mStateTimestamp = System.currentTimeMillis();
    	
    	if (Wallpaper.LOG_DEBUG) {
    		Log.d(Game.TAG, "Changing game state to " + state);
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
		this.mLives = Game.INITIAL_LIVES;
		this.mScore = 0;
		this.mLevel = 0; //changed to 1 in newLevel
		this.mIsBonusLifeGiven = false;
        this.mIsOnKillScreen = false;
        this.mTickCount = 0;
        this.mFruitsEaten.clear();
    	
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
    	this.setState(Game.State.READY);
    	
    	//Kill screen on level 256
    	if ((this.mIsKillScreenEnabled && (this.mLevel == Game.KILL_SCREEN_LEVEL)) || (this.mIsTrophyLegendEnabled && (Game.RANDOM.nextInt(Game.KILL_SCREEN_TROPHY_PROBABILITY) == 0))) {
    		this.mIsOnKillScreen = true;
    		
			//Load the kill screen sprite
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inScaled = false;
			this.mKillScreen = BitmapFactory.decodeResource(Wallpaper.CONTEXT.getResources(), R.drawable.killscreen, options);
    		
    		if (Wallpaper.LOG_DEBUG) {
    			Log.d(Game.TAG, "Kill screen enabled for this level");
    		}
    	} else {
    		if (this.mIsKillScreenEnabled && (this.mLevel > Game.KILL_SCREEN_LEVEL)) {
    			//Loop levels
    			this.mLevel = 1;
    		}
    		
    		this.mIsOnKillScreen = false;
    		this.mKillScreen = null;
    	}
    	
    	//Check trophy CEOs earned
    	if (this.mLevel == Game.TROPHY_CEOS_THRESHOLD) {
    		this.earnTrophyCeos();
    	}
    	
    	//Initialize dot grid
    	final int cellWidth = this.mCellColumnSpacing + 1;
    	final int cellHeight = this.mCellRowSpacing + 1;
    	for (int y = 0; y < this.mCellsTall; y++) {
    		for (int x = 0; x < this.mCellsWide; x++) {
    			
    			if ((x % cellWidth == 0) || (y % cellHeight == 0)) {
    				this.mBoard[y][x] = Game.Cell.DOT;
    			} else {
    				this.mBoard[y][x] = Game.Cell.WALL;
    			}
    		}
    	}
    	
    	//Remove dots under widgets
    	for (final Rect widget : this.mWidgetLocations) {
    		Log.d(Game.TAG, "L: " + widget.left + ", T: " + widget.top + ", R: " + widget.right + ", B: " + widget.bottom);
    		final int left = (widget.left * cellWidth) + 1;
    		final int top = (widget.top * cellHeight) + 1;
    		final int bottom = (widget.bottom * cellHeight) + this.mCellRowSpacing;
    		final int right = (widget.right * cellWidth) + this.mCellColumnSpacing;
    		for (int y = top; y <= bottom; y++) {
    			for (int x = left; x <= right; x++) {
    				this.mBoard[y][x] = Game.Cell.WALL;
    			}
    		}
    	}
    	
    	//Initialize juggerdots
    	this.mBoard[this.mCellRowSpacing + 1][0] = Cell.JUGGERDOT;
    	this.mBoard[0][this.mCellsWide - this.mCellColumnSpacing - 2] = Cell.JUGGERDOT;
    	this.mBoard[this.mCellsTall - this.mCellRowSpacing - 2][this.mCellsWide - 1] = Cell.JUGGERDOT;
    	this.mBoard[this.mCellsTall - 1][this.mCellColumnSpacing + 1] = Cell.JUGGERDOT;
    	this.mJuggerdotsRemaining = Game.NUMBER_OF_JUGGERDOTS;
    	
    	//Count dots
    	this.mDotsRemaining = 0;
    	for (int y = 0; y < this.mCellsTall; y++) {
    		for (int x = 0; x < this.mCellsWide; x++) {
    			if (this.mBoard[y][x] == Cell.DOT) {
    				this.mDotsRemaining += 1;
				}
    		}
    	}
    	this.mDotsTotal = this.mDotsRemaining + this.mJuggerdotsRemaining;
    	
    	this.mAllFleeingGhostsEaten = 0;
    	this.mGhostEatenThisLevel = 0;
    	
    	//Initialize entities
    	this.mTheMan.newLevel(this);
    	this.mFruit.newLevel(this);
    	for (final Ghost ghost : this.mGhosts) {
    		ghost.newLevel(this);
    	}
    	
    	if (Wallpaper.LOG_VERBOSE) {
    		Log.v(Game.TAG, "< newLevel()");
    	}
    }
    
    /**
     * Called after The Man dies to reset entity positions.
     */
    private void newLife() {
    	this.mTheMan.newLife(this);
    	for (final Ghost ghost : this.mGhosts) {
    		ghost.newLife(this);
    	}
    }
    
    /**
     * Iterate all entities one step.
     */
    public void tick() {
    	this.mTickCount += 1;
    	
    	//Check for level complete
    	if ((this.mDotsRemaining <= 0) && (this.mState != Game.State.LEVEL_COMPLETE)) {
    		//Check for The Mandroid trophy
    		if (this.hasNotLostLife() && (this.mGhostEatenThisLevel == 0) && (this.mJuggerdotsRemaining == 0)) {
    			this.earnTrophyTheMandroid();
    		}
    		
        	this.setState(Game.State.LEVEL_COMPLETE);
    	}
    	
    	//Always tick the fruit if the level is active, it waits for no one, living or dead
    	if (this.mState != Game.State.LEVEL_COMPLETE) {
    		this.mFruit.tick(this);
    	}
    	
    	if (this.mState != Game.State.PLAYING) {
    		//continue to tick The Man in death
    		if (this.mState == Game.State.DYING) {
    			this.mTheMan.tick(this);
    		}
    		
    		//check if current state has expired
    		if ((System.currentTimeMillis() - this.mStateTimestamp) > this.mState.length) {
	    		switch (this.mState) {
	    			case GAME_OVER:
	    				this.newGame();
	    				break;
	    				
	    			case DYING:
	    				if ((this.mLives < 0) && (this.mMode != Game.Mode.ENDLESS)) {
	    					this.setState(Game.State.GAME_OVER);
	    					break;
	    				} else {
	    					this.newLife();
	    					//fall through to next case
	    				}
	    				
	    			case READY:
	    				this.setState(Game.State.PLAYING);
	    				break;
	    				
	    			case LEVEL_COMPLETE:
	    				this.newLevel();
	    				break;
	    		}
    		}
    	} else {
	    	//The Man is ticked when playing
	    	this.mTheMan.tick(this);
	    	
	    	//Ghosts are only ticked when playing
	    	for (final Ghost ghost : this.mGhosts) {
	    		ghost.tick(this);
	    	}
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
    	
    	//Background image
    	if (this.mBackgroundPath != null) {
			try {
				final Bitmap temp = BitmapFactory.decodeStream(Wallpaper.CONTEXT.getContentResolver().openInputStream(Uri.parse(this.mBackgroundPath)));
				final float pictureAR = temp.getWidth() / (temp.getHeight() * 1.0f);
				final float screenAR = screenWidth / (screenHeight * 1.0f);
				int newWidth;
				int newHeight;
				int x;
				int y;
				
				if (pictureAR > screenAR) {
					//wider than tall related to the screen AR
					newHeight = screenHeight;
					newWidth = (int)(temp.getWidth() * (screenHeight / (temp.getHeight() * 1.0f)));
					x = (newWidth - screenWidth) / 2;
					y = 0;
				} else {
					//taller than wide related to the screen AR
					newWidth = screenWidth;
					newHeight = (int)(temp.getHeight() * (screenWidth / (temp.getWidth() * 1.0f)));
					x = 0;
					y = (newHeight - screenHeight) / 2;
				}
				
	    		final Bitmap scaled = Bitmap.createScaledBitmap(temp, newWidth, newHeight, false);
	    		this.mBackground = Bitmap.createBitmap(scaled, x, y, screenWidth, screenHeight);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				Log.w(Game.TAG, "Unable to load background bitmap.");
				this.mBackground = null;
			}
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
    	
    	//Update cell size
    	this.mCellSize.right = this.mCellWidth;
    	this.mCellSize.bottom = this.mCellHeight;
    	
    	if (Wallpaper.LOG_DEBUG) {
    		Log.d(Game.TAG, "Is Landscape: " + this.mIsLandscape);
    		Log.d(Game.TAG, "Screen Width: " + screenWidth);
    		Log.d(Game.TAG, "Screen Height: " + screenHeight);
    		Log.d(Game.TAG, "Cell Width: " + this.mCellWidth);
    		Log.d(Game.TAG, "Cell Height: " + this.mCellHeight);
    	}
    	
    	//Resize entities
    	this.mFruit.performResize(this);
    	this.mTheMan.performResize(this);
    	for (final Ghost ghost : this.mGhosts) {
    		ghost.performResize(this);
    	}
    	
    	//For on-board HUD text
    	if (this.mIsLandscape) {
    		this.mTextLocation.x = (this.mScreenHeight - (this.mDotGridPaddingBottom + this.mDotGridPaddingLeft + this.mDotGridPaddingRight)) / 2.0f;
    		this.mTextLocation.y = ((this.mTheMan.getInitialPosition(this).y - 1) * this.mCellWidth) - (this.mDotGridPaddingTop + this.mDotGridPaddingLeft);
    	} else {
    		this.mTextLocation.x = (this.mScreenWidth - this.mDotGridPaddingLeft - this.mDotGridPaddingRight) / 2.0f;
    		this.mTextLocation.y = (this.mTheMan.getInitialPosition(this).y * this.mCellHeight);
    	}
    	
    	//Calculate walls
    	this.mWalls.clear();
    	final float cellOverEight = 1 / 8.0f;
		
    	//Widget walls
    	for (final Rect widget : this.mWidgetLocations) {
			float left = (widget.left * (this.mCellColumnSpacing + 1)) + 1;
			float top = (widget.top * (this.mCellRowSpacing + 1)) + 1;
    		float right = ((widget.right * (this.mCellColumnSpacing + 1)) + this.mCellColumnSpacing + 1);
    		float bottom = ((widget.bottom * (this.mCellRowSpacing + 1)) + this.mCellRowSpacing + 1);
			
			this.mWalls.add(new RectF(left, top, right, bottom));
			
			left += cellOverEight;
			top += cellOverEight;
			right -= cellOverEight;
			bottom -= cellOverEight;

			this.mWalls.add(new RectF(left, top, right, bottom));
    	}
		
    	//Icon walls
    	for (int y = 0; y < this.mIconRows; y++) {
    		for (int x = 0; x < this.mIconCols; x++) {
    			boolean contained = false;
    			for (final Rect widget : this.mWidgetLocations) {
    				if (x >= widget.left && x <= widget.right && y >= widget.top && y <= widget.bottom) {
    					contained = true;
    					break;
    				}
    			}
    			if (contained) {
    				continue;
    			}
    			
    			float left = (x * (this.mCellColumnSpacing + 1)) + 1;
    			float top = (y * (this.mCellRowSpacing + 1)) + 1;
    			float right = left + this.mCellColumnSpacing;
    			float bottom = top + this.mCellRowSpacing;

    			this.mWalls.add(new RectF(left, top, right, bottom));
    			
    			left += cellOverEight;
    			top += cellOverEight;
    			right -= cellOverEight;
    			bottom -= cellOverEight;

    			this.mWalls.add(new RectF(left, top, right, bottom));
    		}
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
    	
    	//Clear the screen in case of transparency in the image
		c.drawColor(this.mGameBackground);
    	if (this.mBackground != null) {
    		//Bitmap should already be sized to the screen so draw it at the origin
    		c.drawBitmap(this.mBackground, 0, 0, this.mBackgroundPaint);
    	}
        
        if (this.mIsLandscape) {
        	//Perform counter-clockwise rotation
        	c.rotate(-90, this.mScreenWidth / 2.0f, this.mScreenWidth / 2.0f);
        	c.translate(0, this.mDotGridPaddingLeft);

        	//Draw HUD after rotation and translation
        	this.drawHud(c);
        } else {
        	//Draw HUD before translation
        	this.drawHud(c);
        	
        	c.translate(this.mDotGridPaddingLeft, this.mDotGridPaddingTop);
        }
        
        //Draw dots and walls
        this.drawGameBoard(c);
        
        if (this.mIsOnKillScreen) {
        	final RectF screenSize = new RectF(0, 0, this.mCellsWide * this.mCellWidth, this.mCellsTall * this.mCellHeight);
        	c.drawBitmap(this.mKillScreen, null, screenSize, null);
        }
        
        //Draw the fruit only if it is enabled and the game isn't over or level completed
    	if (this.mIsFruitEnabled && (this.mState != Game.State.GAME_OVER) && (this.mState != Game.State.LEVEL_COMPLETE)) {
        	this.mFruit.draw(this, c);
    	}
    	
    	//Draw "The Man"
    	this.mTheMan.draw(this, c);
    	
    	//Draw the ghosts if we are ready or playing
    	if ((this.mState == Game.State.READY) || (this.mState == Game.State.PLAYING)) {
	    	for (final Ghost ghost : this.mGhosts) {
	    		ghost.draw(this, c);
	    	}
    	}

        if (this.mIsLandscape) {
        	//Perform clockwise rotation back to normal
        	c.rotate(90, this.mScreenWidth / 2.0f, this.mScreenWidth / 2.0f);
        }
        
    	switch (this.mState) {
    		case READY:
    			c.drawText(this.mTextReady, this.mTextLocation.x - (this.mReadyForeground.measureText(this.mTextReady) / 2.0f), this.mTextLocation.y, this.mReadyForeground);
    			break;
    		case GAME_OVER:
    			c.drawText(this.mTextGameOver, this.mTextLocation.x - (this.mGameOverForeground.measureText(this.mTextGameOver) / 2.0f), this.mTextLocation.y, this.mGameOverForeground);
    			break;
    	}
        
        c.restore();
    }

    /**
     * Render the dots and walls.
     * 
     * @param c Canvas to draw on.
     */
    private void drawGameBoard(final Canvas c) {
    	if (this.mDots != Game.Dots.CIRCLES) {
    		//Set filter in case of Bitmaps
    		c.setDrawFilter(Game.FILTER_SET);
    	}
        
    	//draw dots
        for (int y = 0; y < this.mCellsTall; y++) {
        	for (int x = 0; x < this.mCellsWide; x++) {
        		final Game.Cell cell = this.mBoard[y][x];
        		if (cell == Cell.DOT) {
            		if (this.mDots == Game.Dots.CIRCLES) {
            			final float left = (x * this.mCellWidth) + ((this.mCellWidth * 0.75f) / 2);
            			final float top = (y * this.mCellHeight) + ((this.mCellHeight * 0.75f) / 2);
            			final float right = left + (this.mCellWidth * 0.25f);
            			final float bottom = top + (this.mCellHeight * 0.25f);
            		
            			c.drawOval(new RectF(left, top, right, bottom), this.mDotForeground);
            		} else {
            			final float left = (x * this.mCellWidth) + (this.mCellWidth / 4.0f);
            			final float top = (y * this.mCellHeight) + (this.mCellHeight / 4.0f);
            			final float right = left + (this.mCellWidth / 2.0f);
            			final float bottom = top + (this.mCellHeight / 2.0f);
            			
            			c.drawBitmap(this.mDotSprite, null, new RectF(left, top, right, bottom), Entity.SPRITE_PAINT);
            		}
        		} else if ((cell == Cell.JUGGERDOT) && (this.mTickCount % this.mJuggerdotBlinkLength < this.mJuggerdotBlinkInterval)) {
            		if (this.mDots == Game.Dots.CIRCLES) {
            			final float left = (x * this.mCellWidth) + ((this.mCellWidth * 0.25f) / 2);
            			final float top = (y * this.mCellHeight) + ((this.mCellHeight * 0.25f) / 2);
            			final float right = left + (this.mCellWidth * 0.75f);
            			final float bottom = top + (this.mCellHeight * 0.75f);

            			c.drawOval(new RectF(left, top, right, bottom), this.mJuggerdotForeground);
            		} else {
            			final float left = x * this.mCellWidth;
            			final float top = y * this.mCellHeight;
            			final float right = left + this.mCellWidth;
            			final float bottom = top + this.mCellHeight;
            			
            			c.drawBitmap(this.mDotSprite, null, new RectF(left, top, right, bottom), Entity.SPRITE_PAINT);
            		}
        		}
        	}
        }
        
        if (this.mDots != Game.Dots.CIRCLES) {
        	//Remove filter
        	c.setDrawFilter(Game.FILTER_REMOVE);
        }
    	
        //draw walls if enabled
        if (this.mIsDisplayingWalls) {
        	for (final RectF wall : this.mWalls) {
        		c.drawRect(wall, this.mWallsForeground);
        	}
        }
    }
    
    /**
     * Draw the lives, score, and level
     * 
     * @param c Canvas to draw on.
     */
    private void drawHud(final Canvas c) {
    	if (this.mIsDisplayingHud) {
	        //Lives and score
	        final float top = this.mScreenHeight - this.mHudOffset;
	        String score;

	        if (this.mMode != Game.Mode.ENDLESS) {
		        for (int i = 0; i < this.mLives; i++) {
		        	final RectF dest = new RectF((i * (this.mHudSize + Game.HUD_PADDING)) + Game.HUD_PADDING, top - this.mHudSize, ((i + 1) * (this.mHudSize + Game.HUD_PADDING)), top);
		        	switch (this.mTheMan.getCharacter()) {
		        		case THEMAN:
		        		case GOOGOL:
		        		case THEMANDROID:
			        		c.drawArc(dest, Game.HUD_THEMAN_ANGLE, Game.HUD_THEMAN_ARC, true, this.mTheManForeground);
		        			break;
		        			
		        		case ANDY:
		        			c.drawBitmap(this.mAndy, null, dest, Entity.SPRITE_PAINT);
		        			break;
		        	}
		        }
		        
		        //Don't display larger than 999,999 (bug in original game)
		        score = String.valueOf(Game.SCORE_FORMAT.format(this.mScore % Game.SCORE_FLIPPING)) + " L" + String.valueOf(this.mLevel);
	        } else {
	        	//In endless mode display actual score
	        	score = String.valueOf(this.mScore);
	        }
	        
	        final float landscapeOffset = this.mIsLandscape ? this.mDotGridPaddingTop : 0;
	        c.drawText(score, this.mScreenWidth - this.mHudForeground.measureText(score) - Game.HUD_PADDING - landscapeOffset, top, this.mHudForeground);
    	}
    }
}
