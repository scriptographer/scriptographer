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
 * File created on May 2, 2010.
 */

package com.scratchdisk.script;

import java.util.Map;

/**
 * @author lehni
 *
 */
public class MapArgumentReader extends ArgumentReader {

	protected Map map;

	public MapArgumentReader(Converter converter, Map map) {
		super(converter);
		this.map = map;
	}

	/**
	 * Creates a new ArgumentReader that shares the converter with an other 
	 * existing one. This is to be able to create new Maps, copy over values
	 * form another map or reader, and still be able to use the engine's
	 * conversion logic.
	 */
	public MapArgumentReader(ArgumentReader reader, Map map) {
		super(reader.converter);
		this.map = map;
	}

	public Object[] keys() {
		return map.keySet().toArray();
	}

	protected Object readNext(String name) {
		return map.get(name);
	}

	public boolean has(String name) {
		return map.containsKey(name);
	}

	public boolean isMap() {
		return true;
	}
}
