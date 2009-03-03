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
 * @author lehni
 */
public class Point {

	protected double x;
	protected double y;
	
	public Point() {
		x = y = 0;
	}

	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public Point(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public Point(Point pt) {
		this(pt != null ? pt.x : 0, pt != null ? pt.y : 0);
	}

	public Point(Point2D p) {
		this(p != null ? p.getX() : 0, p != null ? p.getY() : 0);
	}

	public Point(Size size) {
		x = size.width;
		y = size.height;
	}

	public Point(ArgumentReader reader) {
		this(reader.has("x") ? reader.readDouble("x", 0) : reader.readDouble("width", 0),
				reader.has("y") ? reader.readDouble("y", 0) : reader.readDouble("height", 0));
	}

	public void set(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public void set(Point pt) {
		if (pt != null)
			set(pt.x, pt.y);
		else
			set(0, 0);
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	/**
	 * Returns a copy of the point. This is useful as the following code only
	 * generates a flat copy:
	 * 
	 * <pre>
	 * var point1 = new Point();
	 * var point2 = point1;
	 * point2.x = 1; // also changes point1.x
	 * 
	 * var point2 = pt1.clone();
	 * point2.x = 1; // doesn't change point1.x
	 * </pre>
	 * 
	 * @return the cloned point
	 */
	public Object clone() {
		return new Point(this);
	}

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
	 * Returns the addition of the supplied x and y values to the point object
	 * as a new point. The object itself is not modified!
	 * Sample code:
	 * <pre>
	 * var firstPoint = new Point(5,10);
	 * var secondPoint = firstPoint.add(10,20);
	 * print(secondPoint); // returns { x: 15.0, y: 30.0 }
	 * </pre>
	 * 
	 * @param x the x value to add
	 * @param y the y value to add
	 * @return the addition of the two points as a new point
	 */
	public Point add(double x, double y) {
		return new Point(this.x + x, this.y + y);
	}

	/**
	 * Returns the addition of the supplied point to the point object as a new
	 * point. The object itself is not modified!
	 * Sample code:
	 * <pre>
	 * var firstPoint = new Point(5,10);
	 * var secondPoint = new Point(10,20);
	 * var thirdPoint = firstPoint.add(secondPoint);
	 * print(thirdPoint); // returns { x: 15.0, y: 30.0 }
	 * </pre>
	 * 
	 * @param pt the point to add
	 * @return the addition of the two points as a new point
	 */
	public Point add(Point pt) {
		return add(pt.x, pt.y);
	}

	public Point add(double value) {
		return add(value, value);
	}

	/**
	 * Returns the subtraction of the supplied x and y values to the point
	 * object as a new point. The object itself is not modified!
	 * Sample code:
	 * <pre>
	 * var firstPoint = new Point(10,20);
	 * var secondPoint = firstPoint.subtract(5,5);
	 * print(secondPoint); // returns { x: 5.0, y: 15.0 }
	 * </pre>
	 * 
	 * @param x The x value to subtract
	 * @param y The y value to subtract
	 * @return the subtraction of the two points as a new point
	 */
	public Point subtract(double x, double y) {
		return new Point(this.x - x, this.y - y);
	}

	/**
	 * Returns the subtraction of the supplied point to the point object as a
	 * new point. The object itself is not modified!
	 * Sample code:
	 * <pre>
	 * var firstPoint = new Point(10,20);
	 * var secondPoint = new Point(5,5);
	 * var thirdPoint = firstPoint.subtract(secondPoint);
	 * print(thirdPoint); // returns { x: 5.0, y: 15.0 }
	 * </pre>
	 * 
	 * @param pt the point to subtract
	 * @return the subtraction of the two points as a new point
	 */
	public Point subtract(Point pt) {
		return subtract(pt.x, pt.y);
	}

	public Point subtract(double value) {
		return subtract(value, value);
	}

	/**
	 * Returns the multiplication of the point object by the supplied x and y
	 * values as a new point. When no y value is supplied, the point's x and y
	 * values are multiplied by scale (x). The object itself is not modified!
	 * Sample code:
	 * <pre>
	 * var firstPoint = new Point(5,10);
	 * 
	 * var secondPoint = firstPoint.multiply(4,2);
	 * print(secondPoint); // returns { x: 20.0, y: 20.0 }
	 * 
	 * var secondPoint = firstPoint.multiply(2);
	 * print(secondPoint); // returns { x: 10.0, y: 20.0 }
	 * </pre>
	 * 
	 * @param x the x (or scale) value to multiply with
	 * @param y the y value to multiply with
	 * @return the multiplication of the two points as a new point
	 */
	public Point multiply(double x, double y) {
		return new Point(this.x * x, this.y * y);
	}

	/**
	 * Returns the multiplication of the point object by the supplied point as a
	 * new point. The object itself is not modified!
	 * Sample code:
	 * <pre>
	 * var firstPoint = new Point(5,10);
	 * var secondPoint = new Point(4,2);
	 * var thirdPoint = firstPoint.multiply(secondPoint);
	 * print(thirdPoint); // returns { x: 20.0, y: 20.0 }
	 * </pre>
	 * 
	 * @param pt the point to multiply with
	 * @return the multiplication of the two points as a new point
	 */
	public Point multiply(Point pt) {
		return multiply(pt.x, pt.y);
	}

	public Point multiply(double value) {
		return multiply(value, value);
	}

	public Point divide(double x, double y) {
		return new Point(this.x / x, this.y / y);
	}

	public Point divide(Point pt) {
		return divide(pt.x, pt.y);
	}

	public Point divide(double value) {
		return divide(value, value);
	}

	public Point negate() {
		return new Point(-x, -y);
	}

	/**
	 * Checks if the point is within a given distance of another point
	 * 
	 * @param pt the point to check against
	 * @param tolerance the maximum distance allowed
	 * @return <code>true</code> if it is within the given distance, false
	 *         otherwise
	 */
	public boolean isClose(Point pt, double tolerance) {
		return getDistance(pt) < tolerance;
	}
	
	/**
	 * Returns the distance between the point and another point.
	 * Sample code:
	 * <pre>
	 * var firstPoint = new Point(5,10);
	 * 
	 * var distance = firstPoint.getDistance(5,20);
	 * 
	 * print(distance); // returns 10
	 * </pre>
	 * @param px
	 * @param py
	 * @return
	 */
	public double getDistance(double px, double py) {
		px -= x;
		py -= y;
		return Math.sqrt(px * px + py * py);
	}

	/**
	 * Returns the distance between the point and another point.
	 * Sample code:
	 * <pre>
	 * var firstPoint = new Point(5, 10);
	 * var secondPoint = new Point(5, 20);
	 * 
	 * var distance = firstPoint.getDistance(secondPoint);
	 * 
	 * print(distance); // returns 10
	 * </pre>
	 * 
	 * @param px
	 * @param py
	 * @return
	 */
	public double getDistance(Point pt) {
		return getDistance(pt.x, pt.y);
	}

	public double getDistanceSquared(double px, double py) {
		px -= x;
		py -= y;
		return px * px + py * py;
	}

	public double getDistanceSquared(Point pt) {
		return getDistanceSquared(pt.x, pt.y);
	}

	public double getLength() {
		return Math.sqrt(x * x + y * y);
	}

	/**
	 * Returns the angle from the x axis to the vector in radians,
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
	 * @param pt
	 * @return
	 */
	public double getAngle(Point pt) {
		double div = getLength() * pt.getLength();
		if (div == 0) return Double.NaN;
		else return Math.acos(this.dot(pt) / div);
	}

	/**
	 * Returns the angle between two vectors in radians.
	 * The angle is directional and signed, giving information about
	 * the rotational direction.
	 * 
	 * @param pt
	 * @return
	 */
	public double getDirectedAngle(Point pt) {
		double angle = this.getAngle() - pt.getAngle();
		if (angle < -Math.PI)
			return angle + Math.PI * 2;
		else if (angle > Math.PI)
			return angle - Math.PI * 2;
		return angle;
	}

	/**
	 * Returns the interpolation point between the point and another point. The
	 * object itself is not modified!
	 * 
	 * @param pt
	 * @param t the position between the two points as a value between 0 and 1
	 * @return the interpolation point
	 */
	public Point interpolate(Point pt, double t) {
		return new Point(
			x * (1f - t) + pt.x * t,
			y * (1f - t) + pt.y * t
		);
	}

	/**
	 * Checks whether the point is inside the rectangle
	 * 
	 * @param rect the rectangle to check against
	 * @return <code>true</code> if the point is inside the rectangle, false
	 *         otherwise
	 */
	public boolean isInside(Rectangle rect) {
		return rect.contains(this);
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
	 * @param theta the rotation angle in radians
	 * @return the rotated point
	 */
	public Point rotate(double theta) {
		double s = Math.sin(theta);
		double c = Math.cos(theta);
		return new Point(
				x * c - y * s,
				y * c + x * s
 		);
	}

	/**
	 * Rotates the point around a center point.
	 * The object itself is not modified.
	 * 
	 * @param theta the rotation angle in radians
	 * @param center the center point of the rotation
	 * @return the rotated point
	 */
	public Point rotate(double theta, Point center) {
		return rotate(theta,
				center != null ? center.x : 0,
				center != null ? center.y : 0
		);
	}

	/**
	 * Rotates the point around a center point.
	 * The object itself is not modified.
	 * 
	 * @param theta the rotation angle in radians
	 * @param x the x coordinate of the center point
	 * @param y the y coordinate of the center point
	 * @return the rotated point
	 */
	public Point rotate(double theta, double x, double y) {
		return subtract(x, y).rotate(theta).add(x, y);
	}

	/**
	 * Returns the dot product of the point and another point.
	 * @param pt
	 * @return the dot product of the two points
	 */

	public double dot(Point pt) {
		return x * pt.x + y * pt.y;
	}

	/**
	 * Returns the projection of the point on another point. Both points are
	 * interpreted as vectors.
	 * 
	 * @param pt
	 * @return the project of the point on another point
	 */
	public Point project(Point pt) {
		if (pt.x == 0 && pt.y == 0) {
			return new Point(0, 0);
		} else {
			double scale = dot(pt) / pt.dot(pt);
			return new Point(
				pt.x * scale,
				pt.y * scale
			);
		}
	}

	public Point transform(Matrix m) {
		return m.transform(this);
	}

	/**
	 * @return
	 */
	protected Point2D toPoint2D() {
		return new Point2D.Double(x, y);
	}

	public String toString() {
	   	return "{ x: " + x + ", y: " + y + " }";
	}
}
