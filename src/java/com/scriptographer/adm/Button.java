/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2006 Juerg Lehni, http://www.scratchdisk.com.
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
 * $RCSfile: Button.java,v $
 * $Author: lehni $
 * $Revision: 1.5 $
 * $Date: 2006/10/18 14:08:29 $
 */

package com.scriptographer.adm;

import java.awt.Insets;
import java.io.IOException;

import com.scriptographer.ScriptographerEngine;

/**
 * A Button is by default text based.
 * Only if it is created with an image passed to the constructor,
 * It is picture based.
 * Picture based items (CheckBox, Static, Button, RadioButton),
 * this policy has been chosen to avoid 4 more classes.
 */
public class Button extends TextItem {

	protected Button(Dialog dialog, int type) {
		super(dialog, type);
	}

	protected Button(Dialog dialog, long itemHandle) {
		super(dialog, itemHandle);
	}
	
	public Button(Dialog dialog) {
		super(dialog, TYPE_TEXT_PUSHBUTTON);
	}

	/*
	 * Callback functions
	 */
	
	protected void onClick() throws Exception {
		callFunction("onClick");
	}
	
	protected void onNotify(int notifier) throws Exception {
		super.onNotify(notifier);
		switch (notifier) {
			case Notifier.NOTIFIER_USER_CHANGED:
				onClick();
				break;
		}
	}
	
	/**
	 * Picture stuff
	 */

	private Image image = null;
	private Image rolloverImage = null;
	private Image selectedImage = null;
	private Image disabledImage = null;
	
	/* 
	 * picture ID accessors
	 * 
	 */

	private native void nativeSetImage(int iconHandle);
	private native void nativeSetRolloverImage(int iconHandle);
	private native void nativeSetSelectedImage(int iconHandle);
	private native void nativeSetDisabledImage(int iconHandle);
	
	/*
	 * These are all protected so they can be made public in the Image* subclasses
	 * This is the only way to share this code among ImageCheckBox, ImageButton and ImageRadioButton
	 */

	protected Image getImage() {
		return image;
	}
	
	protected void setImage(Object obj) throws IOException {
		image = Image.getImage(obj);
		nativeSetImage(image != null ? image.createIconHandle() : 0);
	}
	
	protected Image getRolloverImage() {
		return rolloverImage;
	}
	
	protected void setRolloverImage(Object obj) throws IOException {
		rolloverImage = Image.getImage(obj);
		nativeSetRolloverImage(rolloverImage != null ? rolloverImage.createIconHandle() : 0);
	}
	
	protected Image getSelectedImage() {
		return selectedImage;
	}
	
	protected void setSelectedImage(Object obj) throws IOException {
		selectedImage = Image.getImage(obj);
		nativeSetSelectedImage(selectedImage != null ? selectedImage.createIconHandle() : 0);
	}

	protected Image getDisabledImage() {
		return disabledImage;
	}

	protected void setDisabledImage(Object obj) throws IOException {
		disabledImage = Image.getImage(obj);
		nativeSetDisabledImage(disabledImage != null ? disabledImage.createIconHandle() : 0);
	}
	
	// int top, int left, int bottom, int right
	protected static final Insets INSETS_IMAGE = new Insets(0, 0, 0, 0);
	protected static final Insets INSETS_TEXT = ScriptographerEngine.isMacintosh() ? new Insets(1, 2, 3, 3) : INSETS_IMAGE;
	
	protected Insets getButtonInsets() {
		return INSETS_TEXT;
	}

	public void setInsets(int left, int top, int right, int bottom) {
		Insets in = getButtonInsets();
		super.setInsets(left + in.left, top + in.top, right + in.right, bottom + in.bottom);
	}
	
	public Insets getInsets() {
		Insets in = getButtonInsets();
		return new Insets(insets.top - in.top, insets.left - in.left, insets.bottom - in.bottom, insets.right - in.right);
	}
}
