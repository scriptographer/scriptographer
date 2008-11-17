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
 * File created on 20.10.2005.
 * 
 * $Id:ImageButton.java 402 2007-08-22 23:24:49Z lehni $
 */

package com.scriptographer.ui;

import java.io.IOException;

import com.scratchdisk.util.IntegerEnumUtils;

/**
 * @author lehni
 */
public class ImageButton extends Button {
	
	public ImageButton(Dialog dialog) {
		super(dialog, ItemType.PICTURE_PUSHBUTTON);
	}

	public ImageButtonStyle getStyle() {
		return IntegerEnumUtils.get(ImageButtonStyle.class, nativeGetStyle());
	}

	public void setStyle(ImageButtonStyle style) {
		if (style != null)
			nativeSetStyle(style.value);
	}
	
	public Image getImage() {
		return super.getImage();
	}

	public void setImage(Image image) {
		super.setImage(image);
	}

	public void setImage(Object obj) throws IOException {
		super.setImage(obj);
	}

	public Image getRolloverImage() {
		return super.getRolloverImage();
	}

	public void setRolloverImage(Image image) {
		super.setRolloverImage(image);
	}

	public void setRolloverImage(Object obj) throws IOException {
		super.setRolloverImage(obj);
	}

	public Image getSelectedImage() {
		return super.getSelectedImage();
	}

	public void setSelectedImage(Image image) {
		super.setSelectedImage(image);
	}

	public void setSelectedImage(Object obj) throws IOException {
		super.setSelectedImage(obj);
	}

	public Image getDisabledImage() {
		return super.getDisabledImage();
	}

	public void setDisabledImage(Image image) {
		super.setDisabledImage(image);
	}

	public void setDisabledImage(Object obj) throws IOException {
		super.setDisabledImage(obj);
	}

	protected Border getButtonMargin() {
		return MARGIN_IMAGE;
	}
}
