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

package com.scriptographer.swt;

import com.scratchdisk.util.IntegerEnum;

/**
 * @author lehni
 *
 */
public enum ImageType implements IntegerEnum {
	RGB(0),
	ARGB(1),
	SCREEN(2),
	ASCREEN(3);

	protected int value;

	private ImageType(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}

