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

import com.scratchdisk.util.IntegerEnumUtils;

/**
 * @author lehni
 */
public class Dial extends ValueItem {

	public Dial(Dialog dialog) {
		super(dialog, ItemType.DIAL);
	}

	public DialStyle getStyle() {
		return IntegerEnumUtils.get(DialStyle.class, nativeGetStyle());
	}

	public void setStyle(DialStyle style) {
		if (style != null)
			nativeSetStyle(style.value);
	}
}
