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
 * AITracingVisualizeVectorType
 * 
 * @author lehni
 */
public enum TracingVectorDisplay implements IntegerEnum {
	/**
	 * No Artwork.
	 **/
	NONE(0),
	/**
	 * Artwork. 
	 * The full result, paths and fills, of the tracing.
	 **/
	ARTWORK(1),
	/**
	 * Paths.
	 * A paths only version of the "Artwork" mode with black stroked paths.
	 **/
	OUTLINES(2),
	/**
	 * Paths and Transparency.
	 * A "transparent" version of the "Artwork" mode with black stroked paths overlaid.
	 **/
	ARTWORK_AND_OUTLINES(3);

	protected int value;

	private TracingVectorDisplay(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}