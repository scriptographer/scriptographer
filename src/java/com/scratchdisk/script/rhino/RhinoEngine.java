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

package com.scratchdisk.script.rhino;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import org.eclipse.wst.jsdt.debug.rhino.debugger.RhinoDebugger;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.PropertyDescriptor;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.tools.debugger.ScopeProvider;

import com.scratchdisk.script.ArgumentReader;
import com.scratchdisk.script.PropertyObserver;
import com.scratchdisk.script.Scope;
import com.scratchdisk.script.Script;
import com.scratchdisk.script.ScriptEngine;

/**
 * @author lehni
 *
 */
public class RhinoEngine extends ScriptEngine implements ScopeProvider {
	protected TopLevel topLevel;
	protected Context context;
	protected RhinoWrapFactory wrapFactory;
	private RhinoScope globalScope;
	private RhinoDebugger debugger;

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

			protected void observeInstructionCount(Context cx,
					int instructionCount) {
				RhinoEngine.this.observeInstructionCount(cx, instructionCount);
			}
		};

		ContextFactory.initGlobal(contextFactory);

		// The debugger needs to be created before the context, otherwise
		// notification won't work
		String rhinoDebug = System.getProperty("rhino.debug");
		if (rhinoDebug != null) {
			try {
				debugger = new RhinoDebugger(rhinoDebug);
				debugger.start();
				contextFactory.addListener(debugger);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

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

	public Script compile(String code, String name) {
		return new RhinoScript(this,
				context.compileString(code, name, 1, null), null);
	}

	public Object evaluate(String code, String name, Scope scope)
			throws RhinoScriptException {
		try {
			// TODO: Typecast to RhinoScope can be wrong, e.g. when calling
			// from another language
			return context.evaluateString(((RhinoScope) scope).getScope(), code,
					name, 1, null);
		} catch (RhinoException e) {
			throw new RhinoScriptException(this, e);
		}
	}
	
	public Scope createScope() {
		Scriptable scope = new NativeObject();
		// Sharing the top level scope:
		// https://developer.mozilla.org/En/Rhino_documentation/Scopes_and_Contexts
		scope.setPrototype(topLevel);
		scope.setParentScope(null);
		return new RhinoScope(this, scope);
	}

	public Scope getScope(Object object) {
		if (object instanceof Scriptable)
			return new RhinoScope(this, (Scriptable) object);
		return null;
	}

	public Scope getGlobalScope() {
		if (globalScope == null)
			globalScope = new RhinoScope(this, topLevel);
		return globalScope;
	}

	public Scriptable getScope() {
		return topLevel;
	}

	/**
	 * Required by RhinoCallable, to find the wrapper object (scope) for the
	 * native object the callalbe is executed on.
	 */
	protected static Scriptable getWrapper(Object object, Scriptable scope) {
		if (object instanceof Scriptable) {
			return (Scriptable) object;
		} else {
			Context cx = Context.getCurrentContext();
			return cx.getWrapFactory().wrapAsJavaObject(cx, scope,
					object, object.getClass(), false);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T toJava(Object object, Class<T> type) {
		return (T) Context.jsToJava(object, type);
	}

	@Override
	public ArgumentReader getArgumentReader(Object object) {
		return wrapFactory.getArgumentReader(object);
	}

	@Override
	public boolean observe(Map object, Object key, PropertyObserver observer) {
		if (object instanceof ScriptableObject) {
			ScriptableObject obj = (ScriptableObject) object;
			Context cx = Context.getCurrentContext();
			PropertyDescriptor desc = obj.getOwnPropertyDescriptor(cx, key);
			if (desc != null && desc.isDataDescriptor()) {
				ObserverGetterSetter getterSetter =
						new ObserverGetterSetter(obj, key, desc, observer);
				obj.defineOwnProperty(cx, key, new PropertyDescriptor(
						getterSetter,
						getterSetter,
						desc.isEnumerable(),
						desc.isConfigurable(),
						true));
			}
			return true;
		}
		return false;
	}

	/*
	 * Use one function as both getter and setter and look at the argument count
	 * to decide which one of the two is required.
	 */
	private static class ObserverGetterSetter extends BaseFunction {

		private ScriptableObject object;
		private Object id;
		private PropertyDescriptor descriptor;
		private PropertyObserver observer;

		ObserverGetterSetter(ScriptableObject object, Object id,
				PropertyDescriptor descriptor, PropertyObserver observer) {
			this.object = object;
			this.id = id;
			this.descriptor = descriptor;
			this.observer = observer;
		}

		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			if (args.length == 1) {
				Object value = args[0];
				descriptor.setValue(value);
				observer.onChangeProperty(object, id, value);
				return Undefined.instance;
			} else {
				return descriptor.getValue();
			}
		}
	}
}
