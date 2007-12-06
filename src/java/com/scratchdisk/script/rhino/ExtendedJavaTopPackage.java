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
 * File created on Mar 31, 2007.
 *
 * $Id$
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

		/* TODO: needed?
		String[] names = Kit.semicolonSplit(commonPackages);
		for (int i = 0; i != names.length; ++i)
			top.forcePackage(names[i], scope);

		// getClass implementation
		IdFunctionObject getClass =
				new IdFunctionObject(top, FTAG, Id_getClass, "getClass", 1,
						scope);
		if (sealed) {
			getClass.sealObject();
		}
		getClass.exportAsScopeProperty();
		*/

		// It's safe to downcast here since initStandardObjects takes
		// a ScriptableObject.
		ScriptableObject global = (ScriptableObject) scope;
		global.defineProperty("Packages", top, ScriptableObject.DONTENUM);
		global.defineProperty("java", top.get("java", top), ScriptableObject.DONTENUM);
	}
}
