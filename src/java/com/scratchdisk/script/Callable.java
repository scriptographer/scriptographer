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
 * File created on Feb 17, 2007.
 */

package com.scratchdisk.script;

/**
 * @author lehni
 * 
 * Callable cannot be an interface, as Rhino tries to convert
 * functions to interfaces...
 */
public abstract class Callable {

	private static Object[] emptyArgs = new Object[0];

	public abstract Object call(Object obj, Object[] args)
			throws ScriptException;

	public Object call(Object obj) throws ScriptException {
		return this.call(obj, emptyArgs);
	}

	public abstract Object getCallable();

	public abstract Scope getScope();
}
