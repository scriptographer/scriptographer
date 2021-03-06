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
 * File created on Feb 11, 2008.
 */

package com.scratchdisk.script;

import java.lang.reflect.Constructor;
import java.util.IdentityHashMap;

import com.scratchdisk.util.ClassUtils;
import com.scratchdisk.util.ConversionUtils;
import com.scriptographer.script.EnumUtils;

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

	public Object[] keys() {
		return new Object[]{};
	}

	public boolean isArray() {
		return false;
	}

	public boolean isString() {
		return false;
	}

	public boolean isMap() {
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

	protected static void registerConverter(Class type,
			ArgumentConverter converter) {
		converters.put(type, converter);
	}

	@SuppressWarnings("unchecked")
	protected static <T> ArgumentConverter<T> getConverter(Class<T> type) {
		return converters.get(type);
	}

	@SuppressWarnings("unchecked")
	public <T> T readObject(String name, Class<T> type) {
		Object obj = readNext(name);
		if (obj != null) {
			if (type.isInstance(obj))
				return (T) obj;
			ArgumentConverter<T> converter = getConverter(type);
			T res;
			if (converter != null) {
				// Make a new ArgumentReader for this object first,
				// using convert:
				ArgumentReader reader = this.converter.convert(obj,
						ArgumentReader.class);
				if (reader == null)
					throw new IllegalArgumentException("Cannot read from "
							+ obj);
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

	public <T> T readObject(Class<T> type) {
		return readObject(null, type);
	}

	public Object readObject(String name) {
		return readObject(name, Object.class);
	}

	public Object readObject() {
		return readObject(Object.class);
	}

	public <T extends Enum<T>> T readEnum(String name, Class<T> type,
			T defaultValue) {
		T value = EnumUtils.get(type, readString(name));
		return value != null ? value : defaultValue;
	}
	
	public <T extends Enum<T>> T readEnum(String name, Class<T> type) {
		return readEnum(name, type, null);
	}

	public <T extends Enum<T>> T readEnum(Class<T> type) {
		return readEnum(null, type);
	}

	public <T extends Enum<T>> T readEnum(Class<T> type, T defaultValue) {
		return readEnum(null, type, defaultValue);
	}

	public static boolean canConvert(Class to) {
		return ArgumentReader.class.isAssignableFrom(to)
				|| getArgumentReaderConstructor(to) != null
				|| converters.get(to) != null;
	}

	public static Object convert(ArgumentReader reader, Object from, Class<?> to,
			Converter converter) {
		if (ArgumentReader.class.isAssignableFrom(to))
			return reader;
		ArgumentConverter argumentConverter = converters.get(to);
		if (argumentConverter != null) {
			Object result = argumentConverter.convert(reader, from);
			// ArgumentConverter can return another convertible type, to be
			// passed forward to the Converter. This is used e.g. for
			// java.awt.Color <-> com.scriptographer.script.ColorConverter which
			// returns com.scriptographer.ai.Color...
			if (to.isInstance(result))
				return result;
			else if (converter != null)
				return converter.convert(result, to);
		} else {
			Constructor ctor = getArgumentReaderConstructor(to);
			if (ctor != null) {
				// Create an object using the rgumentReader constructor.
				// Argument readers can either be created from a NativeArray or
				// a Scriptable object
				try {
					return ctor.newInstance(new Object[] { reader });
				} catch (Exception e) {
					e.printStackTrace();
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


	public void setProperties(Object object) {
		converter.setProperties(object, this);
	}
}
