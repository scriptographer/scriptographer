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
 * File created on Apr 11, 2008.
 *
 * $Id$
 */

package com.scriptographer.ai;

import com.scratchdisk.util.IntegerEnum;

/**
 * AIBlendingModeValues
 * 
 * @author lehni
 */
public enum BlendMode implements IntegerEnum {
	NORMAL(0),
	MULTIPLY(1),
	SCREEN(2),
	OVERLAY	(3),
	SOFT_LIGHT(4),
	HARD_LIGHT(5),
	COLOR_DODGE	(6),
	COLOR_BURN(7),
	DARKEN(8),
	LIGHTEN	(9),
	DIFFERENCE(10),
	EXCLUSION(11),
	HUE(12),
	SATURATION(13),
	COLOR(14),
	LUMINOSITY(15);

	protected int value;

	private BlendMode(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
