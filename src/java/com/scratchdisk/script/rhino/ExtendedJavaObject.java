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
 * File created on 02.01.2005.
 *
 * $Id$
 */

package com.scratchdisk.script.rhino;

import java.util.HashMap;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import com.scratchdisk.script.ChangeNotifier;
import com.scratchdisk.script.ChangeListener;

/**
 * @author lehni
 */
public class ExtendedJavaObject extends NativeJavaObject {
	HashMap<String, Object> properties;
	ExtendedJavaClass classWrapper = null;
	
	/**
	 * @param scope
	 * @param javaObject
	 * @param staticType
	 */
	public ExtendedJavaObject(Scriptable scope, Object javaObject,
		Class staticType, boolean unsealed) {
		super(scope, javaObject, staticType);
		properties = unsealed ? new HashMap<String, Object>() : null;
		classWrapper = ExtendedJavaClass.getClassWrapper(scope, staticType);
	}

	public Scriptable getPrototype() {
		Scriptable prototype = super.getPrototype();
		return prototype == null
				? classWrapper.getInstancePrototype()
				: prototype;
	}

	protected ExtendedJavaObject changeListener = null;
	protected String changeName = null;
	protected int currentChangeVersion = -1;
	public static int changeVersion = -1;

	public Object get(String name, Scriptable start) {
		// Properties need to come first, as they might override something
		// defined in the underlying Java object
		if (properties != null && properties.containsKey(name)) {
			// See whether this object defines the property.
			return properties.get(name);
		} else {
			// If there is a modification listener, fetch the new value of this object
			// from it before calling anything on it. We are doing this by replacing
			// the underlying native object each time.
			if (changeListener != null && currentChangeVersion != changeVersion) {
				Object result = changeListener.get(changeName, this);
				if (result != this && result instanceof ExtendedJavaObject) {
					ExtendedJavaObject update = (ExtendedJavaObject) result;
					javaObject = update.javaObject;
					// Help the garbage collector
					update.javaObject = null;
				}
				currentChangeVersion = changeVersion;
			}
			Scriptable prototype = getPrototype();
			Object result = prototype.get(name, this);
			if (result != Scriptable.NOT_FOUND)
				return result;
			result = members.get(this, name, javaObject, false);
			if (result != Scriptable.NOT_FOUND) {
				// Now see if the result is a modification dispatcher for this listener.
				// If so, create the links between the two.
				if (javaObject instanceof ChangeListener
						&& result instanceof ExtendedJavaObject) {
					ExtendedJavaObject other = (ExtendedJavaObject) result;
					if (other.javaObject instanceof ChangeNotifier) {
						other.changeListener = this;
						other.changeName = name;
					}
				}
				return result;
			}
			if (name.equals("prototype"))
				return prototype;
			return Scriptable.NOT_FOUND;
		}
	}

	public void put(String name, Scriptable start, Object value) {
		EvaluatorException error = null;
        if (members.has(name, false)) {
			try {
				// Try setting the value on member first
	            members.put(this, name, javaObject, value, false);
	            // If there is a modification listener, update this field in it right away now.
	            if (changeListener != null)
	            	changeListener.put(changeName, changeListener, this);
				return; // done
			} catch (EvaluatorException e) {
				// Rethrow errors that have another cause (a real Java exception from the
				// called getter / setter) or that are about conversion problems.
				if (e.getCause() != null || e.getMessage().indexOf("Cannot convert") != -1)
					throw e;
				error = e;
			}
		}
		// Still here? An exception has happened. Let's try other things
		if (name.equals("prototype")) {
			if (value instanceof Scriptable)
				setPrototype((Scriptable) value);
		} else if (properties != null) {
			// Ignore EvaluatorException exceptions that might have happened in
			// members.put above. These would happen if the user tries to
			// override a Java method or field. We allow this on the level of
			// the wrapper though, if the wrapper was created unsealed (meaning
			// properties exist).
			properties.put(name, value);
		} else if (error != null) {
			// If nothing of the above worked, throw the error again.
			throw error;
		}
	}

	public boolean has(String name, Scriptable start) {
		return members.has(name, false) ||
				properties != null && properties.containsKey(name);
	}

	public void delete(String name) {
		if (properties != null)
			properties.remove(name);
	}
	
	public Object[] getIds() {
		// Concatenate the super classes ids array with the keySet from
		// properties HashMap:
		Object[] javaIds = super.getIds();
		if (properties != null) {
			int numProps = properties.size();
			if (numProps == 0)
				return javaIds;
			Object[] ids = new Object[javaIds.length + numProps];
			properties.keySet().toArray(ids);
			System.arraycopy(javaIds, 0, ids, numProps, javaIds.length);
			return ids;
		} else {
			return javaIds;
		}
	}

	public Object getDefaultValue(Class<?> hint) {
		// Try the ScriptableObject version first that tries to call toString / valueOf,
		// and fallback on the Java toString only if that does not work out.
		try {
			return ScriptableObject.getDefaultValue(this, hint);
		} catch (EcmaError e) {
			return super.getDefaultValue(hint);
		}
	}
}
