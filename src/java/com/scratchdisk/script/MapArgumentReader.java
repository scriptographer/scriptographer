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
