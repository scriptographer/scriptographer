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
 * File created on 21.10.2005.
 */

package com.scratchdisk.list;

import java.lang.reflect.Array;


/**
 * @author lehni 
 */
public class Lists {
	public static <T> ExtendedList<T> asList(T[] array) {
		return new ExtendedArrayList<T>(array);
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] toArray(ReadOnlyList<T> list) {
		int size = list.size();
		Object array = Array.newInstance(list.getComponentType(), size);
		for (int i = 0; i < size; i++)
			Array.set(array, i, list.get(i));
		return (T[]) array;
	}
	
	public static <T> ExtendedList<T> createSubList(ReadOnlyList<T> list, int fromIndex, int toIndex) {
		if (fromIndex < 0)
			throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
		if (toIndex > list.size())
			throw new IndexOutOfBoundsException("toIndex = " + toIndex);
		if (fromIndex > toIndex)
			throw new IllegalArgumentException("fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
		
		ExtendedArrayList<T> subList = new ExtendedArrayList<T>(toIndex - fromIndex);
		for (int i = fromIndex; i < toIndex; i++)
			subList.add(list.get(i));
		
		return subList;
	}
}
