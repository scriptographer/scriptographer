/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Scripting Plugin for Adobe Illustrator
 * http://scriptographer.org/
 *
 * Copyright (c) 2002-2010, Juerg Lehni
 * http://scratchdisk.com/
 *
 * All rights reserved. See LICENSE file for details.
 * 
 * File created on Oct 18, 2006.
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
