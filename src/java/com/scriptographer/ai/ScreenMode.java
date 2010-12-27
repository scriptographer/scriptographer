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
 * File created on Apr 11, 2008.
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
