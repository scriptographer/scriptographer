/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2005 Juerg Lehni, http://www.scratchdisk.com.
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
 * File created on 20.12.2004.
 *
 * $RCSfile: ScriptographerObject.java,v $
 * $Author: lehni $
 * $Revision: 1.1 $
 * $Date: 2005/02/23 22:00:58 $
 */

package com.scriptographer.js;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.mozilla.javascript.*;
import com.scriptographer.ScriptographerEngine;


/**
 * 
 * @author Lehni
 */
public abstract class ScriptographerObject extends ScriptableObject {
	/**
	 * overrides ScriptableObject.getPrototype, so Scriptable objects can easily
	 * be created within java by directly calling their constructor. Object's
	 * created like this do not have the internal prototype set correctly. So by
	 * adding a little check to getPrototype, everything afterwards work as it
	 * should:
	 */
	public Scriptable getPrototype() {
		Scriptable proto = super.getPrototype();
		if (proto == null) {
			try {
				// fetch this classes prototype from the global scope:
				proto = ScriptableObject.getClassPrototype(
					ScriptographerEngine.getEngine(), getClassName());
				// and set it:
				setPrototype(proto);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return proto;
	}

	/**
	 * Internal function for defining series of static fields. The fields' names
	 * are matched against a prefix. If they match, their value is determined
	 * and a property with the same name and value is created in the scope. This
	 * is very handy for automatically expose a series of static fields to a
	 * class' constructor, making them available just like static constants in
	 * java.
	 * 
	 * @param scope
	 * @param fields
	 * @param prefix
	 * @param attributes
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private static void defineProperties(Scriptable scope, Field[] fields,
		String prefix, int attributes) throws IllegalArgumentException,
		IllegalAccessException {
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			// only allow static public fields to be defined like this:
			int mods = field.getModifiers();
			String name = field.getName();
			if (Modifier.isPublic(mods) && Modifier.isStatic(mods) && name.startsWith(prefix)) {
				ScriptableObject.defineProperty(scope, name, field.get(null),
					attributes);
			}
		}
	}

	/**
	 * Defines series of static fields. The fields' names are matched against a
	 * prefix. If they match, their value is determined and a property with the
	 * same name and value is created in the scope. This is very handy for
	 * automatically expose a series of static fields to a class' constructor,
	 * making them available just like static constants in java.
	 * 
	 * @param scope
	 * @param cls
	 * @param prefix
	 * @param attributes
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static void defineProperties(Scriptable scope, Class cls,
		String prefix, int attributes) throws IllegalArgumentException,
		IllegalAccessException {
		defineProperties(scope, cls.getDeclaredFields(), prefix, attributes);
	}

	/**
	 * Defines series of static fields. The fields' names are matched against a
	 * bezierList of prefixes. If they match, their value is determined and a property
	 * with the same name and value is created in the scope. This is very handy
	 * for automatically expose a series of static fields to a class'
	 * constructor, making them available just like static constants in java.
	 * 
	 * @param scope
	 * @param cls
	 * @param prefixes
	 * @param attributes
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static void defineProperties(Scriptable scope, Class cls,
		String[] prefixes, int attributes) throws IllegalArgumentException,
		IllegalAccessException {
		Field[] fields = cls.getDeclaredFields();
		for (int i = 0; i < prefixes.length; i++) {
			defineProperties(scope, fields, prefixes[i], attributes);
		}
	}
}