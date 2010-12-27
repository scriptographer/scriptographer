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