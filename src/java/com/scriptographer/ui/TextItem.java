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
 * File created on 18.10.2005.
 *
 * $Id:TextItem.java 402 2007-08-22 23:24:49Z lehni $
 */

package com.scriptographer.ui;

/**
 * @author lehni
 */
public abstract class TextItem extends Item {

	protected TextItem(Dialog dialog, int handle) {
		super(dialog, handle);
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
		nativeSetText(text.trim());
	}

	public String getText() {
		return text;
	}
}
