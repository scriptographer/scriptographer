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
public class ArrayList<E> extends AbstractExtendedList<E> {
	List<E> list;

	public ArrayList() {
		list = new List<E>();
	}

	public ArrayList(int initialCapacity) {
		list = new List<E>(initialCapacity);
	}

	public ArrayList(E[] objects) {
		list = new List<E>(Arrays.asList(objects));
	}

	public ArrayList(List<E> list) {
		list = new List<E>(list);
	}

	public int size() {
		return list.size();
	}

	public E get(int index) {
		return list.get(index);
	}

	public E add(int index, E element) {
		list.add(index, element);
		return element;
	}

	public E set(int index, E element) {
		return list.set(index, element);
	}

	public E remove(int index) {
		return list.remove(index);
	}

	public void remove(int fromIndex, int toIndex) {
		list.remove(fromIndex, toIndex);
	}

	public E add(E element) {
		if (list.add(element))
			return element;
		return null;
	}

	public boolean addAll(int index, Collection<? extends E> c) {
		return list.addAll(index, c);
	}

	/*
	 * Adds setSize and the public removeRange to java.util.ArrayList:
	 */
	public static class List<E> extends java.util.ArrayList<E> {
		public List() {
			super();
		}

		public List(int initialCapacity) {
			super(initialCapacity);
		}

	    public List(Collection<? extends E> c) {
	    	super(c);
	    }

		public void setSize(int newSize) {
			int size = size();
			if (newSize > size) {
				// fill with null:
				ensureCapacity(newSize);
				for (int i = size; i < newSize; i++)
					add(i, null);
			} else if (newSize < size) {
				remove(newSize, size);
			}
		}

		public void remove(int fromIndex, int toIndex) {
			super.removeRange(fromIndex, toIndex);
		}
	}
}
