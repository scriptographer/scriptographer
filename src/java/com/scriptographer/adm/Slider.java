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
public class Slider extends ValueItem {

	public Slider(Dialog dialog) {
		super(dialog, ItemType.SLIDER);
	}

	public SliderStyle getStyle() {
		return IntegerEnumUtils.get(SliderStyle.class, nativeGetStyle());
	}

	public void setStyle(SliderStyle style) {
		if (style != null)
			nativeSetStyle(style.value);
	}

	protected static final Border MARGIN = new Border(4, 0, 0, 0);

	protected Border getNativeMargin() {
		return MARGIN;
	}
}
