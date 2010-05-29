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
 * File created on Aug 13, 2009.
 */

package com.scratchdisk.util;

import java.util.Collection;

/**
 * com.scratchdisk.util.ArrayList extends java.util.ArrayList and adds some
 * useful public methods, such as {@link #setSize}, {@link #remove}. 
 * 
 * @author lehni
 */
public class ArrayList<E> extends java.util.ArrayList<E> {
	public ArrayList() {
		super();
	}

	public ArrayList(int initialCapacity) {
		super(initialCapacity);
	}

	public ArrayList(Collection<? extends E> c) {
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
			removeRange(newSize, size);
		}
	}

	public void remove(int fromIndex, int toIndex) {
		removeRange(fromIndex, toIndex);
	}
}