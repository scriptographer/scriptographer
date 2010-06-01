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
 * File created on Apr 15, 2008.
 */

package com.scriptographer.adm;

import com.scratchdisk.util.IntegerEnum;

/**
 * ADMColor
 * 
 * @author lehni
 */
public enum DialogColor implements IntegerEnum {
	BLACK(0),
	WHITE(1),
	HILITE(2),
	HILITE_TEXT(3),
	LIGHT(4),
	BACKGROUND(5),
	SHADOW(6),
	DISABLED(7),
	BUTTON_UP(8),
	BUTTON_DOWN(9),
	BUTTON_DOWN_SHADOW(10),
	TOOLTIP_BACKGROUND(11),
	TOOLTIP_FOREGROUND(12),
	WINDOW(13),
	FOREGROUND(14),
	TEXT(15),
	RED(16),
	TAB_BACKGROUND(17),
	ACTIVE_TAB(18),
	INACTIVE_TAB(19);

	protected int value;

	private DialogColor(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}