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
 * File created on Apr 15, 2008.
 *
 * $Id$
 */

package com.scriptographer.adm;

import com.scratchdisk.util.IntegerEnum;

/**
 * @author lehni
 *
 */
public enum ListStyle implements IntegerEnum {
	MULTISELECT(1 << 0),
	DIVIDED(1 << 1),
	TILE(1 << 2),
	ENTRY_ALWAYS_SELECTED(1 << 3),
	BLACK_RECT(1 << 4),
	USE_IMAGE(1 << 5),
	ENTRYTEXT_EDITABLE(1 << 6);

	protected int value;

	private ListStyle(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
