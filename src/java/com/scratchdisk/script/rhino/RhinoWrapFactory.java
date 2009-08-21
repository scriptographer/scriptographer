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
 * File created on Apr 10, 2007.
 *
 * $Id$
 */

package com.scratchdisk.script.rhino;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.util.IdentityHashMap;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
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

	public Scriptable wrapCustom(Context cx, Scriptable scope,
			Object javaObj, Class<?> staticType) {
		return null;
	}

	public Object wrap(Context cx, Scriptable scope, Object obj, Class<?> staticType) {
        if (obj == null || obj == Undefined.instance || obj instanceof Scriptable)
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
		return (Scriptable) (obj instanceof Scriptable ? obj :
				wrapAsJavaObject(cx, scope, obj, null));
	}

	public Scriptable wrapAsJavaObject(Context cx, Scriptable scope,
			Object javaObj, Class<?> staticType) {
		// Keep track of wrappers so that if a given object needs to be
		// wrapped again, take the wrapper from the pool...
        WeakReference<Scriptable> ref = wrappers.get(javaObj);
		Scriptable obj = ref == null ? null : ref.get();
		if (obj == null) {
	        // Allays override staticType and set it to the native type
			// of the class. Sometimes the interface used to access an
			// object of a certain class is passed. But why should it
			// be wrapped that way?
			staticType = javaObj.getClass();
			if (staticType != null && staticType.isArray())
				obj = new ExtendedJavaArray(scope, javaObj, staticType, true);
			else {
				if (javaObj instanceof ReadOnlyList) {
					obj = new ListWrapper(scope, (ReadOnlyList) javaObj, staticType, true);
				} else if (javaObj instanceof Map) {
					obj = new MapWrapper(scope, (Map) javaObj, staticType);
				} else {
					obj = wrapCustom(cx, scope, javaObj, staticType);
					if (obj == null)
						obj = new ExtendedJavaObject(scope, javaObj, staticType, true);
				}
			}
			wrappers.put(javaObj, new WeakReference<Scriptable>(obj));
		}
		return obj;
	}

	public int getConversionWeight(Object from, Class<?> to, int defaultWeight) {
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
			if (isNativeObject && Map.class.isAssignableFrom(to)) {
				// If there are two version of a method, e.g. one with Map and the other with EnumMap
				// prefer the more general one:
				if (Map.class.equals(to))
					return CONVERSION_TRIVIAL + 1;
				else
					return CONVERSION_TRIVIAL + 2;
			} else if (!isString) {
				// String and ArgumentReader we tried above already
				if (getZeroArgumentConstructor(to) != null || ArgumentReader.canConvert(to)) {
					if (from instanceof Wrapper)
						from = ((Wrapper) from).unwrap();
					// Now if there are more options here to convert from, e.g. Size and Point
					// prefer the one that has the same simple name, to encourage conversion
					// between ADM and AI Size, Rectangle, Point objects!
					if (from.getClass().getSimpleName().equals(to.getSimpleName()))
						return CONVERSION_TRIVIAL + 1;
					else
						return CONVERSION_TRIVIAL + 2;
				}
			}
		}
		return defaultWeight;
	}

	private ArgumentReader getArgumentReader(Object obj) {
		if (obj instanceof NativeArray) return new ArrayArgumentReader(this, (NativeArray) obj);
		else if (obj instanceof Scriptable) return new HashArgumentReader(this, (Scriptable) obj);
		else if (obj instanceof String) return new StringArgumentReader(this, (String) obj);
		return null;
	}

	public Object coerceType(Class<?> type, Object value) {
		// Coerce native objects to maps when needed
		if (value instanceof Function) {
			if (type == Callable.class)
				return new RhinoCallable(engine, (Function) value);
		} else if (value instanceof Scriptable || value instanceof String) {
			// Let through string as well, for ArgumentReader
			// TODO: Add support for constructor detection that receives the passed value,
			// or can convert to it.
			if (Map.class.isAssignableFrom(type)) {
				return toMap((Scriptable) value);
			} else {
				ArgumentReader reader = null;
				if (ArgumentReader.canConvert(type) && (reader = getArgumentReader(value)) != null) {
				    return ArgumentReader.convert(reader, unwrap(value), type);
				} else if (value instanceof NativeObject && getZeroArgumentConstructor(type) != null) {
					// Try constructing an object of class type, through
					// the JS ExtendedJavaClass constructor that takes 
					// a last optional argument: A NativeObject of which
					// the fields define the fields to be set in the native type.
					Scriptable scope = ((RhinoEngine) this.engine).getScope();
					ExtendedJavaClass cls =
							ExtendedJavaClass.getClassWrapper(scope, type);
					if (cls != null) {
						Object obj = cls.construct(Context.getCurrentContext(),
								scope, new Object[] { value });
						if (obj instanceof Wrapper)
							obj = ((Wrapper) obj).unwrap();
						return obj;
					}
				}
			}
		} else if (value == Undefined.instance) {
			// Convert undefined to false if destination is boolean
			if (type == Boolean.TYPE)
				return Boolean.FALSE;
		} else if (value instanceof Boolean) {
			// Convert false to null / undefined for non primitive destination classes.
			if (!((Boolean) value).booleanValue() && !type.isPrimitive())
				return Undefined.instance;
		}
		return null;
	}

	public Object convert(Object from, Class<?> to) {
		return Context.jsToJava(from, to);
	}

	public Object unwrap(Object obj) {
		if (obj instanceof Wrapper)
			return ((Wrapper) obj).unwrap();
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
		return ClassUtils.getConstructor(cls, new Class[] { }, zeroArgumentConstructors);
	}

    private static IdentityHashMap<Class, Constructor> zeroArgumentConstructors = new IdentityHashMap<Class, Constructor>();
}

