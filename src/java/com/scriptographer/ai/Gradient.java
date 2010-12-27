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
