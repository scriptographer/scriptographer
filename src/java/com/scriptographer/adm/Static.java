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
 * $Revision: 1.1 $
 * $Date: 2005/02/23 22:00:59 $
 */

package com.scriptographer.adm;

import java.awt.geom.Rectangle2D;
import java.io.IOException;

public class Static extends PictureItem {

	//  Text static styles
	public final static int
		STYLE_CLIPPEDTEXT = (1 << 0),
		STYLE_DISABLE_AUTO_ACTIVATE_TEXT = (1 << 1),
		STYLE_TRUNCATE_ENDTEXT = (1 << 2),    // clipped style has priority
		STYLE_TRUNCATE_MIDDLETEXT = (1 << 3), // truncate end has priority
		
		// a fake ADMStyle that tells the constructor to construct a MULTILINE static item.
		STYLE_MULTILINE = (1 << 16);

	public Static(Dialog dialog, Rectangle2D bounds, String text, int style) {
		super(dialog,
			(style & STYLE_MULTILINE) != 0 ? Item.TYPE_TEXT_STATIC_MULTILINE
				: Item.TYPE_TEXT_STATIC, bounds, text, style & ~STYLE_MULTILINE);
	}
	
	public Static(Dialog dialog, Rectangle2D bounds, Image image) {
		super(dialog, Item.TYPE_PICTURE_STATIC, bounds, null, 0);
		try {
			setPicture(image);
		} catch (IOException e) {
			// will never happen with type Image
		}
	}
}
