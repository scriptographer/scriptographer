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
 * File created on Jul 9, 2009.
 */

package com.scriptographer.ai;

import com.scriptographer.list.AbstractStructList;

/**
 * @author lehni
 *
 * @jshide
 */
public class ArtboardList extends AbstractStructList<Document, Artboard> {
	
	protected ArtboardList(Document document) {
		super(document);
	}

	public Class<Artboard> getComponentType() {
		return Artboard.class;
	}

	protected Artboard createEntry(int index) {
		return new Artboard(reference, index);
	}

	protected int nativeGetSize() {
		return nativeGetSize(reference.handle);
	}

	protected int nativeRemove(int fromIndex, int toIndex) {
		return nativeRemove(reference.handle, fromIndex, toIndex);
	}

	private static native int nativeGetSize(int handle);

	private static native int nativeRemove(int handle, int fromIndex, int toIndex);

	protected static native boolean nativeGet(int handle, int index, Artboard artboard);

	protected static native boolean nativeInsert(int handle, int index,
			Rectangle bounds, boolean showCenter, boolean showCrossHairs,
			boolean showSafeAreas, double pixelAspectRatio);

	protected static native boolean nativeSet(int handle, int index,
			Rectangle bounds, boolean showCenter, boolean showCrossHairs,
			boolean showSafeAreas, double pixelAspectRatio);
}
