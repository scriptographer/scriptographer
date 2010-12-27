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
 * ADMSliderStyle
 * 
 * @author lehni
 */
public enum SliderStyle implements IntegerEnum {
	NONE(0),
	NONLINEAR(1),
	SHOW_FRACTION(2);

	protected int value;

	private SliderStyle(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
