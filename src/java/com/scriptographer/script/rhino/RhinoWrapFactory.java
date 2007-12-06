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
 * File created on Apr 10, 2007.
 *
 * $Id$
 */

package com.scriptographer.script.rhino;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import com.scratchdisk.script.rhino.RhinoCallable;
import com.scriptographer.ai.Style;

/**
 * @author lehni
 *
 */
public class RhinoWrapFactory extends com.scratchdisk.script.rhino.RhinoWrapFactory {

	public Object wrap(Context cx, Scriptable scope, Object obj,
			Class staticType) {
		// these are not wrappers, the java return types are simply converted to
		// these scriptographer types and wrapped afterwards:
		/*
		if (obj instanceof java.awt.geom.Rectangle2D) {
			obj = new Rectangle((java.awt.geom.Rectangle2D) obj);
		} else if (obj instanceof java.awt.geom.Point2D) {
			obj = new Point((java.awt.geom.Point2D) obj);
		} else if (obj instanceof java.awt.geom.AffineTransform
			&& !(obj instanceof Matrix)) {
			obj = new Matrix((java.awt.geom.AffineTransform) obj);
		} else if (obj instanceof java.awt.Dimension) {
			// TODO: expose Dimension to JS?
			obj = new Point((java.awt.Dimension) obj);
		} else
		*/
		if (obj instanceof RhinoCallable) {
			// Handle the ScriptFunction special case, return the unboxed
			// function value.
			// TODO: move to com.scratchdisk
			obj = ((RhinoCallable) obj).getCallable();
		}
		return super.wrap(cx, scope, obj, staticType);
	}

	public Scriptable wrapCustom(Context cx, Scriptable scope,
			Object javaObj, Class staticType) {
		if (javaObj instanceof Style)
			return new StyleWrapper(scope, (Style) javaObj, staticType, true);
		return null;
	}
}
