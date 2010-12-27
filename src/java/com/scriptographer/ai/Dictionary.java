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
 * File created on 08.04.2005.
 */

package com.scriptographer.ai;

import java.util.Iterator;
import java.util.Map;

import com.scratchdisk.util.AbstractMap;
import com.scratchdisk.util.IntMap;

/**
 * @author lehni
 * 
 * @jshide
 */
public class Dictionary extends AbstractMap<String, Object>
		implements ValidationObject {
	protected int handle;
	protected Document document;
	protected boolean release;
	protected ValidationObject validation;

	protected static IntMap<Dictionary> dictionaries =
			new IntMap<Dictionary>();

	protected Dictionary(int handle, Document document, boolean release,
			ValidationObject validation) {
		this.handle = handle;
		this.document = document != null
				 ? document : Document.getWorkingDocument();
		this.release = release;
		this.validation = validation;
		dictionaries.put(handle, this);
	}

	protected Dictionary(int handle, boolean release,
			ValidationObject validation) {
		this(handle, Document.getWorkingDocument(), release, validation);
	}

	public Dictionary() {
		// By default use the working document as the validation scope
		this(nativeCreate(), true, Document.getWorkingDocument());
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
		// Also check validation object that this dictionary depends on.
		if (validation != null && !validation.isValid())
			return false;
		return handle != 0;
	}

	/**
	 * Called from the native environment when a dictionary becomes part of
	 * another. This will create a validation chain that is checked in isValid,
	 * down to the originating native object, e.g. Item or Document.
	 */
	protected void setValidation(ValidationObject validation) {
		this.validation = validation;
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

	protected static Dictionary wrapHandle(int handle, Document document,
			ValidationObject validation) {
		Dictionary dict = dictionaries.get(handle);
		if (dict == null || document != null && dict.document != document) {
			// Reused handle in a different document, set handle of old
			// wrapper to 0 and produce a new one.
			if (dict != null)
				dict.handle = 0;
			dict = new Dictionary(handle, document, true, validation);
		}
		return dict;
	}

	/**
	 * Called from the native environment to wrap a Dictionary:
	 */
	protected static Dictionary wrapHandle(int handle, int docHandle,
			ValidationObject validation) {
		return wrapHandle(handle, Document.wrapHandle(docHandle), validation); 
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
	public static void releaseInvalid() {
		// Release all invalid dictionaries and then clear the lookup table
		Iterator<Dictionary> it = dictionaries.values().iterator();
		while (it.hasNext()) {
			Dictionary dict = it.next();
			// Also get rid of cached dictionaries that do not need to release
			// themselves, as we never know when they become invalid
			if (dict.validation != null && !dict.validation.isValid()
					&& dict.release() || !dict.release)
				it.remove();
		}
	}
}
