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
 * StrikethroughPosition
 * 
 * @author lehni
 */
public enum StrikethroughPosition implements IntegerEnum {
	OFF(0),
	X_HEIGHT(1),
	EM_BOX(2);

	protected int value;

	private StrikethroughPosition(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
