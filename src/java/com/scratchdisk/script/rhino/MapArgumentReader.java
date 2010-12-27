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
 * File created on Feb 11, 2008.
 */

package com.scratchdisk.script.rhino;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import com.scratchdisk.script.ArgumentReader;
import com.scratchdisk.script.Converter;

/**
 * @author lehni
 *
 */
public class MapArgumentReader extends ArgumentReader {

	protected Scriptable scriptable;

	public MapArgumentReader(Converter converter, Scriptable scriptable) {
		super(converter);
		this.scriptable = scriptable;
	}

	public Object[] keys() {
		return scriptable.getIds();
	}

	protected Object readNext(String name) {
		Object obj = scriptable.get(name, scriptable);
		return obj != ScriptableObject.NOT_FOUND ? obj : null;
	}

	public boolean has(String name) {
		return scriptable.has(name, scriptable);
	}

	public boolean isMap() {
		return true;
	}
}
