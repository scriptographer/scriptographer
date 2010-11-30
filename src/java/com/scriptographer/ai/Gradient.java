/*
 * Scriptographer
 * 
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 * 
 * Copyright (c) 2002-2010 Juerg Lehni, http://www.scratchdisk.com.
 * All rights reserved.
 *
 * Please visit http://scriptographer.org/ for updates and contact.
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

import com.scratchdisk.list.Lists;
import com.scratchdisk.list.ReadOnlyList;
import com.scratchdisk.util.IntegerEnumUtils;

/**
 * @author lehni
 */
public class Gradient extends DocumentObject {

	GradientStopList stops = null;

	/*
	 * Needed by wrapHandle mechanism in NativeObject
	 */
	protected Gradient(int handle, Document document) {
		super(handle, document);
	}

	private static native int nativeCreate();

	public Gradient() {
		super(nativeCreate());
	}
	
	protected static Gradient wrapHandle(int handle, Document document) {
		return (Gradient) wrapHandle(Gradient.class, handle, document);
	}
	
	/**
	 * The gradient stops on the gradient ramp.
	 */
	public GradientStopList getStops() {
		if (stops == null)
			stops = new GradientStopList(this);
		else
			stops.update();
		return stops;
	}

	public void setStops(ReadOnlyList<GradientStop> list) {
		int size = list.size();
		GradientStopList stops = getStops();
		if (size < 2)
			throw new UnsupportedOperationException("Gradient stop list needs to contain at least two stops.");
		for (int i = 0; i < size; i++) {
			GradientStop stop = list.get(i);
			if (i < stops.size()) {
				stops.set(i, stop);
			} else {
				stops.add(stop);
			}
		}
		stops.setSize(size);
	}

	public void setStops(GradientStop[] stops) {
		setStops(Lists.asList(stops));
	}

	private native int nativeGetType();
	
	private native void nativeSetType(int type);

	/**
	 * The type of the gradient.
	 */
	public GradientType getType() {
		return IntegerEnumUtils.get(GradientType.class, nativeGetType());
	}

	public void setType(GradientType type) {
		nativeSetType(type.value);
	}

	public native boolean isValid();
	
	protected native boolean nativeRemove();
	
	public boolean remove() {
		// make super.remove() public
		return super.remove();
	}
}
