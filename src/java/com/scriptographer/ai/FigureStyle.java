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
 * FigureStyle
 * 
 * @author lehni
 */
public enum FigureStyle implements IntegerEnum {
	DEFAULT(0),
	TABULAR(1),
	PROPORTIONAL_OLDSTYLE(2),
	PROPORTIONAL(3),
	TABULAR_OLDSTYPE(4);

	protected int value;

	private FigureStyle(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
