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
		
		/**
		 * Return which type of fruit should appear on which level.
		 * 
		 * @param level The level you wish to get fruit for.
		 * @return The Type of fruit for the level.
		 */
		public static Type getForLevel(int level) {
			if (level <= 0) {
				throw new IllegalArgumentException("Level number must be greater than zero.");
			}
			
			switch (level) {
				case 1:
					return CHERRY;
				case 2:
					return STRAWBERRY;
				case 3:
				case 4:
					return PEACH;
				case 5:
				case 6:
					return APPLE;
				case 7:
				case 8:
					return GRAPES;
				case 9:
				case 10:
					return GALAXIAN;
				case 11:
				case 12:
					return BELL;
				default:
					return KEY;
			}
		}
	}
	
	private final Type mType;
	private final int mVisible;
	private final long mCreated;
	
	/**
	 * Initialize a new fruit adhering to the parameters.
	 * 
	 * @param startingPositionX X coordinate of the position of the fruit.
	 * @param startingPositionY Y coordinate of the position of the fruit.
	 * @param type Type value representing the type of fruit.
	 * @param visible The length (in milliseconds) that the fruit will be visible on screen.
	 */
	public Fruit(int startingPositionX, int startingPositionY, Type type, int visible) {
		super(startingPositionX, startingPositionY, Direction.STOPPED);
		
		this.mType = type;
		this.mVisible = visible;
		this.mCreated = System.currentTimeMillis();
	}
	
	/**
	 * Boolean to indicate whether or not the fruit should still be visible.
	 * 
	 * @return Boolean.
	 */
	public boolean isStillVisible() {
		return ((System.currentTimeMillis() - this.mCreated) <= this.mVisible);
	}
	
	/**
	 * The number of points the current fruit is worth.
	 * 
	 * @return Integer point value.
	 */
	public int getPoints() {
		return this.mType.points;
	}

    /**
     * Render the entity on the Canvas.
     * 
     * @param c Canvas to draw on.
     */
	@Override
	public void draw(Canvas c) {
		c.save();
		c.translate(this.getLocationX(), this.getLocationY());
		
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
