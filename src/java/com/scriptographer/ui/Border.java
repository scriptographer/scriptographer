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
 * File created on May 11, 2007.
 *
 * $Id$
 */

package com.scriptographer.ui;

import java.awt.Insets;

import com.scratchdisk.script.ArgumentReader;

/**
 * @author lehni
 *
 */
public class Border {
	public int top;
	public int right;
	public int bottom;
	public int left;

	public Border() {
		top = right = bottom = left = 0;
	}

	public Border(int top, int right, int bottom, int left) {
		set(top, right, bottom, left);
	}
	
	public Border(Border margins) {
		set(margins.top, margins.right, margins.bottom, margins.left);
	}

	public Border(Insets insets) {
		set(insets.top, insets.right, insets.bottom, insets.left);
	}

	public Border(ArgumentReader reader) {
		this(reader.readInteger("top", 0),
				reader.readInteger("right", 0),
				reader.readInteger("bottom", 0),
				reader.readInteger("left", 0));
	}

	public void set(int top, int right, int bottom, int left) {
		this.top = top;
		this.right = right;
		this.bottom = bottom;
		this.left = left;
	}

	public Border add(Border border) {
		top += border.top;
		right += border.right;
		bottom += border.bottom;
		left += border.left;
		return this;
	}

	public Border subtract(Border border) {
		top -= border.top;
		right -= border.right;
		bottom -= border.bottom;
		left -= border.left;
		return this;
	}

	public Insets toInsets() {
		return new Insets(top, left, bottom, right);
	}

	public Object clone() {
		return new Border(this);
	}

	public boolean equals(Object object) {
		if (object instanceof Border) {
			Border border = (Border) object;
			return border.top == top && border.right == right
				&& border.bottom == bottom && border.left == left;
		} else {
			// TODO: support other margin types?
			return false;
		}
	}
}
