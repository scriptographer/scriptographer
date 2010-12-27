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
 * File created on Feb 19, 2007.
 */

package com.scratchdisk.script;

/**
 * @author lehni
 *
 */
public abstract class Scope {

	public abstract Object[] getKeys();

	public abstract Object getScope();

	public abstract Object get(String name);

	public abstract Object put(String name, Object value, boolean readOnly);

	public Object put(String name, Object value) {
		return put(name, value, false);
	}

	public Callable getCallable(String name) {
		Object obj = get(name);
		return obj instanceof Callable ? (Callable) obj : null;
	}
}
