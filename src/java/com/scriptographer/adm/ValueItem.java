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
 * $Revision: 1.4 $
 * $Date: 2005/03/25 00:27:57 $
 */

package com.scriptographer.adm;

public abstract class ValueItem extends Item {
	public ValueItem(Dialog dialog, int type, int options) {
		super(dialog, type, options);
	}
	
	/*
	 * Callback functions
	 */
	
	protected void onChange() throws Exception {
		callFunction("onChange");
	}
	
	protected void onPreChange() throws Exception {
		callFunction("onPreChange");
	}
	
	protected void onNumberOutOfBounds() throws Exception {
		callFunction("onNumberOutOfBounds");
	}

	protected void onNotify(int notifier) throws Exception {
		super.onNotify(notifier);
		switch (notifier) {
			case Notifier.NOTIFIER_NUMBER_OUT_OF_BOUNDS:
				onNumberOutOfBounds();
				break;
			case Notifier.NOTIFIER_USER_CHANGED:
				onChange();
				break;
			case Notifier.NOTIFIER_INTERMEDIATE_CHANGED:
				onPreChange();
				break;
		}
	}

	/* 
	 * item value accessors
	 * 
	 */

	public native float[] getRange();
	public native void setRange(float minValue, float maxValue);
	
	public void setRange(float[] range) {
		setRange(range[0], range[1]);
	}

	// TODO: check wether precision belongs to TextEdit?
	
	public native int getPrecision();
	public native void setPrecision(int precision);
	
	public native float[] getIncrements();
	public native void setIncrements(float small, float large);
	
	public void setIncrements(float[] increments) {
		setIncrements(increments[0], increments[1]);
	}
	
	public native float getValue();
	public native void setValue(float value);
}
