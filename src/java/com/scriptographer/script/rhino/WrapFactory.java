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
 * File created on 02.01.2005.
 *
 * $Id: ScriptographerWrapFactory.java 230 2007-01-16 20:36:33Z lehni $
 */

package com.scriptographer.script.rhino;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Wrapper;

import com.scriptographer.ai.CharacterStyle;
import com.scriptographer.ai.FillStyle;
import com.scriptographer.ai.Matrix;
import com.scriptographer.ai.ParagraphStyle;
import com.scriptographer.ai.PathStyle;
import com.scriptographer.ai.Point;
import com.scriptographer.ai.Rectangle;
import com.scriptographer.ai.SegmentPoint;
import com.scriptographer.ai.StrokeStyle;
import com.scriptographer.util.ReadOnlyList;
import com.scriptographer.util.WeakIdentityHashMap;

/**
 * @author lehni
 */
public class WrapFactory extends org.mozilla.javascript.WrapFactory {
	private WeakIdentityHashMap instances = new WeakIdentityHashMap();
	private HashMap wrappers = new HashMap();
	
	public WrapFactory() {
		// some classes need special wrappers. map these here:
		wrappers.put(SegmentPoint.class, SegmentPointWrapper.class);
		wrappers.put(PathStyle.class, StyleWrapper.class);
		wrappers.put(FillStyle.class, StyleWrapper.class);
		wrappers.put(StrokeStyle.class, StyleWrapper.class);
		wrappers.put(ParagraphStyle.class, StyleWrapper.class);
		wrappers.put(CharacterStyle.class, StyleWrapper.class);
		// Now convert to wrapperClasses to constructors
		for (Iterator it = wrappers.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			Constructor ctor;
			try {
				ctor = ((Class) entry.getValue()).getDeclaredConstructor(
						new Class[] { Scriptable.class, Object.class, Class.class });
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
				ctor = null;
			}
			wrappers.put(entry.getKey(), ctor);
		}
	}

	public Object wrap(Context cx, Scriptable scope, Object obj,
			Class staticType) {
		// these are not wrappers, the java return types are simply converted to
		// these scriptographer types and wrapped afterwards:
		if (obj instanceof java.awt.geom.Rectangle2D
			&& !(obj instanceof Rectangle)) {
			obj = new Rectangle((java.awt.geom.Rectangle2D) obj);
		} else if (obj instanceof java.awt.geom.Point2D
			&& !(obj instanceof Point)) {
			obj = new Point((java.awt.geom.Point2D) obj);
		} else if (obj instanceof java.awt.geom.AffineTransform
			&& !(obj instanceof Matrix)) {
			obj = new Matrix((java.awt.geom.AffineTransform) obj);
		} else if (obj instanceof java.awt.Dimension) {
			// TODO: expose Dimension to JS?
			obj = new Point((java.awt.Dimension) obj);
		}
		return super.wrap(cx, scope, obj, staticType);
	}

	public Scriptable wrapAsJavaObject(Context cx, Scriptable scope,
			Object javaObj, Class staticType) {
		// keep track of wrappers so that if a given object needs to be
		// wrapped again, take the wrapper from the pool...
		// TODO: see wether this really makes sense or wether rewrapping
		// every time is the way to go
		Scriptable obj = (Scriptable) instances.get(javaObj);
		if (obj == null) {
			// See if a special constructor fo that staticType was declared
			// Create the wraper through it if it was, otherwise use the default
			// fallback scenario
			Constructor ctor = (Constructor) wrappers.get(staticType);
			if (ctor != null) {
				try {
					obj = (Scriptable) ctor.newInstance(
							new Object[] { scope, javaObj, staticType });
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (javaObj instanceof ReadOnlyList) {
				obj = new ListWrapper(scope, (ReadOnlyList) javaObj, staticType);
			} else if (javaObj instanceof Map) {
				obj = new MapWrapper(scope, (Map) javaObj, staticType);
			} else {
				// The default for Scriptographer is unsealed
				obj = new UnsealedWrapper(scope, javaObj, staticType);
			}
			instances.put(javaObj, obj);
		}
		return obj;
	}
	
    public Object coerceType(Class type, Object value) {
    	// coerce native objects to maps when needed
    	if (value instanceof NativeObject && Map.class.isAssignableFrom(type)) {
    		return convertToMap((NativeObject) value);
    	}
    	return null;
	}

	public Map convertToMap(NativeObject object) {
		HashMap map = new HashMap();
		Object[] ids = object.getIds();
		for (int i = 0; i < ids.length; i++) {
			Object id = ids[i];
			Object obj = id instanceof String ? object.get((String) id, object)
				: object.get(((Number) id).intValue(), object);
			map.put(id, convertObject(obj));
		}
		return map;
	}

	public Object[] convertToArray(NativeArray array) {
		Object[] objects = new Object[(int) array.getLength()];
		for (int i = 0; i < objects.length; i++)
			objects[i] = convertObject(array.get(i, array));
		return objects;
	}

	public Object convertObject(Object obj) {
		if (obj instanceof Wrapper) {
			return ((Wrapper) obj).unwrap();
		} else if (obj instanceof NativeArray) {
			return convertToArray((NativeArray) obj);
		} else if (obj instanceof NativeObject) {
			return convertToMap((NativeObject) obj);
		}
		return obj;
	}
}