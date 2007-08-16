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
 * @author lehni
 */
public class Gradient extends NativeWrapper {
	
	public final static short
		TYPE_LINEAR = 0,
		TYPE_RADIAL = 1;

	GradientStopList stops = null;

	/*
	 * Needed by wrapHandle mechanism
	 */
	protected Gradient(int handle) {
		super(handle, true);
	}

	private static native int nativeCreate();

	public Gradient() {
		super(nativeCreate(), true);
	}
	
	protected static Gradient wrapHandle(int handle, Document document) {
		return (Gradient) wrapHandle(Gradient.class, handle, document, true);
	}
	
	public GradientStopList getStops() {
		if (stops == null)
			stops = new GradientStopList(this);
		else
			stops.update();
		return stops;
	}
	
	public native String getName();
	
	public native void setName(String name);
	
	/**
	 * 
	 * @return #TYPE_*
	 */
	public native short getType();
	
	/**
	 * @param type #TYPE_*
	 */
	public native void setType(short type);

	public native boolean isValid();
	
	protected native boolean nativeRemove();
	
	public boolean remove() {
		// make super.remove() public
		return super.remove();
	}
}
