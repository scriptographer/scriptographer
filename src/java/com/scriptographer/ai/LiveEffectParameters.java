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
 * File created on Mar 1, 2010.
 *
 * $Id$
 */

package com.scriptographer.ai;

import java.util.Map;

/**
 * @author lehni
 *
 * @jshide
 */
public class LiveEffectParameters extends Dictionary {

	protected LiveEffectParameters(int handle, Document document) {
		super(handle, document, false);
	}

	public LiveEffectParameters() {
		super(nativeCreateLiveEffectParameters(), false);
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
			dict = new LiveEffectParameters(handle, document);
		}
		return (LiveEffectParameters) dict; 
	}
}
