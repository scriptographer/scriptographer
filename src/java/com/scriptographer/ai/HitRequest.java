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
 * File created on Apr 14, 2008.
 */

package com.scriptographer.ai;

import com.scratchdisk.util.IntegerEnum;

/**
 * AIHitRequest
 * 
 * @author lehni
 */
public enum HitRequest implements IntegerEnum {
	/**
	 * Any object hits anywhere.
	 */
	ALL(0),
	/**
	 * Only hits on bezier points.
	 */
	POINTS(1),
	/**
	 * Only first or last bezier point hits on path.
	 */
	END_POINTS(2),
	/**
	 * Only guide object hits.
	 */
	GUIDES(3),
	/**
	 * Only hits on points on paths or any guides.
	 */
	PATHS(4),
	/**
	 * Only hits on text objects.
	 */
	TEXTS(5),
	/**
	 * Only hits already-selected objects.
	 */
	SELECTION(6),
	/**
	 * Only hits paintable objects (non-guide paths & text)
	 */
	PAINTABLES(7),
	/**
	 * Same as all but doesn't test against object fills
	 */
	ALL_EXCEPT_FILLS(8),
	/**
	 * Same as paint but doesn't test against object fills
	 */
	PAINTABLES_EXCEPT_FILLS(9),
	/**
	 * Same as all but doesn't test against direction line end points (the in and
	 * out handles of a bezier)
	 */
	ALL_EXCEPT_HANDLES(10),
	/**
	 * Same as paint but no locked objects
	 */
	PAINTABLES_EXCEPT_LOCKED(11);

	protected int value;

	private HitRequest(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
