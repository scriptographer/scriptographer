/*
 * Scriptographer
 * AbstractReadOnlyList.java
 * 
 * Created by  Lehni on 16.02.2005.
 * Copyright (c) 2004 http://www.scratchdisk.com. All rights reserved.
 * 
 */
package com.scriptographer.util;

public abstract class AbstractReadOnlyList extends AbstractList {

	public boolean add(int index, Object element) {
		throw new UnsupportedOperationException();
	}

	public Object set(int index, Object element) {
		throw new UnsupportedOperationException();
	}

	public Object remove(int index) {
		throw new UnsupportedOperationException();
	}
}
