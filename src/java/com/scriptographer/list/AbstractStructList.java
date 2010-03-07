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
 * File created on Jul 10, 2009.
 *
 * $Id$
 */

package com.scriptographer.list;

import com.scriptographer.CommitManager;
import com.scriptographer.Committable;

/**
 * An abstract list to reflect simple struct based native lists, as used by
 * GradientStop and Artboard.
 * 
 * @author lehni
 */
public abstract class AbstractStructList<R, E extends AbstractStructList.Entry<R>>
		extends AbstractNativeList<E> {

	protected R reference;
	protected int version = -1;

	protected AbstractStructList(R reference) {
		this.reference = reference;
		update();
	}

	protected abstract int nativeGetSize();

	protected abstract int nativeRemove(int fromIndex, int toIndex);

	protected abstract E createEntry(int index);

	/**
	 * Updates the synchronization between the cached segments in java
	 * and the underlying Illustrator object.
	 * Only called from Gradient.getStops()
	 */
	public void update() {
		if (reference != null && version != CommitManager.version) {
			size = nativeGetSize();
			list.setSize(size);
			version = CommitManager.version;
		}
	}

	public E get(int index) {
		E element = list.get(index);
		if (element == null) {
			element = createEntry(index);
			list.set(index, element);
		}
		// Always update, even when newly fetched, to make sure it is
		// initialised.
		return element.update() ? element : null;
	}

	public E add(int index, E element) {
		// Add to internal structure
		list.add(index, element);
		// Update version:
		element.version = CommitManager.version;
		element.index = index;
		element.reference = reference;
		// And add to illustrator as well
		if (element.insert()) {
			// Increase size
			size++;
			// Update indices
			for (int i = index + 1; i < size; i++) {
				E e = list.get(i);
				if (e != null)
					e.index = i;
			}
			return element;
		}
		return null;
	}

	public E set(int index, E element) {
		E ret = get(index);
		list.set(index, element);
		element.reference = reference;
		element.index = index;
		element.markDirty();
		// It might be that this element was already inserted elsewhere in the
		// same list.
		// Only clear reference / index if it is still valid.
		if (ret != null && ret.index == index) {
			ret.reference = null;
			ret.index = -1;
		}
		return ret;
	}
	
	public void remove(int fromIndex, int toIndex) {
		if (fromIndex < toIndex) {
			int newSize = size + fromIndex - toIndex;
			for (int i = fromIndex; i < toIndex; i++) {
				E obj = list.get(i);
				// Again, only clear reference / index if it's actually still valid
				if (obj != null && obj.index == i) {
					obj.reference = null;
					obj.index = -1;
				}
			}
			if (reference != null)
				size = nativeRemove(fromIndex, toIndex);
			list.remove(fromIndex, toIndex);
			size = newSize;
		}

	}

	public abstract static class Entry<R> implements Committable {
		protected R reference;
		protected int index;
		protected boolean dirty;
		protected int version = -1;

		protected Entry() {
			reference = null;
			index = -1;
			// A new entry is dirty by default, since this is the base
			// constructor used by initialising constructors.
			dirty = true;
		}

		protected Entry(R reference, int index) {
			this.reference = reference;
			this.index = index;
			// A representation for an existing entry is not dirty, since it
			// will be fetched from the array.
			this.dirty = false;
		}

		protected void markDirty() {
			// Only mark it as dirty if it's attached to an object already and
			// if the dirty flag is not already set.
			// Put both non dirty ones that were fetched before and dirty ones
			// that were never fetched so far (= newly created entries) into
			// the committable list.
			if (reference != null && (!dirty && version != -1 || dirty && version == -1)) {
				CommitManager.markDirty(reference, this);
				dirty = true;
			}
		}
		
		protected abstract boolean nativeInsert();

		protected abstract boolean nativeSet();

		protected abstract boolean nativeGet();

		protected boolean insert() {
			if (reference != null && index != -1) {
				CommitManager.commit(reference);
				if (nativeInsert()) {
					version = CommitManager.version;
					dirty = false;
					return true;
				}
			}
			return false;
		}

		public void commit() {
			if (dirty && reference != null && index != -1 && nativeSet()) {
				version = CommitManager.version;
				dirty = false;
			}
		}
		
		protected boolean update() {
			if (!dirty && reference != null && index != -1
					&& version != CommitManager.version) {
				if (!nativeGet())
					return false;
				version = CommitManager.version;
			}
			return true;
		}

		/**
		 * The index of the object in the list it belongs to.
		 */
		public int getIndex() {
			return index;
		}
	}
}
