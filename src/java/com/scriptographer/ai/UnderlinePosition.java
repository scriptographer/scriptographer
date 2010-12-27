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
 * UnderlinePosition
 * 
 * @author lehni
 */
public enum UnderlinePosition implements IntegerEnum {
	OFF(0),
	RIGHT_IN_VERTICAL(1),
	LEFT_IN_VERTICAL(2);

	protected int value;

	private UnderlinePosition(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
