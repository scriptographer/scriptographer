/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2006 Juerg Lehni, http://www.scratchdisk.com.
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
 * $RCSfile: ExtendedJavaClass.java,v $
 * $Author: lehni $
 * $Revision: 1.3 $
 * $Date: 2006/10/18 14:12:51 $
 */

package com.scriptographer.js;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.mozilla.javascript.*;

public class ExtendedJavaClass extends NativeJavaClass {
	String className;
	Method constructor;

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

		// jsConstructor override?
		try {
			constructor = cls.getDeclaredMethod("jsConstructor", new Class[] {
				Context.class, Object[].class, Function.class, Boolean.TYPE
			});
			// constructor needs to be public static:
			int mods = constructor.getModifiers();
			if (!Modifier.isStatic(mods) ||
				!Modifier.isPublic(mods) ||
				constructor.getReturnType() != Scriptable.class)
				constructor = null;
		} catch (Exception e) {
		}
	}

	public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
		// see wether the class overrides the constructor with a static jsConstructor
		// method:
		if (constructor != null) {
			try {
				return (Scriptable) constructor.invoke(null, new Object[] {
					cx, args, this, Boolean.TRUE
				});
			} catch (Exception e) {
				throw new RuntimeException(e.getCause());
			}
		} else { // otherwise use the default behavior
			return super.construct(cx, scope, args);
		}
	}
	
	public Class getClassObject() {
		// Why calling super.unwrap() when all it does is returning the internal
		// javaObject?
		// That's how it's done in NativeJavaClass...
		return (Class) javaObject;
	}

	public String getClassName() {
		return className;
	}

	public String toString() {
		return "[" + className + "]";
	}
}