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

import com.scriptographer.ScriptographerException;
import com.scriptographer.list.AbstractStructList;

/**
 * @author lehni
 * 
 * @jshide
 */
public class GradientStopList extends AbstractStructList<Gradient, GradientStop> {

	protected GradientStopList(Gradient gradient) {
		super(gradient);
	}
	
	public Gradient getGradient() {
		return reference;
	}

	public Class<GradientStop> getComponentType() {
		return GradientStop.class;
	}

	protected int nativeGetSize() {
		return nativeGetSize(reference.handle);
	}

	protected int nativeRemove(int fromIndex, int toIndex) {
		return nativeRemove(reference.handle, reference.document.handle,
				fromIndex, toIndex);
	}

	protected GradientStop createEntry(int index) {
		return new GradientStop(reference, index);
	}

	public void remove(int fromIndex, int toIndex) {
		if (fromIndex < toIndex && size + fromIndex - toIndex < 2)
			throw new ScriptographerException(
					"There need to be at least two gradient stops");

		super.remove(fromIndex, toIndex);
	}

	private static native int nativeGetSize(int handle);

	private static native int nativeRemove(int handle, int docHandle,
			int fromIndex, int toIndex);

	protected static native boolean nativeGet(int handle, int index,
			GradientStop stop);

	protected static native boolean nativeSet(int handle, int docHandle,
			int index, double midPoint, double rampPoint, float[] color);

	protected static native boolean nativeInsert(int handle, int docHandle,
			int index, double midPoint, double rampPoint, float[] color);
}
