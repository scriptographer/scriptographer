/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2010 Juerg Lehni, http://www.scratchdisk.com.
 * All rights reserved.
 *
 * Please visit http://scriptographer.org/ for updates and contact.
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
 */

package com.scriptographer.ui;

import java.util.EnumSet;

import com.scratchdisk.util.EnumUtils;
import com.scratchdisk.util.IntegerEnumUtils;

/**
 * @author lehni
 */
public class TextPane extends TextValueItem {

	public TextPane(Dialog dialog, EnumSet<TextOption> options) {
		super(dialog, options != null && options.contains(TextOption.MULTILINE)
				? ItemType.TEXT_STATIC_MULTILINE : ItemType.TEXT_STATIC,
				IntegerEnumUtils.getFlags(options));
	}

	/**
	 * Creates a text based Static item.
	 * @param dialog
	 * @param options
	 */
	public TextPane(Dialog dialog, TextOption[] options) {
		this(dialog, EnumUtils.asSet(options));
	}
	
	public TextPane(Dialog dialog) {
		this(dialog, (EnumSet<TextOption>) null);
	}

	public TextPaneStyle getStyle() {
		return IntegerEnumUtils.get(TextPaneStyle.class, nativeGetStyle());
	}

	public void setStyle(TextPaneStyle style) {
		if (style != null)
			nativeSetStyle(style.value);
	}
}
