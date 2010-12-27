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
 * File created on Apr 10, 2007.
 */

package com.scratchdisk.script.rhino;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;

import com.scratchdisk.script.Callable;

/**
 * @author lehni
 *
 */
public class RhinoCallable extends Callable {
	Function function;
	RhinoEngine engine;

	RhinoCallable(RhinoEngine engine, Function function) {
		this.engine = engine;
		this.function = function;
	}

	public Object call(Object obj, Object[] args) throws RhinoScriptException {
		// Retrieve wrapper object for the native java object, and call the
		// function on it.
		try {
			Scriptable scope = ScriptableObject.getTopLevelScope(function);
			Scriptable wrapper = RhinoEngine.getWrapper(obj, scope);
			for (int i = 0; i < args.length; i++)
				args[i] = Context.javaToJS(args[i], scope);
			Context cx = Context.getCurrentContext();
			Object ret = function.call(cx, wrapper, wrapper, args);
			if (ret == Undefined.instance) {
				// Do not return undefined, as it cannot be handled by the
				// native side, e.g. ConversionUtils.toBoolean would produce
				// true.
				ret = null;
			} else if (ret instanceof Wrapper) {
				// Unwrap if the return value is a native java object:
				ret = ((Wrapper) ret).unwrap();
			}
			return ret;
		} catch (Throwable t) {
			// Re-throw if it was a RhinoScriptException already
			if (t.getCause() instanceof RhinoScriptException)
				throw (RhinoScriptException) t.getCause();
			throw new RhinoScriptException(engine, t);
		}
	}

	public Function getCallable() {
		return function;
	}

	public RhinoScope getScope() {
		return new RhinoScope(engine,
				ScriptableObject.getTopLevelScope(function));
	}
}

