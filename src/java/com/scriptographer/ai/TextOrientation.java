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
 * AITextOrientation
 * 
 * @author lehni
 */
public enum TextOrientation implements IntegerEnum {
	HORIZONTAL(0),
	VERTICAL(1);

	protected int value;

	private TextOrientation(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
