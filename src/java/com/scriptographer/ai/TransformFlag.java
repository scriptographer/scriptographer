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
 * @author lehni
 */
public enum TransformFlag implements IntegerEnum {
	OBJECTS(1 << 0),
	FILL_GRADIENTS(1 << 1),
	FILL_PATTERNS(1 << 2),
	STROKE_PATTERNS(1 << 3),
	LINES(1 << 4),
	LINKED_MASKS(1 << 5),
	CHILDREN(1 << 6),
	SELECTION_ONLY(1 << 7);

	protected int value;

	private TransformFlag(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
