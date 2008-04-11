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
 * AIBlendingModeValues
 * 
 * @author lehni
 */
public class BlendMode extends NamedOption {

	public static final BlendMode
		NORMAL				= new BlendMode("normal",		0),
		MULTIPLY			= new BlendMode("multiply",	1),
		SCREEN				= new BlendMode("screen",		2),
		OVERLAY				= new BlendMode("overlay",		3),
		SOFT_LIGHT			= new BlendMode("softLight",	4),
		HARD_LIGHT			= new BlendMode("hardLight",	5),
		COLOR_DODGE			= new BlendMode("colorDodge",	6),
		COLOR_BURN			= new BlendMode("colorBurn",	7),
		DARKEN				= new BlendMode("darken",		8),
		LIGHTEN				= new BlendMode("lighten",		9),
		DIFFERENCE			= new BlendMode("difference",	10),
		EXCLUSION			= new BlendMode("exclusion",	11),
		HUE					= new BlendMode("hue",			12),
		SATURATION			= new BlendMode("saturation",	13),
		COLOR				= new BlendMode("color",		14),
		LUMINOSITY			= new BlendMode("luminosity",	15);

	private BlendMode(String name, int value) {
		super(name, value);
	}

	protected static BlendMode get(int value) {
		return (BlendMode) get(BlendMode.class, value);
	}
}
