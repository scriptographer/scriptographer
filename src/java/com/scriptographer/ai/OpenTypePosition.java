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
 * FontOpenTypePositionOption
 * 
 * @author lehni
 */
public enum OpenTypePosition implements IntegerEnum {
	NORMAL(0),
	SUPERSCRIPT(1),
	SUBSCRIPT(2),
	NUMERATOR(3),
	DENOMINATOR(4);

	protected int value;

	private OpenTypePosition(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
