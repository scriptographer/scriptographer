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
 * $Revision: 1.1 $
 * $Date: 2005/02/23 22:00:59 $
 */

package com.scriptographer.adm;

import java.awt.geom.Rectangle2D;
import java.io.IOException;

/**
 * This should actually be called PictureOrTextItem, as it is used for both and derived
 * from TextItem. like this, PushButton, CheckBox, RadioButton can derive PictureItem
 * and handle both cases... Not very elegant, but better than having even more classes
 * (PictureRadioButton, ...). or maybe not?
 * 
 * @author Lehni
 */
public abstract class PictureItem extends TextItem {
	private Image picture = null;
	private Image rolloverPicture = null;
	private Image selectedPicture = null;
	private Image disabledPicture = null;
	// unfortunatelly PictureItem is also used for text buttons, checkboxes, ...
	// these call the constructor with the text param set, and won't allow
	// setting of images...
	private boolean isPictureType;

	protected PictureItem(Dialog dialog, String type, Rectangle2D bounds, String text, int style) {
		super(dialog, type, bounds, text, style, 0);
		isPictureType = text == null;
	}
	
	/* 
	 * picture ID accessors
	 * 
	 */

	private native void nativeSetPicture(int iconRef);
	private native void nativeSetRolloverPicture(int iconRef);
	private native void nativeSetSelectedPicture(int iconRef);
	private native void nativeSetDisabledPicture(int iconRef);

	public Image getPicture() {
		return picture;
	}
	
	public void setPicture(Object obj) throws IOException {
		if (isPictureType) {
			picture = Image.getImage(obj);
			nativeSetPicture(picture != null ? picture.createIconRef() : 0);
		}
	}
	
	public Image getRolloverPicture() {
		return rolloverPicture;
	}
	
	public void setRolloverPicture(Object obj) throws IOException {
		if (isPictureType) {
			rolloverPicture = Image.getImage(obj);
			nativeSetRolloverPicture(rolloverPicture != null ? rolloverPicture.createIconRef() : 0);
		}
	}
	
	public Image getSelectedPicture() {
		return selectedPicture;
	}
	
	public void setSelectedPicture(Object obj) throws IOException {
		if (isPictureType) {
			selectedPicture = Image.getImage(obj);
			nativeSetSelectedPicture(selectedPicture != null ? selectedPicture.createIconRef() : 0);
		}
	}

	public Image getDisabledPicture() {
		return disabledPicture;
	}

	public void setDisabledPicturesetDisabledPicture(Object obj) throws IOException {
		if (isPictureType) {
			disabledPicture = Image.getImage(obj);
			nativeSetDisabledPicture(disabledPicture != null ? disabledPicture.createIconRef() : 0);
		}
	}
}
