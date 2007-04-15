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

package com.scratchdisk.script;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;

/**
 * @author lehni
 *
 */
public abstract class ScriptEngine {
	private static HashMap enginesByName = new HashMap();
	private static HashMap enginesByExtension = new HashMap();
	private static boolean loaded = false;

	private HashMap scriptCache = new HashMap();

	public static void loadEngines() {
		// Do not call loadEngines immediatelly, as we want the scripting engines
		// to be instanciated in the same thread as from where they are used...
		// TODO: Move to a multi threaded scenario, where RhinoEngine creates
		// Contexts for reach script call...
		URL url = ScriptEngine.class.getResource("/META-INF/services/" +
					ScriptEngine.class.getName());
		if (url != null) {
			try {
				BufferedReader buffer = new BufferedReader(
						new InputStreamReader(url.openStream()));
				for (String str = buffer.readLine(); str != null;
						str = buffer.readLine()) {
					try {
						Class.forName(str).newInstance();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			loaded = true;
		}
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

	public abstract Scope createScope();

	public abstract Scope getScope(Object obj);

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

	public abstract void evaluate(String string, Scope scope)
			throws ScriptException;

	public static double toDouble(Object val) {
		if (val instanceof Number)
			return ((Number) val).doubleValue();
		if (val == null)
			return +0.0;
		if (val instanceof String)
            return Double.valueOf((String) val).doubleValue();
		if (val instanceof Boolean)
			return ((Boolean) val).booleanValue() ? 1 : +0.0;
		return Double.NaN;
	}

	public static int toInt(Object val) {
        if (val instanceof Integer)
            return ((Integer) val).intValue();
        else return (int) Math.round(toDouble(val));
	}

	public static boolean toBoolean(Object val) {
        if (val instanceof Boolean)
            return ((Boolean) val).booleanValue();
        if (val == null)
            return false;
        if (val instanceof String)
            return ((String) val).length() != 0;
        if (val instanceof Number) {
            double d = ((Number) val).doubleValue();
            return (d == d && d != 0.0);
        }
        return true;
	}
}
