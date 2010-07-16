package com.jakewharton.wakkawallpaper;

import com.jakewharton.wakkawallpaper.Entity.Direction;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

/**
 * Wakka wakka wakka wakka...
 * 
 * @author Jake Wharton
 */
public class Wallpaper extends WallpaperService {
	public static final String SHARED_PREFERENCES_NAME = "WakkaWallpaper";
	
    private final Handler mHandler = new Handler();

    @Override
    public Engine onCreateEngine() {
        return new WakkaEngine();
    }

    /**
     * Wallpaper engine to manage the Game instance.
     * 
     * @author Jake Wharton
     */
    private class WakkaEngine extends Engine implements SharedPreferences.OnSharedPreferenceChangeListener {
    	private static final String TAG = "WakkaWallpaper.WakkaEngine";
    	private static final int MILLISECONDS_IN_SECOND = 1000;
    	private static final int DEFAULT_FPS = 10;
    	private static final int DEFAULT_ICON_ROWS = 4;
    	private static final int DEFAULT_ICON_COLS = 4;
    	
    	private Game mGame;
    	private int mIconRows;
    	private int mIconCols;
        private boolean mIsVisible;
        private int mFPS;
        private int mScreenWidth;
        private int mScreenHeight;
        private float mScreenCenterX;
        private float mScreenCenterY;
        private final SharedPreferences mPreferences;

        private final Runnable mDrawWakka = new Runnable() {
            public void run() {
            	WakkaEngine.this.mGame.tick();
                WakkaEngine.this.drawFrame();
            }
        };

        /**
         * Create instance of the engine.
         */
        public WakkaEngine() {
            this.mGame = null;
            
            this.mPreferences = Wallpaper.this.getSharedPreferences(Wallpaper.SHARED_PREFERENCES_NAME, 0);
            this.mPreferences.registerOnSharedPreferenceChangeListener(this);
            
            //Load all preferences or their defaults
            this.onSharedPreferenceChanged(this.mPreferences, null);
        }

        /**
         * Handle the changing of a preference
         */
		public void onSharedPreferenceChanged(final SharedPreferences preferences, final String key) {
			final boolean all = (key == null);
			
			final String fps = Wallpaper.this.getString(R.string.settings_display_fps_key);
			if (all || key.equals(fps)) {
				this.mFPS = preferences.getInt(fps, WakkaEngine.DEFAULT_FPS);
			}
			
			boolean iconsChanged = false;
			final String iconRows = Wallpaper.this.getString(R.string.settings_display_iconrows_key); 
			if (all || key.equals(iconRows)) {
				this.mIconRows = preferences.getInt(iconRows, WakkaEngine.DEFAULT_ICON_ROWS);
				iconsChanged = true;
			}
			
			final String iconCols = Wallpaper.this.getString(R.string.settings_display_iconcols_key);
			if (all || key.equals(iconCols)) {
				this.mIconCols = preferences.getInt(iconCols, WakkaEngine.DEFAULT_ICON_COLS);
				iconsChanged = true;
			}
			
			if (iconsChanged) {
				if (this.mGame != null) {
					this.mGame.performResize(this.mScreenWidth, this.mScreenHeight);
					this.mGame.newGame();
				}
			}
		}

        @Override
        public void onVisibilityChanged(final boolean visible) {
            this.mIsVisible = visible;
            if (visible) {
                this.drawFrame();
            } else {
                Wallpaper.this.mHandler.removeCallbacks(this.mDrawWakka);
            }
        }
        
        @Override
        public void onCreate(final SurfaceHolder surfaceHolder) {
        	super.onCreate(surfaceHolder);

            // By default we don't get touch events, so enable them.
            this.setTouchEventsEnabled(true);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            Wallpaper.this.mHandler.removeCallbacks(mDrawWakka);
        }
        
        @Override
        public void onTouchEvent(final MotionEvent event) {
        	if ((event.getAction() == MotionEvent.ACTION_DOWN) && (this.mGame != null)) {
        		final float deltaX = this.mScreenCenterX - event.getX();
        		final float deltaY = this.mScreenCenterY - event.getY();
        		
        		if (Math.abs(deltaX) > Math.abs(deltaY)) {
        			if (deltaX > 0) {
        				this.mGame.setWantsToGo(Direction.WEST);
        			} else {
        				this.mGame.setWantsToGo(Direction.EAST);
        			}
        		} else {
        			if (deltaY > 0) {
        				this.mGame.setWantsToGo(Direction.NORTH);
        			} else {
        				this.mGame.setWantsToGo(Direction.SOUTH);
        			}
        		}
        	}
        }

        @Override
        public void onSurfaceChanged(final SurfaceHolder holder, final int format, final int width, final int height) {
            super.onSurfaceChanged(holder, format, width, height);
            
            Log.v(WakkaEngine.TAG, "Screen Height: " + height);
            Log.v(WakkaEngine.TAG, "Screen Width: " + width);
            this.mScreenWidth = width;
            this.mScreenHeight = height;
            
            this.mScreenCenterX = width / 2.0f;
            Log.v(WakkaEngine.TAG, "Center X: " + this.mScreenCenterX);
            this.mScreenCenterY = height / 2.0f;
            Log.v(WakkaEngine.TAG, "Center Y: " + this.mScreenCenterY);
            
            if (this.mGame != null) {
            	this.mGame.performResize(width, height);
            } else {
            	this.mGame = new Game(this.mIconRows, this.mIconCols, width, height);
            }
            
            this.drawFrame();
        }

        @Override
        public void onSurfaceDestroyed(final SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            this.mIsVisible = false;
            Wallpaper.this.mHandler.removeCallbacks(this.mDrawWakka);
        }

        /**
         * Draws the current state of the game to the wallpaper.
         */
        void drawFrame() {
            final SurfaceHolder holder = this.getSurfaceHolder();

            Canvas c = null;
            try {
                c = holder.lockCanvas();
                if (c != null) {
                    this.mGame.draw(c);
                }
            } finally {
                if (c != null) {
                	holder.unlockCanvasAndPost(c);
                }
            }

            if (this.mIsVisible) {
                Wallpaper.this.mHandler.postDelayed(this.mDrawWakka, WakkaEngine.MILLISECONDS_IN_SECOND / this.mFPS);
            }
        }
    }
}