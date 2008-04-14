/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2008 Juerg Lehni, http://www.scratchdisk.com.
 * All rights reserved.
 *
 * Please visit http://scriptographer.com/ for updates and contact.
 *
 * -- GPL LICENSE NOTICE --
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * -- GPL LICENSE NOTICE --
 * 
 * File created on Apr 11, 2008.
 *
 * $Id$
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
