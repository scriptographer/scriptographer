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
 * File created on Dec 22, 2007.
 *
 * $Id$
 */

package com.scriptographer.ai;

import com.scratchdisk.script.ArgumentReader;
import com.scratchdisk.script.ChangeNotifier;

/**
 * @author lehni
 */
public class Size implements ChangeNotifier {
	protected double width;
	protected double height;

	public Size() {
		width = height = 0;
	}

	/**
	 * Creates a Size object with the given width and height.
	 * 
	 * @param width The width of the Size {@default 0}
	 * @param height The height of the Size {@default 0}
	 */
	public Size(double width, double height) {
		set(width, height);
	}

	public Size(float width, float height) {
		set(width, height);
	}

	/**
	 * Creates a Size object using the width and height of the given Size
	 * object.
	 * 
	 * @param size
	 */
	public Size(Size size) {
		this(size.width, size.height);
	}

	/**
	 * Creates a Size object using the x and y coordinates of the given Point
	 * object.
	 * 
	 * @param point
	 */
	public Size(Point point) {
		this(point.x, point.y);
	}

	/**
	 * @jshide
	 */
	public Size(ArgumentReader reader) {
		this(reader.has("width") ? reader.readDouble("width", 0) : reader.readDouble("x", 0),
				reader.has("height") ? reader.readDouble("height", 0) : reader.readDouble("y", 0));
	}

	/**
	 * @jshide
	 */
	public void set(double width, double height) {
		this.width = width;
		this.height = height;
	}

	/**
	 * The width of the size.
	 */
	public double getWidth() {
		return width;
	}
	
	public void setWidth(double width) {
		this.width = width;
	}

	/**
	 * The height of the size.
	 */
	public double getHeight() {
		return height;
	}
	
	public void setHeight(double height) {
		this.height = height;
	}

	/**
	 * @jshide
	 */
	public Size add(double w, double h) {
		return new Size(width + w, height + h);
	}

	/**
	 * Returns the addition of the width and height of the supplied size to the
	 * size as a new size.
	 * The object itself is not modified!
	 * 
	 * @param size The addition of the two sizes as a new size
	 */
	public Size add(Size size) {
		return add(size.width, size.height);
	}

	/**
	 * Returns the addition of the supplied value to the width and height of the size as a new size.
	 * The object itself is not modified!
	 * 
	 * @param value
	 * @return the addition of the size and the value as a new size
	 */
	public Size add(double value) {
		return add(value, value);
	}

	/**
	 * @jshide
	 */
	public Size subtract(double w, double h) {
		return new Size(width - w, height - h);
	}

	/**
	 * Returns the subtraction of the width and height of the supplied size from
	 * the size as a new size.
	 * The object itself is not modified!
	 * 
	 * @param size The subtraction of the two sizes as a new size
	 */
	public Size subtract(Size size) {
		return subtract(size.width, size.height);
	}

	/**
	 * Returns the subtraction of the supplied value from the width and
	 * height of the size as a new size.
	 * The object itself is not modified!
	 * 
	 * @param value
	 * @return the subtraction of the value from the size as a new size
	 */
	public Size subtract(double value) {
		return subtract(value, value);
	}

	/**
	 * @jshide
	 */
	public Size multiply(double w, double h) {
		return new Size(width * w, height * h);
	}

	/**
	 * Returns the multiplication of the width and height of the supplied size with
	 * the size as a new size.
	 * The object itself is not modified!
	 * 
	 * @param size The multiplication of the two sizes as a new size
	 */
	public Size multiply(Size size) {
		return multiply(size.width, size.height);
	}

	/**
	 * Returns the multiplication of the supplied value with the width and height of the size as a new size.
	 * The object itself is not modified!
	 * 
	 * @param value
	 * @return the multiplication of the size by the value as a new size
	 */
	public Size multiply(double value) {
		return multiply(value, value);
	}

	/**
	 * @jshide
	 */
	public Size divide(double w, double h) {
		return new Size(width / w, height / h);
	}

	/**
	 * Returns the division of the width and height of the supplied size by
	 * the size as a new size.
	 * The object itself is not modified!
	 * 
	 * @param size The division of the two sizes as a new size
	 */
	public Size divide(Size size) {
		return divide(size.width, size.height);
	}

	/**
	 * Returns the division of the supplied value by the width and height of the size as a new size.
	 * The object itself is not modified!
	 * 
	 * @param value
	 * @return the division of the size by the value as a new size
	 */
	public Size divide(double value) {
		return divide(value, value);
	}

	/**
	 * @jshide
	 */
	public Size modulo(double w, double h) {
		return new Size(this.width % w, this.height % h);
	}

	public Size modulo(Point point) {
		return modulo(point.x, point.y);
	}

	public Size modulo(double value) {
		return modulo(value, value);
	}

	/**
	 * @jshide
	 */
	public Size negate() {
		return new Size(-width, -height);
	}

	/**
	 * Returns a copy of the size.
	 * This is useful as the following code only generates a flat copy:
	 * 
	 * <code>
	 * var size2 = new Size();
	 * var size2 = size1;
	 * size2.x = 1; // also changes size1.x
	 * 
	 * var size2 = size1.clone();
	 * size2.x = 1; // doesn't change size1.x
	 * </code>
	 * 
	 * @return the cloned size object
	 */
	public Object clone() {
		return new Size(this);
	}

	/**
	 * Checks whether the width and height of the size are equal to those of the
	 * supplied size.
	 * 
	 * Sample code:
	 * <code>
	 * var size = new Size(5, 10);
	 * print(size == new Size(5, 10)); // true
	 * print(size == new Size(1, 1)); // false
	 * print(size != new Size(1, 1)); // true
	 * </code>
	 */
	public boolean equals(Object object) {
		if (object instanceof Size) {
			Size size = (Size) object;
			return size.width == width && size.height == height;
		} else {
			// TODO: support other point types?
			return false;
		}
	}

	public String toString() {
	   	return "{ width: " + width + ", height: " + height + " }";
	}
}
