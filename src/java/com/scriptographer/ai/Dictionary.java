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
 * File created on 08.04.2005.
 *
 * $Id$
 */

package com.scriptographer.ai;

import java.util.Iterator;
import java.util.Map;

import com.scratchdisk.util.AbstractMap;
import com.scratchdisk.util.SoftIntMap;

/**
 * @author lehni
 * 
 * @jshide
 */
public class Dictionary extends AbstractMap<String, Object> {
	protected int handle;
	protected Document document;
	private boolean release;

	// Internal hash map that keeps track of already wrapped objects. defined
	// as soft so they can be finalized by GC.
	protected static SoftIntMap<Dictionary> dictionaries =
			new SoftIntMap<Dictionary>();

	protected Dictionary(int handle, Document document, boolean release) {
		this.handle = handle;
		this.document = document != null
				 ? document : Document.getWorkingDocument();
		this.release = release;
		dictionaries.put(handle, this);
	}

	protected Dictionary(int handle, boolean release) {
		this(handle, Document.getWorkingDocument(), release);
	}

	public Dictionary() {
		this(nativeCreate(), true);
	}

	private static native int nativeCreate();

	protected static native int nativeCreateLiveEffectParameters();

	public Dictionary(Map<?, ?> map) {
		this();
		for (Map.Entry<?, ?> entry : map.entrySet())
			put(entry.getKey().toString(), entry.getValue());
	}

	private native Object nativeGet(int handle, int docHandle, Object key);

	public Object get(Object key) {
		return nativeGet(handle, document.handle, key);
	}

	private native boolean nativePut(int handle, String key, Object value);

	public Object put(String key, Object value) {
		Object previous = get(key);
		if (!nativePut(handle, key, value))
			throw new IllegalArgumentException(
					"Dictionaries do not support objects of type "
					+ value.getClass().getSimpleName());
		return previous;
	}

	private native boolean nativeRemove(int handle, Object key);

	public Object remove(Object key) {
		Object previous = get(key);
		return nativeRemove(handle, key) ? previous : null;
	}

	public native boolean containsKey(Object key);

	public native int size();

	// TODO: instead of producing a full array for all keys, we could add
	// support for iterators to AbstractMap as an alternative (supporting both),
	// and implementing a wrapper for the native dictionary iterator here.
	protected native String[] keys();

	private native void nativeRelease(int handle);

	protected boolean release() {
		if (release && handle != 0) {
			nativeRelease(handle);
			handle = 0;
			return true;
		}
		return false;
	}

	protected void finalize() {
		if (release())
			dictionaries.remove(handle);
	}

	public Document getDocument() {
		return document;
	}

	protected static Dictionary wrapHandle(int handle, Document document) {
		Dictionary dict = dictionaries.get(handle);
		if (dict == null || document != null && dict.document != document) {
			// Reused handle in a different document, set handle of old
			// wrapper to 0 and produce a new one.
			if (dict != null)
				dict.handle = 0;
			dict = new Dictionary(handle, document, true);
		}
		return dict;
	}

	public Object clone() {
		return new Dictionary(this);
	}

	public boolean equals(Object obj) {
		if (obj instanceof Map) {
			Map map = (Map) obj;
			Object[] keys2 = map.keySet().toArray();
			if (keys2.length != this.size())
				return false;
			Object[] keys1 = this.keys();
			for (int i = keys1.length - 1; i >= 0; i--) {
				Object key1 = keys1[i];
				Object key2 = keys2[i];
				if (!key1.equals(key2))
					return false;
				Object value1 = this.get(key1);
				Object value2 = map.get(key2);
				if (value1 != value2 && (value1 == null || !value1.equals(value2)))
					return false;					
			}
			return true;
		}
		return super.equals(obj);
	}

	public int hashCode() {
		return handle != 0 ? handle : super.hashCode();
	}

	public boolean isValid() {
		return handle != 0;
	}

	/**
	 * @jshide
	 */
	public String getId() {
		return "@" + Integer.toHexString(hashCode());
	}

	public String toString() {
		return getClass().getSimpleName() + " (" + getId() + ")";
	}

	/**
	 * Called from the native environment to wrap a Dictionary:
	 */
	protected static Dictionary wrapHandle(int handle, int docHandle) {
		return wrapHandle(handle, Document.wrapHandle(docHandle)); 
	}

	/**
	 * Releases all dictionaries that were used since last executed, and sets
	 * their handles to 0 so existing references will be invalid.
	 * 
	 * This needs to be executed at the end of each history cycle, as 
	 * Illustrator produces very odd crashes when dictionary references to
	 * item dictionaries are kept alive to items that do not exist any longer
	 * due to undoing. Confusingly, these crashes happen either in
	 * AWS_CUI_RevertAlert or AWS_CUI_GetVersionComments.
	 * 
	 * This is called automatically from ScriptographerEngine.endExecution().
	 * 
	 * @jshide
	 */
	public static void releaseAll() {
		// Use Iterator to remove to avoid ConcurrentModificationExceptions
		Iterator<Dictionary> it = dictionaries.values().iterator();
		while (it.hasNext()) {
			Dictionary dict = it.next();
			if (dict.release())
				it.remove();
		}
	}
}
