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
 * File created on 08.12.2004.
 *
 * $RCSfile: Raster.java,v $
 * $Author: lehni $
 * $Revision: 1.2 $
 * $Date: 2005/03/25 00:27:57 $
 */

package com.scriptographer.ai;

import com.scriptographer.util.Handle;

public class Raster extends Art {

	// native pointer to an attached data struct:
	private int rasterData = 0;

	public Raster(Handle handle) {
		super(handle);
	}

	/**
	 * Creates a raster object
	 */
	public Raster() {
		super(TYPE_RASTER);
	}

	// TODO: unfinished: implement setWidth, setHeight, get/setType, setPixel, ...
	public native int getWidth();
	public native void setWidth(int width);
	public native int getHeight();
	public native void setHeight(int height);
	public native int getType();
	public native void setType(int type);

	public native Color getPixel(int x, int y);
	public native void setPixel(int x, int y, Color color);

	native protected void finalize();
}
