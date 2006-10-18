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
 * File created on Oct 18, 2006.
 * 
 * $RCSfile: Gradient.java,v $
 * $Author: lehni $
 * $Revision: 1.1 $
 * $Date: 2006/10/18 14:10:26 $
 */

package com.scriptographer.ai;

public class Gradient extends AIWrapper {

	protected Gradient(int handle) {
		super(handle);
	}
	
	public final static short
		TYPE_LINEAR = 0,
		TYPE_RADIAL = 1;

	private static native int nativeCreate();
	
	public Gradient() {
		super(nativeCreate());
	}
	
	protected static Gradient wrapHandle(int handle, Document document) {
		return (Gradient) wrapHandle(Gradient.class, handle, document, true);
	}
	
	public native String getName();
	
	public native void setName(String name);
	
	public native short getType();
	
	public native void setType(short type);

	public native boolean isValid();
	
	protected native boolean nativeRemove();
	
	public boolean remove() {
		// make it public
		return super.remove();
	}
}
