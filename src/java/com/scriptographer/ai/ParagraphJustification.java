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
 * ParagraphJustification
 * 
 * @author lehni
 *
 */
public enum ParagraphJustification implements IntegerEnum {
	LEFT(0),
	RIGHT(1),
	CENTER(2),
	FULL_LAST_LINE_LEFT(3),
	FULL_LAST_LINE_RIGHT(4),
	FULL_LAST_LINE_CENTER(5),
	FULL(6);

	protected int value;

	private ParagraphJustification(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
