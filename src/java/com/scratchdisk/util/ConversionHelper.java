/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2007 Juerg Lehni, http://www.scratchdisk.com.
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
 * File created on May 23, 2007.
 *
 * $Id: $
 */

package com.scratchdisk.util;

import java.util.Map;

/**
 * @author lehni
 *
 */
public class ConversionHelper {
	private ConversionHelper() {
	}

	/*
	 * Helpers, similar to rhino's ScriptRuntime.to* methods,
	 * but language independent. Used mostly for handling values
	 * returned by callables.
	 */
	public static double toDouble(Object val) {
		if (val instanceof Number)
			return ((Number) val).doubleValue();
		if (val == null)
			return +0.0;
		if (val instanceof String)
            return Double.valueOf((String) val).doubleValue();
		if (val instanceof Boolean)
			return ((Boolean) val).booleanValue() ? 1 : +0.0;
		return Double.NaN;
	}

	public static int toInt(Object val) {
        if (val instanceof Integer)
            return ((Integer) val).intValue();
        else return (int) Math.round(toDouble(val));
	}

	public static boolean toBoolean(Object val) {
        if (val instanceof Boolean)
            return ((Boolean) val).booleanValue();
        if (val == null)
            return false;
        if (val instanceof String)
            return ((String) val).length() != 0;
        if (val instanceof Number) {
            double d = ((Number) val).doubleValue();
            return (d == d && d != 0.0);
        }
        return true;
	}

	public static String toString(Object val) {
		return val != null ? val.toString() : null;
	}
	
	/*
	 * Helpers to retrieve values from maps. Used by native
	 * constructors that take a map argument.
	 * TODO: move to a helper?
	 */
	public static double getDouble(Map map, Object key) {
		return toDouble(map.get(key));
	}

	public static int getInt(Map map, Object key) {
		return toInt(map.get(key));
	}

	public static boolean getBoolean(Map map, Object key) {
		return toBoolean(map.get(key));
	}

	public static String getString(Map map, Object key) {
		return toString(map.get(key));
	}
}
