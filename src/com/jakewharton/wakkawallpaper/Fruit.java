package com.jakewharton.wakkawallpaper;

import android.graphics.Canvas;

public class Fruit extends Entity {
	enum Type {
		CHERRY(100), STRAWBERRY(300), PEACH(500), APPLE(700), GRAPES(1000), GALAXIAN(2000), BELL(3000), KEY(5000);
		
		public final int points;
		
		private Type(int points) {
			this.points = points;
		}
		
		public static Type getForLevel(int level) {
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
					if (level > 0) {
						return KEY;
					} else {
						throw new IllegalArgumentException("Level number must be greater than zero.");
					}
			}
		}
	}
	
	private final Type mType;
	private final int mVisible;
	private final long mCreated;
	
	public Fruit(int startingPositionX, int startingPositionY, Type type, int visible) {
		super(startingPositionX, startingPositionY, Direction.STOPPED);
		
		this.mType = type;
		this.mVisible = visible;
		this.mCreated = System.currentTimeMillis();
	}
	
	public boolean isStillVisible() {
		return ((System.currentTimeMillis() - this.mCreated) <= this.mVisible);
	}
	public int getPoints() {
		return this.mType.points;
	}
	
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
