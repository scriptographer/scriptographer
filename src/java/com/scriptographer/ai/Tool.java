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
 * $Revision: 1.3 $
 * $Date: 2005/03/25 00:27:57 $
 */

package com.scriptographer.ai;

import org.mozilla.javascript.*;

import com.scriptographer.*;
import com.scriptographer.js.FunctionHelper;
import com.scriptographer.util.Handle;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

public class Tool {
	private int toolHandle = 0;
	private int index;

	private static HashMap tools = null;

	protected Tool(int toolHandle, int index) {
		this.toolHandle = toolHandle;
		this.index = index;
	}

	private Scriptable scope;
	private Function mouseUpFunc;
	private Function mouseDragFunc;
	private Function mouseDownFunc;
	
	private Event event = new Event();
	private Object[] eventArgs = new Object[] { event };
	
	public void setScript(File file) throws Exception {
		ScriptographerEngine engine = ScriptographerEngine.getInstance();
		scope = engine.executeFile(file, null);
		if (scope != null) {
			mouseUpFunc = FunctionHelper.getFunction(scope, "onMouseUp");
			mouseDragFunc = FunctionHelper.getFunction(scope, "onMouseDrag");
			mouseDownFunc = FunctionHelper.getFunction(scope, "onMouseDown");
			FunctionHelper.callFunction(scope, "onInit");
		}
	}

	private static HashMap getTools() {
		if (tools == null)
			tools = nativeGetTools();
		return tools;
	}

	/**
	 * Returns all tools that have been created by this plugin.
	 * This is necessary because the java part of the plugin may be reloaded.
	 * The plugin needs to be capable of reestablish the connections between the wrappers
	 * and the real objects.
	 *
	 * @return
	 */
	private static native HashMap nativeGetTools();

	public native boolean hasPressure();

	protected void onEditOptions() throws Exception {
		if (scope != null) {
			FunctionHelper.callFunction(scope, "onOptions");
		}
	}

	protected void onSelect() throws Exception {
		if (scope != null) {
			FunctionHelper.callFunction(scope, "onSelect");
		}
	}
	
	protected void onDeselect() throws Exception {
		if (scope != null) {
			FunctionHelper.callFunction(scope, "onDeselect");
		}
	}
	
	protected void onReselect() throws Exception {
		if (scope != null) {
			FunctionHelper.callFunction(scope, "onReselect");
		}
	}
	
	protected void onMouseDown(float x, float y, int pressure) throws Exception {
		if (scope != null && mouseDownFunc != null) {
			event.setValues(x, y, pressure);
			FunctionHelper.callFunction(scope, mouseDownFunc, eventArgs);
		}
	}
	
	protected void onMouseDrag(float x, float y, int pressure) throws Exception {
		if (scope != null && mouseDragFunc != null) {
			event.setValues(x, y, pressure);
			FunctionHelper.callFunction(scope, mouseDragFunc, eventArgs);
		}
	}
	
	protected void onMouseUp(float x, float y, int pressure) throws Exception {
		if (scope != null && mouseUpFunc != null) {
			event.setValues(x, y, pressure);
			FunctionHelper.callFunction(scope, mouseUpFunc, eventArgs);
		}
	}

	/**
	 * To be called from the native environment:
	 */
	private static void onEditOptions(int toolHandle) throws Exception {
		Tool tool = getToolByHandle(toolHandle);
		if (tool != null)
			tool.onEditOptions();
	}

	private static void onSelect(int toolHandle) throws Exception {
		Tool tool = getToolByHandle(toolHandle);
		if (tool != null)
			tool.onSelect();
	}

	private static void onDeselect(int toolHandle) throws Exception {
		Tool tool = getToolByHandle(toolHandle);
		if (tool != null)
			tool.onDeselect();
	}

	private static void onReselect(int toolHandle) throws Exception {
		Tool tool = getToolByHandle(toolHandle);
		if (tool != null)
			tool.onReselect();
	}

	private static void onMouseDown(int toolHandle, float x, float y, int pressure) throws Exception {
		Tool tool = getToolByHandle(toolHandle);
		if (tool != null)
			tool.onMouseDown(x, y, pressure);
	}

	private static void onMouseDrag(int toolHandle, float x, float y, int pressure) throws Exception {
		Tool tool = getToolByHandle(toolHandle);
		if (tool != null)
			tool.onMouseDrag(x, y, pressure);
	}

	private static void onMouseUp(int toolHandle, float x, float y, int pressure) throws Exception {
		Tool tool = getToolByHandle(toolHandle);
		if (tool != null)
			tool.onMouseUp(x, y, pressure);
	}

	public static Tool getTool(int index) {
		for (Iterator iterator = getTools().values().iterator(); iterator.hasNext();) {
			Tool tool = (Tool) iterator.next();
			if (tool.index == index)
				return tool;
		}
		return null;
	}

	private static Tool getToolByHandle(int toolHandle) {
		return (Tool) getTools().get(new Handle(toolHandle));
	}
}
