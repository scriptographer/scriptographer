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
 * File created on 14.03.2005.
 *
 * $Id$
 */

package com.scriptographer.ui;

import java.util.Arrays;
import java.util.EnumSet;

/**
 * @author lehni
 */
public class FloatingDialog extends Dialog {

	public FloatingDialog(EnumSet<DialogOption> options) {
		super(getStyle(options), options);
	}

	public FloatingDialog(DialogOption[] options) {
		this(EnumSet.copyOf(Arrays.asList(options)));
	}

	public FloatingDialog() {
		this((EnumSet<DialogOption>) null);
	}

	/*
	 * Extract the style from the pseudo options:
	 */
	private static int getStyle(EnumSet<DialogOption> options) {
		if (options != null) {
			if (options.contains(DialogOption.TABBED)) {
				if (options.contains(DialogOption.RESIZING)) {
					return STYLE_TABBED_RESIZING_FLOATING;
				} else {
					return STYLE_TABBED_FLOATING;
				}
			} else if (options.contains(DialogOption.LEFT_SIDED)) {
				if (options.contains(DialogOption.NO_CLOSE)) {
					return STYLE_LEFTSIDED_NOCLOSE_FLOATING;
				} else {
					return STYLE_LEFTSIDED_FLOATING;
				}
			} else {
				if (options.contains(DialogOption.RESIZING)) {
					return STYLE_RESIZING_FLOATING;
				} else if (options.contains(DialogOption.NO_CLOSE)) {
					return STYLE_NOCLOSE_FLOATING;
				}
			}
		}
		return STYLE_FLOATING;
	}
}
