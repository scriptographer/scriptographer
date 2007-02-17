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
 * File created on 01.01.2005.
 *
 * $Id: ExtendedJavaClass.java 230 2007-01-16 20:36:33Z lehni $
 */

package com.scriptographer.script.rhino;

import java.lang.reflect.Modifier;

import org.mozilla.javascript.*;

import com.scriptographer.adm.Item;

/**
 * @author lehni
 */
public class ExtendedJavaClass extends NativeJavaClass {
	String className;

	public ExtendedJavaClass(Scriptable scope, Class cls) {
		super(scope, cls);
		className = cls.getName();
		// use simple class name instead of the full name with all packages:
		int pos = className.lastIndexOf('.');
		if (pos > 0)
			className = className.substring(pos + 1);
		// define it in the global scope:
		ScriptableObject.defineProperty(scope, className, this,
			ScriptableObject.PERMANENT | ScriptableObject.READONLY
				| ScriptableObject.DONTENUM);
	}

	public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
		// If the last object passed to the constructor is a NativeObject,
		// use it as a hashtable containing methods to be added to the class:
		Scriptable obj = null;
		NativeObject properties = null;
		Class classObject = getClassObject();
		int modifiers = classObject.getModifiers();
		if (args.length > 0 && args[args.length - 1] instanceof NativeObject &&
		// Unsealed.class.isAssignableFrom(classObject) &&
			!Modifier.isInterface(modifiers) && !Modifier.isAbstract(modifiers)) {
			properties = (NativeObject) args[args.length - 1];
			// remove the last argument from the list, so the right constructor
			// will be found:
			Object[] newArgs = new Object[args.length - 1];
			for (int i = 0; i < newArgs.length; i++)
				newArgs[i] = args[i];
			args = newArgs;
		}
		obj = super.construct(cx, scope, args);
		// if properties are to be added, do it now:
		if (properties != null) {
			Object[] ids = properties.getIds();
			for (int i = 0; i < ids.length; i++) {
				Object id = ids[i];
				if (id instanceof String)
					obj.put((String) id, obj, properties.get((String) id, properties));
			}
			FunctionHelper.callFunction(obj, "$constructor");
		}
		return obj;
	}

	public Class getClassObject() {
		// Why calling super.unwrap() when all it does is returning the internal
		// javaObject? That's how it's done in NativeJavaClass...
		return (Class) javaObject;
	}

	public String getClassName() {
		return className;
	}

	public String toString() {
		return "[" + className + "]";
	}
}