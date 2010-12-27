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
 * CaseChangeType
 * 
 * @author lehni
 */
public enum TextCase implements IntegerEnum {
	UPPER(0),
	LOWER(1),
	TITLE(2),
	SENTENCE(3);

	protected int value;

	private TextCase(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
