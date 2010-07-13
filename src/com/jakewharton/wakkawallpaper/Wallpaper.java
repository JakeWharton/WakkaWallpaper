package com.jakewharton.wakkawallpaper;

import com.jakewharton.wakkawallpaper.Entity.Direction;

import android.graphics.Canvas;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public class Wallpaper extends WallpaperService {
    private final Handler mHandler = new Handler();

    @Override
    public Engine onCreateEngine() {
        return new WakkaEngine();
    }

    private class WakkaEngine extends Engine {
    	private static final String TAG = "WakkaWallpaper.WakkaEngine";
    	private static final int MILLISECONDS_IN_SECOND = 1000;
    	
    	private Game mGame;
    	private int mIconRows;
    	private int mIconCols;
        private boolean mIsVisible;
        private int mFPS;
        private float mScreenCenterX;
        private float mScreenCenterY;

        private final Runnable mDrawWakka = new Runnable() {
            public void run() {
            	mGame.tick();
                drawFrame();
            }
        };

        WakkaEngine() {
            //TODO: set via settings
            this.mFPS = 15;
            this.mIconRows = 4;
            this.mIconCols = 4;
            
            this.mGame = null;
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            this.mIsVisible = visible;
            if (visible) {
                drawFrame();
            } else {
                mHandler.removeCallbacks(this.mDrawWakka);
            }
        }
        
        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
        	super.onCreate(surfaceHolder);

            // By default we don't get touch events, so enable them.
            this.setTouchEventsEnabled(true);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mHandler.removeCallbacks(mDrawWakka);
        }
        
        @Override
        public void onTouchEvent(MotionEvent event) {
        	if ((event.getAction() == MotionEvent.ACTION_DOWN) && (this.mGame != null)) {
        		float deltaX = this.mScreenCenterX - event.getX();
        		float deltaY = this.mScreenCenterY - event.getY();
        		
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
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            
            Log.d(WakkaEngine.TAG, "Screen Height: " + height);
            Log.d(WakkaEngine.TAG, "Screen Width: " + width);
            
            this.mScreenCenterX = width / 2.0f;
            Log.d(WakkaEngine.TAG, "Center X: " + this.mScreenCenterX);
            this.mScreenCenterY = height / 2.0f;
            Log.d(WakkaEngine.TAG, "Center Y: " + this.mScreenCenterY);
            
            if (this.mGame != null) {
            	this.mGame.performResize(width, height);
            } else {
            	this.mGame = new Game(this.mIconRows, this.mIconCols, width, height);
            }
            
            drawFrame();
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            mIsVisible = false;
            mHandler.removeCallbacks(this.mDrawWakka);
        }

        /*
         * Draw one frame of the animation.
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
                mHandler.postDelayed(this.mDrawWakka, WakkaEngine.MILLISECONDS_IN_SECOND / this.mFPS);
            }
        }
    }
}