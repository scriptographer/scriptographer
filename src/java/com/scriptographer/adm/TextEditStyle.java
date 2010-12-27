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
 * ADMTextEditStyle, ADMTextEditPopupStyle
 * 
 * @author lehni
 */
public enum TextEditStyle implements IntegerEnum {
	SINGLELINE(0),
	NUMERIC(2),			// 'Numeric' means float. Default.
	TRACK_RAW_KEYS(4),	// Mac-only; ignores default Carbon event processing; not compatible with kADMUnicodeEditCreateOption
	EXCLUSIVE(5),		// only for TextEditPopup
	PASSWORD(32);		// Win32 value for ES_PADMSWORD

	protected int value;

	private TextEditStyle(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
