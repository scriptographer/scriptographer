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
 * File created on Feb 12, 2008.
 *
 * $Id$
 */

package com.scriptographer.script;

import com.scratchdisk.script.ArgumentConverter;
import com.scratchdisk.script.ArgumentReader;
import com.scriptographer.ai.CMYKColor;
import com.scriptographer.ai.GrayColor;
import com.scriptographer.ai.RGBColor;

/**
 * @author lehni
 *
 */
public class ColorConverter extends ArgumentConverter {

	public Object convert(ArgumentReader reader, Object from) {
		// TODO: gradient & pattern color
		// Since StringArgumentReaders also return true for isArray (they
		// can behave like arrays as well), always check for isString first!
		if (reader.isString()) {
			String str = reader.readString();
			if (!str.startsWith("#"))
				str = '#' + str;
			try {
				return new RGBColor(java.awt.Color.decode(str));
			} catch (Exception e) {
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
		} else if (reader.isHash()) {
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
