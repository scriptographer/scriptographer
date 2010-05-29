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
 * File created on 14.03.2005.
 */

package com.scriptographer.ui;

import java.util.EnumSet;

import com.scratchdisk.util.EnumUtils;
/**
 * @author lehni
 */
public class PopupDialog extends ModalDialog {

	public PopupDialog(EnumSet<DialogOption> options) {
		super(getStyle(options), options);		
	}

	public PopupDialog(DialogOption[] options) {
		this(EnumUtils.asSet(options));
	}

	public PopupDialog() {
		this((EnumSet<DialogOption>) null);
	}
	
	/*
	 * Extract the style from the pseudo options:
	 */
	private static int getStyle(EnumSet<DialogOption> options) {
		if (options.contains(DialogOption.CONTROL)) {
			return STYLE_POPUP;
		} else {
			return STYLE_POPUP_CONTROL;
		}
	}
}
