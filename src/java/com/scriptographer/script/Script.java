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

package com.scriptographer.script;

import java.io.File;

import com.scriptographer.ScriptographerEngine;

/**
 * @author lehni
 *
 */
public abstract class Script {
	protected File file;
	private long lastModified;
	
	public Script(File file) {
		this.file = file;
		lastModified = -1;
	}

	protected abstract Object executeScript(ScriptScope scope) throws ScriptException;

	public abstract ScriptEngine getEngine();

	public Object execute(ScriptScope scope) {
		boolean started = false;
		Object ret = null;
		try {
			if (scope == null)
				scope = getEngine().createScope();
			started = ScriptographerEngine.beginExecution(file);
			if (started) {
				ScriptographerEngine.showProgress("Executing " + (file != null ?
						file.getName() : "Console Input") + "...");
				// disable output to the console while the script is executed as it
				// won't get updated anyway
				// ConsoleOutputStream.enableOutput(false);
				if (scope.get("scriptFile") == null)
					scope.put("scriptFile", file, true);
				if (scope.get("preferences") == null)
					scope.put("preferences", ScriptographerEngine.getPreferences(file), true);
			}
			
			ret = executeScript(scope);
			if (started) {
				// handle onStart / onStop
				ScriptMethod onStart = scope.getMethod("onStart");
				if (onStart != null)
					onStart.call(scope);
				ScriptMethod onStop = scope.getMethod("onStop");
				if (onStop != null) {
					// add this scope to the scopes that want onStop to be called
					// when the stop button is hit by the user
					// TODO: finish this
				}
				ScriptographerEngine.closeProgress();
			}
		} catch (ScriptException e) {
			ScriptographerEngine.reportError(e);
		} catch (ScriptCanceledException e) {
			System.out.println(file != null ? file.getName() + " Canceled" :
				"Execution Canceled");
		} finally {
			// commit all the changes, even when script has crashed (to synch
			// with
			// direct changes such as creation of paths, etc
			if (started) {
				ScriptographerEngine.endExecution();
				// now reenable the console, this also writes out all the things
				// that were printed in the meantime:
				// ConsoleOutputStream.enableOutput(true);
			}
		}
		return ret;
	}

	public File getFile() {
		return file;
	}

	public boolean hasChanged() {
		return file != null && lastModified != file.lastModified();
	}
}
