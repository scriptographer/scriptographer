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
 * File created on May 14, 2007.
 *
 * $Id$
 */

package com.scriptographer.adm;

import java.util.Map;

import com.scratchdisk.util.ConversionUtils;

/**
 * @author lehni
 *
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


	public Point(Map map) {
		this(ConversionUtils.getInt(map, "x"),
				ConversionUtils.getInt(map, "y"));
	}

	public void set(int x, int y) {
		this.x = x;
		this.y = y;
	}

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

	public String toString() {
	   	return "{ x: " + x + ", y: " + y + " }";
	}
}
