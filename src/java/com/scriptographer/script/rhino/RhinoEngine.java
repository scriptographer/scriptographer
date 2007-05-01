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
 * $Id: $
 */

package com.scriptographer.script.rhino;

import java.io.File;

import org.mozilla.javascript.Context;

import com.scratchdisk.script.ScriptCanceledException;
import com.scriptographer.ScriptographerEngine;
import com.scriptographer.ai.Application;

/**
 * @author lehni
 *
 */
public class RhinoEngine extends com.scratchdisk.script.rhino.RhinoEngine {

	public RhinoEngine() {
		super(new RhinoWrapFactory());
	}

	protected com.scratchdisk.script.rhino.TopLevel makeTopLevel(Context context) {
		return new TopLevel(context);
	}

	protected Context makeContext() {
		context = super.makeContext();
		// Use pure interpreter mode to allow for
		// observeInstructionCount(Context, int) to work
		context.setOptimizationLevel(-1);
		// Make Rhino runtime to call observeInstructionCount
		// each 20000 bytecode instructions
		context.setInstructionObserverThreshold(20000);
		return context;
	}

	protected void observeInstructionCount(Context cx, int instructionCount) {
		if (!Application.updateProgress())
			throw new ScriptCanceledException();
	}

	public File getBaseDirectory() {
		return ScriptographerEngine.getScriptDirectory();
	}
}
