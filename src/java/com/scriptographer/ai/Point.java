/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2008 Juerg Lehni, http://www.scratchdisk.com.
 * All rights reserved.
 *
 * Please visit http://scriptographer.com/ for updates and contact.
 *
 * -- GPL LICENSE NOTICE --
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * -- GPL LICENSE NOTICE --
 *
 * File created on 20.12.2004.
 *
 * $Id:Point.java 402 2007-08-22 23:24:49Z lehni $
 */

package com.scriptographer.ai;

import java.awt.geom.Point2D;

import com.scratchdisk.script.ArgumentReader;
import com.scriptographer.ui.Size;

/**
 * The Point object represents a point in the two dimensional space of the
 * Illustrator document. Some functions also use it as a two dimensional vector
 * object.
 * 
 * @jsextension {@type field} {@name selected} {@reference SegmentPoint#selected} {@after angle}
 * 
 * @author lehni
 */
public class Point {

	protected double x;
	protected double y;
	
	public Point() {
		x = y = 0;
	}

	/**
	 * Creates a Point object with the given x and y coordinates.
	 * 
	 * Sample code:
	 * <code>
	 * // Create a point at x: 10pt, y: 5pt
	 * var point = new Point(10, 5);
	 * print(point.x); // 10
	 * print(point.y); // 5
	 * </code>
	 * @param x The x coordinate of the point {@default 0}
	 * @param y The y coordinate of the point {@default 0}
	 */
	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public Point(float x, float y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Creates a Point object using the coordinates of the given Point object.
	 * @param point
	 */
	public Point(Point point) {
		this(point != null ? point.x : 0, point != null ? point.y : 0);
	}

	/**
	 * @jshide
	 */
	public Point(Point2D point) {
		this(point != null ? point.getX() : 0, point != null ? point.getY() : 0);
	}

	/**
	 * Creates a Point object using the width and height values of the given
	 * Size object.
	 * 
	 * Sample code:
	 * <code>
	 * // Create a Size with a width of 100pt and a height of 50pt
	 * var size = new Size(100, 50);
	 * print(size); // prints { width: 100.0, height: 50.0 }
	 * var point = new Point(size);
	 * print(point); // prints { x: 100.0, y: 50.0 }
	 * </code>
	 * 
	 * @param size
	 */
	public Point(Size size) {
		x = size.width;
		y = size.height;
	}

	/**
	 * @jshide
	 */
	public Point(ArgumentReader reader) {
		this(reader.has("x") ? reader.readDouble("x", 0) : reader.readDouble("width", 0),
				reader.has("y") ? reader.readDouble("y", 0) : reader.readDouble("height", 0));
	}

	/**
	 * @jshide
	 */
	public void set(double x, double y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * @jshide
	 */
	public void set(Point point) {
		if (point != null)
			set(point.x, point.y);
		else
			set(0, 0);
	}

	/**
	 * The x coordinate of the point
	 */
	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	/**
	 * The y coordinate of the point
	 */
	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	/**
	 * Returns a copy of the point.
	 * This is useful as the following code only generates a flat copy:
	 * 
	 * <code>
	 * var point1 = new Point();
	 * var point2 = point1;
	 * point2.x = 1; // also changes point1.x
	 * 
	 * var point2 = point1.clone();
	 * point2.x = 1; // doesn't change point1.x
	 * </code>
	 * 
	 * @return the cloned point
	 */
	public Object clone() {
		return new Point(this);
	}

	/**
	 * @jshide
	 */
	public Point add(double x, double y) {
		return new Point(this.x + x, this.y + y);
	}

	/**
	 * Returns the addition of the supplied point to the point as a new
	 * point.
	 * The object itself is not modified!
	 * 
	 * Sample code:
	 * <code>
	 * var point1 = new Point(5, 10);
	 * var point2 = new Point(10, 20);
	 * var result = point1 + point2;
	 * print(result); // { x: 15.0, y: 30.0 }
	 * </code>
	 * 
	 * @param point the point to add
	 * @return the addition of the two points as a new point
	 */
	public Point add(Point point) {
		return add(point.x, point.y);
	}

	/**
	 * Returns the addition of the supplied value to both coordinates of
	 * the point as a new point.
	 * The object itself is not modified!
	 * 
	 * Sample code:
	 * <code>
	 * var point = new Point(5, 10);
	 * var result = point + 20;
	 * print(result); // { x: 25.0, y: 30.0 }
	 * </code>
	 * 
	 * @param value the value to add
	 * @return the addition of the point and the value as a new point
	 */
	public Point add(double value) {
		return add(value, value);
	}

	/**
	 * Returns the subtraction of the supplied x and y values from the point
	 * as a new point.
	 * The object itself is not modified!
	 * 
	 * Sample code:
	 * <code>
	 * var firstPoint = new Point(10, 20);
	 * var result = firstPoint.subtract(5,5);
	 * print(result); // { x: 5.0, y: 15.0 }
	 * </code>
	 * 
	 * @param x The x value to subtract
	 * @param y The y value to subtract
	 * @return the subtraction of the two points as a new point
	 * 
	 * @jshide
	 */
	public Point subtract(double x, double y) {
		return new Point(this.x - x, this.y - y);
	}

	/**
	 * Returns the subtraction of the supplied point from the point as a
	 * new point.
	 * The object itself is not modified!
	 * 
	 * Sample code:
	 * <code>
	 * var firstPoint = new Point(10, 20);
	 * var secondPoint = new Point(5, 5);
	 * var result = firstPoint - secondPoint;
	 * print(result); // { x: 5.0, y: 15.0 }
	 * </code>
	 * 
	 * @param point the point to subtract
	 * @return the subtraction of the two points as a new point
	 */
	public Point subtract(Point point) {
		return subtract(point.x, point.y);
	}

	/**
	 * Returns the subtraction of the supplied value from both coordinates of
	 * the point as a new point.
	 * The object itself is not modified!
	 * 
	 * Sample code:
	 * <code>
	 * var point = new Point(10, 20);
	 * var result = point - 5;
	 * print(result); // { x: 5.0, y: 15.0 }
	 * </code>
	 * 
	 * @param point the value to subtract
	 * @return the subtraction of the value from the point as a new point
	 */
	public Point subtract(double value) {
		return subtract(value, value);
	}

	/**
	 * Returns the multiplication of the point with the supplied x and y
	 * values as a new point. When no y value is supplied, the point's x and y
	 * values are multiplied by scale (x).
	 * The object itself is not modified!
	 * 
	 * Sample code:
	 * <code>
	 * var point = new Point(5, 10);
	 * 
	 * var result = point.multiply(4, 2);
	 * print(result); // { x: 20.0, y: 20.0 }
	 * 
	 * var result = point.multiply(2);
	 * print(result); // { x: 10.0, y: 20.0 }
	 * </code>
	 * 
	 * @param x the x (or scale) value to multiply with
	 * @param y the y value to multiply with
	 * @return the multiplication of the two points as a new point
	 * 
	 * @jshide
	 */
	public Point multiply(double x, double y) {
		return new Point(this.x * x, this.y * y);
	}

	/**
	 * Returns the multiplication of the point with the supplied point as a
	 * new point.
	 * The object itself is not modified!
	 * 
	 * Sample code:
	 * <code>
	 * var firstPoint = new Point(5, 10);
	 * var secondPoint = new Point(4, 2);
	 * var result = firstPoint * secondPoint;
	 * print(result); // { x: 20.0, y: 20.0 }
	 * </code>
	 * 
	 * @param point the point to multiply with
	 * @return the multiplication of the two points as a new point
	 */
	public Point multiply(Point point) {
		return multiply(point.x, point.y);
	}

	/**
	 * Returns the multiplication of the supplied value with both coordinates of
	 * the point as a new point.
	 * The object itself is not modified!
	 * 
	 * Sample code:
	 * <code>
	 * var point = new Point(10, 20);
	 * var result = point * 2;
	 * print(result); // { x: 20.0, y: 40.0 }
	 * </code>
	 * 
	 * @param point the value to multiply with
	 * @return the multiplication of the point by the supplied value as a new
	 *         point
	 */
	public Point multiply(double value) {
		return multiply(value, value);
	}

	/**
	 * @jshide
	 */
	public Point divide(double x, double y) {
		return new Point(this.x / x, this.y / y);
	}

	/**
	 * Returns the division of the point by the supplied point as a
	 * new point.
	 * The object itself is not modified!
	 * 
	 * Sample code:
	 * <code>
	 * var firstPoint = new Point(8, 10);
	 * var secondPoint = new Point(2, 5);
	 * var result = firstPoint / secondPoint;
	 * print(result); // { x: 4.0, y: 2.0 }
	 * </code>
	 * 
	 * @param point the point to divide by
	 * @return the division of the two points as a new point
	 */
	public Point divide(Point point) {
		return divide(point.x, point.y);
	}

	/**
	 * Returns the division of both coordinates of the point by the supplied
	 * value and returns it as a new point.
	 * The object itself is not modified!
	 * 
	 * Sample code:
	 * <code>
	 * var point = new Point(10, 20);
	 * var result = point / 2;
	 * print(result); // { x: 5.0, y: 10.0 }
	 * </code>
	 * 
	 * @param point the value to divide by
	 * @return the division of the point by the supplied value as a new point
	 */
	public Point divide(double value) {
		return divide(value, value);
	}

	/**
	 * @jshide
	 */
	public Point negate() {
		return new Point(-x, -y);
	}

	/**
	 * Checks wether the coordinates of the point are equal to that of the
	 * supplied point.
	 * 
	 * Sample code:
	 * <code>
	 * var point = new Point(5, 10);
	 * print(point == new Point(5, 10)); // true
	 * print(point == new Point(1, 1)); // false
	 * print(point != new Point(1, 1)); // true
	 * </code>
	 */
	public boolean equals(Object object) {
		if (object instanceof Point) {
			Point pt = (Point) object;
			return pt.x == x && pt.y == y;
		} else {
			// TODO: support other point types?
			return false;
		}
	}

	/**
	 * Checks whether the point is inside the boundaries of the rectangle.	
	 * 
	 * @param rect the rectangle to check against
	 * @return {@true if the point is inside the rectangle}
	 */
	public boolean isInside(Rectangle rect) {
		return rect.contains(this);
	}

	/**
	 * Checks if the point is within a given distance of another point.
	 * 
	 * @param point the point to check against
	 * @param tolerance the maximum distance allowed
	 * @return {@true if it is within the given distance}
	 */
	public boolean isClose(Point point, double tolerance) {
		return getDistance(point) < tolerance;
	}

	/**
	 * Checks if the vector represented by this point is parallel (collinear) to
	 * another vector.
	 * 
	 * @param point the vector to check against
	 * @return {@true if it is parallel}
	 */
	public boolean isParallel(Point point) {
		return Math.abs(x / point.x - y / point.y) < 0.00001;
	}
	
	/**
	 * Returns the distance between the point and another point.
	 * 
	 * Sample code:
	 * <code>
	 * var firstPoint = new Point(5, 10);
	 * 
	 * var distance = firstPoint.getDistance(5, 20);
	 * 
	 * print(distance); // 10
	 * </code>
	 * @param px
	 * @param py
	 * 
	 * @jshide
	 */
	public double getDistance(double px, double py) {
		px -= x;
		py -= y;
		return Math.sqrt(px * px + py * py);
	}

	/**
	 * Returns the distance between the point and another point.
	 * 
	 * Sample code:
	 * <code>
	 * var firstPoint = new Point(5, 10);
	 * var secondPoint = new Point(5, 20);
	 * 
	 * var distance = firstPoint.getDistance(secondPoint);
	 * 
	 * print(distance); // 10
	 * </code>
	 * 
	 * @param px
	 * @param py
	 */
	public double getDistance(Point point) {
		return getDistance(point.x, point.y);
	}

	/**
	 * @jshide
	 */
	public double getDistanceSquared(double px, double py) {
		px -= x;
		py -= y;
		return px * px + py * py;
	}

	/**
	 * @jshide
	 */
	public double getDistanceSquared(Point point) {
		return getDistanceSquared(point.x, point.y);
	}

	/**
	 * The length of the vector that is represented by this point's coordinates.
	 * Each point can be interpreted as a vector that points from the origin
	 * ({@code x = 0},{@code y = 0}) to the point's location.
	 * Setting the length changes the location but keeps the vector's angle.
	 */
	public double getLength() {
		return Math.sqrt(x * x + y * y);
	}

	public void setLength(double length) {
		double len = getLength();
		if (len != 0) {
			double scale = length / len;
			x *= scale;
			y *= scale;
		}
	}

	/**
	 * The angle from the x axis to the vector in radians,
	 * measured in counter clockwise direction.
	 */
	public double getAngle() {
		return Math.atan2(y, x);
	}

	/**
	 * Returns the smaller angle between two vectors in radians.
	 * The angle is unsigned, no information about rotational
	 * direction is given.
	 * 
	 * @param point
	 */
	public double getAngle(Point point) {
		double div = getLength() * point.getLength();
		if (div == 0) return Double.NaN;
		else return Math.acos(this.dot(point) / div);
	}

	/**
	 * Returns the angle between two vectors in radians.
	 * The angle is directional and signed, giving information about
	 * the rotational direction.
	 * 
	 * @param point
	 */
	public double getDirectedAngle(Point point) {
		double angle = this.getAngle() - point.getAngle();
		if (angle < -Math.PI)
			return angle + Math.PI * 2;
		else if (angle > Math.PI)
			return angle - Math.PI * 2;
		return angle;
	}

	/**
	 * Returns the interpolation point between the point and another point.
	 * The object itself is not modified!
	 * 
	 * @param point
	 * @param t the position between the two points as a value between 0 and 1
	 * @return the interpolation point
	 * 
	 * @jshide
	 */
	public Point interpolate(Point point, double t) {
		return new Point(
			x * (1f - t) + point.x * t,
			y * (1f - t) + point.y * t
		);
	}

	public Point normalize(double length) {
		double len = getLength();
		if (len != 0) {
			double scale = length / len;
			return new Point(x * scale, y * scale);
		} else {
			return new Point(this);
		}
	}

	public Point normalize() {
		return normalize(1);
	}

	/**
	 * Rotates the point by the given angle.
	 * The object itself is not modified.
	 * 
	 * @param angle the rotation angle in radians
	 * @return the rotated point
	 */
	public Point rotate(double angle) {
		double s = Math.sin(angle);
		double c = Math.cos(angle);
		return new Point(
				x * c - y * s,
				y * c + x * s
 		);
	}

	/**
	 * Rotates the point around a center point.
	 * The object itself is not modified.
	 * 
	 * @param angle the rotation angle in radians
	 * @param center the center point of the rotation
	 * @return the rotated point
	 */
	public Point rotate(double angle, Point center) {
		return rotate(angle,
				center != null ? center.x : 0,
				center != null ? center.y : 0
		);
	}

	/**
	 * Rotates the point around a center point.
	 * The object itself is not modified.
	 * 
	 * @param angle the rotation angle in radians
	 * @param x the x coordinate of the center point
	 * @param y the y coordinate of the center point
	 * @return the rotated point
	 * 
	 * @jshide
	 */
	public Point rotate(double angle, double x, double y) {
		return subtract(x, y).rotate(angle).add(x, y);
	}

	/**
	 * Returns the dot product of the point and another point.
	 * @param point
	 * @return the dot product of the two points
	 */

	public double dot(Point point) {
		return x * point.x + y * point.y;
	}

	/**
	 * Returns the projection of the point on another point.
	 * Both points are interpreted as vectors.
	 * 
	 * @param point
	 * @return the project of the point on another point
	 */
	public Point project(Point point) {
		if (point.x == 0 && point.y == 0) {
			return new Point(0, 0);
		} else {
			double scale = dot(point) / point.dot(point);
			return new Point(
				point.x * scale,
				point.y * scale
			);
		}
	}

	public Point transform(Matrix matrix) {
		return matrix.transform(this);
	}

	protected Point2D toPoint2D() {
		return new Point2D.Double(x, y);
	}

	public String toString() {
	   	return "{ x: " + x + ", y: " + y + " }";
	}
}
