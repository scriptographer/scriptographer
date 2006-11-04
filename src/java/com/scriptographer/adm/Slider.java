/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2006 Juerg Lehni, http://www.scratchdisk.com.
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
 * File created on 03.01.2005.
 *
 * $RCSfile: Slider.java,v $
 * $Author: lehni $
 * $Revision: 1.6 $
 * $Date: 2006/11/04 11:47:26 $
 */

package com.scriptographer.adm;

public class Slider extends ValueItem {

	// ADMSliderStyle
	public final static int
		STYLE_NONE = 0,
		STYLE_NONLINEAR = 1,
		STYLE_SHOW_FRACTION = 2;

	public Slider(Dialog dialog) {
		super(dialog, TYPE_SLIDER, OPTION_NONE);
	}

}
