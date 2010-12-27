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
 * File created on Feb 12, 2008.
 */

package com.scriptographer.script;

import java.lang.reflect.Field;

import com.scratchdisk.script.ArgumentConverter;
import com.scratchdisk.script.ArgumentReader;
import com.scriptographer.ai.CMYKColor;
import com.scriptographer.ai.Color;
import com.scriptographer.ai.GrayColor;
import com.scriptographer.ai.RGBColor;

/**
 * @author lehni
 *
 */
public class ColorConverter extends ArgumentConverter<Color> {

	public Color convert(ArgumentReader reader, Object from) {
		// TODO: gradient & pattern color
		// Since StringArgumentReaders also return true for isArray (they
		// can behave like arrays as well), always check for isString first!
		if (reader.isString()) {
			String name = reader.readString();
			if ("".equals(name))
				return Color.NONE;
			try {
				// Try hex string first
				String str = name.startsWith("#") ? name : "#" + name;
				return new RGBColor(java.awt.Color.decode(str));
			} catch (Exception e1) {
				try {
					// If that does not work, try accessing the static Color.NAME field
					Field field = java.awt.Color.class.getField(name.toUpperCase());
					return new RGBColor((java.awt.Color) field.get(java.awt.Color.class));
				} catch (Exception e2) {
				}
			}
		} else if (reader.isArray()) {
			int size = reader.size();
			if (size == 4) {
				// CMYK
				return new CMYKColor(
						reader.readFloat(0),
						reader.readFloat(0),
						reader.readFloat(0),
						reader.readFloat(0)
				);
			} else  if (size == 3) {
				// RGB
				return new RGBColor(
						reader.readFloat(0),
						reader.readFloat(0),
						reader.readFloat(0)
				);
			} else  if (size == 1) {
				// Gray
				return new GrayColor(
						reader.readFloat(0)
				);
			}
		} else if (reader.isMap()) {
			if (reader.has("red")) {
				return new RGBColor(
						reader.readFloat("red", 0),
						reader.readFloat("green", 0),
						reader.readFloat("blue", 0),
						reader.readFloat("alpha", 1)
				);
			} else if (reader.has("cyan")) {
				return new CMYKColor(
						reader.readFloat("cyan", 0),
						reader.readFloat("magenta", 0),
						reader.readFloat("yellow", 0),
						reader.readFloat("black", 0),
						reader.readFloat("alpha", 1)
				);
			} else if (reader.has("gray")) {
				return new GrayColor(
						reader.readFloat("gray", 0),
						reader.readFloat("alpha", 1)
				);
			}
		}
		return null;
	}
}
