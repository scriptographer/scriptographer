/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2010 Juerg Lehni, http://www.scratchdisk.com.
 * All rights reserved.
 *
 * Please visit http://scriptographer.org/ for updates and contact.
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
 * File created on May 16, 2007.
 *
 * $Id$
 */

package com.scriptographer.ui;

import java.awt.Dimension;

import com.scratchdisk.script.ArgumentReader;

/**
 * @author lehni
 * 
 * @jshide
 */
public class Size {
	public int width;
	public int height;

	public Size() {
		width = height = 0;
	}

	public Size(int width, int height) {
		set(width, height);
	}

	public Size(Size size) {
		set(size.width, size.height);
	}

	public Size(Point point) {
		width = point.x;
		height = point.y;
	}

	/**
	 * @jshide
	 */
	public Size(Dimension size) {
		this.width = size.width;
		this.height = size.height;
	}

	/**
	 * @jshide
	 */
	public Size(ArgumentReader reader) {
		this(reader.readInteger("width", 0),
				reader.readInteger("height", 0));
	}

	public void set(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public double getWidth() {
		return width;
	}
	
	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}
	
	public void setHeight(int height) {
		this.height = height;
	}

	public Size add(int w, int h) {
		return new Size(width + w, height + h);
	}

	public Size add(Size size) {
		return add(size.width, size.height);
	}

	public Size add(int value) {
		return add(value, value);
	}

	public Size subtract(int w, int h) {
		return new Size(width - w, height - h);
	}

	public Size subtract(Size size) {
		return subtract(size.width, size.height);
	}

	public Size subtract(int value) {
		return subtract(value, value);
	}

	public Size multiply(int w, int h) {
		return new Size(width * w, height * h);
	}

	public Size multiply(Size size) {
		return multiply(size.width, size.height);
	}

	public Size multiply(int value) {
		return multiply(value, value);
	}

	public Size divide(int w, int h) {
		return new Size(width / w, height / h);
	}

	public Size divide(Size size) {
		return divide(size.width, size.height);
	}

	public Size divide(int value) {
		return divide(value, value);
	}

	public Size modulo(int w, int h) {
		return new Size(this.width % w, this.height % h);
	}

	public Size modulo(Point point) {
		return modulo(point.x, point.y);
	}

	public Size modulo(int value) {
		return modulo(value, value);
	}

	public Size negate() {
		return new Size(-width, -height);
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
