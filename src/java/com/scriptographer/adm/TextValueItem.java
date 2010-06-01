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
 * File created on 03.01.2005.
 */

package com.scriptographer.adm;

import com.scratchdisk.util.IntMap;
import com.scratchdisk.util.IntegerEnumUtils;
import com.scriptographer.ui.TextUnits;

/**
 * @author lehni
 * 
 * @jshide
 */
public abstract class TextValueItem extends ValueItem {

	protected TextValueItem(Dialog dialog, int handle, boolean isChild) {
		super(dialog, handle, isChild);
	}

	protected TextValueItem(Dialog dialog, ItemType type, int options) {
		super(dialog, type, options);
	}

	/*
	 * item text accessors
	 * 
	 */

	public native void setText(String text);
	public native String getText();
	
	private native void nativeSetJustification(int justification);
	private native int nativeGetJustification();
	
	public void setJustification(TextJustification justification) {
		if (justification != null)
			nativeSetJustification(justification.value);
	}

	public TextJustification getJustification() {
		return IntegerEnumUtils.get(TextJustification.class,
				nativeGetJustification());
	}
	// justify and units: useful for TextEdit and Static

	private native void nativeSetUnits(int units);
	private native int nativeGetUnits();

	/*
	 * ADMUnits to TextUnits lookup table
	 */
	private static IntMap<TextUnits> textUnitsLookup = new IntMap<TextUnits>();
	static {
		textUnitsLookup.put(0, TextUnits.NONE);
		textUnitsLookup.put(1, TextUnits.POINT);
		textUnitsLookup.put(2, TextUnits.INCH);
		textUnitsLookup.put(3, TextUnits.MILLIMETER);
		textUnitsLookup.put(4, TextUnits.CENTIMETER);
		textUnitsLookup.put(5, TextUnits.PICA);
		textUnitsLookup.put(6, TextUnits.PERCENT);
		textUnitsLookup.put(7, TextUnits.DEGREE);
		textUnitsLookup.put(10, TextUnits.PIXEL);
	}

	public void setUnits(TextUnits units) {
		Integer key = textUnitsLookup.keyOf(units);
		nativeSetUnits(key != null ? key : 0);
	}

	public TextUnits getTextUnitsLookup() {
		TextUnits units = textUnitsLookup.get(nativeGetUnits());
		return units != null ? units : TextUnits.NONE;
	}
	
	public native void setShowUnits(boolean showUnits);
	public native boolean getShowUnits();

	public boolean isMultiline() {
		return type == ItemType.TEXT_EDIT_MULTILINE
				|| type == ItemType.TEXT_EDIT_MULTILINE_READONLY
				// TODO: TEXT_STATIC appears to support multiline as well,
				// and TEXT_STATIC_MULTILINE is actually a disabled edit field
				// with scrollbars, so TextPane might need different options
				// or handling.
				|| type == ItemType.TEXT_STATIC
				|| type == ItemType.TEXT_STATIC_MULTILINE;
	}

}
