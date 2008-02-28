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
 * File created on Oct 18, 2006.
 * 
 * $Id$
 */

package com.scriptographer.ai;

import com.scratchdisk.list.AbstractExtendedList;
import com.scratchdisk.list.ArrayList;
import com.scriptographer.CommitManager;

/**
 * @author lehni
 */
public class GradientStopList extends AbstractExtendedList {
	protected Gradient gradient;
	protected int size;
	protected ArrayList.List list;

	private int version = -1;

	protected GradientStopList(Gradient gradient) {
		this.gradient = gradient;
		list = new ArrayList.List();
		update();
	}
	
	public Gradient getGradient() {
		return gradient;
	}

	protected static native void nativeGet(int handle, int index,
			GradientStop stop);

	protected static native void nativeSet(int handle, int docHandle,
			int index, float midPoint, float rampPoint, float[] color);

	protected static native void nativeInsert(int handle, int docHandle,
			int index, float midPoint, float rampPoint, float[] color);

	private static native int nativeGetSize(int handle);

	/**
	 * updates the synchronization between the cached segments in java
	 * and the underlying Illustrator object.
	 * Only called from Path.getSegmentList()
	 */
	protected void update() {
		if (gradient != null && version != CommitManager.version) {
			size = nativeGetSize(gradient.handle);
			list.setSize(size);
			version = CommitManager.version;
		}
	}

	// this list is read only:
	public Object add(int index, Object obj) {
		if (obj instanceof GradientStop) {
			GradientStop stop = (GradientStop) obj;
			// add to internal structure
			list.add(index, stop);
			// update verion:
			stop.version = CommitManager.version;
			
			// and link segment to this list
			stop.list = this;
			stop.index = index;
			// increase size
			size++;
			// and add to illustrator as well
			stop.insert();
			// update stop indices
			for (int i = index + 1; i < size; i++) {
				GradientStop s = (GradientStop) list.get(i);
				if (s != null)
					s.index = i;
			}
			return stop;
		}
		return null;
	}

	public Object set(int index, Object obj) {
		if (obj instanceof GradientStop) {
			GradientStop stop = (GradientStop) obj;
			GradientStop ret = (GradientStop) list.set(index, stop);
			stop.list = this;
			stop.index = index;
			stop.markDirty();
			if (ret != null) {
				ret.list = null;
				ret.index = -1;
			}
			return ret;
		}
		return null;
	}

	private static native int nativeRemove(int handle, int docHandle,
			int fromIndex, int toIndex);

	public void remove(int fromIndex, int toIndex) {
		if (fromIndex < toIndex) {
			int newSize = size + fromIndex - toIndex;
			if (newSize < 2)
				throw new RuntimeException(
						"There need to be at least two gradient stops");
			for (int i = fromIndex; i < toIndex; i++) {
				GradientStop obj = (GradientStop) list.get(i);
				if (obj != null) {
					obj.list = null;
					obj.index = -1;
				}
			}
			if (gradient != null) {
				size = nativeRemove(gradient.handle, gradient.document.handle,
						fromIndex, toIndex);
			}
			list.remove(fromIndex, toIndex);
			size = newSize;
		}

	}

	public Object remove(int index) {
		Object obj = get(index);
		remove(index, index + 1);
		return obj;
	}

	public Object get(int index) {
		GradientStop stop = (GradientStop) list.get(index);
		if (stop == null) {
			stop = new GradientStop(this, index);
			list.set(index, stop);
		} else {
			stop.update();
		}
		return stop;
	}

	public int size() {
		return size;
	}

	public boolean isEmpty() {
		return size == 0;
	}
}
