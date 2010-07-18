package com.jakewharton.wakkawallpaper;

import com.jakewharton.wakkawallpaper.Entity.Direction;

import android.content.Context;
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
    public static SharedPreferences PREFERENCES;
    public static Context CONTEXT;
    public static final boolean LOG_DEBUG = true;
    public static final boolean LOG_VERBOSE = true;
    
    private final Handler mHandler = new Handler();

    @Override
    public Engine onCreateEngine() {
    	Wallpaper.PREFERENCES = this.getSharedPreferences(Preferences.SHARED_NAME, 0);
    	Wallpaper.CONTEXT = this;
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
    	
    	private Game mGame;
        private boolean mIsVisible;
        private int mFPS;
        private float mScreenCenterX;
        private float mScreenCenterY;

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
        	if (Wallpaper.LOG_VERBOSE) {
        		Log.v(WakkaEngine.TAG, "> WakkaEngine()");
        	}
        	
            this.mGame = new Game();

            //Load all preferences or their defaults
            Wallpaper.PREFERENCES.registerOnSharedPreferenceChangeListener(this);
            this.onSharedPreferenceChanged(Wallpaper.PREFERENCES, null);
            
        	if (Wallpaper.LOG_VERBOSE) {
        		Log.v(WakkaEngine.TAG, "< WakkaEngine()");
        	}
        }

        /**
         * Handle the changing of a preference.
         */
		public void onSharedPreferenceChanged(final SharedPreferences preferences, final String key) {
        	if (Wallpaper.LOG_VERBOSE) {
        		Log.v(WakkaEngine.TAG, "> onSharedPreferenceChanged()");
        	}
        	
			final boolean all = (key == null);
			
			final String fps = Wallpaper.this.getString(R.string.settings_display_fps_key);
			if (all || key.equals(fps)) {
				this.mFPS = preferences.getInt(fps, WakkaEngine.DEFAULT_FPS);
				
				if (Wallpaper.LOG_DEBUG) {
					Log.d(WakkaEngine.TAG, "FPS = " + this.mFPS);
				}
			}

        	if (Wallpaper.LOG_VERBOSE) {
        		Log.v(WakkaEngine.TAG, "< onSharedPreferenceChanged()");
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
        				this.mGame.getTheMan().setWantsToGo(Direction.WEST);
        			} else {
        				this.mGame.getTheMan().setWantsToGo(Direction.EAST);
        			}
        		} else {
        			if (deltaY > 0) {
        				this.mGame.getTheMan().setWantsToGo(Direction.NORTH);
        			} else {
        				this.mGame.getTheMan().setWantsToGo(Direction.SOUTH);
        			}
        		}
        	}
        }

        @Override
        public void onSurfaceChanged(final SurfaceHolder holder, final int format, final int width, final int height) {
        	if (Wallpaper.LOG_VERBOSE) {
        		Log.v(WakkaEngine.TAG, "> onSurfaceChanged(width = " + width + ", height = " + height + ")");
        	}
        	
            super.onSurfaceChanged(holder, format, width, height);
            
            this.mScreenCenterX = width / 2.0f;
            Log.v(WakkaEngine.TAG, "Center X: " + this.mScreenCenterX);
            this.mScreenCenterY = height / 2.0f;
            Log.v(WakkaEngine.TAG, "Center Y: " + this.mScreenCenterY);
            
            //Trickle down
            this.mGame.performResize(width, height);
            
            //Redraw with new settings
            this.drawFrame();
            
            if (Wallpaper.LOG_VERBOSE) {
            	Log.v(WakkaEngine.TAG, "< onSurfaceChanged()");
            }
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
        private void drawFrame() {
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