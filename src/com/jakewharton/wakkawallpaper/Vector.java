package com.jakewharton.wakkawallpaper;

import android.graphics.Point;

import com.jakewharton.wakkawallpaper.Entity.Direction;

/**
 * Encapsulates a position and direction.
 * 
 * @author Jake Wharton
 */
public class Vector {
	private final Point mPosition;
	private final Direction mDirection;
	private final Direction mInitialDirection;
	
	/**
	 * Creates a Vector at a position setting both the direction and initial direction the same value.
	 * 
	 * @param position Position of the Vector.
	 * @param direction Direction of the Vector.
	 */
	public Vector(final Point position, final Direction direction) {
		this(position, direction, direction);
	}
	
	/**
	 * Creates a Vector at a position with a direction and an initial direction.
	 * 
	 * @param position Position of Vector.
	 * @param direction Direction of Vector.
	 * @param initialDirection Initial direction of Vector.
	 */
	private Vector(final Point position, final Direction direction, final Direction initialDirection) {
		this.mPosition = position;
		this.mDirection = direction;
		this.mInitialDirection = initialDirection;
	}
	
	public Point getPosition() {
		return this.mPosition;
	}
	public Direction getDirection() {
		return this.mDirection;
	}
	public Direction getInitialDirection() {
		return this.mInitialDirection;
	}
	public Vector[] getPossibleMoves() {
		final Vector[] moves = new Vector[4];
		int i = 0;
		
		if (this.mDirection != null) {
			//favor the same direction
			moves[i++] = new Vector(Entity.move(this.mPosition, this.mDirection), this.mDirection, this.mInitialDirection);
		}
		
		//add other three directions (four when this.mDirection is null)
		for (Direction direction : Direction.values()) {
			if (direction != this.mDirection) {
				moves[i++] = new Vector(Entity.move(this.mPosition, direction), direction, this.mInitialDirection);
			}
		}
		
		return moves;
	}
}