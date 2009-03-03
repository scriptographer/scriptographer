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
 * File created on Mar 1, 2009.
 *
 * $Id$
 */

package com.scriptographer.script.rhino;

import java.util.HashMap;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import com.scratchdisk.script.rhino.ExtendedJavaObject;
import com.scriptographer.ai.CMYKColor;
import com.scriptographer.ai.Color;
import com.scriptographer.ai.GrayColor;
import com.scriptographer.ai.RGBColor;

/**
 * @author lehni
 *
 */
public class ColorWrapper extends ExtendedJavaObject {
	private static HashMap<String, Class> colorPropertyToClass = new HashMap<String, Class>();
	static {
		// RGB
		colorPropertyToClass.put("gray", GrayColor.class);
		// RGB
		colorPropertyToClass.put("red", RGBColor.class);
		colorPropertyToClass.put("green", RGBColor.class);
		colorPropertyToClass.put("blue", RGBColor.class);
		// CMYK
		colorPropertyToClass.put("cyan", CMYKColor.class);
		colorPropertyToClass.put("magenta", CMYKColor.class);
		colorPropertyToClass.put("yellow", CMYKColor.class);
		colorPropertyToClass.put("black", CMYKColor.class);
	}

	public ColorWrapper(Scriptable scope, Color color, Class staticType,
			boolean unsealed) {
		super(scope, color, staticType, unsealed);
	}

	public Object get(String name, Scriptable start) {
		if (super.has(name, start)) {
			return super.get(name, start);
		} else {
			// Determine conversion type from property name
			Class type = colorPropertyToClass.get(name);
			if (type != null) {
				Color color = ((Color) javaObject).convert(type);
				Scriptable scriptable = Context.toObject(color, start);
				return scriptable.get(name, start);
			}
		}
		return Scriptable.NOT_FOUND;
	}

	public void put(String name, Scriptable start, Object value) {
		if (super.has(name, start)) {
			super.put(name, start, value);
		} else {
			// Determine conversion type from property name
			Class type = colorPropertyToClass.get(name);
			if (type != null) {
				Color fromColor = (Color) javaObject;
				Color toColor = fromColor.convert(type);
				Scriptable scriptable = Context.toObject(toColor, start);
				scriptable.put(name, start, value);
				toColor = toColor.convert(fromColor.getClass());
				fromColor.set(toColor);
			}
		}
	}

}
