/*
 * Scriptographer
 * List.java
 * 
 * Created by  Lehni on 16.02.2005.
 * Copyright (c) 2004 http://www.scratchdisk.com. All rights reserved.
 * 
 */
package com.scriptographer.util;

import java.util.Collection;

/**
 * This is almost the same as java.util.List, but has a few changes that make it much easier to use in
 * Scirptographer. SegmentList, CurveList, LayerList and ArtSet subclass the AbstractList that implements this
 * interface.
 *
 * Reason to implement this:
 * - getLength adds .length to the JS wrapper
 * - ad(int, element) returns boolean as well, which makes it easier to implement both adds.
 * - defines less functions and makes it therefore easier to implement (no ListIterator, no SubLists)
 * - defines other often needed functions, e.g. remove(fromIndex, toIndex)
 * - gives complete control over behavior of lists
 * - avoids wrapping of standard java.util.lists in the JS ListObject
 * - ...
 */
public interface List extends Collection {
	public int getLength();

	public Object get(int index);
	public boolean add(int index, Object element);
	public Object set(int index, Object element);
	public Object remove(int index);

	public void remove(int fromIndex, int toIndex);

	public int indexOf(Object element);
	public int lastIndexOf(Object element);

	public boolean addAll(int index, Collection elements);
	public boolean addAll(int index, Object[] elements);
	public boolean addAll(Object[] elements);

	public Object[] toArray(int fromIndex, int toIndex);
	public Object[] toArray(Object[] array, int fromIndex, int toIndex);

	public List toList(int fromIndex, int toIndex);
}
