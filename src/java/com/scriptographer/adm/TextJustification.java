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
 * ADMJustify
 * 
 * @author lehni
 */
public enum TextJustification implements IntegerEnum {
	LEFT(0),
	CENTER(1),
	RIGHT(2);

	protected int value;

	private TextJustification(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
