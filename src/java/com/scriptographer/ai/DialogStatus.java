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
 * File created on Apr 13, 2008.
 */

package com.scriptographer.ai;

import com.scratchdisk.util.IntegerEnum;

/**
 * ActionDialogStatus
 * 
 * @author lehni
 */

/*
 * TODO: Instead of passing it as a parameter, implement global 
 * app.dialogStatus, that defines handling for all such functions,
 * also Document#write, etc.
 */
public enum DialogStatus implements IntegerEnum {
	NONE(0),
	ON(1),
	PARTIAL_ON(2),
	OFF(3);

	protected int value;

	private DialogStatus(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
