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

import java.util.HashMap;

import org.mozilla.javascript.Kit;
import org.mozilla.javascript.NativeJavaPackage;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;

/**
 * @author lehni
 * 
 */
public class ExtendedJavaPackage extends NativeJavaPackage {
	// We need to store these here too, as they are private in NativeJavaPackage
	private String packageName;
	private ClassLoader classLoader;
	private HashMap<String, Scriptable> cache = new HashMap<String, Scriptable>();

	public ExtendedJavaPackage(String packageName, ClassLoader classLoader) {
		super(true, packageName, classLoader);
		this.packageName = packageName;
		this.classLoader = classLoader;
	}

	public synchronized Object getPkgProperty(String name, Scriptable start,
			boolean createPkg) {
		// Do not rely on the cache in NativeJavaPackage, as the only
		// way to access it is super.get, which creates instances of
		// NativeJavaPackage / NativeJavaClass, which we want to override here.
		Object cached = cache.get(name);
		if (cached != null)
			return cached;
		String className =
				(packageName.length() == 0) ? name : packageName + '.' + name;
		Scriptable newValue = null;
		Class cl = classLoader != null ? Kit.classOrNull(classLoader, className)
				: Kit.classOrNull(className);
		if (cl != null) {
			newValue = new ExtendedJavaClass(getTopLevelScope(this), cl, true);
			// ExtendedJavaClass sets its own Prototype... newValue.setPrototype(getPrototype());
		}
		if (newValue == null && createPkg) {
			ExtendedJavaPackage pkg =
					new ExtendedJavaPackage(className, classLoader);
			ScriptRuntime.setObjectProtoAndParent(pkg, getParentScope());
			newValue = pkg;
		}
		if (newValue != null) {
			// Make it available for fast lookup and sharing of
			// lazily-reflected constructors and static members.
			cache.put(name, newValue);
		}
		return newValue;
	}
}
