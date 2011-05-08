/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Scripting Plugin for Adobe Illustrator
 * http://scriptographer.org/
 *
 * Copyright (c) 2002-2010, Juerg Lehni
 * http://scratchdisk.com/
 *
 * All rights reserved. See LICENSE file for details.
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
	MOUSEDOWN("AI Mouse Down"),
	MOUSEUP("AI Mouse Up"),
	MOUSEDRAG("AI Mouse Drag"),
	MOUSEMOVE("AI Track Cursor"),

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
