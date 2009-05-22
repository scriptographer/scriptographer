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

/**
 * @author lehni
 *
 */
public class Size {
	protected double width;
	protected double height;

	public Size() {
		width = height = 0;
	}

	public Size(double width, double height) {
		set(width, height);
	}

	public Size(float width, float height) {
		set(width, height);
	}

	public Size(Size size) {
		this(size.width, size.height);
	}

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
	 * @jsbean The width of the size.
	 */
	public double getWidth() {
		return width;
	}
	
	public void setWidth(double width) {
		this.width = width;
	}

	/**
	 * @jsbean The height of the size.
	 */
	public double getHeight() {
		return height;
	}
	
	public void setHeight(double height) {
		this.height = height;
	}

	public Size add(double w, double h) {
		return new Size(width + w, height + h);
	}

	public Size add(Size size) {
		return add(size.width, size.height);
	}

	public Size add(double value) {
		return add(value, value);
	}

	public Size subtract(double w, double h) {
		return new Size(width - w, height - h);
	}

	public Size subtract(Size size) {
		return subtract(size.width, size.height);
	}

	public Size subtract(double value) {
		return subtract(value, value);
	}

	public Size multiply(double w, double h) {
		return new Size(width * w, height * h);
	}

	public Size multiply(Size size) {
		return multiply(size.width, size.height);
	}

	public Size multiply(double value) {
		return multiply(value, value);
	}

	public Size divide(double w, double h) {
		return new Size(width / w, height / h);
	}

	public Size divide(Size size) {
		return divide(size.width, size.height);
	}

	public Size divide(double value) {
		return divide(value, value);
	}

	public Size negate() {
		return new Size(-width, -width);
	}

	public Object clone() {
		return new Size(this);
	}

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
