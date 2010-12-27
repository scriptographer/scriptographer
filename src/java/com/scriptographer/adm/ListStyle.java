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
 * @author lehni
 *
 */
public enum ListStyle implements IntegerEnum {
	MULTISELECT(1 << 0),
	DIVIDED(1 << 1),
	TILE(1 << 2),
	ENTRY_ALWAYS_SELECTED(1 << 3),
	BLACK_RECT(1 << 4),
	USE_IMAGE(1 << 5),
	ENTRYTEXT_EDITABLE(1 << 6);

	protected int value;

	private ListStyle(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
