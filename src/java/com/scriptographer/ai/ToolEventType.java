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
 * File created on Sep 27, 2009.
 */

package com.scriptographer.ai;

import java.util.HashMap;

/**
 * @author lehni
 *
 */
public enum ToolEventType {
	MOUSE_DOWN("AI Mouse Down"),
	MOUSE_UP("AI Mouse Up"),
	MOUSE_DRAG("AI Mouse Drag"),
	MOUSE_MOVE("AI Track Cursor"),

	EDIT_OPTIONS("AI Edit Options"),

	SELECT("AI Select"),
	DESELECT("AI Deselect"),
	RESELECT("AI Reselect"),

	DECREASE_DIAMETER("AI Decrease Diameter"),
	INCREASE_DIAMETER("AI Increase Diameter");

	protected String value;

	private ToolEventType(String value) {
		this.value = value;
	}

	// HashMap for conversation to unique ids that can be compared with ==
	// instead of .equals
	private static HashMap<String, ToolEventType> types =
			new HashMap<String, ToolEventType>();

	static {
		for (ToolEventType type : ToolEventType.values())
			types.put(type.value, type);
	}

	protected static ToolEventType get(String type) {
		return types.get(type);
	}
}
