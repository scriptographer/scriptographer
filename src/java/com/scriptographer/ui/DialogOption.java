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
 * File created on Apr 14, 2008.
 *
 * $Id$
 */

package com.scriptographer.ui;

import com.scratchdisk.util.IntegerEnum;

/**
 * @author lehni
 *
 */
public enum DialogOption implements IntegerEnum {
	/*
	 * Options with value 0 are pseudo options, to simulate the various window
	 * styles.
	 */
	/**
	 * Keypad 'enter' key does not activate default item.
	 */
	IGNORE_KEYPAD_ENTER(1 << 3),
	/**
	 * Reduce flicker by creating items hidden.
	 */
//	ITEMS_HIDDEN(1 << 4),
	/**
	 * Forces for all items within dialog, except as overridden.
	 */
	FORCE_ROMAN(1 << 5),
	/**
	 * Track the enter keys carriage return and keypad enter before the
	 * dialog treats the event as equivalent to pressing the OK button --
	 * and prevent that behavior if the tracker returns true. Note that by
	 * default, the enter keys cause text item trackers to commit their text
	 * and return true, so this option normally prevents the "OK" behavior
	 * when enter is pressed within a text item.
	 * This option currently relevant only on Mac platform.
	 */
	ENTER_BEFORE_OK(1 << 6),
	/**
	 * Create the dialog hidden
	 */
	HIDDEN(0),
	/**
	 * Remember placing of the dialog by automatically storing its state in
	 * the preference file. For each script, a sub-node is created in the
	 * preferences. The dialog's title needs to be unique within one such node,
	 * as it is used to store the dialog's state.
	 */
	REMEMBER_PLACING(0),
	/**
	 * For FloatingDialog
	 * When creating tabbed dialog with a cycle button on the tab.
	 */
	SHOW_CYCLE(1 << 0),
	// Pseudo options, to simulate the various window styles
	/**
	 * For FloatingDialog and ModalDialog
	 */
	RESIZING(0),
	/**
	 * For FloatingDialog
	 */
	TABBED(0),
	/**
	 * For FloatingDialog
	 */
	LEFT_SIDED(0),
	/**
	 * For FloatingDialog
	 */
	NO_CLOSE(0),
	/**
	 * To allow modal dialogs pass mouse down events through to
	 * the user dialog tracker on the application side.
	 */
	PASS_MOUSEDOWN_EVENT(1 << 1),
	/**
	 * 0 by default. If set, modal dialogs on Windows will have a 
	 * close box on the top right hand corner. Also there is a host
	 * option that a user can use if all dialogs in the application
	 * need that behavior. 
	 */
	HAS_SYSTEM_CONTROLS(1 << 7),
	/**
	 * Modal Alert, cannot be combined with OPTION_RESIZING
	 */
	ALERT(0),
	/**
	 * 	Modal System Alert, cannot be combined with OPTION_RESIZING
	 */
	SYSTEM_ALERT(0),
	/**
	 * For PopupDialog
	 * If this option is set for a dialog of style kADMPopupControldialogStyle
	 * then ADM will create the dialog of kFloatingwindowclass. This option
	 * is currently used only on MacOSX.
	 */	
	FLOATING(1 << 8),
	/**
	 * For PopupDialog
	 * Create a STYLE_POPUP_CONTROL instead of STYLE_POPUP
	 */
	CONTROL(0);
	/*
	 * TODO: define kADMDocumentWindowLayerDialogOption,
	 * kADMPaletteLayerDialogOption
	 */

	protected int value;

	private DialogOption(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
