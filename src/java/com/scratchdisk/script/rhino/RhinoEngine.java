/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2008 Juerg Lehni, http://www.scratchdisk.com.
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
 * File created on Feb 19, 2007.
 *
 * $Id$
 */

package com.scratchdisk.script.rhino;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.tools.debugger.ScopeProvider;

import com.scratchdisk.script.Script;
import com.scratchdisk.script.ScriptEngine;
import com.scratchdisk.script.Scope;

/**
 * @author lehni
 *
 */
public class RhinoEngine extends ScriptEngine implements ScopeProvider {
	protected TopLevel topLevel;
	protected Context context;
	protected RhinoWrapFactory wrapFactory;
//	protected Debugger debugger = null;

	public RhinoEngine(RhinoWrapFactory wrapFactory) {
		super("JavaScript", "js");

		this.wrapFactory = wrapFactory;
		// Set the engine pointer by hand here, as otherwise
		// RhinoWrapFactory would take an argument, and could not be created
		// in the super constructor call, which would be annoying...
		// TODO: consider a cleaner solution
		wrapFactory.engine = this;

		// Produce a ContextFactory that only redirects calls to RhinoEngine,
		// so they can be overridden easily in inherited classes.
		ContextFactory contextFactory = new ContextFactory() {
			protected boolean hasFeature(Context cx, int feature) {
				return RhinoEngine.this.hasFeature(cx,
						feature, super.hasFeature(cx, feature));
			}

			protected Context makeContext() {
				Context context = super.makeContext();
				RhinoEngine.this.enter(context);
				return context;
			}

			protected void observeInstructionCount(Context cx, int instructionCount) {
				RhinoEngine.this.observeInstructionCount(cx, instructionCount);
			}
		};

		ContextFactory.initGlobal(contextFactory);

		// The debugger needs to be created before the context, otherwise
		// notification won't work
//		debugger = new Debugger();
//		debugger.setScopeProvider(this);
//		debugger.attachTo(contextFactory);

		context = contextFactory.enterContext();
		topLevel = this.makeTopLevel(context);
	}

	public RhinoEngine() {
		this(new RhinoWrapFactory());
	}

	protected TopLevel makeTopLevel(Context context) {
		return new TopLevel(context);
	}

	/**
	 * @param cx
	 * @param instructionCount
	 */
	protected void observeInstructionCount(Context cx, int instructionCount) {
	}

	protected boolean hasFeature(Context cx, int feature, boolean defaultValue) {
		switch (feature) {
		case Context.FEATURE_E4X:
			return cx.getE4xImplementationFactory() != null;
		}
		return defaultValue;
	}

	protected void enter(Context context) {
		context.setApplicationClassLoader(getClass().getClassLoader());
		context.setWrapFactory(wrapFactory);
	}

	protected Script compileScript(File file)
			throws RhinoScriptException, IOException {
		FileReader in = null;
		try {
			in = new FileReader(file);
			return new RhinoScript(this, context.compileReader(
					in, file.getPath(), 1, null), file);
		} catch (RhinoException e) {
			throw new RhinoScriptException(this, e);
		} finally {
			if (in != null)
				in.close();
		}
	}

	public Object evaluate(String string, Scope scope)
			throws RhinoScriptException {
		try {
			// TODO: typecast to JsContext can be wrong, e.g. when calling
			// from another language
			return context.evaluateString(((RhinoScope) scope).getScope(), string,
					"evaluate", 1, null);
		} catch (RhinoException e) {
			throw new RhinoScriptException(this, e);
		}
	}

	public Scope createScope() {
		Scriptable scope = new NativeObject();
		// Sharing the top level scope:
		// http://www.mozilla.org/rhino/scopes.html
		scope.setPrototype(topLevel);
		scope.setParentScope(null);
		return new RhinoScope(this, scope);
	}
	
	protected static Scriptable getWrapper(Object obj, Scriptable scope) {
		if (obj instanceof Scriptable) {
			return (Scriptable) obj;
		} else {
			Context cx = Context.getCurrentContext();
			return cx.getWrapFactory().wrapAsJavaObject(cx, scope,
					obj, obj.getClass());
		}
	}

	public Scope getScope(Object obj) {
		// Set global as the parent scope, so Tool buttons work.
		// TODO: this might not work for Jython or JRuby. Find a better 
		// way to handle this

		// Only wrap object if it's not a Scriptable already
		Scriptable scope;
		if (obj instanceof Scriptable) {
			scope = (Scriptable) obj;
		} else {
			scope = getWrapper(obj, topLevel);
			// scope.setParentScope(topLevel);
			scope.setPrototype(topLevel);
			scope.setParentScope(null);
		}
		return new RhinoScope(this, scope);
	}

	public Scope getGlobalScope() {
		return new RhinoScope(this, topLevel);
	}

	public Scriptable getScope() {
		return topLevel;
	}
}
