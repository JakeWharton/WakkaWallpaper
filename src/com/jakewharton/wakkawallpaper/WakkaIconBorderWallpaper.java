package com.jakewharton.wakkawallpaper;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

public class WakkaIconBorderWallpaper extends WallpaperService {
	enum Direction {
		NORTH(315), SOUTH(135), EAST(45), WEST(225);
		
		private int angle;
		
		private Direction(int angle) {
			this.angle = angle;
		}
		
		public int getAngle() {
			return this.angle;
		}
	}

    private final Handler mHandler = new Handler();

    @Override
    public Engine onCreateEngine() {
        return new WakkaEngine();
    }

    class WakkaEngine extends Engine {
        private boolean mIsVisible;
        private int mIconRows = 4;
        private int mIconCols = 4;
        private float mDotGridPaddingTop = 45;
        private float mDotGridPaddingLeft = 0;
        private float mDotGridPaddingBottom = 70;
        private float mDotGridPaddingRight = 0;
        private float mDotGridWide = 21;
        private float mDotGridHigh;
        private float mDotPadding = 10;
        private float mDotDiameter;
        private final Paint mDotPaint = new Paint();
        private int mDotColorForeground = 0xff6161a1;
        private int mDotColorBackground = 0xff000040;
        private final Paint mTheManPaint = new Paint();
        private int mTheManColor = 0xfffff000;
        private int mTheManPositionX = 5;
        private int mTheManPositionY = 7;
        private Direction mTheManDirection = Direction.EAST;
        private int mGhostBlinkyColor = 0xfff00000;
        private int mGhostPinkyColor = 0xffff00f0;
        private int mGhostInkyColor = 0xff01d8ff;
        private int mGhostClydeColor = 0xffff8401;
        private int mGhostEyeColorBackground = 0xffffffff;
        private int mGhostEyeColorForeground = 0xff000000;

        private final Runnable mDrawCube = new Runnable() {
            public void run() {
                drawFrame();
            }
        };

        WakkaEngine() {
            // Create a Paint to draw the dots
            final Paint dotPaint = this.mDotPaint;
            dotPaint.setColor(this.mDotColorForeground);
            dotPaint.setAntiAlias(true);
            dotPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            
            final Paint theManPaint = this.mTheManPaint;
            theManPaint.setColor(this.mTheManColor);
            theManPaint.setAntiAlias(true);
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
            
            this.mDotDiameter = (width - (this.mDotGridPaddingLeft + this.mDotGridPaddingRight) - ((this.mDotGridWide - 1) * this.mDotPadding)) / this.mDotGridWide;
            this.mDotGridHigh = (float)Math.floor((height - (this.mDotGridPaddingTop + this.mDotGridPaddingBottom)) / (this.mDotDiameter + this.mDotPadding));
            
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
            
            c.translate(this.mDotGridPaddingLeft, this.mDotGridPaddingTop);
            
            for (int y = 0; y < this.mDotGridHigh; y++) {
            	for (int x = 0; x < this.mDotGridWide; x++) {
            		if ((x % 5 == 0) || (y % 7 == 0)) {
	            		float left = x * (this.mDotDiameter + this.mDotPadding);
	            		float top = y * (this.mDotDiameter + this.mDotPadding);
	            		
	            		c.drawOval(new RectF(left, top, left + this.mDotDiameter, top + this.mDotDiameter), this.mDotPaint);
            		}
            	}
            }
            
            float theManLeft = (this.mTheManPositionX * (this.mDotDiameter + this.mDotPadding)) - (this.mDotPadding / 2.0f);
            float theManTop = (this.mTheManPositionY * (this.mDotDiameter + this.mDotPadding)) - (this.mDotPadding / 2.0f);
            c.drawArc(new RectF(theManLeft, theManTop, theManLeft + this.mDotDiameter + this.mDotPadding, theManTop + this.mDotDiameter + this.mDotPadding), this.mTheManDirection.getAngle(), 270, true, this.mTheManPaint);
            
            c.restore();
        }
    }
}