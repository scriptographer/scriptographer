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
 * @author lehni
 *
 */
public enum Cursor implements IntegerEnum {
	IBEAM(-1),
	CROSS(-2),
	WAIT(-3),
	ARROW(-4),
	CANCEL(-5),
	FINGER(-6),
	FIST(-7),
	FISTPLUS(-8),
	HOSTCONTROLS(-9);

	protected int value;

	private Cursor(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
