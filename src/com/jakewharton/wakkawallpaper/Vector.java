package com.jakewharton.wakkawallpaper;

import android.graphics.Point;

import com.jakewharton.wakkawallpaper.Entity.Direction;

/**
 * Encapsulates a position and direction.
 * 
 * @author Jake Wharton
 */
public class Vector {
	public final Point position;
	public final Direction direction;
	public final Direction initialDirection;
	
	/**
	 * Creates a Vector at a position setting both the direction and initial direction the same value.
	 * 
	 * @param position Position of the Vector.
	 * @param direction Direction of the Vector.
	 */
	public Vector(final Point position, final Direction direction) {
		this(position, direction, null);
	}
	
	/**
	 * Creates a Vector at a position with a direction and an initial direction.
	 * 
	 * @param position Position of Vector.
	 * @param direction Direction of Vector.
	 * @param initialDirection Initial direction of Vector.
	 */
	private Vector(final Point position, final Direction direction, final Direction initialDirection) {
		this.position = position;
		this.direction = direction;
		this.initialDirection = initialDirection;
	}
	
	public Vector[] getPossibleMoves() {
		final Vector[] moves = new Vector[4];
		int i = 0;
		
		if (this.direction != null) {
			//favor the same direction
			moves[i++] = new Vector(Entity.move(this.position, this.direction), this.direction, (this.initialDirection == null) ? this.direction : this.initialDirection);
		}
		
		//add other three directions (four when this.mDirection is null)
		for (Direction direction : Direction.values()) {
			if (direction != this.direction) {
				moves[i++] = new Vector(Entity.move(this.position, direction), direction, (this.initialDirection == null) ? direction : this.initialDirection);
			}
		}
		
		return moves;
	}
}