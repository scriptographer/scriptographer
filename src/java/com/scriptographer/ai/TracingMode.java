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
 * AITracingMode
 * 
 * @author lehni
 */
public enum TracingMode implements IntegerEnum {
	/** Color.  Either RGB or CMYK depending on source image.  */
	COLOR(0),
	/** Gray. */
	GRAY(1),
	/** Bitmap. */
	BITMAP(2);

	protected int value;

	private TracingMode(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
