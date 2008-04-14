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
 * File created on Apr 13, 2008.
 *
 * $Id$
 */

package com.scriptographer.script;

import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * @author lehni
 *
 */
public class EnumUtils extends com.scratchdisk.util.EnumUtils {

	protected EnumUtils() {
	}

	private static HashMap<Enum, String> scriptNames =
			new HashMap<Enum, String>();

	public static String getScriptName(Enum e) {
		String name = scriptNames.get(e);
		if (name == null) {
			String parts[] = e.name().toLowerCase().split("_");
			name = parts[0];
			for (int i = 1; i < parts.length; i++) {
				// CSS style:
				name += '-' + parts[i];
				// Camel Case:
				// name += Character.toUpperCase(parts[i].charAt(0)) + parts[i].substring(1);
			}
			scriptNames.put(e, name);
		}
		return name;
	}

	public static Enum get(Class cls, String name) {
		return getLookup(cls).get(name);
	}

	private static HashMap<Class, Lookup> lookups =
		new HashMap<Class, Lookup>();

	private static Lookup getLookup(Class cls) {
		Lookup lookup =	 lookups.get(cls);
		// Create lookup information grouped by class and name / value:
		if (lookup == null) {
			lookup = new Lookup(cls);
			lookups.put(cls, lookup);
		}
		return lookup;
	}

	private static class Lookup {
		HashMap<String, Enum> lookup = new HashMap<String, Enum>();

		Lookup(Class cls) {
			try {
				// Assume it's an enum implementing option and call its status values() method:
				Method getValues = cls.getDeclaredMethod("values", new Class[] {});
				Enum values[] = (Enum[]) getValues.invoke(cls, new Object[] {});
				for (Enum value : values) {
					// put both variants in:
					lookup.put(getScriptName(value), value);
					lookup.put(value.name(), value);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		Enum get(String name) {
			return lookup.get(name);
		}
	}
}
