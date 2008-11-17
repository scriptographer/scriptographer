/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2008 Juerg Lehni, http://www.scratchdisk.com.
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
 * $Id$
 */

package com.scriptographer.ui;

import com.scriptographer.ScriptographerEngine; 
import com.scratchdisk.script.Callable;

/**
 * @author lehni
 */
public abstract class ValueItem extends Item {

	protected ValueItem(Dialog dialog, int handle) {
		super(dialog, handle);
	}

	protected ValueItem(Dialog dialog, ItemType type, int options) {
		super(dialog, type, options);
	}
	
	protected ValueItem(Dialog dialog, ItemType type) {
		super(dialog, type, 0);
	}
	/*
	 * Callback functions
	 */
	
	private Callable onPreChange = null;

	public Callable getOnPreChange() {
		return onPreChange;
	}

	public void setOnPreChange(Callable onPreChange) {
		this.onPreChange = onPreChange;
	}
	
	protected void onPreChange() throws Exception {
		if (onPreChange != null)
			ScriptographerEngine.invoke(onPreChange, this);
	}
	
	private Callable onChange = null;
	
	protected void onChange() throws Exception {
		if (onChange != null)
			ScriptographerEngine.invoke(onChange, this);
	}

	public Callable getOnChange() {
		return onChange;
	}

	public void setOnChange(Callable onChange) {
		this.onChange = onChange;
	}
	
	private Callable onNumberOutOfBounds = null;

	public Callable getOnNumberOutOfBounds() {
		return onNumberOutOfBounds;
	}

	public void setOnNumberOutOfBounds(Callable onNumberOutOfBounds) {
		this.onNumberOutOfBounds = onNumberOutOfBounds;
	}
	
	protected void onNumberOutOfBounds() throws Exception {
		if (onNumberOutOfBounds != null)
			ScriptographerEngine.invoke(onNumberOutOfBounds, this);
	}

	protected void onNotify(Notifier notifier) throws Exception {
		super.onNotify(notifier);
		switch (notifier) {
			case NUMBER_OUT_OF_BOUNDS:
				onNumberOutOfBounds();
				break;
			case USER_CHANGED:
				onChange();
				break;
			case INTERMEDIATE_CHANGED:
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
	
	public native float[] getIncrements();
	public native void setIncrements(float small, float large);
	
	public void setIncrements(float[] increments) {
		setIncrements(increments[0], increments[1]);
	}
	
	public native float getValue();
	public native void setValue(float value);
}
