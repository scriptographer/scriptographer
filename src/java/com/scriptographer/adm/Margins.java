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
 * File created on May 11, 2007.
 *
 * $Id: $
 */

package com.scriptographer.adm;

import java.awt.Insets;
import java.util.Map;

import com.scratchdisk.util.ConversionHelper;

/**
 * @author lehni
 *
 */
public class Margins {
	public int left;
	public int top;
	public int right;
	public int bottom;

	public Margins() {
		left = top = right = bottom = 0;
	}

	public Margins(int left, int top, int right, int bottom) {
		set(left, top, right, bottom);
	}
	
	public Margins(Margins margins) {
		set(margins.left, margins.top, margins.right, margins.bottom);
	}

	public Margins(Map map) {
		this(ConversionHelper.getInt(map, "left"),
				ConversionHelper.getInt(map, "top"),
				ConversionHelper.getInt(map, "right"),
				ConversionHelper.getInt(map, "bottom"));
	}

	public void set(int left, int top, int right, int bottom) {
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
	}

	public Insets toInsets() {
		return new Insets(top, left, bottom, right);
	}

	public Object clone() {
		return new Margins(this);
	}

	public boolean equals(Object object) {
		if (object instanceof Margins) {
			Margins margins = (Margins) object;
			return margins.left == left && margins.top == top &&
					margins.right == right && margins.bottom == bottom;
		} else {
			// TODO: support other margin types?
			return false;
		}
	}
}
