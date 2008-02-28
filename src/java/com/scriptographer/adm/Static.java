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
 * File created on 03.01.2005.
 *
 * $Id$
 */

package com.scriptographer.adm;

/**
 * @author lehni
 */
public class Static extends TextValueItem {

	//  Text static styles
	public final static int
		STYLE_CLIPPEDTEXT 					= 1 << 0,
		STYLE_DISABLE_AUTO_ACTIVATE_TEXT 	= 1 << 1,
		STYLE_TRUNCATE_ENDTEXT 				= 1 << 2, // clipped style has priority
		STYLE_TRUNCATE_MIDDLETEXT 			= 1 << 3; // truncate end has priority

	public final static int
		// a fake option that tells the constructor to construct a MULTILINE
		// static item.
		OPTION_MULTILINE = 1 << 1;

	/**
	 * Creates a text based Static item.
	 * @param dialog
	 * @param options
	 */
	public Static(Dialog dialog, int options) {
		super(dialog,
			(options & OPTION_MULTILINE) != 0 ? TYPE_TEXT_STATIC_MULTILINE
				: TYPE_TEXT_STATIC, OPTION_NONE);
	}
	
	public Static(Dialog dialog) {
		this(dialog, OPTION_NONE);
	}
}
