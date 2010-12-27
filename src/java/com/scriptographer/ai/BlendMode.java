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
 * AIBlendingModeValues
 * 
 * @author lehni
 */
public enum BlendMode implements IntegerEnum {
	NORMAL(0),
	MULTIPLY(1),
	SCREEN(2),
	OVERLAY	(3),
	SOFT_LIGHT(4),
	HARD_LIGHT(5),
	COLOR_DODGE	(6),
	COLOR_BURN(7),
	DARKEN(8),
	LIGHTEN	(9),
	DIFFERENCE(10),
	EXCLUSION(11),
	HUE(12),
	SATURATION(13),
	COLOR(14),
	LUMINOSITY(15);

	protected int value;

	private BlendMode(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
