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

package com.scriptographer.ai;

import com.scratchdisk.util.IntegerEnum;

/**
 * AIExpandFlagValue
 * 
 * @author lehni
 */
public enum ExpandFlag implements IntegerEnum {
	PLUGIN_ART(0x0001),
	TEXT(0x0002),
	STROKE(0x0004),
	PATTERN(0x0008),
	GRADIENT_TO_MESH(0x0010),
	GRADIENT_TO_PATHS(0x0020),
	SYMBOL_INSTANCES(0x0040),
	ONE_BY_ONE(0x4000),
	SHOW_PROGRESS(0x8000),
	// By default objects that are locked such as those on a locked layer
	// cannot be expanded. Setting this flag allows them to be expanded.
	LOCKED_OBJECTS(0x10000);

	protected int value;

	private ExpandFlag(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
