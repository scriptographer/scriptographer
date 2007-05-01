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
 * File created on 04.12.2004.
 *
 * $Id$
 */

package com.scriptographer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.prefs.Preferences;

import com.scriptographer.adm.Dialog;
import com.scriptographer.adm.MenuItem;
import com.scriptographer.ai.Annotator;
import com.scriptographer.ai.Application;
import com.scriptographer.ai.Document;
import com.scriptographer.ai.LiveEffect;
import com.scriptographer.ai.Timer;
import com.scratchdisk.script.Script;
import com.scratchdisk.script.ScriptCanceledException;
import com.scratchdisk.script.ScriptEngine;
import com.scratchdisk.script.ScriptException;
import com.scratchdisk.script.Callable;
import com.scratchdisk.script.Scope;

/**
 * @author lehni
 */
public class ScriptographerEngine {
	private static File scriptDir = null;
	private static File pluginDir = null;
	private static PrintStream logger = null;

	/**
     * Don't let anyone instantiate this class.
     */
    private ScriptographerEngine() {
	}

	public static void init(String javaPath) throws Exception {
		// Redirect system streams to the console.
		ConsoleOutputStream.enableRedirection(true);

		logger = new PrintStream(new FileOutputStream(new File(javaPath,
			"error.log")), true);
		
		pluginDir = new File(javaPath).getParentFile();

		// This is needed on mac, where there is more than one thread and the
		// Loader is initiated on startup
		// in the second thread. The ScriptographerEngine get loaded through the
		// Loader, so getting the ClassLoader from there is save:
		Thread.currentThread().setContextClassLoader(
				ScriptographerEngine.class.getClassLoader());
		// get the baseDir setting, if it's not set, ask the user
		String dir = ScriptographerEngine.getPreferences(false).get(
			"scriptDir", null);
		// If nothing is defined, try the default place for Scripts: In the
		// plugin's folder
		scriptDir = dir != null ? new File(dir)
			: new File(pluginDir, "scripts");
		// If the specified folder does not exist, ask the user
		if (!scriptDir.exists() || !scriptDir.isDirectory())
			chooseScriptDirectory();

		// Execute all scripts in startup folder:
		if (scriptDir != null)
			executeAll(new File(scriptDir, "startup"));

		// Explicitly initialize all dialogs on startup, as otherwise
		// funny things will happen on CS3 -> see comment in initializeAll
		Dialog.initializeAll();
	}

	public static void destroy() {
		// We're shuting down, so do not display console stuff any more
		ConsoleOutputStream.enableRedirection(false);
		stopAll();
		Dialog.destroyAll();
		LiveEffect.removeAll();
		MenuItem.removeAll();
		Timer.disposeAll();
		Annotator.disposeAll();
		try {
			// This is needed on some versions on Mac CS (CFM?)
			// as the JVM seems to not shoot down properly,
			//and the prefs would then not be flushed to file otherwise.
			ScriptographerEngine.getPreferences(false).flush();
		} catch (java.util.prefs.BackingStoreException e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean chooseScriptDirectory() {
		scriptDir = Dialog.chooseDirectory(
			"Please choose the Scriptographer Script directory:", scriptDir);
		if (scriptDir != null && scriptDir.isDirectory()) {
			ScriptographerEngine.getPreferences(false).put("scriptDir",
				scriptDir.getPath());
			return true;
		}
		return false;
	}

	public static File getPluginDirectory() {
		return pluginDir;
	}

	public static File getScriptDirectory() {
		return scriptDir;
	}
	
	public static Preferences getPreferences(boolean fromScript) {
		if (fromScript && currentFile != null)
			return getPreferences(currentFile);
		// the base prefs for Scriptographer are:
		// com.scriptographer.preferences on mac, three nodes seem to be
		// necessary, otherwise things get mixed up...
		return Preferences.userNodeForPackage(
				ScriptographerEngine.class).node("preferences");
	}

	public static Preferences getPreferences(File file) {
		// determine preferences for the current executing script
		// by walking up the file path to the script directory and using each
		// folder as a preference node.
		Preferences prefs = getPreferences(false).node("scripts");
		ArrayList parts = new ArrayList();
		File root = ScriptographerEngine.getScriptDirectory();
		// collect the directory parts up to root
		do {
			parts.add(file.getName());
			file = file.getParentFile();
		} while (file != null && !file.equals(root));

		for (int i = parts.size() - 1; i >= 0; i--) {
			prefs = prefs.node((String) parts.get(i));
		}
		return prefs;
	}
	
	public static PrintStream getLogger() {
		return logger;
	}

	public static void reportError(Throwable t) {
		String error = t.getMessage();
		PrintStream logger = ScriptographerEngine.getLogger();
		logger.print(error);
		logger.print("Stacktrace: ");
		t.printStackTrace(logger);
		logger.println();
		System.err.print(error);
	}

	static int reloadCount = 0;

	public static int getReloadCount() {
		return reloadCount;
	}

	public static String reload() {
		stopAll();
		reloadCount++;
		return nativeReload();
	}

	public static native String nativeReload();

	static ScriptographerCallback callback;

	public static void setCallback(ScriptographerCallback cback) {
		callback = cback;
		ConsoleOutputStream.setCallback(cback);
	}

	public static void onAbout() {
		callback.onAbout();
	}
	
	private static boolean executing = false;
	private static File currentFile = null;
	private static ArrayList stopScopes = new ArrayList();

	/**
	 * To be called before AI functions are executed
	 */
	private static boolean beginExecution(File file, Scope scope) {
		// Since the interface is done in scripts too, we need to cheat
		// a bit here. When file is set, we ignore the current state
		// of "executing", as we're about to to execute a new script...
		if (!executing || file != null) {
			executing = true;
			Document.beginExecution();
			currentFile = file;
			if (file != null) {
				Application.showProgress("Executing " + (file != null ?
						file.getName() : "Console Input") + "...");
				// Disable output to the console while the script is executed as it
				// won't get updated anyway
				// ConsoleOutputStream.enableOutput(false);

				// Put a script object in the scope to offer the user
				// access to information about it.
				if (scope.get("script") == null)
					scope.put("script", new com.scriptographer.ai.Script(file), true);
			}
			return true;
		}
		return false;
	}

	/**
	 * To be called after AI functions were executed
	 */
	private static void endExecution() {
		if (executing) {
			CommitManager.commit();
			Document.endExecution();
			currentFile = null;
			executing = false;
		}
	}

	/**
	 * Invokes the method on the object, passing the arguments to it and calling
	 * beginExecution before and endExecution after it, which commits all
	 * changes after execution.
	 * 
	 * @param onDraw
	 * @param annotator
	 * @param objects
	 * @throws ScriptException 
	 */
	public static Object invoke(Callable callable, Object obj, Object[] args)
			throws ScriptException {
		boolean started = beginExecution(null, null);
		// Retrieve wrapper object for the native java object, and call the
		// function on it.
		try {
			return callable.call(obj, args);
		} finally {
			// commit all changed objects after a scripting function has been
			// called!
			if (started)
				endExecution();
		}
	}

	public static Object invoke(Callable callable, Object obj)
			throws ScriptException {
		return invoke(callable, obj, new Object[0]);
	}

	/**
	 * executes the specified script file.
	 *
	 * @param file
	 * @return
	 * @throws IOException 
	 * @throws ScriptException 
	 */
	public static Object execute(File file, Scope scope)
			throws ScriptException, IOException {
		ScriptEngine engine = ScriptEngine.getEngineByFile(file);
		if (engine == null)
			throw new ScriptException("Unable to find script engine for " + file);
		Script script = engine.compile(file);
		if (script == null)
			throw new ScriptException("Unable to compile script " + file);
		boolean started = false;
		Object ret = null;
		try {
			if (scope == null)
				scope = script.getEngine().createScope();
			started = ScriptographerEngine.beginExecution(file, scope);
			ret = script.execute(scope);
			if (started) {
				// handle onStart / onStop
				Callable onStart = scope.getCallable("onStart");
				if (onStart != null)
					onStart.call(scope);
				if (scope.getCallable("onStop") != null) {
					// add this scope to the scopes that want onStop to be called
					// when the stop button is hit by the user
					stopScopes.add(scope);
				}
				Application.closeProgress();
			}
		} catch (ScriptException e) {
			ScriptographerEngine.reportError(e);
		} catch (ScriptCanceledException e) {
			System.out.println(file != null ? file.getName() + " canceled" :
				"Execution canceled");
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
					execute(file, null);
				}
			}
		}
	}

	public static void stopAll() {
		Timer.stopAll();
		// Walk through all the stop scopes and call onStop on them:
		for (Iterator it = stopScopes.iterator(); it.hasNext();) {
			Scope scope = (Scope) it.next();
			Callable onStop = scope.getCallable("onStop");
			if (onStop != null) {
				try {
					onStop.call(scope);
				} catch (ScriptException e) {
					ScriptographerEngine.reportError(e);
				}
			}
		}
		stopScopes.clear();
	}
}