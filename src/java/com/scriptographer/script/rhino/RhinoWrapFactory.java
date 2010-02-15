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

package com.scriptographer.script.rhino;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.MemberBox;
import org.mozilla.javascript.Scriptable;

import com.scriptographer.ai.Color;
import com.scriptographer.ai.Style;
import com.scriptographer.script.EnumUtils;

/**
 * @author lehni
 *
 */
public class RhinoWrapFactory extends com.scratchdisk.script.rhino.RhinoWrapFactory {
	
	public Object wrap(Context cx, Scriptable scope, Object obj, Class<?> staticType) {
		// By default, Rhino converts chars to integers. In Scriptographer,
		// we want a string of length 1:
        if (staticType == Character.TYPE)
            return obj.toString();
        else if (obj instanceof Enum)
        	return EnumUtils.getScriptName((Enum) obj);
		return super.wrap(cx, scope, obj, staticType);
	}

	public Scriptable wrapCustom(Context cx, Scriptable scope,
			Object javaObj, Class<?> staticType) {
		if (javaObj instanceof Style)
			return new StyleWrapper(scope, (Style) javaObj, staticType, true);
		else if (javaObj instanceof Color)
			return new ColorWrapper(scope, (Color) javaObj, staticType, true);
		return null;
	}

	public int getConversionWeight(Object from, Class<?> to, int defaultWeight) {
		int weight = super.getConversionWeight(from, to, defaultWeight);
		if (weight == defaultWeight) {
			if (from instanceof String && (Enum.class.isAssignableFrom(to) || to.isArray()))
				weight = CONVERSION_TRIVIAL + 1;
		}
		return weight;
	}

	@SuppressWarnings("unchecked")
	public Object coerceType(Class<?> type, Object value) {
		Object res = super.coerceType(type, value);
		if (res == null) {
			if (value instanceof String) {
				if (Enum.class.isAssignableFrom(type)) {
					return EnumUtils.get((Class<Enum>) type, (String) value);
				} else if (type.isArray()) {
					// Convert a string to an array by splitting into words
					// and trying to convert each to the desired type
					String[] parts = ((String) value).split("\\s");
					Class componentType = type.getComponentType();
					Object[] values = (Object[]) java.lang.reflect.Array.newInstance(componentType, parts.length);
					for (int i = 0; i < values.length; i++)
						values[i] = coerceType(componentType, parts[i]);
					return values;
				}
			}
		}
		return res;
	}

	public boolean shouldAddBean(Class<?> cls, boolean isStatic,
			MemberBox getter, MemberBox setter) {
		Package pkg = cls.getPackage();
		// Only control adding of beans if we're inside com.scriptographer packages. Outside,
		// we always add the bean. Inside, we add it except if it's a read-only bean of which
		// the getter name starts with is, to force isMethodName() style methods to be called
		// as methods.
		return getter != null
				&& !(pkg != null && pkg.getName().startsWith("com.scriptographer."))
				|| setter != null || !getter.getName().startsWith("is");
	}

	public boolean shouldRemoveGetterSetter(Class<?> cls, boolean isStatic,
			MemberBox getter, MemberBox setter) {
		Package pkg = cls.getPackage();
		// Only remove getter / setters of beans that were added within com.scriptographer
		// packages. Outside we use the old convention of never removing them.
		return pkg != null && pkg.getName().startsWith("com.scriptographer.");
	}
}
