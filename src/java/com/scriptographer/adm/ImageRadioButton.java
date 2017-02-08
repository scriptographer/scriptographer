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
 * File created on 20.10.2005.
 */

package com.scriptographer.adm;

import java.io.IOException;
import com.scriptographer.ui.Border;

/**
 * @author lehni
 */
public class ImageRadioButton extends RadioButton {

	public ImageRadioButton(Dialog dialog) {
		super(dialog, ItemType.PICTURE_RADIOBUTTON);
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

	protected Border getNativeMargin() {
		return MARGIN_NONE;
	}
}
