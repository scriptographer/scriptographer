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
 * File created on 08.04.2005.
 *
 * $Id$
 */

package com.scriptographer.ai;

import com.scratchdisk.list.AbstractMap;
import com.scratchdisk.util.SoftIntMap;

/**
 * @author lehni
 * 
 * @jshide
 */
public class Dictionary extends AbstractMap<String, Object> {
	private int handle;
	private Document document;

	// Internal hash map that keeps track of already wrapped objects. defined
	// as soft.
	private static SoftIntMap<Dictionary> dictionaries = new SoftIntMap<Dictionary>();

	private Dictionary(int handle, Document document) {
		this.handle = handle;
		this.document = document;
		dictionaries.put(handle, this);
	}

	private native Object nativeGet(int handle, int docHandle, Object key);

	public Object get(Object key) {
		return nativeGet(handle, document.handle, key);
	}

	private native boolean nativePut(int handle, String key, Object value);

	public Object put(String key, Object value) {
		Object previous = get(key);
		if (!nativePut(handle, key, value))
			throw new IllegalArgumentException();
		return previous;
	}

	private native boolean nativeRemove(int handle, Object key);

	public Object remove(Object key) {
		Object previous = get(key);
		return nativeRemove(handle, key) ? previous : null;
	}

	public native boolean containsKey(Object key);

	public native int size();

	// TODO: instead of producing a full array for all keys, we could add support for 
	// iterators to AbstractMap as an alternative (supporting both), and implementing
	// a wrapper for the native dictionary iterator here.
	protected native String[] keys();

	private native void nativeRelease(int handle);

	protected void finalize() {
		nativeRelease(handle);
	}

	public Document getDocument() {
		return document;
	}

	protected static Dictionary wrapHandle(int handle, Document document) {
		Dictionary dictionary = dictionaries.get(handle);
		if (dictionary == null || dictionary.document != document)
			dictionary = new Dictionary(handle, document);
		return dictionary; 
	}

	protected static Dictionary wrapHandle(int handle) {
		return wrapHandle(handle, Document.getWorkingDocument());
	}

	/**
	 * Called from the native environment to wrap a Dictionary:
	 */
	protected static Dictionary wrapHandle(int handle, int docHandle) {
		return wrapHandle(handle, Document.wrapHandle(docHandle)); 
	}

	public String toString() {
		return getClass().getSimpleName() + " (@" + Integer.toHexString(handle) + ")";
	}
}
