/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2008 Juerg Lehni, http://www.scratchdisk.com.
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
 * File created on 14.12.2004.
 *
 * $Id$
 */

package com.scriptographer.ai;

import com.scratchdisk.list.ArrayList;
import com.scriptographer.list.AbstractFetchList;

/**
 * @author lehni
 */
public class CurveList extends AbstractFetchList {
	protected Path path;
	protected int size;
	protected SegmentList segments;
	protected ArrayList.List list;

	protected CurveList(Path path, SegmentList segments) {
		this.path = path;
		this.segments = segments;
		segments.curves = this;
		list = new ArrayList.List();
		updateSize();
	}
	
	public Path getPath() {
		return path;
	}

	/**
	 * This is called from the linked SegmentList, when this path changes.
	 */
	protected void updateSize() {
		int newSize = segments.size;
		// reduce length by one if it's an open path:
		if (!path.isClosed() && newSize > 0)
			newSize--;
		
		if (size != newSize) {
			list.setSize(newSize);
			size = newSize;
		}
	}

	protected void fetch(int fromIndex, int toIndex) {
		// prefetch all the needed segments now:
		segments.fetch(fromIndex, Math.min(segments.size - 1, toIndex + 1));
	}

	protected void fetch() {
		if (size > 0)
			fetch(0, size);
	}

	// this list is read only:
	public Object add(int index, Object element) {
		throw new UnsupportedOperationException();
	}

	public Object set(int index, Object element) {
		throw new UnsupportedOperationException();
	}

	public Object remove(int index) {
		throw new UnsupportedOperationException();
	}

	public Object get(int index) {
		Curve curve = (Curve) list.get(index);
		if (curve == null) {
			curve = new Curve(segments, index);
			list.set(index, curve);
		} else {
			curve.updateSegments();
		}
		return curve;
	}

	public Curve getCurve(int index) {
		return (Curve) get(index);
	}

	public int size() {
		return size;
	}

	public boolean isEmpty() {
		return size == 0;
	}
}
