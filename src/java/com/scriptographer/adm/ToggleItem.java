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

/**
 * @author lehni
 * 
 * @jshide
 */
public abstract class ToggleItem extends Button {
	protected ToggleItem(Dialog dialog, ItemType type) {
		super(dialog, type);
	}

	protected Border getNativeMargin() {
		return MARGIN_NONE;
	}

	/* 
	 * item value accessors
	 * 
	 */
	
	public native boolean isChecked();
	public native void setChecked(boolean value);
}
