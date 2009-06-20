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

package com.scriptographer.ui;

import java.util.Arrays;
import java.util.EnumSet;

import com.scratchdisk.util.IntegerEnumUtils;

/**
 * @author lehni
 * 
 * @jshide
 */
public class TextEdit extends TextEditItem<TextEditStyle> {

	protected TextEdit(Dialog dialog, int handle) {
		super(dialog, handle);
	}
	
	public TextEdit(Dialog dialog, EnumSet<TextOption> options) {
		// filter out the pseudo styles from the options:
		// (max. real bit is 3, and the mask is (1 << (max + 1)) - 1
		super(dialog, options);
	}

	public TextEdit(Dialog dialog, TextOption[] options) {
		this(dialog, EnumSet.copyOf(Arrays.asList(options)));
	}

	public TextEdit(Dialog dialog) {
		this(dialog, (EnumSet<TextOption>) null);
	}

	public TextEditStyle getStyle() {
		return IntegerEnumUtils.get(TextEditStyle.class, nativeGetStyle());
	}

	public void setStyle(TextEditStyle style) {
		if (style != null)
			nativeSetStyle(style.value);
	}
}
