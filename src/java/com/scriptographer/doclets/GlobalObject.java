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
 * $Id: GlobalObject.java 230 2007-01-16 20:36:33Z lehni $
 */

package com.scriptographer.doclets;

import org.mozilla.javascript.*;
import org.mozilla.javascript.tools.debugger.ScopeProvider;

import java.io.File;
import java.io.FileReader;

/**
 * @author lehni
 */
public class GlobalObject extends ImporterTopLevel implements ScopeProvider {

	protected GlobalObject(Context context) {
		super(context);

		// define some global functions and objects:
		defineFunctionProperties(new String[] { "print", "include" },
				GlobalObject.class,
				ScriptableObject.READONLY | ScriptableObject.DONTENUM);
	}

	public String getClassName() {
		return "global";
	}

	/**
	 * Print the string segmentValues of its arguments.
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
	 * Loads and executes a set of JavaScript source files in the current scope.
	 */
	public static void include(Context cx, Scriptable thisObj, Object[] args,
			Function funObj) throws Exception {
		for (int i = 0; i < args.length; i++) {
			File file = new File((String) args[i]);
		    FileReader in = new FileReader(file);
		    cx.evaluateReader(thisObj, in, file.getName(), 1, null);
		}
	}

	public Scriptable getScope() {
		return this;
	}
}
