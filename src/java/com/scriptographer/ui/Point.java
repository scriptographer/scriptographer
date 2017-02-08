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
 * File created on May 14, 2007.
 */

package com.scriptographer.ui;

import com.scratchdisk.script.ArgumentReader;

/**
 * @author lehni
 * 
 * @jshide
 */
public class Point {
	public int x;
	public int y;

	public Point() {
		x = y = 0;
	}
	
	public Point(int x, int y) {
		set(x, y);
	}

	public Point(double x, double y) {
		set(x, y);
	}

	public Point(Point pt) {
		set(pt.x, pt.y);
	}

	public Point(Size size) {
		x = size.width;
		y = size.height;
	}

	/**
	 * @jshide
	 */
	public Point(ArgumentReader reader) {
		this(reader.readInteger("x", 0),
				reader.readInteger("y", 0));
	}

	/**
	 * @jshide
	 */
	public void set(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * @jshide
	 */
	public void set(double x, double y) {
		this.x = (int) Math.round(x);
		this.y = (int) Math.round(y);
	}
	public int getX() {
		return x;
	}
	
	public void setX(int x) {
		this.x = x;
	}

	public void setX(double x) {
		this.x = (int) Math.round(x);
	}

	public int getY() {
		return y;
	}
	
	public void setY(int y) {
		this.y = y;
	}

	public void setY(double y) {
		this.y = (int) Math.round(y);
	}

	public Object clone() {
		return new Point(this);
	}

	public Point add(double x, double y) {
		return new Point(this.x + x, this.y + y);
	}

	public Point add(Point point) {
		return add(point.x, point.y);
	}

	public Point add(double value) {
		return add(value, value);
	}

	public Point subtract(double x, double y) {
		return new Point(this.x - x, this.y - y);
	}

	public Point subtract(Point point) {
		return subtract(point.x, point.y);
	}

	public Point subtract(double value) {
		return subtract(value, value);
	}

	public Point multiply(double x, double y) {
		return new Point(this.x * x, this.y * y);
	}

	public Point multiply(Point point) {
		return multiply(point.x, point.y);
	}

	public Point multiply(double value) {
		return multiply(value, value);
	}

	public Point divide(double x, double y) {
		return new Point(this.x / x, this.y / y);
	}

	public Point divide(Point point) {
		return divide(point.x, point.y);
	}

	public Point divide(double value) {
		return divide(value, value);
	}

	public Point modulo(int x, int y) {
		return new Point(this.x % x, this.y % y);
	}

		public Point modulo(Point point) {
		return modulo(point.x, point.y);
	}

	public Point modulo(int value) {
		return modulo(value, value);
	}

	public Point negate() {
		return new Point(-x, -y);
	}

	public boolean equals(Object object) {
		if (object instanceof Point) {
			Point pt = (Point) object;
			return pt.x == x && pt.y == y;
		}
		// TODO: support other point types?
		return false;
	}

	public boolean isInside(Rectangle rect) {
		return rect.contains(this);
	}

	public String toString() {
	   	return "{ x: " + x + ", y: " + y + " }";
	}
}
