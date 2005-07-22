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
 * $Revision: 1.2 $
 * $Date: 2005/07/22 17:39:22 $
 */

package com.scriptographer.adm;

/**
 * A Button is by default text based.
 * Only if it is created with an image passed to the constructor,
 * It is picture based.
 * Picture based items (CheckBox, Static, Button, RadioButton),
 * this policy has been chosen to avoid 4 more classes.
 */
public class Button extends PictureItem {

	// ADMPictureButtonStyle
	public final static int
		STYLE_BLACK_SUNKEN_RECT = 0,
		STYLE_BLACK_RECT = 1;

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
	
	protected void onClick() throws Exception {
		callFunction("onClick");
	}
	
	/**
	 * Redirect onChange to onClick for buttons, as onChange is
	 * not a suitable name for button click handlers
	 */
	protected void onChange() throws Exception {
		onClick();
	}
}
