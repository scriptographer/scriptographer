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
 * File created on Apr 10, 2007.
 *
 * $Id$
 */

package com.scriptographer.script.rhino;

import java.io.File;

import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.OperatorHandler;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Token;

import com.scratchdisk.script.ScriptCanceledException;
import com.scriptographer.ScriptographerEngine;

/**
 * @author lehni
 *
 */
public class RhinoEngine extends com.scratchdisk.script.rhino.RhinoEngine implements OperatorHandler {

	public RhinoEngine() {
		super(new RhinoWrapFactory());
	}

	protected com.scratchdisk.script.rhino.TopLevel makeTopLevel(Context context) {
		return new TopLevel(context);
	}

	protected void enter(Context context) {
		super.enter(context);
		// Use pure interpreter mode to allow for
		// observeInstructionCount(Context, int) to work
		context.setOptimizationLevel(-1);
		// Make Rhino runtime to call observeInstructionCount
		// each 20000 bytecode instructions
		context.setInstructionObserverThreshold(20000);
		context.setOperatorHandler(this);
	}

	protected void observeInstructionCount(Context cx, int instructionCount) {
		if (!ScriptographerEngine.updateProgress())
			throw new ScriptCanceledException();
	}

	public File getBaseDirectory() {
		return ScriptographerEngine.getScriptDirectory();
	}

	public Object handleOperator(Context cx, Scriptable scope, int operator, Object lhs, Object rhs) {
		// There is a very simple convention for arithmetic operations on objects:
		// Just try to get the according functions on scriptable objects,
		// and perform the operation by executing these.
		// Fall back on the default ScriptRuntime.add for adding,
		// return null for everything else.

		// Wrap String as Scriptable for some of the operators, so we can access
		// its prototype easily too.
		// Note that only the operators that are natively defined for JS can
		// be overridden here!
		if (lhs instanceof String && (
				operator == Token.SUB ||
				operator == Token.MUL ||
				operator == Token.DIV))
			lhs = ScriptRuntime.toObject(cx, scope, lhs);
		// Now perform the magic
		if (lhs instanceof Scriptable) {
			String name = null;
			switch (operator) {
			case Token.ADD:
				name = "add";
				break;
			case Token.SUB:
				name = "subtract";
				break;
			case Token.MUL:
				name = "multiply";
				break;
			case Token.DIV:
				name = "divide";
				break;
			case Token.MOD:
				name = "modulo";
				break;
			case Token.EQ:
			case Token.NE:
				name = "equals";
				break;
			}
			if (name != null) {
				Scriptable scriptable = (Scriptable) lhs;
				Object obj = ScriptableObject.getProperty(scriptable, name);
				if (obj instanceof Callable) {
					Object result = ((Callable) obj).call(cx, scope, scriptable, new Object[] { rhs });
					if (operator == Token.EQ || operator == Token.NE) {
						boolean value = ScriptRuntime.toBoolean(result);
						if (operator == Token.NE)
							value = !value;
						return ScriptRuntime.wrapBoolean(value);
					} else {
						return result;
					}
				}
		   }
		}
		return null;
	}

	public Object handleSignOperator(Context cx, Scriptable scope, int operator, Object rhs) {
		if (operator == Token.NEG) {
			// Wrap String as Scriptable, so we can access its prototype easily too.
			if (rhs instanceof String)
				rhs = ScriptRuntime.toObject(cx, scope, rhs);
			// Now perform the magic
			if (rhs instanceof Scriptable) {
				Scriptable scriptable = (Scriptable) rhs;
				Object obj = ScriptableObject.getProperty(scriptable, "negate");
				if (obj instanceof Callable)
					return ((Callable) obj).call(cx, scope, scriptable, new Object[] {});
			}
		}
		return null;
	}
}
