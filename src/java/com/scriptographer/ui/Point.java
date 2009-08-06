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
 * File created on May 14, 2007.
 *
 * $Id$
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

	public void set(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public Object clone() {
		return new Point(this);
	}

	public Point add(int x, int y) {
		return new Point(this.x + x, this.y + y);
	}

	public Point add(Point point) {
		return add(point.x, point.y);
	}

	public Point add(int value) {
		return add(value, value);
	}

	public Point subtract(int x, int y) {
		return new Point(this.x - x, this.y - y);
	}

	public Point subtract(Point point) {
		return subtract(point.x, point.y);
	}

	public Point subtract(int value) {
		return subtract(value, value);
	}

	public Point multiply(int x, int y) {
		return new Point(this.x * x, this.y * y);
	}

	public Point multiply(Point point) {
		return multiply(point.x, point.y);
	}

	public Point multiply(int value) {
		return multiply(value, value);
	}

	public Point divide(int x, int y) {
		return new Point(this.x / x, this.y / y);
	}

	public Point divide(Point point) {
		return divide(point.x, point.y);
	}

	public Point divide(int value) {
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
		} else {
			// TODO: support other point types?
			return false;
		}
	}

	public boolean isInside(Rectangle rect) {
		return rect.contains(this);
	}

	public String toString() {
	   	return "{ x: " + x + ", y: " + y + " }";
	}
}
