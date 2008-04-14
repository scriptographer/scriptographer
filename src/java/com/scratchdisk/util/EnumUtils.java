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
 * File created on Apr 14, 2008.
 *
 * $Id$
 */

package com.scratchdisk.util;

import java.lang.reflect.Method;

/**
 * @author lehni
 *
 */
public class EnumUtils {
	protected EnumUtils() {
	}
	
	@SuppressWarnings("unchecked")
	public static <T/* extends Enum<T>*/> T[] getValues(Class<T> cls) {
		try {
			// Assume it's an enum implementing option and call its status values() method:
			Method getValues = cls.getDeclaredMethod("values", new Class[] {});
			return (T[]) getValues.invoke(cls, new Object[] {});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
