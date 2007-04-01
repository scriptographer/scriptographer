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
 * File created on Jan 23, 2007.
 * 
 * $IdRhinoDoclet.java,v $
 * $Author: lehni lehni $
 * $Revision: 209 $
 * $Date: 2006-12-20 14:37:20 +0100 (Wed, 20 Dec 2006) Jan 23, 2007 $
 */

package com.scriptographer.doclets;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.mozilla.javascript.*;

import com.scriptographer.script.rhino.ExtendedJavaArray;
import com.scriptographer.script.rhino.ExtendedJavaObject;
import com.scriptographer.util.WeakIdentityHashMap;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.Doclet;
import com.sun.javadoc.RootDoc;

public class RhinoDoclet extends Doclet {
	static NativeObject options;
	static File file;
	
	public static boolean start(RootDoc root) {
		RhinoDocletEngine engine = new RhinoDocletEngine();
		engine.put("root", root);
		engine.put("options", options);
		return engine.evaluate(file);
	}

	public static int optionLength(String option) {
		// The parameters used in JS Doclets can only have a name and one value
		return 2;
	}
	
	static public boolean validOptions(String[][] args, DocErrorReporter err) {
		options = new NativeObject();
		for (int i = 0; i < args.length; i++) {
			String[] arg = args[i];
			// cut away the "-" from passed options
			// not specifying a value for any given parameter equals to true
			options.put(arg[0].substring(1), options, arg.length > 1 ? arg[1] : "true");
		}
		Object value = options.get("script", options);
		if (value != Scriptable.NOT_FOUND) {
			file = new File((String) value);
			if (!file.exists()) {
				err.printError("File " + file + " does not exist.");
				return false;
			}
		} else {
			err.printError("Please specify a script file.");
			return false;
		}
		return true;
	}
	
	public static class RhinoDocletEngine extends ImporterTopLevel {
		static {
			ContextFactory.initGlobal(new ContextFactory());
		}

		public RhinoDocletEngine() {
			super(Context.enter(), false);
			// Override the class loading objects with our own extended classes
			new LazilyLoadedCtor(this, "Packages", "com.scriptographer.script.rhino.ExtendedJavaTopPackage", false);
			new LazilyLoadedCtor(this, "java", "com.scriptographer.script.rhino.ExtendedJavaTopPackage", false);
			// new LazilyLoadedCtor(this, "getClass", "com.scriptographer.script.rhino.ExtendedJavaTopPackage", false);
			
			// define some global functions and objects:
			defineFunctionProperties(new String[] { "print", "include" },
					RhinoDocletEngine.class, ScriptableObject.READONLY
							| ScriptableObject.DONTENUM);
		}
		
		public static void main(String[] args) {
			(new RhinoDocletEngine()).evaluate(new File(args[0]));
		}

		public void put(String name, Object value) {
			this.put(name, this, value);
		}

		public Object get(String name) {
			return this.get(name, this);
		}

		/**
		 * @param file
		 * @throws IOException 
		 */
		public boolean evaluate(File file) {
			try {
				FileReader in = new FileReader(file);
				Context.getCurrentContext().evaluateReader(this, in,
						file.getName(), 1, null);
				return true;
			} catch (RhinoException e) {
				System.err.println(e.details());
				System.err.print(e.getScriptStackTrace());
			} catch (IOException e) {
				e.printStackTrace();
			}
			return false;
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

		static class ContextFactory extends org.mozilla.javascript.ContextFactory {
			protected Context makeContext() {
				Context context = new Context();
				context.setWrapFactory(new WrapFactory());
				context.setOptimizationLevel(-1);
				return context;
			}
		}

		static class WrapFactory extends org.mozilla.javascript.WrapFactory {
			private WeakIdentityHashMap wrappers = new WeakIdentityHashMap();

			public WrapFactory() {
				this.setJavaPrimitiveWrap(true);
			}

			public Object wrap(Context cx, Scriptable scope, Object obj, Class staticType) {
				if (obj != null) System.out.println("Wrap " + obj);
				return staticType != null && staticType.isArray() ? new ExtendedJavaArray(scope, obj, staticType, true) :
						super.wrap(cx, scope, obj, staticType);
			}

			public Scriptable wrapNewObject(Context cx, Scriptable scope, Object obj) {
				return (Scriptable) (obj instanceof Scriptable ? obj :
						wrapAsJavaObject(cx, scope, obj, obj.getClass()));
			}

			public Scriptable wrapAsJavaObject(Context cx, Scriptable scope,
					Object javaObj, Class staticType) {
				// As we are using unsealed wrapper, keep track of them here
				// so added fields are not lost. As an alternative, 
				// The UnsealdObject classes could add themselves only to wrappers
				// once additional information is actually stored in them. Not sure
				// what would be the better balance between memory consumption and
				// speed.
				Scriptable obj = (Scriptable) wrappers.get(javaObj);
				if (obj == null) {
					if (staticType != null && staticType.isArray())
						obj = new ExtendedJavaArray(scope, javaObj, staticType, true);
					else
						obj = new ExtendedJavaObject(scope, javaObj, staticType, true);
					wrappers.put(javaObj, obj);
				}
				return obj;
			}
		}
	}
}