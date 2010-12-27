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
 * File created on Mar 1, 2009.
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
		if (super.has(name, start))
			return super.get(name, start);
		// Determine conversion type from property name
		Class type = colorPropertyToClass.get(name);
		if (type != null) {
			Color color = ((Color) javaObject).convert(type);
			if (color != null) {
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
				if (toColor != null) {
	                Scriptable scriptable = Context.toObject(toColor, start);
	                scriptable.put(name, start, value);
	                toColor = toColor.convert(fromColor.getClass());
	                fromColor.set(toColor);
				}
			}
		}
	}

}
