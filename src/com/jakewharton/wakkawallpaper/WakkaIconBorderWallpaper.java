package com.jakewharton.wakkawallpaper;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Random;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public class WakkaIconBorderWallpaper extends WallpaperService {
	enum Direction {
		NORTH(315), SOUTH(135), EAST(45), WEST(225);
		
		private int angle;
		public final int DIRECTIONS = 4;
		
		private Direction(int angle) {
			this.angle = angle;
		}
		
		public int getAngle() {
			return this.angle;
		}
	}
	enum Cell { BLANK, WALL, DOT, JUGGERDOT }

    private final Handler mHandler = new Handler();

    @Override
    public Engine onCreateEngine() {
        return new WakkaEngine();
    }

    class WakkaEngine extends Engine {
    	private static final String TAG = "WakkaEngine";
    	private static final int MILLISECONDS_IN_SECOND = 1000;
    	private static final int THE_MANS_GRILL_SIZE = 270;
    	private static final int NUMBER_OF_GHOSTS = 4;
    	private static final int POINTS_DOT = 10;
    	private static final int POINTS_JUGGERDOT = 50;
    	//TODO: Make static
    	private final NumberFormat SCORE_FORMAT = new DecimalFormat("000000");
    	
    	private Cell[][] mBoard;
    	private Point[] mGhosts;
    	private int mDotsRemaining;
    	private int mLives;
    	private int mScore;
        private boolean mIsVisible;
        private int mFPS;
        private float mScreenCenterX;
        private float mScreenCenterY;
        private int mScreenHeight;
        private int mScreenWidth;
        private int mIconRows;
        private int mIconCols;
        private float mDotGridPaddingTop;
        private float mDotGridPaddingLeft;
        private float mDotGridPaddingBottom;
        private float mDotGridPaddingRight;
        private int mDotGridWide;
        private int mDotGridHigh;
        private float mGridCellWidth;
        private float mGridCellHeight;
        private float mDotPadding;
        private float mDotDiameter;
        private int mDotSpacingX;
        private int mDotSpacingY;
        private final Paint mDotPaint = new Paint();
        private int mDotForeground;
        private int mDotBackground;
        private final Paint mTheManPaint = new Paint();
        private int mTheManForeground;
        private Point mTheManPosition;
        private Direction mTheManDirection;
        private final Random mTheManRandomizer = new Random();
        private int mGhostBlinkyBackground;
        private int mGhostPinkyBackground;
        private int mGhostInkyBackground;
        private int mGhostClydeBackground;
        private int mGhostEyeBackground;
        private int mGhostEyeForeground;
        private int mGhostScaredBackground;
        private int mGhostScaredForeground;
        private int mHUDForeground;
        private int mHUDBackground;
        private final Paint mHUDPaint = new Paint();

        private final Runnable mDrawWakka = new Runnable() {
            public void run() {
                drawFrame();
            }
        };

        WakkaEngine() {
            //TODO: set via settings
            this.mIconRows = 4;
            this.mIconCols = 4;
            this.mFPS = 10;
            this.mDotGridPaddingLeft = -5;
            this.mDotGridPaddingRight = -5;
            this.mDotGridPaddingTop = 40;
            this.mDotGridPaddingBottom = 65;
            this.mDotForeground = 0xff6161a1;
            this.mDotBackground = 0xff000040;
            this.mTheManForeground = 0xfffff000;
            this.mGhostBlinkyBackground = 0xfff00000;
            this.mGhostPinkyBackground = 0xffff00f0;
            this.mGhostInkyBackground = 0xff01d8ff;
            this.mGhostClydeBackground = 0xffff8401;
            this.mGhostEyeBackground = 0xffffffff;
            this.mGhostEyeForeground = 0xff000000;
            this.mGhostScaredBackground = 0xff0033ff;
            this.mGhostScaredForeground = 0xffffcc33;
            this.mHUDForeground = 0xff8181c1;
            this.mHUDBackground = 0xff000000;
            
        	//Create Paints with their values
            final Paint dotPaint = this.mDotPaint;
            dotPaint.setColor(this.mDotForeground);
            dotPaint.setAntiAlias(true);
            dotPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            
            final Paint theManPaint = this.mTheManPaint;
            theManPaint.setColor(this.mTheManForeground);
            theManPaint.setAntiAlias(true);
            
            final Paint theHUDPaint = this.mHUDPaint;
            theHUDPaint.setColor(this.mHUDForeground);
            theHUDPaint.setAntiAlias(true);
            theHUDPaint.setTextSize(20f);
            theHUDPaint.setShadowLayer(1, -1, 1, this.mHUDBackground);
            
            //TODO: calculate these somehow
            this.mDotSpacingX = 4;
            this.mDotSpacingY = 6;
            this.mDotPadding = 9;
            
            this.mDotGridWide = (this.mIconCols * (this.mDotSpacingX + 1)) + 1;
            Log.d(WakkaEngine.TAG, "Grid Wide: " + this.mDotGridWide);
            this.mDotGridHigh = (this.mIconRows * (this.mDotSpacingY + 1)) + 1;
            Log.d(WakkaEngine.TAG, "Grid High: " + this.mDotGridHigh);
            
            //Initialize storage arrays
            this.mBoard = new Cell[this.mDotGridHigh][this.mDotGridWide];
        	this.mGhosts = new Point[WakkaEngine.NUMBER_OF_GHOSTS];
        	
        	//Start with a fresh game
        	this.gameReset();
        }
        
        private void gameReset() {
        	//Game level values
        	this.mLives = 3;
        	this.mScore = 0;
        	
        	//Reset board
        	this.boardReset();
        }
        private void boardReset() {
        	//Setup dot grid
        	this.mDotsRemaining = 0;
        	for (int y = 0; y < this.mDotGridHigh; y++) {
        		for (int x = 0; x < this.mDotGridWide; x++) {
        			if ((x % (this.mDotSpacingX + 1) == 0) || (y % (this.mDotSpacingY + 1) == 0)) {
        				this.mBoard[y][x] = Cell.DOT;
        				this.mDotsRemaining += 1;
        			} else {
        				this.mBoard[y][x] = Cell.WALL;
        			}
        		}
        	}
        	
        	//Initialize juggernaut dots
        	this.mBoard[this.mDotSpacingY + 1][0] = Cell.JUGGERDOT;
        	this.mBoard[0][this.mDotGridWide - this.mDotSpacingX - 2] = Cell.JUGGERDOT;
        	this.mBoard[this.mDotGridHigh - this.mDotSpacingY - 2][this.mDotGridWide - 1] = Cell.JUGGERDOT;
        	this.mBoard[this.mDotGridHigh - 1][this.mDotSpacingX + 1] = Cell.JUGGERDOT;
        	this.mDotsRemaining -= 4;
        	
        	//Initialize "The Man"
        	this.mTheManPosition = new Point(5, 7);
        	this.mTheManDirection = Direction.EAST;
        	
        	//Initialize ghosts
        	this.mGhosts[0] = new Point(this.mDotSpacingX + 1, 0);
        	this.mGhosts[1] = new Point(this.mDotGridWide - 1, this.mDotSpacingY + 1);
        	this.mGhosts[2] = new Point(this.mDotGridWide - this.mDotSpacingX - 2, this.mDotGridHigh - 1);
        	this.mGhosts[3] = new Point(0, this.mDotGridHigh - this.mDotSpacingY - 2);
        }
        
        private boolean isValidPosition(Point position) {
        	return ((position.x >= 0) && (position.x < this.mDotGridWide)
        		    && (position.y >= 0) && (position.y < this.mDotGridHigh)
        		    && (this.mBoard[position.y][position.x] != Cell.WALL));
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
        	if (event.getAction() == MotionEvent.ACTION_DOWN) {
        		float deltaX = this.mScreenCenterX - event.getX();
        		float deltaY = this.mScreenCenterY - event.getY();
        		
        		if (Math.abs(deltaX) > Math.abs(deltaY)) {
        			if (deltaX > 0) {
        				this.tryMove(Direction.WEST);
        			} else {
        				this.tryMove(Direction.EAST);
        			}
        		} else {
        			if (deltaY > 0) {
        				this.tryMove(Direction.NORTH);
        			} else {
        				this.tryMove(Direction.SOUTH);
        			}
        		}
        	}
        }
        
        private boolean tryMove(Direction direction) {
        	Point newPoint = this.move(this.mTheManPosition, direction);
        	if (this.isValidPosition(newPoint)) {
        		this.mTheManPosition = newPoint;
        		this.mTheManDirection = direction;
        		if (this.mBoard[newPoint.y][newPoint.x] == Cell.DOT) {
        			this.mScore += WakkaEngine.POINTS_DOT;
        			this.mBoard[newPoint.y][newPoint.x] = Cell.BLANK;
        			this.mDotsRemaining -= 1;
        			
        			if (this.mDotsRemaining == 0) {
        				this.boardReset();
        			}
        		} else if (this.mBoard[newPoint.y][newPoint.x] == Cell.JUGGERDOT) {
        			this.mScore += WakkaEngine.POINTS_JUGGERDOT;
        			this.mBoard[newPoint.y][newPoint.x] = Cell.BLANK;
        		}
        		return true;
        	} else {
        		return false;
        	}
        }
        
        private void moveTheMan() {
        	//TODO: Use AI logic to determine best direction to proceed
        	boolean success = false;
        	while (!success) {
	        	switch (this.mTheManRandomizer.nextInt(16)) {
	        		case 0:
	        			success = this.tryMove(Direction.NORTH);
	        			break;
	        		case 1:
	        			success = this.tryMove(Direction.SOUTH);
	        			break;
	        		case 2:
	        			success = this.tryMove(Direction.EAST);
	        			break;
	        		case 3:
	        			success = this.tryMove(Direction.WEST);
	        			break;
	        		default: //4-15, 75% of the time stay straight (if possible)
	        			success = this.tryMove(this.mTheManDirection);
	        			break;
	        	}
	        }
        }
        
        private Point move(Point point, Direction direction) {
        	Point newPoint = new Point(point);
        	switch (direction) {
        		case NORTH:
        			newPoint.y -= 1;
    				break;
    				
        		case SOUTH:
        			newPoint.y += 1;
    				break;
    				
        		case WEST:
        			newPoint.x -= 1;
    				break;
    				
        		case EAST:
        			newPoint.x += 1;
    				break;
        	}
        	return newPoint;
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            
            this.mScreenHeight = height;
            Log.d(WakkaEngine.TAG, "Screen Height: " + this.mScreenHeight);
            this.mScreenWidth = width;
            Log.d(WakkaEngine.TAG, "Screen Width: " + this.mScreenWidth);
            
            this.mScreenCenterX = width / 2.0f;
            Log.d(WakkaEngine.TAG, "Center X: " + this.mScreenCenterX);
            this.mScreenCenterY = height / 2.0f;
            Log.d(WakkaEngine.TAG, "Center Y: " + this.mScreenCenterY);
            
            this.mGridCellWidth = (width - (this.mDotGridPaddingLeft + this.mDotGridPaddingRight)) / (this.mDotGridWide * 1.0f);
            Log.d(WakkaEngine.TAG, "Cell Width: " + this.mGridCellWidth);
            this.mGridCellHeight = this.mGridCellWidth; //for now
            Log.d(WakkaEngine.TAG, "Cell Height: " + this.mGridCellHeight);
            this.mDotDiameter = this.mGridCellWidth - (this.mDotPadding * 2);
            Log.d(WakkaEngine.TAG, "Dot Diameter: " + this.mDotDiameter);
            
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
                	this.moveTheMan();
                    this.drawBoard(c);
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

        void drawBoard(Canvas c) {
            c.save();
            c.drawColor(this.mDotBackground);
            
            //Lives and score
            final float textY = this.mScreenHeight - this.mDotGridPaddingBottom + 5;
            final String score = String.valueOf(this.SCORE_FORMAT.format(this.mScore));
            c.drawText(this.mLives + "UP", 10, textY, this.mHUDPaint);
            c.drawText(score, this.mScreenWidth - this.mHUDPaint.measureText(score) - 10, textY, this.mHUDPaint);
            
            //Adjust for notification bar
            c.translate(this.mDotGridPaddingLeft, this.mDotGridPaddingTop);
            
            //Draw dot grid
            for (int y = 0; y < this.mDotGridHigh; y++) {
            	for (int x = 0; x < this.mDotGridWide; x++) {
            		if (this.mBoard[y][x] == Cell.DOT) {
	            		float left = (x * this.mGridCellWidth) + this.mDotPadding;
	            		float top = (y * this.mGridCellHeight) + this.mDotPadding;
	            		
	            		c.drawOval(new RectF(left, top, left + this.mDotDiameter, top + this.mDotDiameter), this.mDotPaint);
            		} else if (this.mBoard[y][x] == Cell.JUGGERDOT) {
	            		float left = (x * this.mGridCellWidth) + (this.mDotPadding / 3.0f);
	            		float top = (y * this.mGridCellHeight) + (this.mDotPadding / 3.0f);
	            		
	            		c.drawOval(new RectF(left, top, left + this.mDotDiameter + (this.mDotPadding * 4 / 3.0f), top + this.mDotDiameter + (this.mDotPadding * 4 / 3.0f)), this.mDotPaint);
            		}
            	}
            }
            
            //Draw "The Man"
            float theManLeft = this.mTheManPosition.x * this.mGridCellWidth;
            float theManTop = this.mTheManPosition.y * this.mGridCellHeight;
            c.drawArc(new RectF(theManLeft, theManTop, theManLeft + this.mGridCellWidth, theManTop + this.mGridCellHeight), this.mTheManDirection.getAngle(), WakkaEngine.THE_MANS_GRILL_SIZE, true, this.mTheManPaint);
            
            c.restore();
        }
    }
}