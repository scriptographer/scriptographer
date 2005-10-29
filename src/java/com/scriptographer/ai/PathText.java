/*
 * Scriptographer
 * 
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 * 
 * Copyright (c) 2004-2005 Juerg Lehni, http://www.scratchdisk.com.
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
 * $Revision: 1.1 $
 * $Date: 2005/10/29 10:18:38 $
 */

package com.scriptographer.ai;

public class PathText extends Text {

	protected PathText(long handle) {
		super(handle);
	}
	
	/**
	 * Creates a path text object
	 */

	native private static int nativeCreate(int docHandle, int orient, int artHandle);
	native private static int nativeCreate(int docHandle, int orient, int artHandle, float x, float y);

	public PathText(Document document, Path path, int orient) {
		this(nativeCreate(document != null ? document.handle : 0, orient, path != null ? path.handle : 0));
		// TODO: check what exactly do startT endT vs start anchor!
	}

	public PathText(Document document, Path path) {
		this(document, path, ORIENTATION_HORIZONTAL);
	}

	public PathText(Path path, int orient) {
		this(null, path, orient);
	}

	public PathText(Path path) {
		this(null, path, ORIENTATION_HORIZONTAL);
	}

	public Path getTextPath() {
		return (Path) getFirstChild();
	}
	
	public native float[] getPathRange();
	public native void setPathRange(float start, float end);
}
