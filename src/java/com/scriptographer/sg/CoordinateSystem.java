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
 * File created on Sep 26, 2010.
 */

package com.scriptographer.sg;

/**
 * @author lehni
 *
 */
public enum CoordinateSystem {
	TOP_DOWN,
	BOTTOM_UP;

	// The new default coordinates system in Scriptographer top-down.
	public static final CoordinateSystem DEFAULT = TOP_DOWN;
}
