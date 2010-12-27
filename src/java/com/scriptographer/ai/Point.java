/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Scripting Plugin for Adobe Illustrator
 * http://scriptographer.org/
 *
 * Copyright (c) 2002-2010, Juerg Lehni
 * http://scratchdisk.com/
 *
 * All rights reserved. See LICENSE file for details.
 *
 * File created on 20.12.2004.
 */

package com.scriptographer.ai;

import java.awt.geom.Point2D;

import com.scratchdisk.script.ArgumentReader;
import com.scratchdisk.script.ChangeEmitter;
import com.scriptographer.ScriptographerEngine;

/**
 * The Point object represents a point in the two dimensional space of the
 * Illustrator document. It is also used to represent two dimensional vector
 * objects.
 * 
 * @jsreference {@type field} {@name selected} {@reference
 *              SegmentPoint#selected} {@after angle}
 * 
 * @author lehni
 */
public class Point implements ChangeEmitter {

	protected double x;
	protected double y;
	// Caching of angle if used
	protected Double angle;

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
	 * Creates a Point object using the coordinates of the given Point object.
	 * @param point
	 */
	public Point(Point point) {
		set(point);
	}

	/**
	 * @jshide
	 */
	public Point(Point2D point) {
		if (point != null)
			set(point.getX(), point.getY());
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
		// Reset angle
		angle = null;
	}

	/**
	 * @jshide
	 */
	public final void set(Point point) {
		if (point != null) {
			set(point.x, point.y);
			// Copy over angle, in case of length == 0
			angle = point.angle;
		} else {
			set(0, 0);
		}
	}

	/**
	 * The x coordinate of the point
	 */
	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
		// Reset angle
		angle = null;
	}

	/**
	 * The y coordinate of the point
	 */
	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
		// Reset angle
		angle = null;
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
		Point res = new Point(x * value, y * value);
		// Preserve angle
		res.angle = angle;
		return res;
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
		Point res = new Point(x / value, y / value);
		// Preserve angle
		res.angle = angle;
		return res;
	}

	/**
	 * @jshide
	 */
	public Point modulo(double x, double y) {
		return new Point(this.x % x, this.y % y);
	}

	/**
	 * The modulo operator returns the integer remainders of dividing the point
	 * by the supplied point as a new point.
	 * 
	 * Sample code:
	 * <code>
	 * var point = new Point(12, 6);
	 * print(point % new Point(5, 2)); // {x: 2, y: 0}
	 * </code>
	 * 
	 * @param point
	 * @return the integer remainders of dividing the points by each other as a
	 *         new point
	 */
	public Point modulo(Point point) {
		return modulo(point.x, point.y);
	}

	/**
	 * The modulo operator returns the integer remainders of dividing the point
	 * by the supplied value as a new point.
	 * 
	 * Sample code:
	 * <code>
	 * var point = new Point(12, 6);
	 * print(point % 5); // {x: 2, y: 1}
	 * </code>
	 * 
	 * @param value
	 * @return the integer remainders of dividing the point by the value as a new point
	 */
	public Point modulo(double value) {
		return modulo(value, value);
	}

	/**
	 * @jshide
	 */
	public Point negate() {
		return new Point(-x, -y);
	}

	/**
	 * Checks whether the coordinates of the point are equal to that of the
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
		}
		// TODO: support other point types?
		return false;
	}

	public Point transform(Matrix matrix) {
		return matrix.transform(this);
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
	 * {@grouptitle Distance & Length}
	 * 
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
		if (isZero()) {
			// Use angle now to set x and y
			if (angle != null) {
				double a = angle;
				x = Math.cos(a) * length;
				y = Math.sin(a) * length;
			} else {
				// Assume angle = 0
				x = length;
				// y is already 0
			}
		} else {
			double scale = length / getLength();
			if (scale == 0.0) {
				// Calculate angle now, so it will be preserved even when
				// x and y are 0.
				getAngle();
			}
			x *= scale;
			y *= scale;
		}
	}

	public Point normalize(double length) {
		double len = getLength();
		// Prevent division by 0
		double scale = len != 0 ? length / len : 0;
		Point res = new Point(x * scale, y * scale);
		// Preserve angle.
		res.angle = angle;
		return res;
	}

	public Point normalize() {
		return normalize(1);
	}

	/**
	 * For internal use regardless of userland angle units.
	 */
	protected double getAngleInRadians() {
		return Math.atan2(y, x);
	}

	/**
	 * For internal use regardless of userland angle units.
	 */
	protected double getAngleInDegrees() {
		return Math.atan2(y, x) * 180.0 / Math.PI;
	}

	/**
	 * The vector's angle, measured from the x-axis to the vector.
	 * 
	 * Angle units are controlled by the
	 * {@link com.scriptographer.sg.Script#getAngleUnits() } property, and are in
	 * degrees by default.
	 * 
	 * The angle orientation is controlled by the
	 * {@link com.scriptographer.sg.Script#getCoordinateSystem() } property,
	 * which is {@code 'top-down' } by default, leading to clockwise angle
	 * orientation. In the {@code 'bottom-up' } coordinate system, angles are
	 * specified in counter-clockwise orientation.
	 */
	public double getAngle() {
		// Cache the angle in the internal angle field, so we can return
		// that next time and also preserve the angle if length is set to 0.
		if (angle == null)
			angle = Math.atan2(y, x);
		return ScriptographerEngine.anglesInDegrees
				? angle * 180.0 / Math.PI
				: angle;
	}

	public int getQuadrant() {
		if (x >= 0) {
			if (y >= 0) {
				return 1;
			} else {
				return 4;
			}
		} else {
			if (y >= 0) {
				return 2;
			} else {
				return 3;
			}
		}
	}

	public void setAngle(double angle) {
		if (ScriptographerEngine.anglesInDegrees)
			angle = angle * Math.PI / 180.0;
		this.angle = angle;
		if (!isZero()) {
			double length = getLength();
			x = Math.cos(angle) * length;
			y = Math.sin(angle) * length;
		}
	}

	/**
	 * {@grouptitle Angle & Rotation}
	 * 
	 * Returns the smaller angle between two vectors. The angle is unsigned, no
	 * information about rotational direction is given.
	 * 
	 * Read more about angle units and orientation in the description of the
	 * {@link #getAngle()} property.
	 * 
	 * @param point
	 */
	public double getAngle(Point point) {
		double div = getLength() * point.getLength();
		if (div == 0) return Double.NaN;
		else {
			double angle = Math.acos(this.dot(point) / div);
			return ScriptographerEngine.anglesInDegrees
					? angle * 180.0 / Math.PI
					: angle;
		}
	}

	/**
	 * Returns the angle between two vectors. The angle is directional and
	 * signed, giving information about the rotational direction.
	 * 
	 * Read more about angle units and orientation in the description of the
	 * {@link #getAngle()} property.
	 * 
	 * @param point
	 */
	public double getDirectedAngle(Point point) {
		double angle = this.getAngle() - point.getAngle();
		double bounds = ScriptographerEngine.anglesInDegrees ? 180.0 : Math.PI;
		if (angle < -bounds)
			return angle + bounds * 2;
		else if (angle > bounds)
			return angle - bounds * 2;
		return angle;
	}

	/**
	 * Rotates the point by the given angle.
	 * The object itself is not modified.
	 * 
	 * Read more about angle units and orientation in the description of the
	 * {@link #getAngle()} property.
	 * 
	 * @param angle the rotation angle
	 * @return the rotated point
	 */
	public Point rotate(double angle) {
		if (ScriptographerEngine.anglesInDegrees)
			angle = angle * Math.PI / 180.0;
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
	 * Read more about angle units and orientation in the description of the
	 * {@link #getAngle()} property.
	 * 
	 * @param angle the rotation angle
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
	 * Read more about angle units and orientation in the description of the
	 * {@link #getAngle()} property.
	 * 
	 * @param angle the rotation angle
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

	/**
	 * {@grouptitle Tests}
	 * 
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
	 * Checks if this point has both the x and y coordinate set to 0. 
	 * 
	 * @return {@true if both x and y are 0}
	 */
	public boolean isZero() {
		return x == 0 && y == 0;
	}

	/**
	 * Checks if this point has an undefined value for at least one of its
	 * coordinates.
	 * 
	 * @return {@true if either x or y are not a number}
	 */
	public boolean isNaN() {
		return Double.isNaN(x) || Double.isNaN(y);
	}
	
	
	/**
	 * {@grouptitle Math Functions}
	 * 
	 * Returns a new point with rounded {@link #x} and {@link #y} values. The
	 * object itself is not modified!
	 * 
	 * Sample code:
	 * <code>
	 * var point = new Point(10.2, 10.9);
	 * var roundPoint = point.round();
	 * print(roundPoint); // { x: 10.0, y: 11.0 }
	 * </code>
	 */
	public Point round() {
		return new Point(Math.round(x), Math.round(y));
	}

	/**
	 * Returns a new point with the nearest greater non-fractional values to the
	 * specified {@link #x} and {@link #y} values. The object itself is not
	 * modified!
	 * 
	 * Sample code:
	 * <code>
	 * var point = new Point(10.2, 10.9);
	 * var ceilPoint = point.ceil();
	 * print(ceilPoint); // { x: 11.0, y: 11.0 }
	 * </code>
	 */
	public Point ceil() {
		return new Point(Math.ceil(x), Math.ceil(y));
	}

	/**
	 * Returns a new point with the nearest smaller non-fractional values to the
	 * specified {@link #x} and {@link #y} values. The object itself is not
	 * modified!
	 * 
	 * Sample code:
	 * <code>
	 * var point = new Point(10.2, 10.9);
	 * var floorPoint = point.floor();
	 * print(floorPoint); // { x: 10.0, y: 10.0 }
	 * </code>
	 */
	public Point floor() {
		return new Point(Math.floor(x), Math.floor(y));
	}

	/**
	 * Returns a new point with the absolute values of the specified {@link #x}
	 * and {@link #y} values. The object itself is not modified!
	 * 
	 * Sample code:
	 * <code>
	 * var point = new Point(-5, 10);
	 * var absPoint = point.abs();
	 * print(absPoint); // { x: 5.0, y: 10.0 }
	 * </code>
	 */
	public Point abs() {
		return new Point(Math.abs(x), Math.abs(y));
	}

	
	/**
	 * {@grouptitle Vectorial Math Functions}
	 * 
	 * Returns the dot product of the point and another point.
	 * @param point
	 * @return the dot product of the two points
	 */
	public double dot(Point point) {
		return x * point.x + y * point.y;
	}

	/**
	 * Returns the cross product of the point and another point.
	 * @param point
	 * @return the cross product of the two points
	 */
	public double cross(Point point) {
		return x * point.y - y * point.x;
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
	
	/**
	 * Returns a new point object with the smallest {@link #x} and
	 * {@link #y} of the supplied points.
	 * 
	 * Sample code:
	 * <code>
	 * var point1 = new Point(10, 100);
	 * var point2 = new Point(200, 5);
	 * var minPoint = Point.min(point1, point2);
	 * print(minPoint); // { x: 10.0, y: 5.0 }
	 * </code>
	 * 
	 * @param point1
	 * @param point2
	 * @return The newly created point object
	 */
	public static Point min(Point point1, Point point2) {
		return new Point(
				Math.min(point1.x, point2.x),
				Math.min(point1.y, point2.y));
	}

	/**
	 * Returns a new point object with the largest {@link #x} and
	 * {@link #y} of the supplied points.
	 * 
	 * Sample code:
	 * <code>
	 * var point1 = new Point(10, 100);
	 * var point2 = new Point(200, 5);
	 * var maxPoint = Point.max(point1, point2);
	 * print(maxPoint); // { x: 200.0, y: 100.0 }
	 * </code>
	 * 
	 * @param point1
	 * @param point2
	 * @return The newly created point object
	 */
	public static Point max(Point point1, Point point2) {
		return new Point(
				Math.max(point1.x, point2.x),
				Math.max(point1.y, point2.y));
	}

	/**
	 * Returns a point object with random {@link #x} and {@link #y} values
	 * between {@code 0} and {@code 1}.
	 * 
	 * Sample code:
	 * <code>
	 * var maxPoint = new Point(100, 100);
	 * var randomPoint = Point.random();
	 * 
	 * // A point between {x:0, y:0} and {x:100, y:100}:
	 * var point = maxPoint * randomPoint;
	 * </code>
	 */
	public static Point random() {
		return new Point(Math.random(), Math.random());
	}

	protected Point2D toPoint2D() {
		return new Point2D.Double(x, y);
	}

	public String toString() {
	   	return "{ x: " + ScriptographerEngine.numberFormat.format(x)
	   		+ ", y: " + ScriptographerEngine.numberFormat.format(y)
	   		+ " }";
	}
}
