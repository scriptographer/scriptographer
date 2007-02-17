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

import com.scriptographer.adm.*;
import com.scriptographer.ai.*;
import com.scriptographer.gui.*;
import com.scriptographer.script.ScriptCanceledException;
import com.scriptographer.script.rhino.ContextFactory;
import com.scriptographer.script.rhino.Debugger;
import com.scriptographer.script.rhino.FunctionHelper;
import com.scriptographer.script.rhino.GlobalObject;
import com.scriptographer.util.StringUtils;

import java.io.*;
import java.util.HashMap;
import java.util.prefs.Preferences;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.WrappedException;
import org.mozilla.javascript.Wrapper;

/**
 * @author lehni
 */
public class ScriptographerEngine {
	private static Context context;
	private static HashMap scriptCache = new HashMap();
	private static GlobalObject global;
	private static long progressCurrent;
	private static long progressMax;
	private static boolean progressAutomatic;
	private static final boolean isWindows, isMacintosh;
	private static ConsoleDialog consoleDialog;
	private static MainDialog mainDialog;
	private static File scriptDir = null;
	private static File pluginDir = null;
	private static PrintStream logger = null;
	private static Debugger debugger = null;

	static {
		String os = System.getProperty("os.name").toLowerCase();
		isWindows = (os.indexOf("windows") != -1);
		isMacintosh = (os.indexOf("mac os x") != -1);
	}

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

		// initialize the JS stuff
		ContextFactory factory = new ContextFactory();
		ContextFactory.initGlobal(factory);

		// The debugger needs to be created before the context, otherwise
		// notification won't work
		// debugger = new ScriptographerDebugger();
		// debugger.attachTo(factory);
		
		context = Context.enter();
		global = new GlobalObject(context);

		// now define the scope provider. Things are a bit intertwingled here...
		// debugger.setScopeProvider(global);

        // This is needed on mac, where there is more than one thread and the
		// Loader is initiated on startup
		// in the second thread. The ScriptographerEngine get loaded through the
		// Loader, so getting the ClassLoader from there is save:
		/* TODO: still needed??
		Thread.currentThread().setContextClassLoader(
			ScriptographerEngine.class.getClassLoader());
		*/
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

		consoleDialog = new ConsoleDialog();
		mainDialog = new MainDialog(consoleDialog);

		// Execute all scripts in startup folder:
		if (scriptDir != null)
			executeAll(new File(scriptDir, "startup"));

		// Explicitly initialize all dialogs on startup, as otherwise
		// funny things will happen on CS3 -> see comment in initializeAll
		Dialog.initializeAll();
	}

	public static void destroy() {
		Dialog.destroyAll();
		LiveEffect.removeAll();
		MenuItem.removeAll();
		Timer.disposeAll();
		Annotator.disposeAll();
		ConsoleOutputStream.enableRedirection(false);
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

	public static boolean isWindows() {
		return isWindows;
	}

	public static boolean isMacintosh() {
		return isMacintosh;
	}
	
	public static Preferences getPreferences(boolean checkScope) {
		// TODO: move code from GlobalScope to ScriptographerEngine
		if (checkScope) {
			Context cx = Context.getCurrentContext();
			if (cx != null && ScriptRuntime.hasTopCall(cx)) {
				Scriptable scope = ScriptRuntime.getTopCallScope(cx);
				return (Preferences) ((Wrapper) ScriptableObject.getProperty(
					scope, "preferences")).unwrap();
			}
		}
		// the base prefs for Scriptographer are:
		// com.scriptographer.scriptographer
		// on mac, three nodes seem to be necessary,
		// otherwise things get mixed up
		return Preferences.userNodeForPackage(ScriptographerEngine.class).node(
			"scriptographer");
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

	public static void onAbout() {
		AboutDialog.show();
	}

	/**
	 * to be called before ai functions are executed
	 */
	public static void beginExecution() {
		Document.beginExecution();
		
	}

	/**
	 * to be called before ai functions are executed
	 */
	public static void endExecution() {
		CommitManager.commit();
		Document.endExecution();
	}

	/**
	 * Internal Class used for caching compiled scripts
	 */
	static class ScriptCacheEntry {
		File file;
		long lastModified;
		Script script;

		ScriptCacheEntry(File file) {
			this.file = file;
			lastModified = -1;
			script = null;
		}

		Script compile() {
			long modified = file.lastModified();
			if (script == null || modified > lastModified) {
				script = null;
				FileReader in = null;
				try {
					in = new FileReader(file);
					script = context.compileReader(in, file.getPath(), 1, null);
					lastModified = modified;
				} catch (RhinoException re) {
					reportError(re);
				} catch (FileNotFoundException ex) {
					Context.reportError("File does not exist: " + file);
				} catch (IOException ioe) {
					Context.reportError(
						"A error occured while reading the file: " + file + 
						": " + ioe.toString());
				} finally {
					if (in != null) {
						try {
							in.close();
						} catch (IOException ioe) {
							System.err.println(ioe.toString());
						}
					}
				}
			}
			return script;
		}
	}
	
	public static String formatError(Throwable t) {
		// TODO: move to ScriptEngine!
		RhinoException re = t instanceof RhinoException ? (RhinoException) t
			: new WrappedException(t);

		String basePath = scriptDir.getAbsolutePath();

		StringWriter buf = new StringWriter();
		PrintWriter writer = new PrintWriter(buf);

		/*
		String source = re.sourceName();
		if (source.startsWith(basePath))
			source = source.substring(basePath.length());
		writer.print("Error at ");
		writer.print(source);
		writer.print(":" + re.lineNumber());
		if (re.columnNumber() > 0)
			writer.print("," + re.columnNumber());
		writer.println();
		writer.println("    " + re.details());
		*/
		writer.println(re.details());
		writer.print(StringUtils.replace(StringUtils.replace(
			re.getScriptStackTrace(), basePath, ""), "\t", "    "));
		
		String error = buf.toString();
		logger.print(error);
		logger.print("Stacktrace: ");
		re.printStackTrace(logger);
		logger.println();
		return error;
	}

	public static void reportError(Throwable t) {
		System.err.print(formatError(t));
	}

	/**
	 * Compiles the specified file.
	 * Caching for the compiled scripts is used for speed increase.
	 * 
	 * @param file
	 * @return
	 */
	public static Script compileFile(File file) {
		String path = file.getPath();
		ScriptCacheEntry entry = (ScriptCacheEntry) scriptCache.get(path);
		if (entry == null) {
			entry = new ScriptCacheEntry(file);
			scriptCache.put(path, entry);
		}
		return entry.compile();
	}

	public static Script compileString(String string) {
		try {
			return context.compileString(string, null, 1, null);
		} catch (RhinoException re) {
			reportError(re);
		}
		return null;
	}

	public static Scriptable executeScript(Script script, File scriptFile,
			Scriptable scope) {
		Scriptable ret = null;
		try {
			showProgress("Executing " + (scriptFile != null ?
					scriptFile.getName() : "Console Input") + "...");

			if (scope == null)
				scope = global.createScope(scriptFile);
			// disable output to the console while the script is executed as it
			// won't get updated anyway
			// ConsoleOutputStream.enableOutput(false);
			
			beginExecution();
			script.exec(context, scope);
			// handle onStart / onStop
			FunctionHelper.callFunction(scope, "onStart");
			Object onStop = scope.get("onStop", scope);
			if (onStop instanceof Function) {
				// add this scope to the scopes that want onStop to be called
				// when the stop button is hit by the user
				// TODO: finish this
			}
			ret = scope;
			closeProgress();
		} catch (RhinoException e) {
			reportError(e);
		} catch (ScriptCanceledException e) {
			System.out.println(scriptFile.getName() + " Canceled");
		} finally {
			// commit all the changes, even when script has crashed (to synch
			// with
			// direct changes such as creation of paths, etc
			endExecution();
			// now reenable the console, this also writes out all the things
			// that were printed in the meantime:
			// ConsoleOutputStream.enableOutput(true);
		}
		return ret;
	}

	/**
	 * executes all scripts in the given folder
	 *
	 * @param dir
	 */
	private static void executeAll(File dir) {
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
	 */
	public static Scriptable executeFile(File file, Scriptable scope) {
		Script script = compileFile(file);
		if (script != null)
			return executeScript(script, file, scope);
		return null;
	}

	public static Scriptable executeFile(String path, Scriptable scope) {
		return executeFile(new File(path), scope);
	}

	public static Scriptable executeString(String string, Scriptable scope) {
		Script script = compileString(string);
		if (script != null)
			return executeScript(script, null, scope);
		return null;
	}

	public static Scriptable createScope(File scriptFile) {
		return global.createScope(scriptFile);
	}

	public static Object javaToJS(Object value) {
		return Context.javaToJS(value, global);
	}

	/**
	 * Launches the filename with the default associated editor.
	 * 
	 * @param filename
	 * @return
	 */

	public static native boolean launch(String filename);

	public static boolean launch(File file) {
		return launch(file.getPath());
	}

	public static native long getNanoTime();

	private static native void nativeShowProgress(String text);
	
	public static void showProgress(String text) {
		progressAutomatic = true;
		progressCurrent = 0;
		progressMax = 1 << 8;
		nativeUpdateProgress(progressCurrent, progressMax);
		nativeShowProgress(text);
	}
	
	private static native boolean nativeUpdateProgress(long current, long max);
	
	public static void initDebugger() {
		// debugger.setVisible(true);
	}

	public static boolean updateProgress(long current, long max) {
		progressCurrent = current;
		progressMax = max;
		progressAutomatic = false;
		return nativeUpdateProgress(current, max);
	}
	
	public static boolean updateProgress() {
		boolean ret = nativeUpdateProgress(progressCurrent, progressMax);
		if (progressAutomatic) {
			progressCurrent++;
			progressMax++;
		}
		return ret;
	}
	
	public static native void dispatchNextEvent();
	
	protected static native boolean closeProgress();
}