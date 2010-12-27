/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Scripting Plugin for Adobe Illustrator
 * http://scriptographer.org/
 *
 * Copyright (c) 2002-2010, Juerg Lehni
 * http://scratchdisk.com/
 *
 * All rights reserved. See LICENSE file for details.
 * 
 * File created on Jan 23, 2007.
 */

package com.scratchdisk.script.rhino;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.mozilla.javascript.*;

import com.scratchdisk.script.Script;
import com.scratchdisk.script.ScriptException;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.Doclet;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.RootDoc;

public class RhinoDoclet extends Doclet {
	static NativeObject options;
	static File file;

	public static boolean start(RootDoc root) {
		try {
			RhinoDocletEngine engine = new RhinoDocletEngine();
			engine.put("root", root);
			engine.put("options", options);
			return engine.evaluate(file);
		} catch (Throwable e) {
			e.printStackTrace();
			return false;
		}
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
			String name = arg[0].substring(1);
			String value = arg.length > 1 ? arg[1] : "true";
			options.put(name, options, value);
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
		// Add the base directory to the options
		options.put("directory", options, file.getParentFile());
		return true;
	}

	public static LanguageVersion languageVersion() {
		return LanguageVersion.JAVA_1_5;
	}

	public static class RhinoDocletEngine extends RhinoEngine {

		protected void enter(Context context) {
			super.enter(context);
			context.setOptimizationLevel(-1);
		}

		protected TopLevel makeTopLevel(Context context) {
			TopLevel topLevel = new TopLevel(context, false);
			// define some global functions and objects:
			topLevel.defineFunctionProperties(new String[] { "include" },
					RhinoDocletEngine.class, ScriptableObject.READONLY
							| ScriptableObject.DONTENUM);
			return topLevel;
		}

		public void put(String name, Object value) {
			topLevel.put(name, topLevel, value);
		}

		public Object get(String name) {
			return topLevel.get(name, topLevel);
		}

		/**
		 * For simple Rhino debugging
		 */
		public static void main(String[] args) {
			(new RhinoDocletEngine()).evaluate(new File(args[0]));
		}

		/**
		 * @param file
		 * @throws  
		 * @throws IOException 
		 */
		public boolean evaluate(File file) {
			try {
				Script script = compileScript(file);
				script.execute(createScope());
				return true;
			} catch (ScriptException e) {
				System.err.println(e.getFullMessage());
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return false;
		}

		/**
		 * Loads and executes a set of JavaScript source files in the current scope.
		 */
		public static void include(Context cx, Scriptable thisObj,
				Object[] args, Function funObj) throws Exception {
			for (int i = 0; i < args.length; i++) {
				File scriptFile = new File(file.getParentFile(),
						(String) args[i]);
				cx.evaluateReader(thisObj, new FileReader(scriptFile),
						scriptFile.getName(), 1, null);
			}
		}
	}
}