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
 * File created on Apr 11, 2008.
 *
 * $Id$
 */

package com.scriptographer.ai;

import com.scratchdisk.util.IntegerEnum;

/**
 * FontCapsOption
 * 
 * @author lehni
 */
public enum ScreenMode implements IntegerEnum {
	/** Only when there is no visible document */
	NO_SCREEN(0),
	/** The normal display mode. Multiple windows are visible. */
	MULTI_WINDOW(1),
	/** A single view takes up the whole screen but the menu is visible. */
	FULLSCREEN_MENU(2),
	/** A single view takes up the whole screen, the menu is not visible. */
	FULLSCREEN(3);

	protected int value;

	private ScreenMode(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
