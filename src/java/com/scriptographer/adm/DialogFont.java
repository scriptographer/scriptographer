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
 * ADMFont
 * 
 * @author lehni
 */
public enum DialogFont implements IntegerEnum {
	DEFAULT(0),
	DIALOG(1),
	DIALOG_ITALIC(3),
	DIALOG_BOLD(5),
	DIALOG_BOLD_ITALIC(7),
	PALETTE(2),
	PALETTE_ITALIC(4),
	PALETTE_BOLD(6),
	PALETTE_BOLD_ITALIC(8),
	MONOSPACED(9),
	MONOSPACED_ITALIC(10),
	MONOSPACED_BOLD(11),
	MONOSPACED_BOLD_ITALIC(12);

	protected int value;

	private DialogFont(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
