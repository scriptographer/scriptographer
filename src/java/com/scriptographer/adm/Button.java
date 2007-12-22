/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2007 Juerg Lehni, http://www.scratchdisk.com.
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
 * $Id:Button.java 402 2007-08-22 23:24:49Z lehni $
 */

package com.scriptographer.adm;

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

	protected Button(Dialog dialog, int type) {
		super(dialog, type);
	}

	protected Button(Dialog dialog, long handle) {
		super(dialog, handle);
	}
	
	public Button(Dialog dialog) {
		super(dialog, TYPE_TEXT_PUSHBUTTON);
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

	protected void onClick() throws Exception {
		if (onClick != null)
			ScriptographerEngine.invoke(onClick, this);
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
	
	// int top, int left, int bottom, int right
	protected static final Margins INSETS_IMAGE = new Margins(0, 0, 0, 0);
	protected static final Margins INSETS_TEXT = ScriptographerEngine.isMacintosh() ?
			new Margins(4, 3, 4, 3) : new Margins(0, 0, 0, 0);

	protected Margins getButtonMargins() {
		return INSETS_TEXT;
	}

	public void setMargins(int left, int top, int right, int bottom) {
		Margins m = getButtonMargins();
		super.setMargins(left + m.left, top + m.top,
				right + m.right, bottom + m.bottom);
	}
	
	public Margins getMargins() {
		Margins m = getButtonMargins();
		return new Margins(margins.left - m.left, margins.top - m.top, 
				margins.right - m.right, margins.bottom - m.bottom);
	}
}
