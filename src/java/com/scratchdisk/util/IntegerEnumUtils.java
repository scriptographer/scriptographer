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
 * File created on Apr 13, 2008.
 *
 * $Id$
 */

package com.scratchdisk.util;

import java.util.EnumSet;
import java.util.HashMap;

/**
 * @author lehni
 *
 */
public class IntegerEnumUtils {

	protected IntegerEnumUtils() {
	}

	private static HashMap<Class, Lookup> lookups =
			new HashMap<Class, Lookup>();

	@SuppressWarnings("unchecked")
	private static <E extends IntegerEnum> Lookup<E> getLookup(Class<E> cls) {
		Lookup<E> lookup = lookups.get(cls);
		// Create lookup information grouped by class and name / value:
		if (lookup == null) {
			lookup = new Lookup<E>(cls);
			lookups.put(cls, lookup);
		}
		return lookup;
	}

	public static <E extends IntegerEnum> E get(Class<E> cls, Integer value) {
		return getLookup(cls).get(value);
	}

	private static class Lookup<E extends IntegerEnum> {
		HashMap<Integer, E> lookup = new HashMap<Integer, E>();

		Lookup(Class<E> cls) {
			for (E value : cls.getEnumConstants())
				lookup.put(value.value(), value);
		}

		E get(Integer value) {
			return lookup.get(value);
		}
	}

	/**
	 * @param set
	 */
	public static int getFlags(EnumSet<? extends IntegerEnum> set) {
		int flags = 0;
		if (set != null)
			for (Enum e : set)
				flags |= ((IntegerEnum) e).value();
		return flags;
	}

	public static <E extends Enum<E>> EnumSet<E> getSet(Class<E> cls, int flags) {
		EnumSet<E> set = EnumSet.noneOf(cls);
		for (E e : cls.getEnumConstants())
			if ((((IntegerEnum) e).value() & flags) != 0)
				set.add(e);
		return set;
	}
}
