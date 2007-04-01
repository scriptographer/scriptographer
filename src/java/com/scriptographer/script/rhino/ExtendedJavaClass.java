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

/**
 * @author lehni
 */
public class ExtendedJavaClass extends NativeJavaClass {
	String className;
	Scriptable instanceProto = null;

	public ExtendedJavaClass(Scriptable scope, Class cls) {
		super(scope, cls);
		// Set the function prototype, as basically Java constructors
		// behave like JS constructor functions. Like this, all properties
		// from Function.prototype are inherited.
		setPrototype(((Scriptable) scope.get("Function", scope)).getPrototype());
		// Determine short className:
		className = cls.getName();
		// Use simple class name instead of the full name with all packages:
		int pos = className.lastIndexOf('.');
		if (pos > 0)
			className = className.substring(pos + 1);
	}

	public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
		// If the last object passed to the constructor is a NativeObject,
		// use it as a hashtable containing methods to be added to the class:
		Scriptable obj = null;
		NativeObject properties = null;
		Class classObject = getClassObject();
		int modifiers = classObject.getModifiers();
		if (args.length > 0 && args[args.length - 1] instanceof NativeObject &&
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
		}
		// Add on: If a prototype defines a $constructor function, call it after
		// creation. This can even return another object than the real ctor!
		Object ctor = obj.get("$constructor", obj);
		if (ctor instanceof Function) {
			Object ret = ((Function) ctor).call(cx, scope, obj, new Object[] {});
			if (ret != null && ret != Undefined.instance)
				obj = cx.getWrapFactory().wrapNewObject(cx,
						ScriptableObject.getTopLevelScope(scope),
						ret);
		}
		return obj;
	}

	public Class getClassObject() {
		// Why calling super.unwrap() when all it does is returning the internal
		// javaObject? That's how it's done in NativeJavaClass...
		return (Class) javaObject;
	}

	public Object get(String name, Scriptable start) {
		Object result = Scriptable.NOT_FOUND;
		// TODO: In NativeJavaClass, first staticFieldAndMethods are checked
		// why not members? Shouldn't it be the other way round, as this is
		// the more common case?
		// TODO: "prototype" is checked there, and null is returned. And here
		// we have to check for "prototype" again. Ideally, these things would
		// happen only once.
		// TODO: Remove nasty exc work-around! 
		// The goal will be to merge everything into NativeJavaClass
		RuntimeException exc = null;
		try {
			result = super.get(name, start);
			if (result == null)
				result = Scriptable.NOT_FOUND;
		} catch (RuntimeException e) {
			exc = e;
		}
		if (result == Scriptable.NOT_FOUND) {
			if (name.equals("prototype")) {
				//getPrototype creates prototype Objects on the fly:
				result = getInstancePrototype();
			} else {
				Scriptable proto = getPrototype();
				if (proto != null)
					result = proto.get(name, start);
			}
		}
		// Throw exception again, if nothing was found.
		if (result == ScriptableObject.NOT_FOUND && exc != null)
			throw exc;
		return result;
	}

	public Scriptable getInstancePrototype() {
		if (instanceProto == null) {
			instanceProto = new NativeObject();
			// Set the prototype chain correctly for this prototype object, 
			// so properties in the prototype of parent classes are found too:
			Scriptable top = ScriptableObject.getTopLevelScope(this);
			Class sup = getClassObject().getSuperclass();
			Scriptable parent;
			if (sup != null) {
				parent = ExtendedJavaTopPackage.getClassWrapper(top, sup).getInstancePrototype();
			} else {
				// At the end of the chain, there is allways the Object prototype.
				parent = ((Scriptable) top.get("Object", top)).getPrototype(); 
			}
			instanceProto.setPrototype(parent);
		}
		return instanceProto;
	}

	public String getClassName() {
		return className;
	}

	public String toString() {
		return "[" + className + "]";
	}
}