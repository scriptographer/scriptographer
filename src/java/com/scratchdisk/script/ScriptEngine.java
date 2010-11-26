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
 */

package com.scratchdisk.script;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

	public abstract <T> T toJava(Object object, Class<T> type);

	public abstract ArgumentReader getArgumentReader(Object object);

	public abstract boolean observe(Map object, Object key,
			PropertyObserver observer);

	@SuppressWarnings("unchecked")
	public static <T> T convertToJava(Object object, Class<T> type) {
		if (type.isInstance(object))
			return (T) object;
		for (ScriptEngine engine : enginesByName.values()) {
			T res = engine.toJava(object, type);
			if (type.isInstance(res))
				return res;
		}
		return null;
	}

	public static ArgumentReader convertToArgumentReader(Object object) {
		for (ScriptEngine engine : enginesByName.values()) {
			ArgumentReader reader = engine.getArgumentReader(object);
			if (reader != null)
				return reader;
		}
		return null;
	}

	public static boolean observeChanges(Map object, Object key,
			PropertyObserver observer) {
		for (ScriptEngine engine : enginesByName.values()) {
			if (engine.observe(object, key, observer))
				return true;
		}
		return false;
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

	public abstract Script compile(String code, String name);

	public Object evaluate(String code, String name, Scope scope)
			throws ScriptException {
		Script script = compile(code, name);
		return script.execute(scope);
	}

	/**
	 * Returns a shortened version of the path to the script file. The script
	 * engine can provide its own mechanism, e.g. allowing multiple script
	 * roots, through this one simple method. Returning null means the path
	 * should not be shown to the user.
	 */
	public String[] getScriptPath(File file) {
		ArrayList<String> parts = new ArrayList<String>();
		while (file != null) {
			parts.add(0, file.getName());
			file = file.getParentFile();
		}
		return parts.toArray(new String[parts.size()]);
	}
}
