/*
 * Scriptographer
 * 
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 * 
 * Copyright (c) 2004-2005 Juerg Lehni, http://www.scratchdisk.com.
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
 * File created on 21.10.2005.
 * 
 * $RCSfile: Lists.java,v $
 * $Author: lehni $
 * $Revision: 1.3 $
 * $Date: 2005/11/03 00:00:15 $
 */

package com.scriptographer.util;

public class Lists {
	public static ExtendedList asList(Object[] array) {
		return new ArrayList(array);
	}

	public static ExtendedList createSubList(ReadOnlyList list, int fromIndex, int toIndex) {
		if (fromIndex < 0)
			throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
		if (toIndex > list.getLength())
			throw new IndexOutOfBoundsException("toIndex = " + toIndex);
		if (fromIndex > toIndex)
			throw new IllegalArgumentException("fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
		
		ExtendedArrayList subList = new ExtendedArrayList(toIndex - fromIndex);
		for (int i = fromIndex; i < toIndex; i++)
			subList.add(list.get(i));
		
		return subList;
	}

	private static class ArrayList extends AbstractExtendedList {

		private Object[] array;

		ArrayList(Object[] array) {
			if (array == null)
				throw new NullPointerException();
			this.array = array;
		}

		public int getLength() {
			return array.length;
		}

		public Object get(int index) {
			return array[index];
		}

		public Object set(int index, Object element) {
			Object prev = array[index];
			array[index] = element;
			return prev;
		}

		public Object add(int index, Object element) {
			throw new UnsupportedOperationException();
		}

		public Object remove(int index) {
			throw new UnsupportedOperationException();
		}
	}
}
