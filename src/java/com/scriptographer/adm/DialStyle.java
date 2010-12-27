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
 * ADMDialStyle
 * 
 * @author lehni
 */
public enum DialStyle implements IntegerEnum {
	NO_ARROW(0),
	ARROW_AT_END(1),
	ARROW_AT_CENTER(2);

	protected int value;

	private DialStyle(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
