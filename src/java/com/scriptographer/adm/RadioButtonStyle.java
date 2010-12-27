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
 * ADMRadioButtonStyle
 * 
 * @author lehni
 */
public enum RadioButtonStyle implements IntegerEnum {
	ONE_ALWAYS_SET(0),
	ALLOW_NONE_SET(2);

	protected int value;

	private RadioButtonStyle(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
