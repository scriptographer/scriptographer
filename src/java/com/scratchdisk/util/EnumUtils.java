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
 * File created on Aug 14, 2009.
 *
 * $Id$
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

	protected EnumUtils() {
	}

	/**
	 * An alternative to EnumSet.copyOf, with the addition to filter out null
	 * values.
	 */
	public static <E extends Enum<E>> EnumSet<E> asSet(Collection<E> list) {
		if (list instanceof EnumSet) {
			return ((EnumSet<E>)list).clone();
		} else {
			Iterator<E> i = list.iterator();
			E first = i.next();
			while (first == null)
				first = i.next();
			EnumSet<E> result = EnumSet.of(first);
			while (i.hasNext()) {
				E next = i.next();
				if (next != null)
					result.add(next);
			}
			return result;
		}
	}

	public static <E extends Enum<E>> EnumSet<E> asSet(E[] array) {
		return asSet(Arrays.asList(array));
	}
}
