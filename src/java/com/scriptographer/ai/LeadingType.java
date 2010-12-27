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

package com.scriptographer.ai;

import com.scratchdisk.util.IntegerEnum;

/**
 * @author lehni
 *
 */
public enum LeadingType implements IntegerEnum {
	ROMAN(0),
	JAPANESE(1);
	
	protected int value;

	private LeadingType(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
