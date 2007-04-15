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
 * File created on Feb 19, 2007.
 *
 * $Id: $
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
import org.mozilla.javascript.WrapFactory;
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
	protected WrapFactory wrapFactory;
//	protected Debugger debugger = null;

	public RhinoEngine(WrapFactory wrapFactory) {
		super("JavaScript", "js");
		this.wrapFactory = wrapFactory;

		ContextFactory contextFactory = new ContextFactory() {
			protected boolean hasFeature(Context cx, int featureIndex) {
				switch (featureIndex) {
					case Context.FEATURE_E4X:
						return false;
				}
				return super.hasFeature(cx, featureIndex);
			}

			protected Context makeContext() {
				return RhinoEngine.this.makeContext();
			}

			protected void observeInstructionCount(Context cx, int instructionCount) {
				RhinoEngine.this.observeInstructinCount(cx, instructionCount);
			}
		};

		ContextFactory.initGlobal(contextFactory);

		// The debugger needs to be created before the context, otherwise
		// notification won't work
//		debugger = new Debugger();
//		debugger.setScopeProvider(this);
//		debugger.attachTo(contextFactory);

		context = Context.enter();
		topLevel = this.makeTopLevel(context);
	}

	public RhinoEngine() {
		this(new RhinoWrapFactory());
	}

	/**
	 * @param cx
	 * @param instructionCount
	 */
	protected void observeInstructinCount(Context cx, int instructionCount) {
	}

	protected TopLevel makeTopLevel(Context context) {
		return new TopLevel(context);
	}

	protected Context makeContext() {
		Context context = new Context();
		context.setApplicationClassLoader(getClass().getClassLoader());
		context.setWrapFactory(wrapFactory);
		return context;
	}

	protected Script compileScript(File file)
			throws RhinoScriptException, IOException {
		FileReader in = null;
		try {
			in = new FileReader(file);
			return new RhinoScript(this, context.compileReader(
					in, file.getPath(), 1, null), file);
		} catch (RhinoException e) {
			throw new RhinoScriptException(e);
		} finally {
			if (in != null)
				in.close();
		}
	}

	public void evaluate(String string, Scope scope)
			throws RhinoScriptException {
		try {
			// TODO: typecast to JsContext can be wrong, e.g. when calling
			// from another language
			context.evaluateString(((RhinoScope) scope).getScope(), string,
					null, 1, null);
		} catch (RhinoException e) {
			throw new RhinoScriptException(e);
		}
	}

	public Scope createScope() {
		Scriptable scope = new NativeObject();
		// Sharing the top level scope:
		// http://www.mozilla.org/rhino/scopes.html
		scope.setPrototype(topLevel);
		scope.setParentScope(null);
		return new RhinoScope(scope);
	}
	
	protected static Scriptable getWrapper(Scriptable scope, Object obj) {
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
		Scriptable scope = getWrapper(topLevel, obj);
		scope.setParentScope(topLevel);
		return new RhinoScope(scope);
	}

	public Scriptable getScope() {
		return topLevel;
	}
}
