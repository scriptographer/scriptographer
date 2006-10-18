/*
 * Scriptographer
 * 
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 * 
 * Copyright (c) 2002-2006 Juerg Lehni, http://www.scratchdisk.com.
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
 * File created on 03.11.2005.
 * 
 * $RCSfile: NullToUndefinedWrapper.java,v $
 * $Author: lehni $
 * $Revision: 1.4 $
 * $Date: 2006/10/18 14:12:51 $
 */

package com.scriptographer.js;

import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;

import com.scriptographer.ai.Color;
import com.scriptographer.ai.FontWeight;


// wrappable interface

/**
 * Create a simple wrapper that converts null to Undefined in both ways,
 * and null to Color.NONE in case of color
 * 
 * In Java, null means undefined, while in javascript null is e.g. Color.NONE
 * This is more intuitive...
 * 
 * This wrapper is used by PathSTyle, FillStyle, StrokeStyle
 */
public class NullToUndefinedWrapper extends NativeJavaObject {
	public NullToUndefinedWrapper(Scriptable scope, Object javaObject, Class staticType) {
		super(scope, javaObject, staticType);
	}

	public void put(String name, Scriptable start, Object value) {
		if (value == Undefined.instance)
			value = null;
		else if (value == null) {
			if (name.equals("color"))
				value = Color.NONE;
			else if (name.equals("font"))
				value = FontWeight.NONE;
		}
		super.put(name, start, value);
	}
	
	public Object get(String name, Scriptable start) {
		Object value = super.get(name, start);
		// convert back
		if (value == null) {
			value = Undefined.instance;
		} else if (value instanceof Wrapper) {
			Object obj = ((Wrapper) value).unwrap();
			if (obj == Color.NONE || obj == FontWeight.NONE)
				value = null;
		}
		return value;
	}
}
