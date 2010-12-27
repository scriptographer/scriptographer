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
 * File created on Apr 13, 2008.
 */

package com.scriptographer.script;

import java.util.HashMap;

/**
 * @author lehni
 *
 */
public class EnumUtils {

	protected EnumUtils() {
	}

	private static HashMap<Enum, String> scriptNames =
			new HashMap<Enum, String>();

	public static String getScriptName(String name) {
		String parts[] = name.toLowerCase().split("_");
		name = parts[0];
		for (int i = 1; i < parts.length; i++) {
			// CSS style:
			name += '-' + parts[i];
			// Camel Case:
			// name += Character.toUpperCase(parts[i].charAt(0)) + parts[i].substring(1);
		}
		return name;
	}

	public static String getScriptName(Enum e) {
		String name = scriptNames.get(e);
		if (name == null) {
			name = getScriptName(e.name());
			scriptNames.put(e, name);
		}
		return name;
	}

	public static <T extends Enum<T>> T get(Class<T> cls, String name) {
		return getLookup(cls).get(name);
	}

	private static HashMap<Class, Lookup> lookups =
		new HashMap<Class, Lookup>();

	@SuppressWarnings("unchecked")
	private static <T extends Enum<T>> Lookup<T> getLookup(Class<T> cls) {
		Lookup<T> lookup = lookups.get(cls);
		// Create lookup information grouped by class and name / value:
		if (lookup == null) {
			lookup = new Lookup<T>(cls);
			lookups.put(cls, lookup);
		}
		return lookup;
	}

	private static class Lookup<T extends Enum<T>> {
		HashMap<String, T> lookup = new HashMap<String, T>();

		Lookup(Class<T> cls) {
			try {
				T values[] = cls.getEnumConstants();
				for (T value : values) {
					// Put both variants (Java name and script name) in:
					lookup.put(getScriptName(value), value);
					lookup.put(value.name(), value);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		T get(String name) {
			return lookup.get(name);
		}
	}
}
