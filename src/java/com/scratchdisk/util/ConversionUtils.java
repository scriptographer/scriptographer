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
 * File created on May 23, 2007.
 *
 * $Id$
 */

package com.scratchdisk.util;

import java.util.Map;

/**
 * @author lehni
 *
 */
public class ConversionUtils {
	private ConversionUtils() {
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

	public static double toDouble(Object val, double defaultValue) {
		double value = toDouble(val);
		return value == Double.NaN ? defaultValue : value;
	}
	
	public static float toFloat(Object val) {
		return (float) toDouble(val);
	}

	public static float toFloat(Object val, float defaultValue) {
		float value = toFloat(val);
		return value == Float.NaN ? defaultValue : value;
	}

	public static int toInt(Object val) {
		if (val instanceof Integer)
			return ((Integer) val).intValue();
		else return (int) Math.round(toDouble(val));
	}

	public static int toInt(Object val, int defaultValue) {
		if (val instanceof Integer)
			return ((Integer) val).intValue();
		else return (int) Math.round(toDouble(val, defaultValue));
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
			return d != 0.0 && !Double.isNaN(d);
		}
		return true;
	}

	public static String toString(Object val) {
		return toString(val, null);
	}

	public static String toString(Object val, String defaultValue) {
		return val != null ? val.toString() : defaultValue;
	}

	public static boolean equals(Object x, Object y) {
		if (x == null) {
			return y == null;
		} else if (x instanceof Number) {
			return equals(((Number)x).doubleValue(), y);
		} else if (x instanceof String) {
			return equals((String)x, y);
		} else if (x instanceof Boolean) {
			boolean b = ((Boolean)x).booleanValue();
			if (y instanceof Boolean)
				return b == ((Boolean)y).booleanValue();
			return equals(b ? 1.0 : 0.0, y);
		}
		return false;
	}

	static boolean equals(double x, Object y) {
		if (y == null) {
			return false;
		} else if (y instanceof Number) {
			return x == ((Number)y).doubleValue();
		} else if (y instanceof String) {
			return x == toDouble(y);
		} else if (y instanceof Boolean) {
			return x == (((Boolean)y).booleanValue() ? 1.0 : +0.0);
		}
		return false;
	}

	private static boolean equals(String x, Object y) {
		if (y == null) {
			return false;
		} else if (y instanceof String) {
			return x.equals(y);
		} else if (y instanceof Number) {
			return toDouble(x) == ((Number)y).doubleValue();
		} else if (y instanceof Boolean) {
			return toDouble(x) == (((Boolean)y).booleanValue() ? 1.0 : 0.0);
		}
		return false;
	}
 
	/*
	 * Helpers to retrieve values from maps. Used by native
	 * constructors that take a map argument.
	 */
	public static double getDouble(Map map, Object key) {
		return toDouble(map.get(key));
	}

	public static float getFloat(Map map, Object key) {
		return toFloat(map.get(key));
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
