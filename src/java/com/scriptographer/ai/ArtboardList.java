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
