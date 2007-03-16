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
import java.io.IOException;
import java.util.HashMap;

import com.scriptographer.ai.Timer;

/**
 * @author lehni
 *
 */
public abstract class ScriptEngine {

	private static HashMap enginesByName = new HashMap();
	private static HashMap enginesByExtension = new HashMap();

	private HashMap scriptCache = new HashMap();
	
	static {
		// Try finding the Scripting engines for Scriptographer...
		String[] engines = {
			"com.scriptographer.script.rhino.RhinoEngine"
		};
		for (int i = 0; i < engines.length; i++) {
			try {
				Class.forName(engines[i]).newInstance();
			} catch (Exception e) {
			}
		}
			
	}

	public ScriptEngine(String name, String[] extensions) {
		// Register engine by name and by extensions, so we can quickly
		// find the right one
		enginesByName.put(name,this);
		for (int i = 0; i < extensions.length; i++)
			enginesByExtension.put(extensions[i], this);
	}
	
	public static ScriptEngine getInstanceByName(String name) {
		return (ScriptEngine) enginesByName.get(name);
	}
	
	public static ScriptEngine getInstanceByExtension(String extension) {
		return (ScriptEngine) enginesByExtension.get(extension);
	}
	
	public static ScriptEngine getInstanceByFile(File file) {
		String name = file.getName();
		int pos = name.lastIndexOf('.');
		return pos != -1 ? (ScriptEngine) enginesByExtension.get(
				name.substring(pos + 1)) : null;
	}

	public abstract ScriptScope createScope();

	public abstract ScriptScope getScope(Object obj);

	protected abstract Script compileScript(File file)
			throws ScriptException, IOException;

	/**
	 * Compiles the specified file.
	 * Caching for the compiled scripts is used for speed increase.
	 * 
	 * @param file
	 * @return
	 * @throws IOException 
	 * @throws ScriptException 
	 */

	public Script compile(File file)
			throws ScriptException, IOException {
		Script script = (Script) scriptCache.get(file);
		if (script == null || script.hasChanged()) {
			script = compileScript(file);
			scriptCache.put(file, script);
		}
		return script;
	}

	public abstract void evaluate(String string, ScriptScope scope)
			throws ScriptException;

	public static Script compileFile(File file) throws ScriptException,
			IOException {
		ScriptEngine engine = getInstanceByFile(file);
		return engine != null ? engine.compile(file) : null;
	}

	/**
	 * executes all scripts in the given folder
	 *
	 * @param dir
	 * @throws IOException 
	 * @throws ScriptException 
	 */
	public static void executeAll(File dir) throws ScriptException, IOException {
		File []files = dir.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				if (file.isDirectory()) {
					executeAll(file);
				} else if (file.getName().endsWith(".js")) {
					executeFile(file, null);
				}
			}
		}
	}
	
	public static void stopAll() {
		Timer.stopAll();
	}

	/**
	 * evaluates the specified file.
	 *
	 * @param file
	 * @return
	 * @throws IOException 
	 * @throws ScriptException 
	 */
	public static Object executeFile(File file, ScriptScope scope)
			throws ScriptException, IOException {
		Script script = compileFile(file);
		// Do not call script.execute directly, as we want the begin / end
		// execution sugar to be called here...
		return script != null ? script.execute(scope) : null;
	}

	/**
	 * Internal Class used for caching compiled scripts
	 */
	class ScriptCacheEntry {
		File file;
		long lastModified;
		Script script;

		ScriptCacheEntry(File file) {
			this.file = file;
			lastModified = -1;
			script = null;
		}

		Script compile() throws ScriptException, IOException {
			long modified = file.lastModified();
			if (script == null || modified != lastModified) {
				script = ScriptEngine.this.compile(file);
				lastModified = modified;
			}
			return script;
		}
	}
}
