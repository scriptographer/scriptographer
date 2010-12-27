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
 * File created on Apr 10, 2007.
 */

package com.scriptographer.script.rhino;

import java.util.IdentityHashMap;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.MemberBox;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import com.scratchdisk.script.rhino.ExtendedJavaObject;
import com.scriptographer.ai.Color;
import com.scriptographer.ai.RGBColor;
import com.scriptographer.ai.Style;
import com.scriptographer.script.EnumUtils;

/**
 * @author lehni
 *
 */
public class RhinoWrapFactory extends
		com.scratchdisk.script.rhino.RhinoWrapFactory {

	public Object wrap(Context cx, Scriptable scope, Object obj,
			Class<?> staticType) {
		// By default, Rhino converts chars to integers. In Scriptographer, we
		// want a string of length 1:
 	   if (staticType == Character.TYPE)
			return obj.toString();
		else if (obj instanceof Enum)
			// TODO: Could this be moved to wrapCustom?
			return EnumUtils.getScriptName((Enum) obj);
		return super.wrap(cx, scope, obj, staticType);
	}

	IdentityHashMap<Class, Function> mappedJavaClasses =
			new IdentityHashMap<Class, Function>();
	
	/**
	 * Maps a Java class to a JavaScript prototype, so this can be used instead
	 * for wrapping of returned java types. So far this is only used for
	 * java.io.File in Scriptographer.
	 */
	public void mapJavaClass(Class cls, Function ctor) {
		mappedJavaClasses.put(cls, ctor);
	}

	public Scriptable wrapCustom(Context cx, Scriptable scope,
			Object javaObj, Class<?> staticType, boolean newObject) {
		if (javaObj instanceof Style)
			return new StyleWrapper(scope, (Style) javaObj, staticType, true);
		else if (javaObj instanceof Color)
			return new ColorWrapper(scope, (Color) javaObj, staticType, true);
		else if (javaObj instanceof java.awt.Color)
			return new ColorWrapper(scope, new RGBColor(
					(java.awt.Color) javaObj), staticType, true);
		else {
			Function ctor = mappedJavaClasses.get(staticType);
			if (ctor != null) {
				// If this native object was explicitly created from JS,
				// lets wrap it in a uncached ExtendedJavaObject.
				if (newObject)
					return null;
				scope = ScriptableObject.getTopLevelScope(scope);
				// Do not go through Context.javaToJS as this would again 
				// end up here in wrapCustom for a native object.
				// Just wrap it as an ExtendedJavaObject.
				return ctor.construct(cx, scope, new Object[] {
					new ExtendedJavaObject(scope, javaObj, javaObj.getClass(),
							false)
				});
			}
		}
		return new ExtendedJavaObject(scope, javaObj, staticType, true);
	}

	public int calculateConversionWeight(Object from, Object unwrapped, 
			Class<?> to, int defaultWeight) {
		int weight = super.calculateConversionWeight(from, unwrapped, to,
				defaultWeight);
		if (weight == defaultWeight) {
			if (unwrapped instanceof String
					&& (Enum.class.isAssignableFrom(to) || to.isArray()))
				weight = CONVERSION_TRIVIAL + 1;
			else if (unwrapped instanceof Color
					&& java.awt.Color.class.equals(to))
				weight = CONVERSION_TRIVIAL;
			else if (unwrapped instanceof java.awt.Color
					&& Color.class.equals(to))
				weight = CONVERSION_TRIVIAL;
		}
		return weight;
	}

	@SuppressWarnings("unchecked")
	public Object coerceType(Class<?> type, Object value, Object unwrapped) {
		Object res = super.coerceType(type, value, unwrapped);
		if (res == null) {
			if (unwrapped instanceof String) {
				if (Enum.class.isAssignableFrom(type)) {
					return EnumUtils.get((Class<Enum>) type,
							(String) unwrapped);
				} else if (type.isArray()) {
					// Convert a string to an array by splitting into words
					// and trying to convert each to the desired type
					String[] parts = ((String) unwrapped).split("\\s");
					Class componentType = type.getComponentType();
					Object[] values = (Object[])
							java.lang.reflect.Array.newInstance(
									componentType, parts.length);
					for (int i = 0; i < values.length; i++) {
						String part = parts[i];
						values[i] = coerceType(componentType, part, part);
					}
					return values;
				}
			} else if (unwrapped instanceof java.awt.Color
					&& Color.class.equals(type)) {
				return new RGBColor((java.awt.Color) unwrapped);
			} else if (unwrapped instanceof Color
					&& java.awt.Color.class.equals(type)) {
				return ((Color) unwrapped).toAWTColor();
			}
		}
		return res;
	}

	public boolean shouldAddBean(Class<?> cls, boolean isStatic,
			MemberBox getter, MemberBox setter) {
		Package pkg = cls.getPackage();
		// Only control adding of beans if we're inside com.scriptographer
		// packages. Outside, we always add the bean. Inside, we add it except
		// if it's a read-only bean of which the getter name starts with is, to
		// force isMethodName() style methods to be called as methods.
		return getter != null && !(pkg != null
				&& pkg.getName().startsWith("com.scriptographer."))
				|| setter != null || !getter.getName().startsWith("is");
	}

	public boolean shouldRemoveGetterSetter(Class<?> cls, boolean isStatic,
			MemberBox getter, MemberBox setter) {
		Package pkg = cls.getPackage();
		// Only remove getter / setters of beans that were added within
		// com.scriptographer packages. Outside we use the old convention of
		// never removing them.
		return pkg != null && pkg.getName().startsWith("com.scriptographer.");
	}
}
