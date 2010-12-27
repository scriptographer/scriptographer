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
public enum AngleUnits {
	DEGREES,
	RADIANS;

	// The new default units for angles in Scriptographer are degrees.
	public static final AngleUnits DEFAULT = DEGREES;
}
