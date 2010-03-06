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
 * File created on Feb 19, 2007.
 *
 * $Id$
 */

package com.scratchdisk.script;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import com.scratchdisk.util.ClassUtils;

/**
 * @author lehni
 *
 */
public abstract class ScriptEngine {
	private static HashMap<String, ScriptEngine> enginesByName =
		new HashMap<String, ScriptEngine>();
	private static HashMap<String, ScriptEngine> enginesByExtension =
		new HashMap<String, ScriptEngine>();
	private static boolean loaded = false;
	private HashMap<File, Script> scriptCache =
		new HashMap<File, Script>();

	public static void loadEngines() {
		// Do not call loadEngines immediately, as we want the scripting engines
		// to be instantiated in the same thread as from where they are used...
		// TODO: Move to a multi threaded scenario, where RhinoEngine creates
		// Contexts for reach script call...
		String[] lines = ClassUtils.getServiceInformation(ScriptEngine.class);
		if (lines != null) {
			for (int i = 0; i < lines.length; i++) {
				try {
					Class.forName(lines[i]).newInstance();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			loaded = true;
		}
		ArgumentConverter.loadConverters();
	}

	public ScriptEngine(String name, String extension) {
		addName(name);
		addExtension(extension);
	}

	public void addExtension(String extension) {
		enginesByExtension.put(extension, this);
	}

	public void addName(String name) {
		enginesByName.put(name, this);
	}

	public static ScriptEngine getEngineByName(String name) {
		if (!loaded)
			loadEngines();
		return (ScriptEngine) enginesByName.get(name);
	}
	
	public static ScriptEngine getEngineByExtension(String extension) {
		if (!loaded)
			loadEngines();
		return (ScriptEngine) enginesByExtension.get(extension);
	}
	
	public static ScriptEngine getEngineByFile(File file) {
		String name = file.getName();
		int pos = name.lastIndexOf('.');
		return pos != -1 ? getEngineByExtension(name.substring(pos + 1)) : null;
	}

	public abstract Object toJava(Object object, Class type);

	public static Object convertToJava(Object object, Class type) {
		for (ScriptEngine engine : enginesByName.values()) {
			Object res = engine.toJava(object, type);
			if (type.isInstance(res))
				return res;
		}
		return null;
	}

	public abstract Scope createScope();

	public abstract Scope getScope(Object object);

	public abstract Scope getGlobalScope();
	
	protected abstract Script compileScript(File file)
			throws ScriptException, IOException;

	/**
	 * Compiles the specified file.
	 * Caching for the compiled scripts is used for speed increase.
	 * 
	 * @param file
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

	public abstract Object evaluate(String string, Scope scope)
			throws ScriptException;

	/**
	 * Returns the base of all paths, to be cut away from Error messages,
	 * if desired.
	 * TODO: This should be changed to a more script oriented approach,
	 * Where the base directly is determined from a variable in the current
	 * scope...
	 * Think about a convention for naming baseDirectroies, scriptFiles, etc.
	 * in the scope. e.g. app.directory, script.file, ...
	 */
	public File getBaseDirectory() {
		return null;
	}
}
