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
