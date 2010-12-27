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

/**
 * @author lehni
 */
public class ImagePane extends Item {

	/**
	 * Creates a ImageStatic Item.
	 * 
	 * @param dialog
	 * @param image
	 */
	public ImagePane(Dialog dialog) {
		super(dialog, ItemType.PICTURE_STATIC);
	}

	private Image image = null;
	
	private native void nativeSetImage(int iconHandle);

	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		nativeSetImage(image != null ? image.createIconHandle() : 0);
		this.image = image;
	}
	
	public void setImage(Object obj) throws IOException {
		setImage(Image.getImage(obj));
	}
}
