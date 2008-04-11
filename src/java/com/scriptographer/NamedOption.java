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
 * File created on Apr 11, 2008.
 *
 * $Id$
 */

package com.scriptographer;

import java.util.HashMap;

import com.scratchdisk.list.ArrayList;

/**
 * A base class for all scriptable classes that have options / styles / flags.
 * The convention in JS is to not use Java Style ClassName.SOME_CONSTANT but
 * simple strings. In order to still be able to use the Java convention in the
 * API as well, this class was introduced. 
 * Basically a NamedOption provides both a numeric value and a string name for
 * any given option.
 * 
 * The Rhino layer then takes care of the conversion back and forth between the two.
 * 
 * @author lehni
 */
public abstract class NamedOption {
	final public String name;
	final public int value;

	public NamedOption(String name, int value) {
		this.name = name;
		this.value = value;
		Lookup lookup = (Lookup) lookups.get(getClass());
		// Create lookup information grouped by class and name / value:
		if (lookup == null) {
			lookup = new Lookup();
			lookups.put(getClass(), lookup);
		}
		lookup.add(this);
	}

	public Integer toInteger() {
		return new Integer(value);
	}

	public String toString() {
		return name;
	}

	private static HashMap lookups = new HashMap();

	public static NamedOption get(Class cls, Object key) {
		if (key == null)
			return null;
		// Walk up the inheritance chain as well, since NamedOptions
		// may inherit from others:
		do {
			Lookup lookup = (Lookup) lookups.get(cls);
			NamedOption option = lookup.get(key);
			if (option != null)
				return option;
			cls = cls.getSuperclass();
		} while (!cls.equals(NamedOption.class));
		return null;
	}

	public static NamedOption get(Class cls, int value) {
		return get(cls, new Integer(value));
	}

	public static NamedOption[] getAll(Class cls) {
		ArrayList options = new ArrayList();
		// Walk up the inheritance chain as well, since NamedOptions
		// may inherit from others:
		do {
			Lookup lookup = (Lookup) lookups.get(cls);
			options.addAll(0, lookup.options);
			cls = cls.getSuperclass();
		} while (!cls.equals(NamedOption.class));
		return (NamedOption[]) options.toArray(new NamedOption[options.size()]);
	}

	static class Lookup {
		HashMap lookup = new HashMap();
		ArrayList options = new ArrayList();

		void add(NamedOption option) {
			lookup.put(new Integer(option.value), option);
			lookup.put(option.name, option);
			options.add(option);
		}

		NamedOption get(Object key) {
			return (NamedOption) lookup.get(key);
		}
	}
}