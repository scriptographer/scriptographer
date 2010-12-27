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
public enum TextCapitalization implements IntegerEnum {
	NORMAL(0),
	SMALL(1),
	ALL(2),
	ALL_SMALL(3);

	protected int value;

	private TextCapitalization(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
