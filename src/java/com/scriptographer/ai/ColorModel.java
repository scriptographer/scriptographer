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
 * AIWorkingColorSpace, AIColorModel
 * 
 * @author lehni
 */
public enum ColorModel implements IntegerEnum {
	GRAY(0),
	RGB(1),
	CMYK(2);

	protected int value;

	private ColorModel(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
