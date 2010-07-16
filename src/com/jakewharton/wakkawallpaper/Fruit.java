package com.jakewharton.wakkawallpaper;

import android.graphics.Canvas;

/**
 * The Fruit class is a special reward entity that appears only at specific times.
 * 
 * @author Jake Wharton
 */
public class Fruit extends Entity {
	enum Type {
		CHERRY(100), STRAWBERRY(300), PEACH(500), APPLE(700), GRAPES(1000), GALAXIAN(2000), BELL(3000), KEY(5000);
		
		public final int points;
		
		private Type(int points) {
			this.points = points;
		}
	}
	
    private static final int DEFAULT_THRESHOLD_FIRST = 70;
    private static final int DEFAULT_THRESHOLD_SECOND = 170;
    private static final int DEFAULT_VISIBLE_LOWER = 9000;
    private static final int DEFAULT_VISIBLE_UPPER = 10000;
	
	private Type mType;
	private long mCreated;
	private boolean mVisible;
	private int mVisibleLength;
	private int mVisibleLower;
	private int mVisibleUpper;
	private int mThresholdFirst;
	private int mThresholdSecond;
	
	/**
	 * Initialize a new fruit adhering to the parameters.
	 * 
	 * @param startingPositionX X coordinate of the position of the fruit.
	 * @param startingPositionY Y coordinate of the position of the fruit.
	 * @param type Type value representing the type of fruit.
	 * @param visible The length (in milliseconds) that the fruit will be visible on screen.
	 */
	public Fruit() {
		super();
		
        this.mThresholdFirst = Fruit.DEFAULT_THRESHOLD_FIRST;
        this.mThresholdSecond = Fruit.DEFAULT_THRESHOLD_SECOND;
        this.mVisibleLower = Fruit.DEFAULT_VISIBLE_LOWER;
        this.mVisibleUpper = Fruit.DEFAULT_VISIBLE_UPPER;
	}
	
	/**
	 * Return the number of points the current fruit is worth and hide from the screen.
	 * 
	 * @return Integer point value.
	 */
	public int eat() {
		this.hide();
		return this.mType.points;
	}
	
	/**
	 * Move off screen and make invisible.
	 */
	private void hide() {
		this.mVisible = false;
		this.mPosition.set(-1, -1);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.jakewharton.wakkawallpaper.Entity#tick(com.jakewharton.wakkawallpaper.Game)
	 */
	@Override
	public void tick(Game game) {
		if (this.mVisible) {
			if ((System.currentTimeMillis() - this.mCreated) > this.mVisibleLength) {
				this.newLevel(game);
			}
		} else {
			final int dotsEaten = game.getDotsEaten();
			if ((dotsEaten > this.mThresholdFirst) || (dotsEaten > this.mThresholdSecond)) {
				this.mVisible = true; 
				this.mVisibleLength = Game.RANDOM.nextInt(this.mVisibleUpper - this.mVisibleLower + 1) + this.mVisibleLower;
				//TODO: determine random valid location
				this.mCreated = System.currentTimeMillis();
			}
		}
	}

    /*
     * (non-Javadoc)
     * @see com.jakewharton.wakkawallpaper.Entity#draw(android.graphics.Canvas)
     */
	@Override
	public void draw(Canvas c) {
		if (this.mType != null) {
			c.save();
			c.translate(this.mLocation.x - this.mCellWidthOverTwo, this.mLocation.y - this.mCellHeightOverTwo);
			
			switch (this.mType) {
				case CHERRY:
					break;
					
				case STRAWBERRY:
					break;
					
				case PEACH:
					break;
					
				case APPLE:
					break;
					
				case GRAPES:
					break;
					
				case GALAXIAN:
					break;
					
				case BELL:
					break;
					
				case KEY:
					break;
			}
			
			c.restore();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.jakewharton.wakkawallpaper.Entity#moved(com.jakewharton.wakkawallpaper.Game)
	 */
	@Override
	protected void moved(Game game) {
		//We do not move
	}

	/*
	 * (non-Javadoc)
	 * @see com.jakewharton.wakkawallpaper.Entity#reset(com.jakewharton.wakkawallpaper.Game)
	 */
	@Override
	protected void newLevel(Game game) {
		this.hide();
		this.mType = Fruit.getForLevel(game.getLevel());
	}
	

	/**
	 * Return which type of fruit should appear on which level.
	 * 
	 * @param level The level you wish to get fruit for.
	 * @return The Type of fruit for the level.
	 */
	private static Type getForLevel(int level) {
		if (level <= 0) {
			throw new IllegalArgumentException("Level number must be greater than zero.");
		}
		
		switch (level) {
			case 1:
				return Type.CHERRY;
			case 2:
				return Type.STRAWBERRY;
			case 3:
			case 4:
				return Type.PEACH;
			case 5:
			case 6:
				return Type.APPLE;
			case 7:
			case 8:
				return Type.GRAPES;
			case 9:
			case 10:
				return Type.GALAXIAN;
			case 11:
			case 12:
				return Type.BELL;
			default:
				return Type.KEY;
		}
	}
}
