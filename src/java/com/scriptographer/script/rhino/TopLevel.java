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
 * File created on 06.03.2005.
 */

package com.scriptographer.script.rhino;

import java.io.File;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Wrapper;

import com.scratchdisk.script.Scope;
import com.scratchdisk.script.Script;
import com.scratchdisk.script.ScriptEngine;
import com.scratchdisk.script.ScriptException;
import com.scratchdisk.script.rhino.ExtendedJavaClass;
import com.scriptographer.ScriptographerEngine;
import com.scriptographer.ai.Document;
import com.scriptographer.ai.DocumentList;
import com.scriptographer.ai.FontList;
import com.scriptographer.sg.Illustrator;
import com.scriptographer.sg.Scriptographer;



/**
 * @author lehni
 */
public class TopLevel extends com.scratchdisk.script.rhino.TopLevel {

	
	
	public TopLevel(Context context) {
		super(context);

		// define classes. the createPrototypes flag is set so
		// the classes' constructors can now whether an object
		// is created as prototype or as real object through
		// isCreatingPrototypes()
		Class[] classes;
		if (ScriptographerEngine.getIllustratorVersion() < 16)
			classes = AdmClasses.classes;
		else
			classes = SwtClasses.classes;
		
		for (int i = 0; i < classes.length; i++) {
			ExtendedJavaClass cls = new ExtendedJavaClass(this, classes[i], true);
			// Put it in the global scope:
			ScriptableObject.defineProperty(this, cls.getClassName(), cls,
				ScriptableObject.PERMANENT | ScriptableObject.READONLY
					| ScriptableObject.DONTENUM);
		}

		// Define some global functions and objects:
		String[] names = { "include", "execute", "mapJavaClass" };
		defineFunctionProperties(names, TopLevel.class,
			ScriptableObject.READONLY | ScriptableObject.DONTENUM);

		// Properties:

		// Define the global reference here, for scripts that get executed
		// directly in the TopLevel scope (libraries)
		// This is overridden by RhinoEngine#createScope for all other scopes.
		defineProperty("global", this,
				ScriptableObject.READONLY | ScriptableObject.DONTENUM);

		defineProperty("documents", DocumentList.getInstance(),
				ScriptableObject.READONLY | ScriptableObject.DONTENUM);

		defineProperty("fonts", FontList.getInstance(),
				ScriptableObject.READONLY | ScriptableObject.DONTENUM);

		try {
			defineProperty(this, "document", "getActiveDocument", null);
			// Expose deprecated activeDocument
			defineProperty(this, "activeDocument", "getActiveDocument", null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		defineProperty("scriptographer", Scriptographer.getInstance(),
				ScriptableObject.READONLY | ScriptableObject.DONTENUM);

		defineProperty("illustrator", Illustrator.getInstance(),
				ScriptableObject.READONLY | ScriptableObject.DONTENUM);

		// deprecated:
		defineProperty("app", Illustrator.getInstance(),
				ScriptableObject.READONLY | ScriptableObject.DONTENUM);
	}

	/**
	 * Determines the directory of a script by reading it's scriptFile property
	 * in the main scope.
	 * 
	 * @param scope
	 */
	protected static File getDirectory(Scriptable scope) {
		Object obj = scope.get("script", scope);
		if (obj instanceof Wrapper)
			obj = ((Wrapper) obj).unwrap();
		if (obj instanceof com.scriptographer.sg.Script)
			return ((com.scriptographer.sg.Script) obj).getFile().getParentFile();
		return null;
	}

	/**
	 * @param script
	 * @param scope
	 * @throws ScriptException 
	 */
	private static void executeScript(Script script, Scope scope)
			throws ScriptException {
		if (script != null) {
			// Temporarily override script with the new one, so includes in
			// other directories work. Also pass it as the parent script to
			// the newly created script, so it can inherit coordinate system
			// / angle unit settings.
			com.scriptographer.sg.Script parent =
					(com.scriptographer.sg.Script) scope.get("script");
			try {
				scope.put("script", new com.scriptographer.sg.Script(
						script.getFile(), parent), true);
				script.execute(scope);
			} finally {
				scope.put("script", parent, true);
			}
		}
	}

	/*
	 * JavaScript functions
	 */
	protected static Object getActiveDocument(Scriptable obj) {
		return Context.javaToJS(Document.getActiveDocument(), obj);
	}

	/**
	 * Loads and executes a set of JavaScript source files in the current scope.
	 */
	public static void include(Context cx, Scriptable thisObj, Object[] args,
			Function funObj) throws Exception {
		File baseDir = getDirectory(thisObj);
		ScriptEngine engine = ScriptEngine.getEngineByName("JavaScript");
		for (int i = 0; i < args.length; i++) {
			File file = new File(baseDir, Context.toString(args[i]));
			executeScript(engine.compile(file), engine.getScope(thisObj));
		}
	}

	/**
	 * Loads and executes a set of JavaScript source files in a newly created
	 * scope.
	 */
	public static void execute(Context cx, Scriptable thisObj, Object[] args,
			Function funObj) throws Exception {
		File baseDir = getDirectory(thisObj);
		ScriptEngine engine = ScriptEngine.getEngineByName("JavaScript");
		for (int i = 0; i < args.length; i++) {
			File file = new File(baseDir, Context.toString(args[i]));
			executeScript(engine.compile(file), engine.createScope());
		}
	}

	/**
	 * Maps a Java class to a JavaScript prototype, so this can be used
	 * instead for wrapping of returned java types. So far this is only
	 * used for java.io.File in Scriptographer.
	 */
	public static void mapJavaClass(Context cx, Scriptable thisObj,
			Object[] args, Function funObj) {
		if (args.length == 2) {
			for (int i = 0; i < args.length; i++)
				args[i] = Context.jsToJava(args[i], Object.class);
			if (args[0] instanceof Class && args[1] instanceof Function) {
				Class cls = (Class) args[0];
				Function proto = (Function) args[1];
				RhinoWrapFactory factory =
						(RhinoWrapFactory) cx.getWrapFactory();
				factory.mapJavaClass(cls, proto);
			}
		}
	}
}
