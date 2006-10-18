/*
 * Scriptographer
 * 
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 * 
 * Copyright (c) 2002-2006 Juerg Lehni, http://www.scratchdisk.com.
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
 * File created on 23.10.2005.
 * 
 * $RCSfile: AreaText.java,v $
 * $Author: lehni $
 * $Revision: 1.5 $
 * $Date: 2006/10/18 14:17:42 $
 */

package com.scriptographer.ai;

public class AreaText extends TextFrame {

	protected AreaText(int handle) {
		super(handle);
	}
	
	native private static int nativeCreate(int orient, int artHandle);
	
	/**
	 * Creates an area text object
	 */

	public AreaText(Path area, int orient) {
		this(nativeCreate(orient, area != null ? area.handle : 0));
	}

	public AreaText(Path area) {
		this(area, ORIENTATION_HORIZONTAL);
	}

	public Path getTextPath() {
		return (Path) getFirstChild();
	}
	
	public native int getRowCount();
	public native void setRowCount(int count);
	
	public native int getColumnCount();
	public native void setColumnCount(int count);

	public native boolean getRowMajorOrder();
	public native void setRowMajorOrder(boolean isRowMajor);
	
	public native float getRowGutter();
	public native void setRowGutter(float gutter);

	public native float getColumnGutter();
	public native void setColumnGutter(float gutter);
}
