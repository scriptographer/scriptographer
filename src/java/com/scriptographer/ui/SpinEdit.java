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
 * File created on 20.10.2005.
 * 
 * $Id$
 */

package com.scriptographer.ui;

import java.util.EnumSet;

import com.scratchdisk.util.EnumUtils;
import com.scriptographer.ScriptographerEngine;

/**
 * @author lehni
 */
public class SpinEdit extends TextEditItem<SpinEditStyle> {

	public SpinEdit(Dialog dialog, EnumSet<TextOption> options) {
		super(dialog, getType(options), options);
	}

	/**
	 * @param dialog
	 * @param options only TextEdit.OPTION_POPUP and TextEdit.OPTION_SCROLLING
	 *        are valid for SpinEdit
	 */
	public SpinEdit(Dialog dialog, TextOption[] options) {
		this(dialog, EnumUtils.asSet(options));
	}

	public SpinEdit(Dialog dialog) {
		this(dialog, (EnumSet<TextOption>) null);
	}

	private static ItemType getType(EnumSet<TextOption> options) {
		// abuse the ADM's password style for creating it as a type...
		if (options != null && options.contains(TextOption.POPUP)) {
			return options.contains(TextOption.SCROLLING)
					? ItemType.SPINEDIT_SCROLLING_POPUP
					: ItemType.SPINEDIT_POPUP;
		}
		return ItemType.SPINEDIT;
	}
	
	public SpinEditStyle getStyle() {
		// For some weird reason vertical has different values for popup and
		// non-popup
		return nativeGetStyle() != 0
				? SpinEditStyle.VERTICAL
				: SpinEditStyle.HORIZONTAL;
	}

	public void setStyle(SpinEditStyle style) {
		// VERTICAL = 4 for popups and 1 for non popups
		nativeSetStyle(style == SpinEditStyle.VERTICAL
				? (type == ItemType.SPINEDIT ? 1 : 4)
				: 0);
	}

	/*
	 *  child items
	 */

	private static final int
		ITEM_UP_BUTTON = 1,
		ITEM_DOWN_BUTTON = 2,
		ITEM_TEXT_EDIT = 3;
	
	private Button upButton;
	private Button downButton;
	private TextEdit editItem;
	
	public Button getUpButton() {
		if (upButton == null) {
			int handle = getChildItemHandle(ITEM_UP_BUTTON);
			upButton = handle != 0 ? new Button(dialog, handle, true) : null;
		}
		return upButton;
	}
	
	public Button getDownButton() {
		if (upButton == null) {
			int handle = getChildItemHandle(ITEM_DOWN_BUTTON);
			downButton = handle != 0 ? new Button(dialog, handle, true) : null;
		}
		return downButton;
	}

	public TextEdit getTextEdit() {
		if (editItem == null) {
			int handle = getChildItemHandle(ITEM_TEXT_EDIT);
			editItem = handle != 0 ? new TextEdit(dialog, handle, true) : null;
		}
		return editItem;
	}

	private int xDiff = -1;

	protected void nativeSetBounds(int x, int y, int width, int height) {
		// This seems needed on Mac, as all TextEditItems appear 2px smaller
		// than they are told to. Also if it has a popup list, the popup button
		// gets cropped on the 2nd time bounds are set otherwise. 
//		if (ScriptographerEngine.isMacintosh())
//			height -= 2;
		super.nativeSetBounds(x, y, width, height);
	}

	protected void updateBounds(int x, int y, int width, int height, boolean sizeChanged) {
		if (ScriptographerEngine.isMacintosh()) {
			TextEdit edit = getTextEdit();
			if (xDiff == -1)
				xDiff = edit.getPosition().x + getPosition().x;
			super.updateBounds(x, y, width, height, sizeChanged);
			edit.setSize(width - xDiff, height);
		} else {
			super.updateBounds(x, y, width, height, sizeChanged);
		}
	}
}
