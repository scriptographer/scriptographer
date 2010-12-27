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
 * File created on Apr 11, 2008.
 */

package com.scriptographer.ai;

import com.scratchdisk.util.IntegerEnum;

/**
 * AIRasterizeType, AIColorConversionSpaceValue
 * Used in Color.convert() and Raster
 * the conversion to the right AIColorConversionSpaceValue values is done
 * in native code. 
 * 
 * @author lehni
 */
public enum ColorType implements IntegerEnum {
	RGB(0, false), // RGB no alpha
	CMYK(1, false), // CMYK no alpha
	GRAY(2, false), // Grayscale no alpha
	BITMAP(3, false), // opaque bitmap
	ARGB(4, true), // RGB with alpha
	ACMYK(5, true), // CMYK with alpha
	AGRAY(6, true), // Grayscale with alpha
	ABITMAP(8, true); // bitmap with transparent 0-pixels
	/* TODO: 
	kRasterizeSeparation,
	kRasterizeASeparation,
	kRasterizeNChannel,
	kRasterizeANChannel
	*/

	protected int value;
	protected boolean alpha;

	private ColorType(int value, boolean alpha) {
		this.value = value;
		this.alpha = alpha;
	}

	public int value() {
		return value;
	}
}
