/*
 * Scriptographer
 * 
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 * 
 * Copyright (c) 2004-2005 Juerg Lehni, http://www.scratchdisk.com.
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
 * File created on 31.07.2005.
 * 
 * $RCSfile: SegmentPointObject.java,v $
 * $Author: lehni $
 * $Revision: 1.1 $
 * $Date: 2005/07/31 12:09:52 $
 */

package com.scriptographer.js;

import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;

import com.scriptographer.ai.SegmentPoint;

/**
 * PointObject is neeeded so the public fields Point2D.Float.x and Point2D.Float.y are overriden
 * with the setX and setY setters, in order to reflect the changes in the underlying AI points
 */
public class SegmentPointObject extends NativeJavaObject {
	public SegmentPointObject(Scriptable scope, SegmentPoint point, Class staticType) {
		super(scope, point, staticType);
	}

	public void put(String name, Scriptable start, Object value) {
		if (javaObject != null) {
			if (name.equals("x"))
				((SegmentPoint) javaObject).setX((float) ScriptRuntime.toNumber(value));
			else if (name.equals("y"))
				((SegmentPoint) javaObject).setY((float) ScriptRuntime.toNumber(value));
		}
	}
}
