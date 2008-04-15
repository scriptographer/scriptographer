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

import java.util.Arrays;
import java.util.EnumSet;

/**
 * @author lehni
 */
public class Static extends TextEdit {

	//  Text static styles
	public static final int
		STYLE_CLIPPEDTEXT 					= 1 << 0,
		STYLE_DISABLE_AUTO_ACTIVATE_TEXT 	= 1 << 1,
		STYLE_TRUNCATE_ENDTEXT 				= 1 << 2, // clipped style has priority
		STYLE_TRUNCATE_MIDDLETEXT 			= 1 << 3; // truncate end has priority

	/**
	 * Creates a text based Static item.
	 * @param dialog
	 * @param options
	 */
	public Static(Dialog dialog, EnumSet<TextOption> options) {
		super(dialog, options != null && options.contains(TextOption.MULTILINE)
				? ItemType.TEXT_STATIC_MULTILINE : ItemType.TEXT_STATIC, options);
	}

	public Static(Dialog dialog, TextOption[] options) {
		this(dialog, EnumSet.copyOf(Arrays.asList(options)));
	}
	
	public Static(Dialog dialog) {
		this(dialog, (EnumSet<TextOption>) null);
	}
}
