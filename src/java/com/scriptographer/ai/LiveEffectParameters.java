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
 * File created on Mar 1, 2010.
 */

package com.scriptographer.ai;

import java.util.Map;

/**
 * @author lehni
 *
 * @jshide
 */
public class LiveEffectParameters extends Dictionary {
	protected LiveEffectParameters(int handle, Document document, boolean release) {
		super(handle, document, release, null);
	}

	public LiveEffectParameters() {
		// When creating new LiveEffectParameters, we start with release = true
		// Item#addEffect then sets it to false once the parameters becomes part
		// of an effect.
		this(nativeCreateLiveEffectParameters(), null, true);
	}

	public LiveEffectParameters(Map<?, ?> map) {
		this();
		for (Map.Entry<?, ?> entry : map.entrySet())
			put(entry.getKey().toString(), entry.getValue());
	}

	public Object clone() {
		return new LiveEffectParameters(this);
	}

	protected static LiveEffectParameters wrapHandle(int handle,
			Document document) {
		// As we are sharing the lookup table with Dictonary and apparently
		// there are rare occasions where handles are reused, we need to check
		// that previous cashed dictionaries are indeed of the required type,
		// and if not, we create new ones
		Dictionary dict = dictionaries.get(handle);
		if (dict == null || !(dict instanceof LiveEffectParameters)
				|| dict.document != document) {
			// Reused handle in a different document, set handle of old
			// wrapper to 0 and produce a new one.
			if (dict != null)
				dict.handle = 0;
			dict = new LiveEffectParameters(handle, document, false);
		}
		return (LiveEffectParameters) dict; 
	}

	/**
	 * Called from the native environment to wrap a Dictionary:
	 */
	protected static LiveEffectParameters wrapHandle(int handle, int docHandle) {
		return wrapHandle(handle, Document.wrapHandle(docHandle)); 
	}
}
