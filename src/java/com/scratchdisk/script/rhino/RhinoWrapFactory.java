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
 * File created on Apr 10, 2007.
 *
 * $Id$
 */

package com.scratchdisk.script.rhino;

import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.IdentityHashMap;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.WrapFactory;
import org.mozilla.javascript.Wrapper;

import com.scratchdisk.list.ReadOnlyList;
import com.scratchdisk.script.ArgumentReader;
import com.scratchdisk.script.Callable;
import com.scratchdisk.script.Converter;
import com.scratchdisk.script.StringArgumentReader;
import com.scratchdisk.util.ClassUtils;
import com.scratchdisk.util.WeakIdentityHashMap;

/**
 * @author lehni
 */
public class RhinoWrapFactory extends WrapFactory implements Converter {
	private WeakIdentityHashMap<Object, WeakReference<Scriptable>> wrappers =
		new WeakIdentityHashMap<Object, WeakReference<Scriptable>>();

	protected RhinoEngine engine;

	public RhinoWrapFactory() {
		this.setJavaPrimitiveWrap(false);
	}

	/**
	 * wrapCustom should wrap all objects that it would like to be cached in 
	 * the wrappers WeakIdentityHashMap. If it returns null, a temporary
	 * ExtendedJavaObject wrapper is created which is not cached.
	 * This is used for example to allow the definition of JS wrappers for File,
	 * which in itself then explicitly create java.io.File objects for the
	 * same file, which would otherwise then already be in wrappers and returned.
	 */
	public Scriptable wrapCustom(Context cx, Scriptable scope,
			Object javaObj, Class<?> staticType, boolean newObject) {
		return new ExtendedJavaObject(scope, javaObj, staticType, true);
	}

	public Object wrap(Context cx, Scriptable scope, Object obj,
			Class<?> staticType) {
		if (obj == null || obj == Undefined.instance
				|| obj instanceof Scriptable)
			return obj;
		if (obj instanceof RhinoCallable) {
			// Handle the ScriptFunction special case, return the unboxed
			// function value.
			obj = ((RhinoCallable) obj).getCallable();
		}
		// Allays override staticType and set it to the native type of
		// the class. Sometimes the interface used to access an object of
		// a certain class is passed.
		// But why should it be wrapped that way?
		if (staticType == null || !staticType.isPrimitive())
			staticType = obj.getClass();
		Object result = staticType != null && staticType.isArray() ?
				new ExtendedJavaArray(scope, obj, staticType, true) :
				super.wrap(cx, scope, obj, staticType);
		return result;
	}

	public Scriptable wrapNewObject(Context cx, Scriptable scope, Object obj) {
		if (obj instanceof Scriptable)
			return (Scriptable) obj;
		// TODO: Pass as boolean variable instead and change Rhino further
		cx.putThreadLocal("newObject", true);
		try {
			return wrapAsJavaObject(cx, scope, obj, null, true);
		} finally {
			cx.removeThreadLocal("newObject");
		}
	}

	public Scriptable wrapAsJavaObject(Context cx, Scriptable scope,
			Object javaObj, Class<?> staticType, boolean newObject) {
		// Keep track of wrappers so that if a given object needs to be
		// wrapped again, take the wrapper from the pool...
		WeakReference<Scriptable> ref = wrappers.get(javaObj);
		Scriptable obj = ref == null ? null : ref.get();
		if (obj == null) {
			boolean cache = true;
			// Allays override staticType and set it to the native type
			// of the class. Sometimes the interface used to access an
			// object of a certain class is passed. But why should it
			// be wrapped that way?
			staticType = javaObj.getClass();
			if (staticType != null && staticType.isArray())
				obj = new ExtendedJavaArray(scope, javaObj, staticType, true);
			else {
				if (javaObj instanceof ReadOnlyList) {
					obj = new ListWrapper(scope, (ReadOnlyList) javaObj,
							staticType, true);
				} else if (javaObj instanceof Map) {
					obj = new MapWrapper(scope, (Map) javaObj);
				} else {
					obj = wrapCustom(cx, scope, javaObj, staticType, newObject);
					if (obj == null) {
						obj = new ExtendedJavaObject(scope, javaObj, staticType,
								true);
						// See the comment in wrapCustom for an explanation of
						// this:
						cache = false;
					}
				}
			}
			if (cache)
				wrappers.put(javaObj, new WeakReference<Scriptable>(obj));
		}
		return obj;
	}

	private IdentityHashMap<Class, IdentityHashMap<Class, Integer>> conversionCache =
			new IdentityHashMap<Class, IdentityHashMap<Class, Integer>>();

	/**
	 * getConversionWeight is defined here to only calculate the weight per from
	 * - to - class pair once, after that it is cached in the conversionTable
	 * and retrieved from there. calculateConversionWeight is used instead for
	 * the calculations.
	 */
	public int getConversionWeight(Object from, Object unwrapped, Class<?> to,
	        int defaultWeight) {
		Class fromClass = unwrapped.getClass();
		IdentityHashMap<Class, Integer> fromCache =
				conversionCache.get(fromClass);
		if (fromCache == null) {
			fromCache = new IdentityHashMap<Class, Integer>();
			conversionCache.put(fromClass, fromCache);
		}
		Integer res = fromCache.get(to);
		if (res != null)
			return res;
		int weight = calculateConversionWeight(from, unwrapped, to,
				defaultWeight);
		fromCache.put(to, weight);
		return weight;
	}

	/**
	 * getConversionWeight above is defined to call calculateConversionWeight
	 * and cache the results. Do not override getConversionWeight in any
	 * subclasses, override calculateConversionWeight instead.
	 */
	public int calculateConversionWeight(Object from, Object unwrapped,
	        Class<?> to, int defaultWeight) {
		// See if object "from" can be converted to an instance of class "to"
		// by the use of a map constructor or the setting of all the fields
		// of a NativeObject on the instance after its creation,
		// all added features of JS in Scriptographer:
		boolean isString = false;
		// Let through string as well, for ArgumentReader
		if (from instanceof Scriptable || (isString = from instanceof String)) {
			// The preferred conversion is from a native object / array to
			// a class that supports an ArgumentReader constructor.
			// Everything else is less preferred (even conversion using
			// the same constructor and another Scriptable object, e.g.
			// a wrapped Java object).
			boolean isNativeObject = from instanceof NativeObject;
			if (isNativeObject || from instanceof NativeArray || isString) {
				if (ArgumentReader.class.isAssignableFrom(to))
					return CONVERSION_TRIVIAL + 1;
				else if (ArgumentReader.canConvert(to))
					return CONVERSION_TRIVIAL + 2;
			}
			if (isNativeObject) {
				if (Map.class.isAssignableFrom(to)) {
					// If there are two version of a method, e.g. one with Map
					// and the other with EnumMap prefer the more general one:
					if (Map.class.equals(to))
						return CONVERSION_TRIVIAL + 1;
					else
						return CONVERSION_TRIVIAL + 2;
				}
				// Try and see if unwrapping NativeObjects through JS unwrap
				// method brings us to the right type.
				unwrapped = unwrap(from);
				// TODO: Should this be run through calculateConversionWeight
				// again?
				// TODO: The result of this should not be cached under the
				// wrapper type as it will permanently link NativeObject to the
				// result. This should be achieved by never caching results when
				// from is a NativeObject.
				if (unwrapped != from && to.isInstance(unwrapped))
					return CONVERSION_TRIVIAL;
			} else if (!isString) {
				// String and ArgumentReader we tried above already
				if (getZeroArgumentConstructor(to) != null
						|| ArgumentReader.canConvert(to)) {
					// Now if there are more options here to convert from, e.g.
					// Size and Point prefer the one that has the same simple
					// name, to encourage conversion between ADM and AI Size,
					// Rectangle, Point objects!
					if (unwrapped.getClass().getSimpleName().equals(
							to.getSimpleName()))
						return CONVERSION_TRIVIAL + 1;
					else
						return CONVERSION_TRIVIAL + 2;
				}
			}
		}
		return defaultWeight;
	}

	protected ArgumentReader getArgumentReader(Object object) {
		if (object instanceof NativeArray)
			return new ArrayArgumentReader(this, (NativeArray) object);
		else if (object instanceof Scriptable)
			return new MapArgumentReader(this, (Scriptable) object);
		else if (object instanceof String)
			return new StringArgumentReader(this, (String) object);
		return null;
	}

	public void setProperties(Object object, ArgumentReader reader) {
		// Similar to ExtendedJavaClass#setProperties, but we need to wrap the
		// result in a Scriptable object so we can rely on Rhino to set all
		// properties on it. It will automatically find the right setters for us
		// and use all value conversion mechanisms available.
		Scriptable scriptable = wrapNewObject(
				Context.getCurrentContext(),
				engine.getScope(), object);
		for (Object id : reader.keys())
			scriptable.put((String) id, scriptable, reader.readObject(
					id.toString()));
	}

	public Object coerceType(Class<?> type, Object value, Object unwrapped) {
		// Coerce native objects to maps when needed
		if (value instanceof Function) {
			if (type == Callable.class)
				return new RhinoCallable(engine, (Function) value);
		} else if (value instanceof Scriptable || value instanceof String) {
			// Let through string as well, for ArgumentReader
			// TODO: Add support for constructor detection that receives the
			// passed value, or can convert to it.
			if (Map.class.isAssignableFrom(type)) {
				return toMap((Scriptable) value);
			} else {
				// Try and see if unwrapping NativeObjects through JS unwrap
				// method brings us to the right type.
				boolean isNativeObject = value instanceof NativeObject;
				if (isNativeObject) {
					unwrapped = unwrap(value);
					if (unwrapped != value && type.isInstance(unwrapped))
						return unwrapped;
				}
				ArgumentReader reader = null;
				if (ArgumentReader.canConvert(type)
						&& (reader = getArgumentReader(value)) != null) {
					return ArgumentReader.convert(reader, unwrapped, type, this);
				} else if (isNativeObject) {
					Constructor ctor = getZeroArgumentConstructor(type);
					if (ctor != null) {
						try {
							Object result = ctor.newInstance();
							// As a conversion, use setProperties through
							// argument reader to set all values on the newly
							// created object.
							setProperties(result, getArgumentReader(value));
							return result;
						} catch (Exception e) {
				            throw Context.throwAsScriptRuntimeEx(e);
						}
					}
				}
			}
		} else if (value == Undefined.instance) {
			// Convert undefined to false if destination is boolean
			if (type == Boolean.TYPE)
				return Boolean.FALSE;
		} else if (value instanceof Boolean) {
			// Convert false to null / undefined for non primitive destination
			// classes.
			if (!((Boolean) value).booleanValue() && !type.isPrimitive())
				return Undefined.instance;
		} else if (type.isArray() && value instanceof ReadOnlyList) {
			Class componentType = type.getComponentType();
			ReadOnlyList list = (ReadOnlyList) value;
			int size = list.size();
			Object array = Array.newInstance(componentType, size);
			for (int i = 0; i < size; i++) {
				Object entry = Context.jsToJava(list.get(i), componentType);
				Array.set(array, i, entry);
			}
			return array;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public <T> T convert(Object from, Class<T> to) {
		return (T) Context.jsToJava(from, to);
	}

	public Object unwrap(Object obj) {
		if (obj instanceof Wrapper) {
			return ((Wrapper) obj).unwrap();
		} else if (obj instanceof NativeObject) {
			// Allow JS objects to define a unwrap method:
			NativeObject object = (NativeObject) obj;
			Object unwrap = ScriptableObject.getProperty(object, "unwrap");
			if (unwrap != Scriptable.NOT_FOUND
					&& unwrap instanceof org.mozilla.javascript.Callable) {
				obj = ((org.mozilla.javascript.Callable) unwrap).call(
						Context.getCurrentContext(),
						engine.topLevel, object, ScriptRuntime.emptyArgs);
				if (obj != object)
					return unwrap(obj);
			}
		}
		return obj;
	}

	/**
	 * Takes a scriptable and either wraps it in a MapAdapter or unwraps a map
	 * within it if it is a MapWrapper. This avoids multiple wrapping of
	 * MapWrappers and MapAdapters
	 * 
	 * @param scriptable
	 * @return a map object representing the passed scriptable.
	 */
	private Map toMap(Scriptable scriptable) {
		if (scriptable instanceof MapWrapper)
			return (Map) ((MapWrapper) scriptable).unwrap();
		return new MapAdapter(scriptable);
	}

	/**
	 * Determines whether the class has a zero argument constructor or not.
	 * A cache is used to speed up lookup.
	 * 
	 * @param cls
	 * @return true if the class has a zero argument constructor, false
	 *         otherwise.
	 */
	private static Constructor getZeroArgumentConstructor(Class<?> cls) {
		return ClassUtils.getConstructor(cls, new Class[] { },
				zeroArgumentConstructors);
	}

	private static IdentityHashMap<Class, Constructor> zeroArgumentConstructors =
			new IdentityHashMap<Class, Constructor>();
}

