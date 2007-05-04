/*
 * Scriptographer
 * 
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 * 
 * Copyright (c) 2002-2007 Juerg Lehni, http://www.scratchdisk.com.
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
 * $Id$
 */

package com.scriptographer.ai;

/**
 * @author lehni
 */
public class AreaText extends TextFrame {

	protected AreaText(int handle) {
		super(handle);
	}
	
	native private static int nativeCreate(short orient, int artHandle);
	
	/**
	 * Creates an area text object
	 * 
	 * @param area the path in which the text will appear
	 * @param orient the text orientation, TextFrame.ORIENTATION_*
	 */
	public AreaText(Path area, short orient) {
		this(nativeCreate(orient, area != null ? area.handle : 0));
	}

	public AreaText(Path area) {
		this(area, ORIENTATION_HORIZONTAL);
	}

	/**
	 * Returns the path of the AreaText
     */
	public Path getTextPath() {
		return (Path) getFirstChild();
	}
	
	/**
	 * @jsbean A number value that specifies the number of rows for the text frame.
	 */
	public native int getRowCount();
	public native void setRowCount(int count);
	
	/**
	 * @jsbean A number value that specifies the number of columns for the text
	 * @jsbean frame.
	 */
	public native int getColumnCount();
	public native void setColumnCount(int count);

	/**
	 * @jsbean A boolean value that specifies wether the text area uses
	 * @jsbean row major order.
	 */
	public native boolean getRowMajorOrder();
	public native void setRowMajorOrder(boolean isRowMajor);
	
	/**
	 * A number value that specifies the row gutter in the text frame.
	 */
	public native float getRowGutter();
	public native void setRowGutter(float gutter);

	/**
	 * A number value that specifies the column gutter in the text frame.
	 */	
	public native float getColumnGutter();
	public native void setColumnGutter(float gutter);
}
