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
 * File created on 03.01.2005.
 */

package com.scriptographer.adm;

import java.util.EnumSet;

import com.scratchdisk.util.EnumUtils;
import com.scratchdisk.util.IntegerEnumUtils;

/**
 * @author lehni
 */
public class TextEdit extends TextEditItem<TextEditStyle> {

	protected TextEdit(Dialog dialog, int handle, boolean isChild) {
		super(dialog, handle, isChild);
	}

	public TextEdit(Dialog dialog, EnumSet<TextOption> options) {
		// filter out the pseudo styles from the options:
		// (max. real bit is 3, and the mask is (1 << (max + 1)) - 1
		super(dialog, options);
	}

	public TextEdit(Dialog dialog, TextOption[] options) {
		this(dialog, EnumUtils.asSet(options));
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
