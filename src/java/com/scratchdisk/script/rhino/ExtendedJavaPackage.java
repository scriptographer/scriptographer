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
 * $Id: $
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
	private HashMap cache = new HashMap();

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
