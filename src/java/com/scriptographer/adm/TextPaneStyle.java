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
 * File created on Apr 14, 2008.
 */

package com.scriptographer.adm;

import com.scratchdisk.util.IntegerEnum;

/**
 * ADMFrameStyle
 * 
 * @author lehni
 */
public enum TextPaneStyle implements IntegerEnum {
	CLIP(1 << 0),
	DISABLE_AUTO_ACTIVATE(1 << 1),
	TRUNCATE_END(1 << 2), // clipped style has priority
	TRUNCATE_MIDDLE(1 << 3); // truncate end has priority

	protected int value;

	private TextPaneStyle(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
