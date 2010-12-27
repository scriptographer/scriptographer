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
 * File created on Aug 14, 2009.
 */

package com.scratchdisk.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;

/**
 * @author lehni
 *
 */
public class EnumUtils {

	private EnumUtils() {
	}

	/**
	 * An alternative to EnumSet.copyOf, with the addition to filter out null
	 * values.
	 */
	public static <E extends Enum<E>> EnumSet<E> asSet(Collection<E> list) {
		if (list instanceof EnumSet) {
			return ((EnumSet<E>)list).clone();
		} else {
			Iterator<E> it = list.iterator();
			if (it.hasNext()) {
				E first = it.next();
				while (first == null && it.hasNext())
					first = it.next();
				if (first != null) {
					EnumSet<E> result = EnumSet.of(first);
					while (it.hasNext()) {
						E next = it.next();
						if (next != null)
							result.add(next);
					}
					return result;
				}
			}
			return null;
		}
	}

	public static <E extends Enum<E>> EnumSet<E> asSet(E[] array) {
		return array != null ? asSet(Arrays.asList(array)) : null;
	}
}
