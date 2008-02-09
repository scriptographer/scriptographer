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
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.WrapFactory;
import org.mozilla.javascript.Wrapper;

import com.scratchdisk.list.ReadOnlyList;
import com.scratchdisk.script.Callable;
import com.scratchdisk.util.ClassUtils;
import com.scratchdisk.util.WeakIdentityHashMap;

/**
 * @author lehni
 */
public class RhinoWrapFactory extends WrapFactory {
	private WeakIdentityHashMap wrappers = new WeakIdentityHashMap();
	protected RhinoEngine engine;

	public RhinoWrapFactory() {
		this.setJavaPrimitiveWrap(false);
	}

	public Scriptable wrapCustom(Context cx, Scriptable scope,
			Object javaObj, Class staticType) {
		return null;
	}

	public Object wrap(Context cx, Scriptable scope, Object obj, Class staticType) {
        if (obj == null || obj == Undefined.instance || obj instanceof Scriptable)
            return obj;
        // Allways override staticType and set itto the native type of
		// the class. Sometimes the interface used to acces an object of
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
			Object javaObj, Class staticType) {
		// Keep track of wrappers so that if a given object needs to be
		// wrapped again, take the wrapper from the pool...
        WeakReference ref = (WeakReference) wrappers.get(javaObj);
		Scriptable obj = ref == null ? null : (Scriptable) ref.get();
		if (obj == null) {
	        // Allways override staticType and set it to the native type
			// of the class. Sometimes the interface used to acces an
			// object of a certain class is passed. But why should it
			// be wrapped that way?
			staticType = javaObj.getClass();
			if (staticType != null && staticType.isArray())
				obj = new ExtendedJavaArray(scope, javaObj, staticType, true);
			else {
				if (javaObj instanceof ReadOnlyList)
					obj = new ListWrapper(scope, (ReadOnlyList) javaObj, staticType, true);
				else if (javaObj instanceof Map)
					obj = new MapWrapper(scope, (Map) javaObj, staticType);
				else {
					obj = wrapCustom(cx, scope, javaObj, staticType);
					if (obj == null)
						obj = new ExtendedJavaObject(scope, javaObj, staticType, true);
				}
			}
			wrappers.put(javaObj, new WeakReference(obj));
		}
		return obj;
	}

	public int getConversionWeight(Object from, Class to, int defaultValue) {
		// See if object "from" can be converted to an instance of class "to"
		// by the use of a map constructor or the setting of all the fields
		// of a NativeObject on the instance after its creation,
		// all added features of JS in Scriptographer:
		if (from instanceof Scriptable
				&& (getMapConstructor(to) != null || from instanceof NativeObject
						&& (Map.class.isAssignableFrom(to) || getZeroArgumentConstructor(to) != null))) {
			if (from instanceof Wrapper)
				from = ((Wrapper) from).unwrap();
			// Now if there are more options here to convert from, e.g. Size and Point
			// prefer the one that has the same simple name, to encourage conversion
			// between ADM and AI Size, Rectangle, Point objects!
			if (ClassUtils.getSimpleName(from.getClass()).equals(ClassUtils.getSimpleName(to)))
				return CONVERSION_TRIVIAL + 1;
			else
				return CONVERSION_TRIVIAL + 2;
		}
		return defaultValue;
	}

	public Object convert(Object from, Class to) {
		// Coerce native objects to maps when needed
		if (from instanceof Function && to == Callable.class) {
			return new RhinoCallable(engine, (Function) from);
		} else if (from instanceof Scriptable) {
			if (Map.class.isAssignableFrom(to)) {
				return toMap((Scriptable) from);
			} else {
				Constructor ctor = getMapConstructor(to);
				if (ctor != null) {
					try {
						return ctor.newInstance(new Object[] { toMap((Scriptable) from) });
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				} else if (from instanceof NativeObject
						&& getZeroArgumentConstructor(to) != null) {
					// Try constructing an object of class type, through
					// the JS ExtendedJavaClass constructor that takes 
					// a last optional argument: A NativeObject of which
					// the fields define the fields to be set in the native type.
					Scriptable scope = ((RhinoEngine) this.engine).getScope();
					ExtendedJavaClass cls =
							ExtendedJavaClass.getClassWrapper(scope, to);
					if (cls != null) {
						Object obj = cls.construct(Context.getCurrentContext(),
								scope, new Object[] { from });
						if (obj instanceof Wrapper)
							obj = ((Wrapper) obj).unwrap();
						return obj;
					}
				}
			}
		} 
		return null;
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

	private static IdentityHashMap zeroArgConstructors = new IdentityHashMap();
	private static IdentityHashMap mapConstructors = new IdentityHashMap();

	/**
	 * Determines wether the class has a zero argument constructor or not.
	 * A cache is used to speed up lookup.
	 * 
	 * @param cls
	 * @return true if the class has a zero argument constructor, false
	 *         otherwise.
	 */
	private static Constructor getZeroArgumentConstructor(Class cls) {
		return getConstructor(zeroArgConstructors, cls, new Class[] { });
	}


	/**
	 * Determines wether the class has a constructor taking a single map as
	 * argument or not.
	 * A cache is used to speed up lookup.
	 * 
	 * @param cls
	 * @return true if the class has a map constructor, false otherwise.
	 */
	private static Constructor getMapConstructor(Class cls) {
		return getConstructor(mapConstructors, cls, new Class[] { Map.class });
	}

	private static Constructor getConstructor(IdentityHashMap cache, Class cls, Class[] args) {
		Constructor ctor = (Constructor) cache.get(cls);
		if (ctor == null) {
			try {
				ctor = cls.getConstructor(args);
				cache.put(cls, ctor);
			} catch (Exception e) {
			}
		}
		return ctor;
	}
}

