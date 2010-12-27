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
 * File created on Apr 13, 2008.
 */

package com.scriptographer.ai;

import com.scratchdisk.util.IntegerEnum;

/**
 * @author lehni
 *
 */
public enum ViewStyle implements IntegerEnum {
	/** Outline mode. */
	ARTWORK(0x0001),
	/** Preview mode. */
	PREVIEW(0x0002),
	/** Pixel preview mode. */
	RASTER(0x0040),
	/** Unimplemented. Transparency attributes and masks are ignored. */
	OPAQUE(0x0040),
	/** OPP preview mode. */
	INK(0x0100);

	protected int value;

	private ViewStyle(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
