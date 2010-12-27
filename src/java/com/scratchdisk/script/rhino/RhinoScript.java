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

import java.io.File;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Wrapper;

import com.scratchdisk.script.Scope;
import com.scratchdisk.script.Script;
import com.scratchdisk.script.ScriptException;

/**
 * @author lehni
 *
 */
public class RhinoScript extends Script {
	private org.mozilla.javascript.Script script;
	private RhinoEngine engine;

	public RhinoScript(RhinoEngine engine,
			org.mozilla.javascript.Script script, File file) {
		super(file);
		this.engine = engine;
		this.script = script;
	}
	
	public RhinoEngine getEngine() {
		return engine;
	}
	
	public Object execute(Scope scope) throws ScriptException {
		try {
			Context cx = Context.getCurrentContext();
			Object result;
			// TODO: Typecast to RhinoScope can be wrong, e.g. when calling
			// from another language
			result = script.exec(cx, ((RhinoScope) scope).getScope());
			if (result instanceof Wrapper)
				result = ((Wrapper) result).unwrap();
			return result;
		} catch (Throwable t) {
			throw new RhinoScriptException(engine, t);
		}
	}
}

