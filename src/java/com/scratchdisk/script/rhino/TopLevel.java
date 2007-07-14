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
 * File created on 06.03.2005.
 * 
 * $Id: GlobalObject.java 238 2007-02-16 01:09:06Z lehni $
 */

package com.scratchdisk.script.rhino;

import java.lang.reflect.Method;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.LazilyLoadedCtor;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

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

    public void initStandardObjects(Context cx, boolean sealed) {
    	super.initStandardObjects(cx, sealed);
		// Override the class loading objects with our own extended classes
    	String packageClass = "com.scratchdisk.script.rhino.ExtendedJavaTopPackage";
		new LazilyLoadedCtor(this, "Packages", packageClass, false);
		new LazilyLoadedCtor(this, "java", packageClass, false);
		// new LazilyLoadedCtor(this, "getClass", packageClass, false);

		// define some global functions and objects:
		String[] names = { "print", "evaluate" };
		defineFunctionProperties(names, TopLevel.class,
			ScriptableObject.READONLY | ScriptableObject.DONTENUM);
	}

	public static void defineProperty(ScriptableObject obj, String name,
			String getter, String setter) throws SecurityException, NoSuchMethodException {
		Class cls = obj.getClass();
		Method getterMethod = getter != null ?
			cls.getDeclaredMethod(getter,
				new Class[] { ScriptableObject.class }) : null;
		Method setterMethod = setter != null ?
			cls.getDeclaredMethod(setter, new Class[] {
				ScriptableObject.class, Object.class }) : null;
		obj.defineProperty(name, null, getterMethod, setterMethod,
			ScriptableObject.DONTENUM);
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
				System.out.print(", ");

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
	 * <pre>
	 * var obj = {
	 *     eval: evaluate
	 * };
	 * obj.eval("print(this);");
	 * </pre>
	 */
	public static void evaluate(Context cx, Scriptable thisObj, Object[] args,
			Function funObj) throws Exception {
		ScriptEngine engine = ScriptEngine.getEngineByName("JavaScript");
		engine.evaluate(Context.toString(args[0]), engine.getScope(thisObj));
	}
}
