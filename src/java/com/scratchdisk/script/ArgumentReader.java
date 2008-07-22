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
 * File created on Feb 11, 2008.
 *
 * $Id$
 */

package com.scratchdisk.script;

import java.lang.reflect.Constructor;
import java.util.IdentityHashMap;

import com.scratchdisk.util.ClassUtils;
import com.scratchdisk.util.ConversionUtils;

/**
 * @author lehni
 *
 */
public abstract class ArgumentReader {

	protected Converter converter;

	public ArgumentReader(Converter converter) {
		this.converter = converter;
	}

	protected abstract Object readNext(String name);

	public boolean has(String name) {
		return false;
	}

	public int size() {
		return -1;
	}

	public boolean isArray() {
		return false;
	}

	public boolean isString() {
		return false;
	}

	public boolean isHash() {
		return false;
	}

	public void revert() {
		// Do nothing here. For sequentially reading readers go back one,
		// in order to try a different type...
	}

	public Boolean readBoolean(String name) {
		Object obj = readNext(name);
		return obj != null ? new Boolean(ConversionUtils.toBoolean(obj)) : null;
	}

	public boolean readBoolean(String name, boolean defaultValue) {
		Boolean value = readBoolean(name);
		return value != null ? value.booleanValue() : defaultValue;
	}

	public boolean readBoolean(boolean defaultValue) {
		return readBoolean(null, defaultValue);
	}

	public Double readDouble(String name) {
		Object obj = readNext(name);
		return obj != null ? new Double(ConversionUtils.toDouble(obj)) : null;
	}

	public Double readDouble() {
		return readDouble(null);
	}
	
	public double readDouble(String name, double defaultValue) {
		return ConversionUtils.toDouble(readNext(name), defaultValue);
	}

	public double readDouble(double defaultValue) {
		return readDouble(null, defaultValue);
	}

	public Float readFloat(String name) {
		Object obj = readNext(name);
		return obj != null ? new Float(ConversionUtils.toFloat(obj)) : null;
	}

	public Float readFloat() {
		return readFloat(null);
	}

	public float readFloat(String name, float defaultValue) {
		return ConversionUtils.toFloat(readNext(name), defaultValue);
	}

	public float readFloat(float defaultValue) {
		return readFloat(null, defaultValue);
	}

	public Integer readInteger(String name) {
		Object obj = readNext(name);
		return obj != null ? new Integer(ConversionUtils.toInt(obj)) : null;
	}

	public Integer readInteger() {
		return readInteger(null);
	}

	public int readInteger(String name, int defaultValue) {
		return ConversionUtils.toInt(readNext(name), defaultValue);
	}

	public int readInteger(int defaultValue) {
		return readInteger(null, defaultValue);
	}

	public String readString(String name) {
		return readString(name, null);
	}

	public String readString() {
		return readString(null);
	}

	public String readString(String name, String defaultValue) {
		return ConversionUtils.toString(readNext(name), defaultValue);
	}

	protected static IdentityHashMap<Class, ArgumentConverter> converters =
		new IdentityHashMap<Class, ArgumentConverter>(); 

	public Object readObject(String name, Class type) {
		Object obj = readNext(name);
		if (obj != null) {
			ArgumentConverter converter = (ArgumentConverter) converters.get(type);
			Object res;
			if (converter != null) {
				// Make a new ArgumentReader for this object first, using convert:
				ArgumentReader reader = (ArgumentReader) this.converter.convert(obj, ArgumentReader.class);
				if (reader == null)
					throw new IllegalArgumentException("Cannot read from " + obj);
				res = converter.convert(reader, this.converter.unwrap(obj));
			} else {
				try {
	                res = this.converter.convert(obj, type);
				} catch (Exception e) {
					// TODO: report?
					res = null;
				}
			}
			return res;
		}
		return null;
	}

	public Object readObject(Class type) {
		return readObject(null, type);
	}

    public Object readObject(String name) {
    	return readObject(name, Object.class);
    }

    public Object readObject() {
    	return readObject(Object.class);
    }
	
	protected static void registerConverter(Class type, ArgumentConverter converter) {
		converters.put(type, converter);
	}

	public static boolean canConvert(Class to) {
		return ArgumentReader.class.isAssignableFrom(to)
			|| getArgumentReaderConstructor(to) != null
			|| converters.get(to) != null;
	}

	public static Object convert(ArgumentReader reader, Object from, Class to) {
		if (ArgumentReader.class.isAssignableFrom(to)) {
			return reader;
		} else {
			ArgumentConverter converter = (ArgumentConverter) converters.get(to);
			if (converter != null) {
				return converter.convert(reader, from);
			} else {
				Constructor ctor = getArgumentReaderConstructor(to);
				if (ctor != null) {
				    // Create an object using the rgumentReader constructor.
					// Argument readers can either be created from
					// a NativeArray or a Scriptable object
					try {
						return ctor.newInstance(new Object[] { reader });
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}

	/**
	 * Determines whether the class has a constructor taking a single map as
	 * argument or not.
	 * A cache is used to speed up lookup.
	 * 
	 * @param type
	 * @return true if the class has a map constructor, false otherwise.
	 */
	private static Constructor getArgumentReaderConstructor(Class type) {
		return ClassUtils.getConstructor(type,
				new Class[] { ArgumentReader.class },
				argumentReaderConstructors);
	}

    private static IdentityHashMap<Class, Constructor> argumentReaderConstructors =
			new IdentityHashMap<Class, Constructor>();
}
