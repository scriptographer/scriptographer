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
 * File created on Apr 15, 2008.
 */

package com.scriptographer.adm;

import com.scratchdisk.util.IntegerEnum;

/**
 * ADMRecolorStyle
 * 
 * @author lehni
 */
public enum RecolorStyle implements IntegerEnum {
	NO(0),
	ACTIVE(1),
	INACTIVE(2),
	DISABLED(3);

	protected int value;

	private RecolorStyle(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}