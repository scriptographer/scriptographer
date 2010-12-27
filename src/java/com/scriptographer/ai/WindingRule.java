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
 * File created on May 21, 2009.
 */

package com.scriptographer.ai;

import com.scratchdisk.util.IntegerEnum;

/**
 * @author lehni
 *
 */
public enum WindingRule implements IntegerEnum {
	EVEN_ODD(1),
	NON_ZERO(0);

	protected int value;

	private WindingRule(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
