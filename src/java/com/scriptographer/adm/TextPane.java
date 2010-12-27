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
