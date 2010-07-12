package com.jakewharton.wakkawallpaper;

import android.graphics.Canvas;

public class Fruit extends Entity {
	enum Type { CHERRY, STRAWBERRY, ORANGE, APPLE }
	
	private Type mType;
	
	public Fruit(int startingPositionX, int startingPositionY, Type type) {
		super(startingPositionX, startingPositionY, null);
		
		this.mType = type;
	}
	
	public void tick(Game game) {
		
	}
	
	public void draw(Canvas c) {
		c.save();
		c.translate(this.getLocationX(), this.getLocationY());
		
		switch (this.mType) {
			case CHERRY:
				break;
				
			case STRAWBERRY:
				break;
				
			case ORANGE:
				break;
				
			case APPLE:
				break;
		}
		
		c.restore();
	}
}
