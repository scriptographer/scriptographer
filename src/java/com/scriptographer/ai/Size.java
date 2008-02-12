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
	protected float width;
	protected float height;

	public Size() {
		width = height = 0;
	}

	public Size(float width, float height) {
		set(width, height);
	}

	public Size(double x, double y) {
		this((float) x, (float) y);
	}

	public Size(Size size) {
		this(size.width, size.height);
	}

	public Size(Point size) {
		this(size.x, size.y);
	}

	public Size(ArgumentReader reader) {
		this(reader.readFloat("width", 0),
				reader.readFloat("height", 0));
	}

	public void set(float width, float height) {
		this.width = width;
		this.height = height;
	}

	/**
	 * @jsbean The width of the size.
	 */
	public float getWidth() {
		return width;
	}
	
	public void setWidth(float width) {
		this.width = width;
	}

	/**
	 * @jsbean The height of the size.
	 */
	public float getHeight() {
		return height;
	}
	
	public void setHeight(float height) {
		this.height = height;
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
