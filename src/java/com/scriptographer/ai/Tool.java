/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2005 Juerg Lehni, http://www.scratchdisk.com.
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
 * File created on 21.12.2004.
 *
 * $RCSfile: Tool.java,v $
 * $Author: lehni $
 * $Revision: 1.1 $
 * $Date: 2005/02/23 22:01:01 $
 */

package com.scriptographer.ai;

import org.mozilla.javascript.*;

import com.scriptographer.*;
import com.scriptographer.js.FunctionHelper;

public class Tool {
	private Scriptable scope;
	private Function mouseUpFunc;
	private Function mouseDragFunc;
	private Function mouseDownFunc;
	
	private Event event = new Event();
	private Object[] eventArgs = new Object[] { event };
	
	public void initScript(String filename) throws Exception {
		ScriptographerEngine engine = ScriptographerEngine.getEngine();
		scope = engine.evaluateFile(filename);
		if (scope != null) {
			mouseUpFunc = FunctionHelper.getFunction(scope, "onMouseUp");
			mouseDragFunc = FunctionHelper.getFunction(scope, "onMouseDrag");
			mouseDownFunc = FunctionHelper.getFunction(scope, "onMouseDown");
			FunctionHelper.callFunction(scope, "onInit");
		}
	}
	
	public void onEditOptions() throws Exception {
		if (scope != null) {
			FunctionHelper.callFunction(scope, "onOptions");
		}
	}

	public void onSelect() throws Exception {
		if (scope != null) {
			FunctionHelper.callFunction(scope, "onSelect");
		}
	}
	
	public void onDeselect() throws Exception {
		if (scope != null) {
			FunctionHelper.callFunction(scope, "onDeselect");
		}
	}
	
	public void onReselect() throws Exception {
		if (scope != null) {
			FunctionHelper.callFunction(scope, "onReselect");
		}
	}
	
	public void onMouseDown(float x, float y, int pressure) throws Exception {
		if (scope != null && mouseDownFunc != null) {
			event.setValues(x, y, pressure);
			FunctionHelper.callFunction(scope, mouseDownFunc, eventArgs);
		}
	}
	
	public void onMouseDrag(float x, float y, int pressure) throws Exception {
		if (scope != null && mouseDragFunc != null) {
			event.setValues(x, y, pressure);
			FunctionHelper.callFunction(scope, mouseDragFunc, eventArgs);
		}
	}
	
	public void onMouseUp(float x, float y, int pressure) throws Exception {
		if (scope != null && mouseUpFunc != null) {
			event.setValues(x, y, pressure);
			FunctionHelper.callFunction(scope, mouseUpFunc, eventArgs);
		}
	}
	
	public native boolean hasPressure();
}
