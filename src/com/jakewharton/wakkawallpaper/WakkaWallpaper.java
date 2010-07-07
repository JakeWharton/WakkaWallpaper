package com.jakewharton.wakkawallpaper;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

public class WakkaWallpaper extends WallpaperService {

    private final Handler mHandler = new Handler();

    @Override
    public Engine onCreateEngine() {
        return new WakkaEngine();
    }

    class WakkaEngine extends Engine {
        private final Paint mPaint = new Paint();
        private boolean mIsVisible;
        private float mDotsWide = 21;
        private float mDotsHigh;
        private float mDotPadding = 10;
        private float mDotWidth;

        private final Runnable mDrawCube = new Runnable() {
            public void run() {
                drawFrame();
            }
        };

        WakkaEngine() {
            // Create a Paint to draw the lines for our cube
            final Paint paint = mPaint;
            paint.setColor(0xff6161a1);
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            mIsVisible = visible;
            if (visible) {
                drawFrame();
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            
            this.mDotWidth = (width - ((this.mDotsWide - 1) * this.mDotPadding)) / this.mDotsWide;
            this.mDotsHigh = (float)Math.floor(height / (this.mDotWidth + this.mDotPadding));
            
            drawFrame();
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            mIsVisible = false;
        }

        /*
         * Draw one frame of the animation. This method gets called repeatedly
         * by posting a delayed Runnable. You can do any drawing you want in
         * here. This example draws a wireframe cube.
         */
        void drawFrame() {
            final SurfaceHolder holder = getSurfaceHolder();

            Canvas c = null;
            try {
                c = holder.lockCanvas();
                if (c != null) {
                    // draw something
                    drawBoard(c);
                }
            } finally {
                if (c != null) holder.unlockCanvasAndPost(c);
            }

            if (mIsVisible) {
                mHandler.postDelayed(mDrawCube, 1000 / 25);
            }
        }

        void drawBoard(Canvas c) {
            c.save();
            c.drawColor(0xff000040);
            c.translate(0, 45);
            
            for (int y = 0; y < this.mDotsHigh; y++) {
            	for (int x = 0; x < this.mDotsWide; x++) {
            		if ((x % 5 == 0) || (y % 5 == 0)) {
	            		float left = x * (this.mDotWidth + this.mDotPadding);
	            		float top = y * (this.mDotWidth + this.mDotPadding);
	            		
	            		c.drawOval(new RectF(left, top, left + this.mDotWidth, top + this.mDotWidth), this.mPaint);
            		}
            	}
            }
            
            c.restore();
        }
    }
}