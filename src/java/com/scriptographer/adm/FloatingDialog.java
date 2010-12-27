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

package com.scriptographer.adm;

import java.util.EnumSet;

import com.scratchdisk.util.EnumUtils;

/**
 * @author lehni
 */
public class FloatingDialog extends Dialog {

	public FloatingDialog(EnumSet<DialogOption> options) {
		super(getStyle(options), options);
	}

	public FloatingDialog(DialogOption[] options) {
		this(EnumUtils.asSet(options));
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
