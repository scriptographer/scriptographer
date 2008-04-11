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

import com.scriptographer.NamedOption;

/**
 * AIRasterizeType, AIColorConversionSpaceValue
 * Used in Color.convert() and Raster
 * the conversion to the right AIColorConversionSpaceValue values is done
 * in native code. 
 * 
 * @author lehni
 */
public class ColorType extends NamedOption {

	public static final ColorType
		RGB 		= new ColorType("rgb",		0), // RGB no alpha
		CMYK		= new ColorType("cmyk",		1), // CMYK no alpha
		GRAY		= new ColorType("gray",		2), // Grayscale no alpha
		BITMAP		= new ColorType("bitmap",	3), // opaque bitmap
		ARGB		= new ColorType("argb",		4), // RGB with alpha
		ACMYK		= new ColorType("acmyk",	5), // CMYK with alpha
		AGRAY		= new ColorType("agray",	6), // Grayscale with alpha
		ABITMAP		= new ColorType("abitmap", 8); // bitmap with transparent 0-pixels


	private ColorType(String name, int value) {
		super(name, value);
	}

	protected static ColorType get(int value) {
		return (ColorType) get(ColorType.class, value);
	}
}
