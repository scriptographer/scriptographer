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
 * $RCSfile: PictureItem.java,v $
 * $Author: lehni $
 * $Revision: 1.3 $
 * $Date: 2005/03/25 00:27:57 $
 */

package com.scriptographer.adm;

import java.io.IOException;

/**
 * This should actually be called PictureOrTextItem, as it is used for both and derived
 * from TextItem. like this, PushButton, CheckBox, RadioButton can derive PictureItem
 * and handle both cases...
 * PictureItems are used for text items as well, because of the nature
 * of subclassing for ADM Items: PictureCheckBox is subclassed from
 * CheckBox, but CheckBox does not allwo pictures.
 * So only set allowPictures to true in classes that can have pictures
 * This is not so elegant, but an easy solution to the problem and
 * not really visible from the outside (just don't count on the fact
 * that "instanceof PictureItem" actually means the item displays a picture 
 */
public abstract class PictureItem extends TextItem {
	private Image picture = null;
	private Image rolloverPicture = null;
	private Image selectedPicture = null;
	private Image disabledPicture = null;
	
	protected boolean hasPictures = false;
	// unfortunatelly PictureItem is also used for text buttons, checkboxes, ...
	// these call the constructor with the text param set, and won't allow
	// setting of images...

	protected PictureItem(Dialog dialog, int type) {
		super(dialog, type, OPTION_NONE);
	}
	
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
