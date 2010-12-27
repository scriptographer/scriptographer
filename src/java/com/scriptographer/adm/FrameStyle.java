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
public enum FrameStyle implements IntegerEnum {
	BLACK(0),
	GRAY(1),
	SUNKEN(2),
	RAISED(3),
	ETCHED(4);

	protected int value;

	private FrameStyle(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
