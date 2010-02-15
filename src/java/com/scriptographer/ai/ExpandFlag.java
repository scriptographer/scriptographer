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
 *
 * $Id$
 */

package com.scriptographer.ai;

import com.scratchdisk.util.IntegerEnum;

/**
 * AIExpandFlagValue
 * 
 * @author lehni
 */
public enum ExpandFlag implements IntegerEnum {
	PLUGIN_ART(0x0001),
	TEXT(0x0002),
	STROKE(0x0004),
	PATTERN(0x0008),
	GRADIENT_TO_MESH(0x0010),
	GRADIENT_TO_PATHS(0x0020),
	SYMBOL_INSTANCES(0x0040),
	ONE_BY_ONE(0x4000),
	SHOW_PROGRESS(0x8000),
	// By default objects that are locked such as those on a locked layer
	// cannot be expanded. Setting this flag allows them to be expanded.
	LOCKED_OBJECTS(0x10000);

	protected int value;

	private ExpandFlag(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
