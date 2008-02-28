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
 * File created on 16.02.2005.
 *
 * $Id$
 */

package com.scratchdisk.list;

import java.util.Collection;
import java.util.Arrays;

/**
 * Wraps an extended java.util.ArrayList in an ExtendedList interface
 * 
 * @author lehni
 */
public class ArrayList extends AbstractExtendedList {
	List list;

	public ArrayList() {
		list = new List();
	}

	public ArrayList(int initialCapacity) {
		list = new List(initialCapacity);
	}

	public ArrayList(Object[] objects) {
		this(objects.length);
		list.addAll(Arrays.asList(objects));
	}

	public ArrayList(List list) {
		this(list.toArray());
	}

	public int size() {
		return list.size();
	}

	public Object get(int index) {
		return list.get(index);
	}

	public Object add(int index, Object element) {
		list.add(index, element);
		return element;
	}

	public Object set(int index, Object element) {
		return list.set(index, element);
	}

	public Object remove(int index) {
		return list.remove(index);
	}

	public void remove(int fromIndex, int toIndex) {
		list.remove(fromIndex, toIndex);
	}

	public Object add(Object element) {
		if (list.add(element))
			return element;
		return null;
	}

	public boolean addAll(int index, Collection c) {
		return list.addAll(index, c);
	}

	/*
	 * Adds setSize and the public removeRange to java.util.ArrayList:
	 */
	public static class List extends java.util.ArrayList {
		public List() {
			super();
		}

		public List(int initialCapacity) {
			super(initialCapacity);
		}

		public void setSize(int newSize) {
			int size = size();
			if (newSize > size) {
				// fill with null:
				ensureCapacity(newSize);
				for (int i = size; i < newSize; i++)
					add(i, null);
			} else if (newSize < size) {
				// remove the unneeded beziers:
				remove(newSize, size);
			}
		}

		public void remove(int fromIndex, int toIndex) {
			super.removeRange(fromIndex, toIndex);
		}
	}
}
