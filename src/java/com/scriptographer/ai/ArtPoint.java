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
 * File created on Feb 27, 2008.
 *
 * $Id$
 */

package com.scriptographer.ai;

/**
 * A Point class to wrap an art object and control its position.
 *
 * @author lehni
 *
 */
public class ArtPoint extends Point {
	protected Art art;
	protected int version = -1;

	protected ArtPoint(Art art) {
		this.art = art;
		update();
	}

	protected void update() {
		if (version != art.version) {
			Point position = art.nativeGetPosition();
			x = position.x;
			y = position.y;
			version = art.version;
		}
	}

	public void set(float x, float y) {
		// This updates the point object itself too:
		art.setPosition(x, y);
	}

	public void setX(float x) {
		update();
		set(x, y);
	}

	public void setY(float y) {
		update();
		set(x, y);
	}
	
	public float getX() {
		update();
		return x;
	}
	
	public float getY() {
		update();
		return y;
	}
}
