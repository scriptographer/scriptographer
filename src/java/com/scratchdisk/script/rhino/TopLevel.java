/*
 * Scriptographer
 * 
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 * 
 * Copyright (c) 2002-2008 Juerg Lehni, http://www.scratchdisk.com.
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
 * File created on 06.03.2005.
 * 
 * $Id$
 */

package com.scratchdisk.script.rhino;

import java.lang.reflect.Method;
import java.util.Date;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.LazilyLoadedCtor;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;

import com.scratchdisk.script.ScriptEngine;
/**
 * @author lehni
 */
public class TopLevel extends ImporterTopLevel {

	public TopLevel() {
	}

	public TopLevel(Context context, boolean sealed) {
		super(context, sealed);
	}

	public TopLevel(Context context) {
		super(context, false);
	}

	protected static final String[] topPackages = {
		"Packages",		"com.scratchdisk.script.rhino.ExtendedJavaTopPackage",
		"java",			"com.scratchdisk.script.rhino.ExtendedJavaTopPackage",
		"javax",		"com.scratchdisk.script.rhino.ExtendedJavaTopPackage",
		"org",			"com.scratchdisk.script.rhino.ExtendedJavaTopPackage",
		"com",			"com.scratchdisk.script.rhino.ExtendedJavaTopPackage",
		"edu",			"com.scratchdisk.script.rhino.ExtendedJavaTopPackage",
		"net",			"com.scratchdisk.script.rhino.ExtendedJavaTopPackage",
//	   "getClass",		"com.scratchdisk.script.rhino.ExtendedJavaTopPackage"
	};

	public void initStandardObjects(Context cx, boolean sealed) {
		super.initStandardObjects(cx, sealed);
		// Override the class loading objects with our own extended classes

		for (int i = 0; i != topPackages.length; i += 2)
			new LazilyLoadedCtor(this, topPackages[i], topPackages[i + 1], false);

		// define some global functions and objects:
		String[] names = { "print", "evaluate" };
		defineFunctionProperties(names, TopLevel.class,
			ScriptableObject.READONLY | ScriptableObject.DONTENUM);

		ScriptableObject objProto = (ScriptableObject) getObjectPrototype(this);
		objProto.defineFunctionProperties(new String[] { "dontEnum", "toJava" }, TopLevel.class,
                    DONTENUM | READONLY | PERMANENT);
    }

	public static void defineProperty(ScriptableObject obj, String name,
			String getter, String setter) throws SecurityException, NoSuchMethodException {
		Class<? extends ScriptableObject> cls = obj.getClass();
		Method getterMethod = getter != null ?
			cls.getDeclaredMethod(getter,
				new Class[] { Scriptable.class }) : null;
		Method setterMethod = setter != null ?
			cls.getDeclaredMethod(setter, new Class[] {
				Scriptable.class, Object.class }) : null;
		obj.defineProperty(name, null, getterMethod, setterMethod,
			ScriptableObject.DONTENUM);
	}

	/**
	 * Set DONTENUM attributes on the given properties in this object.
	 * This is set on the JavaScript Object prototype.
	 */
	public static Object dontEnum(Context cx, Scriptable thisObj,
			Object[] args, Function funObj) {

		// Don't throw error for now if dontEnum cannot do anything on this object.
		// e.g. if it is a NativeJavaObject.
		// TODO: This needs to change in the future.
		// But since dontEnum will go away in favor of something the standard
		// Object.defineProperty() method described in the EcmaScript 3.1,
		// this is fine for now.
//		if (!(thisObj instanceof ScriptableObject)) {
//			throw new EvaluatorException(
//					"dontEnum() called on non-ScriptableObject");
//		}

		if (thisObj instanceof ScriptableObject) {
			ScriptableObject obj = (ScriptableObject) thisObj;
			for (int i = 0; i < args.length; i++) {
				if (!(args[i] instanceof String)) {
					throw new EvaluatorException(
							"dontEnum() called with non-String argument");
				}
				String str = (String) args[i];
				if (obj.has(str, obj)) {
					int attr = obj.getAttributes(str);
					if ((attr & PERMANENT) == 0)
						obj.setAttributes(str, attr | DONTENUM);
				}
			}
		}
		return null;
	}

    /**
     * Convert an object into a wrapper that exposes the java
     * methods of the object to JavaScript. This is useful for
     * treating native numbers, strings, etc as their java
     * counterpart such as java.lang.Double, java.lang.String etc.
     * @param thisObj a java object that is wrapped in a special way
     * Rhino
     * @return the object wrapped as NativeJavaObject, exposing
     * the public methods of the underlying class.
     */
    public static Object toJava(Context cx, Scriptable thisObj,
			Object[] args, Function funObj) {
        if (thisObj == null || thisObj instanceof NativeJavaObject
                || thisObj == Undefined.instance) {
            return thisObj;
        }
        Scriptable topLevel = ScriptRuntime.getTopCallScope(cx);
        Object obj = thisObj;
        if (thisObj instanceof Wrapper) {
        	obj = ((Wrapper) thisObj).unwrap();
        } else if (thisObj instanceof Scriptable) {
            if ("Date".equals(((Scriptable) thisObj).getClassName())) {
                return new NativeJavaObject(topLevel,
                        new Date((long) ScriptRuntime.toNumber(thisObj)), null);
            }
        }
        return new NativeJavaObject(topLevel, obj, null);
    }

	public String getClassName() {
		return "global";
	}

	/**
	 * Print the string values of its arguments.
	 * 
	 * This method is defined as a JavaScript function. Note that its arguments
	 * are of the "varargs" form, which allows it to handle an arbitrary number
	 * of arguments supplied to the JavaScript function.
	 * 
	 */
	public static void print(Context cx, Scriptable thisObj, Object[] args,
			Function funObj) {
		for (int i = 0; i < args.length; i++) {
			if (i > 0)
				System.out.print(" ");

			// Convert the arbitrary JavaScript value into a string form.
			String s = Context.toString(args[i]);
			System.out.print(s);
		}
		System.out.println();
	}

	/**
	 * Evaluates the given JavaScript string in the current scope. Similar to
	 * eval(), but it allows the use of another object than the global scope:
	 * e.g.:
	 * <code>
	 * var obj = {
	 *     eval: evaluate
	 * };
	 * obj.eval("print(this);");
	 * </code>
	 */
	public static void evaluate(Context cx, Scriptable thisObj, Object[] args,
			Function funObj) throws Exception {
		ScriptEngine engine = ScriptEngine.getEngineByName("JavaScript");
		engine.evaluate(Context.toString(args[0]), engine.getScope(thisObj));
	}
}
