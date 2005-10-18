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
 * $RCSfile: Button.java,v $
 * $Author: lehni $
 * $Revision: 1.3 $
 * $Date: 2005/10/18 15:29:06 $
 */

package com.scriptographer.adm;

import java.io.IOException;

/**
 * A Button is by default text based.
 * Only if it is created with an image passed to the constructor,
 * It is picture based.
 * Picture based items (CheckBox, Static, Button, RadioButton),
 * this policy has been chosen to avoid 4 more classes.
 */
public class Button extends TextItem {

	// ADMPictureButtonStyle
	public final static int
		STYLE_BLACK_SUNKEN_RECT = 0,
		STYLE_BLACK_RECT = 1;

	protected boolean hasPictures = false;

	protected Button(Dialog dialog, int type) {
		super(dialog, type);
	}
	
	public Button(Dialog dialog) {
		super(dialog, Item.TYPE_TEXT_PUSHBUTTON);
	}
	
	public Button(Dialog dialog, Image picture) {
		super(dialog, Item.TYPE_PICTURE_PUSHBUTTON);
		hasPictures = true;
		if (picture != null) {
			try {
				setPicture(picture);
			} catch(Exception e) {
				// OperationNotSupportedException cannot happen because of allowPictures above
				// IOException cannot happen with parameter of class Image
			}
		}
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

	private Image picture = null;
	private Image rolloverPicture = null;
	private Image selectedPicture = null;
	private Image disabledPicture = null;
	
	/* 
	 * picture ID accessors
	 * 
	 */
	
	public boolean hasPictures() {
		return hasPictures;
	}

	private native void nativeSetPicture(int iconHandle);
	private native void nativeSetRolloverPicture(int iconHandle);
	private native void nativeSetSelectedPicture(int iconHandle);
	private native void nativeSetDisabledPicture(int iconHandle);
	
	class PicutreNotAllowedException extends RuntimeException {
		PicutreNotAllowedException() {
			super("Text based items cannot display pictures.");
		}
	}

	public Image getPicture() {
		if (!hasPictures)
			throw new PicutreNotAllowedException();
		return picture;
	}
	
	public void setPicture(Object obj) throws IOException {
		if (!hasPictures)
			throw new PicutreNotAllowedException();
		picture = Image.getImage(obj);
		nativeSetPicture(picture != null ? picture.createIconHandle() : 0);
	}
	
	public Image getRolloverPicture() {
		if (!hasPictures)
			throw new PicutreNotAllowedException();
		return rolloverPicture;
	}
	
	public void setRolloverPicture(Object obj) throws IOException {
		if (!hasPictures)
			throw new PicutreNotAllowedException();
		rolloverPicture = Image.getImage(obj);
		nativeSetRolloverPicture(rolloverPicture != null ? rolloverPicture.createIconHandle() : 0);
	}
	
	public Image getSelectedPicture() {
		if (!hasPictures)
			throw new PicutreNotAllowedException();
		return selectedPicture;
	}
	
	public void setSelectedPicture(Object obj) throws IOException {
		if (!hasPictures)
			throw new PicutreNotAllowedException();
		selectedPicture = Image.getImage(obj);
		nativeSetSelectedPicture(selectedPicture != null ? selectedPicture.createIconHandle() : 0);
	}

	public Image getDisabledPicture() {
		if (!hasPictures)
			throw new PicutreNotAllowedException();
		return disabledPicture;
	}

	public void setDisabledPicturesetDisabledPicture(Object obj) throws IOException {
		if (!hasPictures)
			throw new PicutreNotAllowedException();
		disabledPicture = Image.getImage(obj);
		nativeSetDisabledPicture(disabledPicture != null ? disabledPicture.createIconHandle() : 0);
	}
}
