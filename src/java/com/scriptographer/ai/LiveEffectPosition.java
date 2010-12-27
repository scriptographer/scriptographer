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
 * File created on Feb 27, 2010.
 */

package com.scriptographer.ai;

import com.scratchdisk.util.IntegerEnum;

/**
 * @author lehni
 *
 */
public enum LiveEffectPosition implements IntegerEnum {
	PRE_EFFECT(1),
	POST_EFFECT(2),
	STROKE(3),
	FILL(4);
	
	protected int value;

	private LiveEffectPosition(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
