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
 * File created on 23.10.2005.
 * 
 * $Id$
 */

package com.scriptographer.ai;

/**
 * @author lehni
 */
public class PathText extends TextFrame {

	protected PathText(int handle) {
		super(handle);
	}

	native private static int nativeCreate(int orientation, int artHandle);

	// TODO: not used?
	native private static int nativeCreate(int orientation, int artHandle,
			float x, float y);
	
	/**
	 * Creates a path text item
	 * 
	 * @param path the path that the text will run along
	 * @param orient the text orientation
	 */
	public PathText(Path path, TextOrientation orientation) {
		this(nativeCreate(orientation != null
				? orientation.value : TextOrientation.HORIZONTAL.value,
				path != null ? path.handle : 0));
		// TODO: check what exactly do startT endT vs start anchor!
	}

	public PathText(Path path) {
		this(path, TextOrientation.HORIZONTAL);
	}

	public Path getTextPath() {
		return (Path) getFirstChild();
	}
	
	public native float[] getPathRange();
	
	public native void setPathRange(float start, float end);
	
	public void setPathRange(float[] range) {
		setPathRange(range[0], range[1]);
	}
}
