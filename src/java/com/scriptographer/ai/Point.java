/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2007 Juerg Lehni, http://www.scratchdisk.com.
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
 * $Id$
 */

package com.scriptographer.ai;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;

import org.mozilla.javascript.*;

import com.scriptographer.js.Wrappable;

/*
 * Extend java.awt.geom.Point2D and adds other usefull functions
 */
/**
 * The Point object represents a point in the two dimensional space of the
 * Illustrator document. Some functions also use it as a two dimensional vector
 * object.
 * 
 * @author lehni
 */
public class Point extends java.awt.geom.Point2D.Float implements Wrappable {
	public Point() {
	}

	public Point(java.awt.Point p) {
		super(p.x, p.y);
	}

	public Point(Point2D pt) {
		super((float) pt.getX(), (float) pt.getY());
	}

	public Point(java.awt.Dimension d) {
		super(d.width, d.height);
	}

	public Point(float x, float y) {
		super(x, y);
	}
	
	public Point(double x, double y) {
		super((float) x, (float) y);
	}

	/*
	 * called from native code:
	 */
	public void setLocation(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public void setLocation(double x, double y) {
		this.x = (float) x;
		this.y = (float) y;
	}

	public void setLocation(Point pt) {
		this.x = pt.x;
		this.y = pt.y;
	}

	public void setLocation(Point2D pt) {
		this.x = (float) pt.getX();
		this.y = (float) pt.getY();
	}

	public void setX(float x) {
		this.x = x;
	}

	public void setY(float y) {
		this.y = y;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer(64);
		buf.append("{ x: ").append(x);
		buf.append(", y: ").append(y);
		buf.append(" }");
		return buf.toString();
	}

	/**
	 * Returns a copy of the point. This is useful as the following code only
	 * generates a flat copy:
	 * 
	 * <code>var point1 = new Point();
	 * var point2 = point1;
	 * point2.x = 1; // also changes point1.x
	 * 
	 * var point2 = pt1.clone();
	 * point2.x = 1; // doesn't change point1.x</code>
	 * 
	 * @return the cloned point
	 */
	public Object clone() {
		return new Point(this);
	}

	/**
	 * Returns the addition of the supplied point to the point object as a new
	 * point. The object itself is not modified!
	 * 
	 * @param pt the point to add
	 * @return the addition of the two points as a new point
	 */
	public Point add(Point2D pt) {
		return new Point(x + pt.getX(), y + pt.getY());
	}

	public Point add(Point pt) {
		return new Point(x + pt.x, y + pt.y);
	}

	/**
	 * Returns the addition of the supplied x and y values to the point object
	 * as a new point. The object itself is not modified!
	 * 
	 * @param x the x value to add
	 * @param y the y value to add
	 * @return the addition of the two points as a new point
	 */
	public Point add(float x, float y) {
		return new Point(this.x + x, this.y + y);
	}

	public Point add(double x, double y) {
		return new Point(this.x + x, this.y + y);
	}

	/**
	 * Returns the subtraction of the supplied point to the point object as a
	 * new point. The object itself is not modified!
	 * 
	 * @param pt the point to subtract
	 * @return the subtraction of the two points as a new point
	 */
	public Point subtract(Point2D pt) {
		return new Point(x - pt.getX(), y - pt.getY());
	}

	public Point subtract(Point pt) {
		return new Point(x - pt.x, y - pt.y);
	}

	/**
	 * Returns the subtraction of the supplied x and y values to the point
	 * object as a new point. The object itself is not modified!
	 * 
	 * @param x The x value to subtract
	 * @param y The y value to subtract
	 * @return the subtraction of the two points as a new point
	 */
	public Point subtract(float x, float y) {
		return new Point(this.x - x, this.y - y);
	}

	public Point subtract(double x, double y) {
		return new Point(this.x - x, this.y - y);
	}

	/**
	 * Returns the multiplication of the point object by the supplied point as a
	 * new point. The object itself is not modified!
	 * 
	 * @param pt the point to multiply with
	 * @return the multiplication of the two points as a new point
	 */
	public Point multiply(Point2D pt) {
		return new Point(x * pt.getX(), y * pt.getY());
	}

	public Point multiply(Point pt) {
		return new Point(x * pt.x, y * pt.y);
	}

	/**
	 * Returns the multiplication of the point object by the supplied x and y
	 * values as a new point. When no y value is supplied, the point's x and y
	 * values are multiplied by scale (x). The object itself is not modified!
	 * 
	 * @param x the x (or scale) value to multiply with
	 * @param y the y value to multiply with
	 * @return the multiplication of the two points as a new point
	 */
	public Point multiply(float x, float y) {
		return new Point(this.x * x, this.y * y);
	}

	public Point multiply(double x, double y) {
		return new Point(this.x * x, this.y * y);
	}

	public Point multiply(float scale) {
		return new Point(x * scale, y * scale);
	}

	public Point multiply(double scale) {
		return new Point(x * scale, y * scale);
	}

	/**
	 * Checks if the point is within a given distance of another point
	 * 
	 * @param pt the point to check against
	 * @param tolerance the maximum distance allowed
	 * @return <code>true</code> if it is within the given distance, false
	 *         otherwise
	 */
	public boolean isClose(Point pt, float tolerance) {
		return distance(pt) < tolerance;
	}
	
	public float getDistance(float px, float py) {
		px -= x;
		py -= y;
		return (float) Math.sqrt(px * px + py * py);
	}

	public float getDistance(Point2D pt) {
		return getDistance((float) pt.getX(), (float) pt.getY());
	}

	public float getLength() {
		return (float) Math.sqrt(x * x + y * y);
	}

	public float getAngle(Point pt) {
		float div = getLength() * pt.getLength();
		if (div == 0) return 0;
		else {
			double v = (x * pt.y - y * pt.x) / div;
			if (v < -1.0) v = -1.0;
			else if (v > 1.0) v = 1.0;
			return (float) Math.asin(v);
		}
	}

	public float getAngle() {
		if (x > 0) return (float) Math.atan(y / x);
		else if (x < 0) {
			if (y >= 0) return (float) (Math.atan(y / x) + Math.PI);
			else return (float) (Math.atan(y / x) - Math.PI);
		} else {
			if (y >= 0) return (float) Math.PI * 0.5f;
			else return (float) -Math.PI * 0.5f;
		}
	}

	/**
	 * Returns the interpolation point between the point and another point. The
	 * object itself is not modified!
	 * 
	 * @param pt
	 * @param t the position between the two points as a value between 0 and 1
	 * @return the interpolation point
	 */
	public Point interpolate(Point2D pt, float t) {
		return new Point(
			x * (1f - t) + (float) pt.getX() * t,
			y * (1f - t) + (float) pt.getY() * t
		);
	}

	/**
	 * Checks wether the point is inside the rectangle
	 * 
	 * @param rect the rectangle to check against
	 * @return <code>true</code> if the point is inside the rectangle, false
	 *         otherwise
	 */
	public boolean isInside(Rectangle2D rect) {
		return rect.contains(this);
	}

	public Point normalize(float length) {
		float len = getLength();
		if (len != 0) {
			float scale = length / len;
			return new Point(x * scale, y * scale);
		} else {
			return new Point(this);
		}
	}

	public Point normalize(double length) {
		return normalize((float) length);
	}

	public Point normalize() {
		return normalize(1f);
	}

	/**
	 * Rotates a point. The object itself is not modified!
	 * 
	 * @param theta the rotation angle in radians
	 * @return the rotated point
	 */
	public Point rotate(float theta) {
		double s = Math.sin(theta);
		double c = Math.cos(theta);
		return new Point(
			x * c - y * s,
			y * c + x * s
 		);
	}

	public Point rotate(double theta) {
		return rotate((float) theta);
	}

	/**
	 * Returns the dot product of the point and another point.
	 * @param pt
	 * @return    the dot product of the two points
	 */

	public float dotProduct(Point2D pt) {
		return x * (float) pt.getX() + y * (float) pt.getY();
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
			float scale = dotProduct(pt) / pt.dotProduct(pt);
			return new Point(
				pt.x * scale,
				pt.y * scale
			);
		}
	}

	public Point transform(AffineTransform at) {
		return (Point) at.transform(this, new Point());
	}

	// wrappable interface

	protected Scriptable wrapper;

	public void setWrapper(Scriptable wrapper) {
		this.wrapper = wrapper;
	}
	
	public Scriptable getWrapper() {
		return wrapper;
	}
}
