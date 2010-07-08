package com.jakewharton.wakkawallpaper;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

public class WakkaIconBorderWallpaper extends WallpaperService {

    private final Handler mHandler = new Handler();

    @Override
    public Engine onCreateEngine() {
        return new WakkaEngine();
    }

    class WakkaEngine extends Engine {
        private final Paint mPaint = new Paint();
        private boolean mIsVisible;
        private int mRows = 4;
        private int mCols = 4;
        private float mDotsWide = 21;
        private float mDotsHigh;
        private float mDotPadding = 10;
        private float mDotDiameter;
        private int mDotColorForeground = 0xff6161a1;
        private int mDotColorBackground = 0xff000040;

        private final Runnable mDrawCube = new Runnable() {
            public void run() {
                drawFrame();
            }
        };

        WakkaEngine() {
            // Create a Paint to draw the dots
            final Paint paint = mPaint;
            paint.setColor(this.mDotColorForeground);
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
            
            this.mDotDiameter = (width - ((this.mDotsWide - 1) * this.mDotPadding)) / this.mDotsWide;
            this.mDotsHigh = (float)Math.floor(height / (this.mDotDiameter + this.mDotPadding));
            
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
            c.drawColor(this.mDotColorBackground);
            
            //Temporary(?) notification bar fix
            c.translate(0, 45);
            
            for (int y = 0; y < this.mDotsHigh; y++) {
            	for (int x = 0; x < this.mDotsWide; x++) {
            		if ((x % 5 == 0) || (y % 5 == 0)) {
	            		float left = x * (this.mDotDiameter + this.mDotPadding);
	            		float top = y * (this.mDotDiameter + this.mDotPadding);
	            		
	            		c.drawOval(new RectF(left, top, left + this.mDotDiameter, top + this.mDotDiameter), this.mPaint);
            		}
            	}
            }
            
            c.restore();
        }
    }
}