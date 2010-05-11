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
 * File created on 16.02.2005.
 *
 * $Id$
 */

package com.scratchdisk.list;


/**
 * @author lehni
 */
public abstract class AbstractList<E> extends AbstractReadOnlyList<E> implements List<E> {

	public E add(E element) {
		return add(size(), element);
	}

	public void remove(int fromIndex, int toIndex) {
		for (int i = toIndex - 1; i >= fromIndex; i--) {
			remove(i);
		}
	}

	public void removeAll() {
		remove(0, size());
	}

	/*
	 * Implement addAll with index too, although this is only
	 * required by ExtendedList. It is easier to implement all
	 * in one go like this though.
	 */
	public boolean addAll(int index, ReadOnlyList<? extends E> elements) {
		boolean modified = false;
		for (int i = 0, size = elements.size(); i < size; i++) {
			if (add(index, elements.get(i)) != null) {
				modified = true;
				index++;
			}
		}
		return modified;
	}

	/**
	 * @jshide
	 */
	public boolean addAll(ReadOnlyList<? extends E> elements) {
		return addAll(size(), elements);
	}

	public final boolean addAll(int index, E[] elements) {
		return addAll(index, Lists.asList(elements));
	}

	public final boolean addAll(E[] elements) {
		return addAll(size(), elements);
	}
}
