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
 * $RCSfile: ValueItem.java,v $
 * $Author: lehni $
 * $Revision: 1.1 $
 * $Date: 2005/02/23 22:00:59 $
 */

package com.scriptographer.adm;

import java.awt.geom.Rectangle2D;

import com.scriptographer.js.ArgumentReader;
import com.scriptographer.js.ArgumentReader;

public abstract class ValueItem extends Item {
	public ValueItem(Dialog dialog, String type, Rectangle2D bounds, int style, int options) {
		super(dialog, type, bounds, style, options);
	}

	/* 
	 * item value accessors
	 * 
	 */

	public native float[] getValueRange();
	public native void setValueRange(float minValue, float maxValue);
	
	public void setValueRange(float[] range) {
		setValueRange(range[0], range[1]);
	}

	public native int getPrecision();
	public native void setPrecision(int precision);
	
	public native boolean getBooleanValue();
	public native void setBooleanValue(boolean value);
	
	public native float getFloatValue();
	public native void setFloatValue(float value);

	public void setFloatValue(Object value) {
		Number num = new ArgumentReader().readNumber(value);
		if (num != null)
			setFloatValue(num.floatValue());
	}
	
	public void setBooleanValue(Object value) {
		Boolean bool = new ArgumentReader().readBoolean(value);
		if (bool != null)
			setBooleanValue(bool.booleanValue());
	}
	
	public Object getValue() {
		return new Double(getFloatValue());
	}
	
	public void setValue(Object value) {
		setFloatValue(value);
	}
}
