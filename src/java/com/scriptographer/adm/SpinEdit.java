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
 * File created on 20.10.2005.
 * 
 * $Id$
 */

package com.scriptographer.adm;

import java.util.Arrays;
import java.util.EnumSet;

/**
 * @author lehni
 */
public class SpinEdit extends TextEdit {

	public static final int
	// ADMSpinEditStyle
		STYLE_VERTICAL = 0,
		STYLE_HORIZONTAL = 1,
	// ADMSpinEditPopupStyle
		STYLE_POPUP_VERTICAL = 0,
		STYLE_POPUP_HORIZONTAL = 4;

	/**
	 * @param dialog
	 * @param options
	 *            only TextEdit.OPTION_POPUP and TextEdit.OPTION_SCROLLING are
	 *            valid for SpinEdit
	 */
	public SpinEdit(Dialog dialog, EnumSet<TextOption> options) {
		super(dialog, getType(options), options);
	}

	public SpinEdit(Dialog dialog, TextOption[] options) {
		this(dialog, EnumSet.copyOf(Arrays.asList(options)));
	}

	public SpinEdit(Dialog dialog) {
		this(dialog, (EnumSet<TextOption>) null);
	}

	private static ItemType getType(EnumSet<TextOption> options) {
		// abuse the ADM's password style for creating it as a type...
		if (options != null && options.contains(TextOption.POPUP)) {
			return options.contains(TextOption.SCROLLING) ? ItemType.SPINEDIT_SCROLLING_POPUP
					: ItemType.SPINEDIT_POPUP;
		}
		return ItemType.SPINEDIT;
	}
	
	/*
	 *  child items
	 */

	private static final int
		ITEM_UP_BUTTON = 1,
		ITEM_DOWN_BUTTON = 2;
	
	private Button upButton;
	private Button downButton;
	
	public Button getUpButton() {
		if (upButton == null) {
			long handle = getChildItemHandle(ITEM_UP_BUTTON);
			upButton = handle != 0 ? new Button(dialog, handle) : null;
		}
		return upButton;
	}
	
	public Button getDownButton() {
		if (upButton == null) {
			long handle = getChildItemHandle(ITEM_DOWN_BUTTON);
			downButton = handle != 0 ? new Button(dialog, handle) : null;
		}
		return downButton;
	}
}
