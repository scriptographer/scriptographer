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
