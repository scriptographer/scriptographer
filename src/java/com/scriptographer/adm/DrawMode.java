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
 * ADMDrawMode
 * 
 * @author lehni
 */
public enum DrawMode implements IntegerEnum {
	NORMAL(0),
	XOR(1);

	protected int value;

	private DrawMode(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
