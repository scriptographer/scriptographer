/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2005 Juerg Lehni, http://www.scratchdisk.com.
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
 * File created on 11.03.2005.
 *
 * $RCSfile: DialogGroupInfo.java,v $
 * $Author: lehni $
 * $Revision: 1.1 $
 * $Date: 2005/03/25 00:27:57 $
 */

package com.scriptographer.adm;

public class DialogGroupInfo {
	// dialog position code masks
	public final static int
		POSITION_DOCK				= 0x000000ff,
		POSITION_TAB				= 0x0000ff00,
		POSITION_FRONTTAB			= 0x00010000,
		POSITION_ZOOM				= 0x00020000,
		POSITION_DOCKVISIBLE		= 0x00040000,
		POSITION_DEFAULT			= POSITION_FRONTTAB | POSITION_ZOOM;

	protected String group;
	protected int positionCode;
	
	public DialogGroupInfo(String group, int positionCode) {
		this.group = group;
		this.positionCode = positionCode;
	}
	
	String getGroup() {
		return group;
	}
	
	int getPositionCode() {
		return positionCode;
	}
	
	// TODO: add isDockVisible, isFrontTab, ...
}
