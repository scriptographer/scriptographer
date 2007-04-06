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
 * File created on Oct 18, 2006.
 * 
 * $Id$
 */

package com.scriptographer.ai;

/**
 * A Swatch represents a named color, tint, gradient or pattern contained within
 * an Illustrator document.
 * 
 * @author lehni
 */
public class Swatch extends AIWrapper {
	
	protected Swatch(int handle) {
		super(handle);
	}
	
	private static native int nativeCreate();
	
	public Swatch() {
		super(nativeCreate());
	}
	
	protected static Swatch wrapHandle(int handle, Document document) {
		return (Swatch) wrapHandle(Swatch.class, handle, document, true);
	}
	
	public native String getName();
	
	public native void setName(String name);

	public native Color getColor();
	
	public native void setColor(Color color);
	
	protected native boolean nativeRemove();
	
	/**
	 * Removes the swatch
	 */
	public boolean remove() {
		// make it public
		return super.remove();
	}
}
