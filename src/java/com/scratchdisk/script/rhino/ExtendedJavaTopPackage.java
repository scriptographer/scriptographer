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
 * File created on Mar 31, 2007.
 */

package com.scratchdisk.script.rhino;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Wrapper;

/**
 * @author lehni
 * 
 */
public class ExtendedJavaTopPackage extends ExtendedJavaPackage {

	public ExtendedJavaTopPackage(ClassLoader loader) {
		super("", loader);
	}

	public Object call(Context cx, Scriptable scope, Scriptable thisObj,
			Object[] args) {
		return construct(cx, scope, args);
	}

	public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
		ClassLoader loader = null;
		if (args.length != 0) {
			Object arg = args[0];
			if (arg instanceof Wrapper)
				arg = ((Wrapper) arg).unwrap();
			if (arg instanceof ClassLoader)
				loader = (ClassLoader) arg;
		}
		return new ExtendedJavaPackage("", loader);
	}

	/*
	 * This is needed for LazilyLoadedCtor, as it looks for the static init only
	 * in the class itself, and fails if it is not there.
	 * Also, we create the top ExtendedJavaTopPackage here.
	 */
	public static void init(Context cx, Scriptable scope, boolean sealed) {
		ClassLoader loader = cx.getApplicationClassLoader();
		ExtendedJavaTopPackage top = new ExtendedJavaTopPackage(loader);
		top.setPrototype(getObjectPrototype(scope));
		top.setParentScope(scope);
		// It's safe to downcast here since initStandardObjects takes
		// a ScriptableObject.
		ScriptableObject global = (ScriptableObject) scope;
		global.defineProperty("Packages", top, ScriptableObject.DONTENUM);
		for (int i = 0; i != TopLevel.topPackages.length; i += 2) {
			String name = TopLevel.topPackages[i];
			if (!name.equals("Packages"))
				global.defineProperty(name, top.get(name, top), ScriptableObject.DONTENUM);
		}
	}
}
