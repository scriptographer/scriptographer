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
 * File created on 18.10.2005.
 */

package com.scriptographer.widget;

/**
 * @author lehni
 * 
 * @jshide
 */
public abstract class TextItem extends Item {

	protected TextItem(Dialog dialog, int handle, boolean isChild) {
		super(dialog, handle, isChild);
	}

	protected TextItem(Dialog dialog, ItemType type) {
		super(dialog, type);
	}

	/*
	 * item text accessors
	 * 
	 */

	private String text = "";

	private native void nativeSetText(String text);

	public void setText(String text) {
		this.text = text;
		// Text item often use space for centering text on bigger
		// buttons, etc. Since the native elements center correctly
		// trim the space here, but store it in the text field,
		// so getBestSize takes it into account.
		nativeSetText(text != null ? text.trim() : text);
	}

	public String getText() {
		return text;
	}
}
