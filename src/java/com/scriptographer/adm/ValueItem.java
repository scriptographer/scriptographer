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

import com.scriptographer.ScriptographerEngine;
import com.scratchdisk.script.Callable;

/**
 * @author lehni
 * 
 * @jshide
 */
public abstract class ValueItem extends Item {

	protected ValueItem(Dialog dialog, int handle, boolean isChild) {
		super(dialog, handle, isChild);
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

	protected void onPreChange() {
		if (onPreChange != null)
			ScriptographerEngine.invoke(onPreChange, this);
	}

	private Callable onChange = null;

	protected void onChange() {
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

	protected void onNumberOutOfBounds() {
		if (onNumberOutOfBounds != null)
			ScriptographerEngine.invoke(onNumberOutOfBounds, this);
	}

	protected void onNotify(Notifier notifier) {
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
	 */

	public native float[] getRange();

	/**
	 * @jshide
	 */
	public native void setRange(float minValue, float maxValue);

	public void setRange(float[] range) {
		setRange(range[0], range[1]);
	}

	public native float[] getIncrements();

	/**
	 * @jshide
	 */
	public native void setIncrements(float small, float large);

	public void setIncrements(float[] increments) {
		setIncrements(increments[0], increments[1]);
	}

	public void setIncrements(float increments) {
		setIncrements(increments, increments * 10);
	}

	public native float getValue();

	public native void setValue(float value);
}
