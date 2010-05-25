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
 *
 * $Id:Button.java 402 2007-08-22 23:24:49Z lehni $
 */

package com.scriptographer.ui;

import java.io.IOException;

import com.scratchdisk.script.Callable;
import com.scriptographer.ScriptographerEngine; 

/**
 * A Button is by default text based.
 * Only if it is created with an image passed to the constructor,
 * It is picture based.
 * Picture based items (CheckBox, Static, Button, RadioButton),
 * this policy has been chosen to avoid 4 more classes.
 * 
 * @author lehni
 */
public class Button extends TextItem {

	protected Button(Dialog dialog, ItemType type) {
		super(dialog, type);
	}

	protected Button(Dialog dialog, int handle, boolean isChild) {
		super(dialog, handle, isChild);
	}

	public Button(Dialog dialog) {
		super(dialog, ItemType.TEXT_PUSHBUTTON);
	}

	/*
	 * Callback functions
	 */

	private Callable onClick = null;

	public Callable getOnClick() {
		return onClick;
	}

	public void setOnClick(Callable onClick) {
		this.onClick = onClick;
	}

	protected void onClick() {
		if (onClick != null)
			ScriptographerEngine.invoke(onClick, this);
	}

	protected void onNotify(Notifier notifier) {
		super.onNotify(notifier);
		switch (notifier) {
			case USER_CHANGED:
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
	 * These are all protected so they can be made public in the Image*
	 * subclasses This is the only way to share this code among ImageCheckBox,
	 * ImageButton and ImageRadioButton
	 */
	protected Image getImage() {
		return image;
	}

	protected void setImage(Image image) {
		nativeSetImage(image != null ? image.createIconHandle() : 0);
		this.image = image;
	}

	protected void setImage(Object obj) throws IOException {
		setImage(Image.getImage(obj));
	}
	
	protected Image getRolloverImage() {
		return rolloverImage;
	}

	protected void setRolloverImage(Image image) {
		nativeSetRolloverImage(image != null ? image.createIconHandle() : 0);
		this.rolloverImage = image;
	}

	protected void setRolloverImage(Object obj) throws IOException {
		setRolloverImage(Image.getImage(obj));
	}
	
	protected Image getSelectedImage() {
		return selectedImage;
	}

	protected void setSelectedImage(Image image) {
		nativeSetSelectedImage(image != null ? image.createIconHandle() : 0);
		this.selectedImage = image;
	}
	
	protected void setSelectedImage(Object obj) throws IOException {
		setSelectedImage(Image.getImage(obj));
	}

	protected Image getDisabledImage() {
		return disabledImage;
	}

	protected void setDisabledImage(Image image) {
		nativeSetDisabledImage(image != null ? image.createIconHandle() : 0);
		this.disabledImage = image;
	}

	protected void setDisabledImage(Object obj) throws IOException {
		setDisabledImage(Image.getImage(obj));
	}

	protected static final Border MARGIN_BUTTON = ScriptographerEngine.isMacintosh() ?
			new Border(4, 4, 4, 4) : new Border(1, 0, 1, 0);

	protected Border getNativeMargin() {
		return MARGIN_BUTTON;
	}
}
