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
 * $RCSfile: Static.java,v $
 * $Author: lehni $
 * $Revision: 1.2 $
 * $Date: 2005/03/25 00:27:57 $
 */

package com.scriptographer.adm;

/**
 * A Static is by default text based.
 * Only if it is created with an image passed to the constructor,
 * It is picture based.
 * Picture based items (CheckBox, Static, PushButton, RadioButton),
 * this policy has been chosen to avoid 4 more classes.
 */
public class Static extends PictureItem {

	//  Text static styles
	public final static int
		STYLE_CLIPPEDTEXT 					= 1 << 0,
		STYLE_DISABLE_AUTO_ACTIVATE_TEXT 	= 1 << 1,
		STYLE_TRUNCATE_ENDTEXT 				= 1 << 2,    // clipped style has priority
		STYLE_TRUNCATE_MIDDLETEXT 			= 1 << 3; // truncate end has priority

	public final static int
		// a fake option that tells the constructor to construct a MULTILINE static item.
		OPTION_MULTILINE = 1 << 1;

	/**
	 * Creates a text based Static item.
	 * @param dialog
	 * @param options
	 */
	public Static(Dialog dialog, int options) {
		super(dialog,
			(options & OPTION_MULTILINE) != 0 ? Item.TYPE_TEXT_STATIC_MULTILINE
				: Item.TYPE_TEXT_STATIC);
	}
	
	public Static(Dialog dialog) {
		this(dialog, OPTION_NONE);
	}
	
	/**
	 * Creates a picture based Static Item.
	 * 
	 * @param dialog
	 * @param picture
	 */
	public Static(Dialog dialog, Image picture) {
		super(dialog, Item.TYPE_PICTURE_STATIC);
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
}
