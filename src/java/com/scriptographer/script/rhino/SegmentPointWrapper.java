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
 * File created on Feb 16, 2007.
 * 
 * $IdSegmentPointWrapper.java,v $
 * $Author: lehni lehni $
 * $Revision: 209 $
 * $Date: 2006-12-20 14:37:20 +0100 (Wed, 20 Dec 2006) Feb 16, 2007 $
 */

package com.scriptographer.script.rhino;

import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;

import com.scratchdisk.script.rhino.ExtendedJavaObject;
import com.scriptographer.ai.SegmentPoint;

/**
 * Wrapper is neeeded so the public fields Point2D.Float.x and
 * Point2D.Float.y are overriden with the setX and setY setters, in order to
 * reflect the changes in the underlying AI points
 */
public class SegmentPointWrapper extends ExtendedJavaObject {
	SegmentPointWrapper(Scriptable scope, SegmentPoint javaObject,
			Class staticType, boolean sealed) {
		super(scope, javaObject, staticType, sealed);
	}

	public void put(String name, Scriptable start, Object value) {
		if (javaObject != null) {
			if (name.equals("x"))
				((SegmentPoint) javaObject).setX((float) ScriptRuntime.toNumber(value));
			else if (name.equals("y"))
				((SegmentPoint) javaObject).setY((float) ScriptRuntime.toNumber(value));
			else
				super.put(name, start, value);
		}
	}
}
