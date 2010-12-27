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
 * ASCharType
 * 
 * @author lehni
 */
public enum CharacterType implements IntegerEnum {
	/** undefined character */
	UNDEFINED(-1),
	/** space character */
	SPACE(0),
	/** punctuation character */
	PUNCTUATION(1),
	/** paragraph end character CR */
	PARAGRAPH_END(2),
	/** this character is anything but space, punctuation or paragraph end */
	NORMAL(3);

	protected int value;

	private CharacterType(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
