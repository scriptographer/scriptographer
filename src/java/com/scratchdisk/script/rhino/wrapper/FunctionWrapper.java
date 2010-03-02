/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2010 Juerg Lehni, http://www.scratchdisk.com.
 * All rights reserved.
 *
 * Please visit http://scriptographer.org/ for updates and contact.
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
 * File created on Mar 1, 2010.
 *
 * $Id$
 */

package com.scratchdisk.script.rhino.wrapper;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

class FunctionWrapper extends BaseFunction implements Wrapped {
	private Function function;
	private Callable onChange;

	FunctionWrapper(Function function, Callable onChange) {
		this.function = function;
		this.onChange = onChange;
		setParentScope(function);
	}

	public Object call(Context cx, Scriptable scope, Scriptable thisObj,
			Object[] args) {
		// Wrap results from functions too, as they might cause changes as
		// well...
		return ObjectWrapper.wrap(function.call(cx, scope, thisObj, args),
				onChange);
	}

	public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
		// Wrap results from functions too, as they might cause changes as
		// well...
		return (Scriptable) ObjectWrapper.wrap(function.construct(cx, scope,
				args), onChange);
	}

	public Object getDefaultValue(Class<?> typeHint) {
		return function.getDefaultValue(typeHint);
	}
}