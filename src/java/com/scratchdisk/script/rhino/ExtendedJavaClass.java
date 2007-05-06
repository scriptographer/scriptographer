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

package com.scratchdisk.script.rhino;

import java.lang.reflect.Modifier;
import java.util.HashMap;

import org.mozilla.javascript.*;

/**
 * @author lehni
 */
public class ExtendedJavaClass extends NativeJavaClass {
	private String className;
	private HashMap properties;
	private Scriptable instanceProto = null;
	// A lookup for the associated ExtendedJavaClass wrappers
	private static HashMap classes = new HashMap();

	public ExtendedJavaClass(Scriptable scope, Class cls, boolean unsealed) {
		super(scope, cls);
		// Set the function prototype, as basically Java constructors
		// behave like JS constructor functions. Like this, all properties
		// from Function.prototype are inherited.
		setParentScope(scope);
		setPrototype(ScriptableObject.getFunctionPrototype(scope));
		// Determine short className:
		className = cls.getName();
		// Use simple class name instead of the full name with all packages:
		int pos = className.lastIndexOf('.');
		if (pos > 0)
			className = className.substring(pos + 1);
		properties = unsealed ? new HashMap() : null;
		// put it in the class wrapper table
		classes.put(cls, this);
	}

	public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
		// If the last object passed to the constructor is a NativeObject,
		// use it as a hashtable containing methods to be added to the class:
		Scriptable obj = null;
		NativeObject properties = null;
		Callable initialize = null;
		Class classObject = getClassObject();
		int modifiers = classObject.getModifiers();
		if (args.length > 0 && !Modifier.isInterface(modifiers) &&
				!Modifier.isAbstract(modifiers)) {
			// Look at the last argument to find out if we need to do something
			// special. Possibilities: a object literal that defines fields to
			// be set, or a function that is executed on the object and of which
			// the result can be fields to be set.
			Object last = args[args.length - 1];
			if (last instanceof NativeObject)
				properties = (NativeObject) last;
			else if (last instanceof Callable)
				initialize = (Callable) last;
			// remove the last argument from the list, so the right constructor
			// will be found:
			if (initialize != null || properties != null) {
				Object[] newArgs = new Object[args.length - 1];
				for (int i = 0; i < newArgs.length; i++)
					newArgs[i] = args[i];
				args = newArgs;
			}
		}
		obj = super.construct(cx, scope, args);
		// If an initialize function was passed as the last argument, execute
		// it now. The fields of the result of the function are then injected
		// into the object, if it is a NativeObject.
		if (initialize != null) {
			Object res = initialize.call(cx, scope, obj, args);
			if (res instanceof NativeObject)
				properties = (NativeObject) res;
		}
		// If properties are to be added, do it now:
		if (properties != null) {
			Object[] ids = properties.getIds();
			for (int i = 0; i < ids.length; i++) {
				Object id = ids[i];
				if (id instanceof String)
					obj.put((String) id, obj, properties.get((String) id, properties));
			}
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
			if (properties != null && properties.containsKey(name)) {
				// see wether this object defines the property.
				result = properties.get(name);
			} else if (name.equals("prototype")) {
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

	public void put(String name, Scriptable start, Object value) {
		if (super.has(name, start)) {
			super.put(name, start, value);
		} else if (name.equals("prototype")) {
			if (value instanceof Scriptable)
				this.setPrototype((Scriptable) value);
		} else if (properties != null) {
			properties.put(name, value);
		}
	}

	public boolean has(String name, Scriptable start) {
		boolean has = super.has(name, start);
		if (!has && properties != null)
			has = properties.get(name) != null;
		return has;
	}

	public void delete(String name) {
		if (properties != null)
			properties.remove(name);
	}

	public Scriptable getInstancePrototype() {
		if (instanceProto == null) {
			instanceProto = new NativeObject();
			// Set the prototype chain correctly for this prototype object, 
			// so properties in the prototype of parent classes are found too:
			Class sup = getClassObject().getSuperclass();
			Scriptable parent;
			if (sup != null) {
				ExtendedJavaClass classWrapper = getClassWrapper(
						ScriptableObject.getTopLevelScope(this), sup);
				parent = classWrapper.getInstancePrototype();
			} else {
				// At the end of the chain, there is allways the Object prototype.
				parent = ScriptableObject.getObjectPrototype(this);
			}
			instanceProto.setPrototype(parent);
			instanceProto.put("clazz", instanceProto, getClassObject());
		}
		return instanceProto;
	}

	public String getClassName() {
		return className;
	}

	public String toString() {
		return "[" + className + "]";
	}
	
	protected static ExtendedJavaClass getClassWrapper(Scriptable scope, Class javaClass) {
		ExtendedJavaClass cls = (ExtendedJavaClass) classes.get(javaClass);
		if (cls == null) {
			// Search for the ExtendedJavaClass by splitting the full name into bits
			// separated by '.', and walk up the Packages chain:
			String[] path = javaClass.getName().split("\\.");
			Scriptable global = ScriptableObject.getTopLevelScope(scope);
			// Use ScriptableObject.getProperty so it also looks in the prototypes
			// of shared scopes.
			Object packages = ScriptableObject.getProperty(global, "Packages");
			if (packages != Scriptable.NOT_FOUND) {
				Scriptable current = (Scriptable) packages;
				for (int i = 0; i < path.length; i++)
					current = (Scriptable) current.get(path[i], current);
				// Now obj needs to be an instance of ExtendedJavaClass.
				// Note that we do not need to put it into classes, as the constructor
				// does this for us.
				cls = (ExtendedJavaClass) current;
			}
		}
		return cls;
	}
}