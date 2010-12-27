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
 * AITracingVisualizeRasterType
 * 
 * @author lehni
 */
public enum TracingRasterDisplay implements IntegerEnum {
	/**
	 * None.
	 * No raster is included in the visualization.
	 **/
	NONE(0),
	/**
	 * Original.
	 * The original raster input (before preprocessing).
	 **/
	ORIGINAL(1),
	/**
	 * Preprocessed.
	 * The preprocessed image.
	 **/
	ADJUSTED(2),
	/**
	 * Transparency.
	 * A "transparent" version of the "Original" mode.
	 **/
	TRANSPARENT(3);

	protected int value;

	private TracingRasterDisplay(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}