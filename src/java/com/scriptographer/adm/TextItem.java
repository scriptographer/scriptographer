/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2005 Juerg Lehni, http://www.scratchdisk.com.
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
 * File created on 03.01.2005.
 *
 * $RCSfile: TextItem.java,v $
 * $Author: lehni $
 * $Revision: 1.2 $
 * $Date: 2005/03/07 13:35:07 $
 */

package com.scriptographer.adm;

import java.awt.geom.Rectangle2D;

public abstract class TextItem extends ValueItem {

	// ADMJustify
	public final static int
		JUSTIFY_LEFT = 0,
		JUSTIFY_CENTER = 1,
		JUSTIFY_RIGHT = 2;
	
	// ADMUnits
	public final static int
		UNITS_NO = 0,
		UNITS_POINT = 1,
		UNITS_INCH = 2,
		UNITS_MILLIMETER = 3,
		UNITS_CENTIMETER = 4,
		UNITS_PICA = 5,
		UNITS_PERCENT = 6,
		UNITS_DEGREE = 7,
		UNITS_Q = 8,
		UNITS_BASE16 = 9,
		UNITS_PIXEL = 10,
		UNITS_TIME = 11,
		UNITS_HA = 12;

	protected String text;

	public TextItem(Dialog dialog, String type, Rectangle2D bounds, String text, int style, int options) {
		super(dialog, type, bounds, style, options);
		this.text = text;
		if (text != null)
			nativeSetText(text);
	}

	public TextItem(Dialog dialog, String type, Rectangle2D bounds, String text, int style) {
		this (dialog, type, bounds, text, style, 0);
	}

	/* 
	 * item text accessors
	 * 
	 */

	public native int getFont();
	public native void setFont(int font);

	protected native void nativeSetText(String text);
	protected native String nativeGetText();

	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
		nativeSetText(text);
	}
	
	public native void setJustify(int justify);
	public native int getJustify();
	
	// not sure wether these are at the right place. usefull for Static as well?
	// otherwise move to TextEdit:

	public native void setUnits(int units);
	public native int getUnits();

	public native void setShowUnits(boolean showUnits);
	public native boolean getShowUnits();

}
