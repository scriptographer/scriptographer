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
 * File created on Apr 15, 2008.
 */

package com.scriptographer.ui;

import com.scratchdisk.util.IntegerEnum;

/**
 * ADMFont
 * 
 * @author lehni
 */
public enum DialogFont implements IntegerEnum {
	DEFAULT(0),
	DIALOG(1),
	DIALOG_ITALIC(3),
	DIALOG_BOLD(5),
	DIALOG_BOLD_ITALIC(7),
	PALETTE(2),
	PALETTE_ITALIC(4),
	PALETTE_BOLD(6),
	PALETTE_BOLD_ITALIC(8),
	MONOSPACED(9),
	MONOSPACED_ITALIC(10),
	MONOSPACED_BOLD(11),
	MONOSPACED_BOLD_ITALIC(12);

	public int value;

	private DialogFont(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
