/*
 * Scriptographer
 * ArrayList.java
 * 
 * Created by  Lehni on 16.02.2005.
 * Copyright (c) 2004 http://www.scratchdisk.com. All rights reserved.
 * 
 */
package com.scriptographer.util;

import java.util.Collection;
import java.util.Arrays;

/**
 * Wrapps an extended java.util.ArrayList in a com.scriptographer.util.List interface
 */
public class ArrayList extends AbstractList {
	ExtendedJavaList list;

	public ArrayList() {
		list = new ExtendedJavaList();
	}

	public ArrayList(int initialCapacity) {
		list = new ExtendedJavaList(initialCapacity);
	}

	public ArrayList(Collection objects) {
		this(objects.size());
		list.addAll(objects);
	}

	public ArrayList(Object[] objects) {
		this(Arrays.asList(objects));
	}

	public int getLength() {
		return list.size();
	}

	public Object get(int index) {
		return list.get(index);
	}

	public boolean add(int index, Object element) {
		int size = list.size();
		list.add(index, element);
		return (size != list.size());
	}

	public Object set(int index, Object element) {
		return list.set(index, element);
	}

	public Object remove(int index) {
		return list.remove(index);
	}

	public void remove(int fromIndex, int toIndex) {
		list.removeRange(fromIndex, toIndex);
	}

	public boolean add(Object element) {
		return list.add(element);
	}

	public boolean addAll(int index, Collection c) {
		return list.addAll(index, c);
	}
}
