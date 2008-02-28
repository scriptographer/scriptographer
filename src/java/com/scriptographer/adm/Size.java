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
 * File created on May 16, 2007.
 *
 * $Id$
 */

package com.scriptographer.adm;

import java.awt.Dimension;

import com.scratchdisk.script.ArgumentReader;

/**
 * @author lehni
 *
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

	public Size(Dimension size) {
		this.width = size.width;
		this.height = size.height;
	}

	public Size(Point size) {
		this.width = size.x;
		this.height = size.y;
	}

	public Size(ArgumentReader reader) {
		this(reader.readInteger("width", 0),
				reader.readInteger("height", 0));
	}

	public void set(int width, int height) {
		this.width = width;
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
