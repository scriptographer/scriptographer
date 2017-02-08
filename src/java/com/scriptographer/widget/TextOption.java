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

package com.scriptographer.widget;

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