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
 * File created on 04.12.2004.
 *
 * $RCSfile: ScriptographerEngine.java,v $
 * $Author: lehni $
 * $Revision: 1.5 $
 * $Date: 2005/03/10 22:48:43 $
 */

package com.scriptographer;

import com.scriptographer.adm.*;
import com.scriptographer.ai.*;
import com.scriptographer.gui.*;

import org.mozilla.javascript.*;

import java.io.*;
import java.util.HashMap;
import java.util.prefs.Preferences;

public class ScriptographerEngine {
	public static File baseDir = null;

	private static ScriptographerEngine engine = null;
	private Context context;
	private HashMap scriptCache = new HashMap();
	private GlobalScope global;
	public static final boolean isWindows, isMacOSX;

	static {
		// immediatelly redirect system streams.
		ConsoleOutputStream.getInstance().enableRedirection(true);
		// getSystem variables
		String os = System.getProperty("os.name").toLowerCase();
		isWindows = (os.indexOf("windows") != -1);
		isMacOSX = (os.indexOf("mac os x") != -1);
	}

	public ScriptographerEngine() throws Exception {
		// create the context
		context = Context.enter();
		global = new GlobalScope(context);
	}

	public static void init() throws Exception {
		// add the menus:
		// use a space in the beginning of the name so it appears on top of all entries :)
		MenuItem mainItem = new MenuItem(MenuGroup.GROUP_TOOL_PALETTES, " Scriptographer");
		MenuItem reloadItem = new MenuItem(mainItem, "Reload") {
			public void onExecute() {
				String errors = ScriptographerEngine.reload();
				if (errors != null) {
					System.err.println(errors);
				} else {
					System.out.println("The classpath was successfully reloaded.");
				}
			}
		};
		
		// get the baseDir setting, if it's not set, ask the user
		Preferences prefs = Preferences.userNodeForPackage(ScriptographerEngine.class); 
		String dir = prefs.get("baseDir", null);
		baseDir = dir != null ? new File(dir) : null;
		if (baseDir == null || !baseDir.isDirectory()) {
			baseDir = Dialog.chooseDirectory("Please choose the Scriptographer base directory:");
			if (baseDir != null && baseDir.isDirectory()) {
				prefs.put("baseDir", baseDir.getPath());
			}
		}
		
		new ConsoleWindow();
		new MainWindow();
		ConsoleOutputStream.enableOutput(true);
		// execute all scripts in startup folder:
		if (baseDir != null)
			getInstance().executeAll(new File(baseDir, "startup"));
	}

	public static void destroy() {
		Dialog.destroyAll();
		LiveEffect.removeAll();
		MenuItem.removeAll();
		ConsoleOutputStream.getInstance().enableRedirection(false);
	}
	
	private static native String reload();

	public static ScriptographerEngine getInstance() throws Exception {
		if (engine == null)
			engine = new ScriptographerEngine();
		return engine;
	}
	
	private void reportRhinoException(RhinoException re) {
		String source = re.sourceName();
		if (source != null) {
			System.err.print(source);
			System.err.print(":");
		}
		System.err.println(re.lineNumber() + "," + re.columnNumber() + ": " + re.getMessage());
	}

	public Script compileFile(File file) {
		FileReader in = null;
		Script script = null;
		try {
			in = new FileReader(file);
			script = context.compileReader(in, file.getPath(), 1, null);
		} catch (RhinoException re) {
			reportRhinoException(re);
		} catch (FileNotFoundException ex) {
			Context.reportError("Couldn't open file \"" + file + "\".");
		} catch (IOException ioe) {
			System.err.println(ioe.toString());
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ioe) {
					System.err.println(ioe.toString());
				}
			}
		}
		return script;
	}

	public Script compileString(String string) {
		try {
			return context.compileString(string, null, 1, null);
		} catch (RhinoException re) {
			reportRhinoException(re);
		}
		return null;
	}

	public Scriptable executeScript(Script script, Scriptable scope) {
		Scriptable ret = null;
		try {
			// This is needed on mac, where there is more than one thread and the Loader is initiated on startup
			// in the second thread. The ScriptographerEngine get loaded through the Loader, so getting the
			// ClassLoader from there is save:
			Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
			if (scope == null)
				scope = global.createScope();
			// disable output to the console while the script is executed as it won't get updated anyway
			ConsoleOutputStream.enableOutput(false);
			script.exec(context, scope);
			// now commit all the changes:
			CommitManager.commit();
			ret = scope;
		} catch (WrappedException we) {
			System.err.println(we.getMessage());
			we.getWrappedException().printStackTrace();
		} catch (RhinoException re) {
			reportRhinoException(re);
		} finally {
			// now reenable the console, this also writes out all the things that were printed in the meantime:
			ConsoleOutputStream.enableOutput(true);
		}
		return ret;
	}

	/**
	 * executes all scripts in the given folder
	 *
	 * @param dir
	 */
	private void executeAll(File dir) {
		File []files = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			if (file.isDirectory()) {
				executeAll(file);
			} else if (file.getName().endsWith(".js")) {
				executeFile(file, null);
			}
		}
	}

	/**
	 * Internal Class used for caching scripts
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

		Scriptable execute(Scriptable scope) {
			long modified = file.lastModified();
			if (script == null || modified > lastModified) {
				lastModified = modified;
				script = ScriptographerEngine.this.compileFile(file);
			}
			if (script != null) {
				return ScriptographerEngine.this.executeScript(script, scope);
			} else {
				return null;
			}
		}
	}

	/**
	 * evaluates the specified file. Caching for the compiled scripts is used for speed increase
	 *
	 * @param file
	 * @return
	 */
	public Scriptable executeFile(File file, Scriptable scope) {
		String path = file.getPath();
		ScriptCacheEntry entry = (ScriptCacheEntry) scriptCache.get(path);
		if (entry == null) {
			entry = new ScriptCacheEntry(file);
			scriptCache.put(path, entry);
		}
		return entry.execute(scope);
	}

	public Scriptable executeFile(String path, Scriptable scope) {
		return executeFile(new File(path), scope);
	}

	public Scriptable executeString(String string, Scriptable scope) {
		Script script = compileString(string);
		if (script != null)
			return executeScript(script, scope);
		return null;
	}
	
	public Scriptable createScope() {
		return global.createScope();
	}

	public static void main(String args[]) throws Exception {
		ConsoleOutputStream.getInstance().enableRedirection(false);
	 	getInstance().executeFile("/Users/Lehni/Development/C & C++/Scriptographer/scripts/test.js", null);
	}
}