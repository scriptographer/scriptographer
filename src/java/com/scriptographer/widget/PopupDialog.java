/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Scripting Plugin for Adobe Illustrator
 * http://scriptographer.org/
 *
 * Copyright (c) 2002-2010, Juerg Lehni
 * http://scratchdisk.com/
 *
 * All rights reserved. See LICENSE file for details.
 *
 * File created on 14.03.2005.
 */

package com.scriptographer.widget;

import java.util.EnumSet;

import com.scratchdisk.util.EnumUtils;
import com.scriptographer.ui.DialogOption;
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
