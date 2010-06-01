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
 * @author lehni
 *
 */
public enum TextOption implements IntegerEnum {
	PASSWORD(1 << 1),
	UNICODE(1 << 2), // [cpaduan] 6/18/02 - Creates a Unicode based edit box (if possible). Currently has no effect on Windows.
	DISABLE_DRAG_DROP(1 << 3), // Disables drag & drop from or to text edits. Currently mac-only.

	// Self defined pseudo options), for creation of the right TYPE:
	READONLY(0),
	MULTILINE(0), 
	// for TYPE_TEXT_EDIT_POPUP:
	POPUP(0),
	SCROLLING(0);

	protected int value;

	private TextOption(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}