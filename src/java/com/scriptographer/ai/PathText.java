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
 * $RCSfile: PathText.java,v $
 * $Author: lehni $
 * $Revision: 1.6 $
 * $Date: 2006/10/25 02:12:51 $
 */

package com.scriptographer.ai;

public class PathText extends TextFrame {

	protected PathText(int handle) {
		super(handle);
	}

	native private static int nativeCreate(short orient, int artHandle);
	// TODO: not used?
	native private static int nativeCreate(short orient, int artHandle, float x, float y);
	
	/**
	 * Creates a path text object
	 */

	public PathText(Path path, short orient) {
		this(nativeCreate(orient, path != null ? path.handle : 0));
		// TODO: check what exactly do startT endT vs start anchor!
	}

	public PathText(Path path) {
		this(path, ORIENTATION_HORIZONTAL);
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
