/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2008 Juerg Lehni, http://www.scratchdisk.com.
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
 * $Id:ScriptographerEngine.java 402 2007-08-22 23:24:49Z lehni $
 */

package com.scriptographer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.prefs.Preferences;

import com.scriptographer.ai.Annotator;
import com.scriptographer.ai.Document;
import com.scriptographer.ai.LiveEffect;
import com.scriptographer.sg.Script;
import com.scriptographer.sg.Timer;
import com.scriptographer.ui.Dialog;
import com.scriptographer.ui.MenuItem;
import com.scratchdisk.script.ScriptCanceledException;
import com.scratchdisk.script.ScriptEngine;
import com.scratchdisk.script.ScriptException;
import com.scratchdisk.script.Callable;
import com.scratchdisk.script.Scope;
import com.scratchdisk.util.ClassUtils;
import com.scratchdisk.util.StringUtils;

/**
 * @author lehni
 */
public class ScriptographerEngine {
	private static File scriptDir = null;
	private static File pluginDir = null;
	private static PrintStream errorLogger = null;
	private static PrintStream consoleLogger = null;
	private static Thread mainThread;
	private static ArrayList<Scope> initScopes;

	protected static final int EVENT_APP_STARTUP = 0;
	protected static final int EVENT_APP_SHUTDOWN = 1;
	protected static final int EVENT_APP_ACTIVATED = 2;
	protected static final int EVENT_APP_DEACTIVATED = 3;
	protected static final int EVENT_APP_ABOUT = 4;

	private static String[] callbackNames = {
		"onStartup",
		"onShutdown",
		"onActivate",
		"onDeactivate",
		"onAbout"
	};

	/**
     * Don't let anyone instantiate this class.
     */
    private ScriptographerEngine() {
	}

    public static void init(String pluginPath) throws Exception {
		mainThread = Thread.currentThread();
		// Redirect system streams to the console.
		ConsoleOutputStream.enableRedirection(true);

		pluginDir = new File(pluginPath);

		errorLogger = getLogger("java.log");
		consoleLogger = getLogger("console.log");

		// This is needed on Mac, where there is more than one thread and the
		// Loader is initiated on startup
		// in the second thread. The ScriptographerEngine get loaded through the
		// Loader, so getting the ClassLoader from there is save:
		Thread.currentThread().setContextClassLoader(
				ScriptographerEngine.class.getClassLoader());
		// Collect all init scripts and compile them to scopes.
		// These are then used to call onStartup / onPostStartup callbacks
		initScopes = new ArrayList<Scope>();
		// Collect core code, if it exists
		File coreDir = new File(pluginDir, "core");
		if (coreDir.isDirectory())
			collectInitScripts(coreDir, initScopes);
		// Collect all __init__ scripts in the Script folder:
		if (scriptDir != null)
			collectInitScripts(scriptDir, initScopes);
	}

	public static void destroy() {
		// We're shutting down, so do not display console stuff any more
		ConsoleOutputStream.enableRedirection(false);
		stopAll();
		Dialog.destroyAll();
		LiveEffect.removeAll();
		MenuItem.removeAll();
		Timer.disposeAll();
		Annotator.disposeAll();
		try {
			// This is needed on some versions on Mac CS (CFM?)
			// as the JVM seems to not shoot down properly, and the
			// preferences would then not be flushed to file otherwise.
			getPreferences(false).flush();
		} catch (java.util.prefs.BackingStoreException e) {
			throw new RuntimeException(e);
		}
	}

	public static File getPluginDirectory() {
		return pluginDir;
	}

	public static File getScriptDirectory() {
		return scriptDir;
	}

	public static void setScriptDirectory(File dir) {
		scriptDir = dir;
	}

	public static Preferences getPreferences(boolean fromScript) {
		if (fromScript && currentFile != null)
			return getPreferences(currentFile);
		// the base preferences for Scriptographer are:
		// com.scriptographer.preferences on Mac, three nodes seem
		// to be necessary, otherwise things get mixed up...
		return Preferences.userNodeForPackage(
				ScriptographerEngine.class).node("preferences");
	}

	public static Preferences getPreferences(File file) {
		// determine preferences for the current executing script
		// by walking up the file path to the script directory and 
		// using each folder as a preference node.
		ArrayList<String> parts = new ArrayList<String>();
		Preferences prefs = getPreferences(false);
		// Collect the directory parts up to either scriptDir or pluginDir
		while (true) {
			parts.add(file.getName());
			file = file.getParentFile();
			if (file == null || file.equals(pluginDir)) {
				break;
			} else if (file.equals(scriptDir)) {
				// Script files use the scripts preference node,
				// all others (including GUI scripts) use the main node.
				prefs = prefs.node("scripts");
				break;
			}
		}
		// Now walk backwards per added folder element and produce sub nodes
		for (int i = parts.size() - 1; i >= 0; i--)
			prefs = prefs.node(parts.get(i));
		return prefs;
	}

    private static PrintStream getLogger(String name) {
		try {
			File logDir = new File(pluginDir, "log");
			if (!logDir.exists())
				logDir.mkdir();
			return new PrintStream(new FileOutputStream(new File(logDir, name)), true);
		} catch (Exception e) {
			// Not allowed to make this log directory or file, so don't log...
		}
		return null;
    }

	public static void logError(Throwable t) {
		if (errorLogger != null) {
			errorLogger.println(new Date());
			t.printStackTrace(errorLogger);
			errorLogger.println();
		}
	}
	
	public static void logError(String str) {
		if (errorLogger != null) {
			errorLogger.println(new Date());
			errorLogger.println(str);
		}
	}

	protected static void logConsole(String str) {
		if (consoleLogger != null)
			consoleLogger.println(str);
	}

	public static void reportError(Throwable t) {
		try {
			String error;
			Throwable cause;
			if (t instanceof ScriptException) {
				error = ((ScriptException) t).getFullMessage();
				cause = ((ScriptException) t).getWrappedException();
			} else {
				error = t.getMessage();
				if (error == null)
					error = t.toString();
				cause = t.getCause();
			}
			// Simplify error messages for Wrapped ScriptographerExceptions:
			if (cause instanceof ScriptographerException)
				error = "Error: " + error;
			else if (cause instanceof UnsupportedOperationException)
				error = "Unsupported Operation: " + error;
			// Shorten file names by removing the script directory form it
			if (scriptDir != null)
				error = StringUtils.replace(error, scriptDir.getAbsolutePath()
						+ System.getProperty("file.separator"), "");
			// Add a line break at the end if the error does
			// not contain one already.
			if (!error.endsWith(System.getProperty("line.separator")))
			// TODO: find out why this regular expression does not work and make it work instead:
//			if (!Pattern.compile("(?:\\n\\r|\\n|\\r)$").matcher(error).matches())
				error +=  System.getProperty("line.separator");
			System.err.print(error);
			logError(t);
		} catch (Throwable e) {
			// Report an error in reportError code...
			// This should not happen!
			e.printStackTrace();
		}
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

	public static void setCallback(ScriptographerCallback cb) {
		ConsoleOutputStream.setCallback(cb);
	}
	
	private static boolean executing = false;
	private static File currentFile = null;
	private static ArrayList<Script> stopScripts = new ArrayList<Script>();
	private static boolean allowScriptCancelation = true;

	/**
	 * To be called before AI functions are executed
	 */
	private static boolean beginExecution(File file, Scope scope) {
		// Since the interface is done in scripts too, we need to cheat
		// a bit here. When file is set, we ignore the current state
		// of "executing", as we're about to to execute a new script...
		if (!executing || file != null) {
			if (!executing)
				Document.beginExecution();
			// Disable output to the console while the script is
			// executed as it won't get updated anyway
			// ConsoleOutputStream.enableOutput(false);
			executing = true;

			Script script = null;
			if (file != null) {
				currentFile = file;
				// Put a script object in the scope to offer the user
				// access to information about it.
				script = (Script) scope.get("script");
				if (script == null) {
					script = new Script(file);
					scope.put("script", script, true);
				}
			}
			if (file == null || script.getShowProgress() && !file.getName().startsWith("__"))
				showProgress(file != null ? "Executing " + file.getName() + "..." : "Executing...");
			return true;
		}
		return false;
	}

	/**
	 * To be called after AI functions were executed
	 */
	private static void endExecution() {
		if (executing) {
			try {
				CommitManager.commit();
			} catch(Throwable t) {
				ScriptographerEngine.reportError(t);
			}
			Document.endExecution();
			closeProgress();
			currentFile = null;
			executing = false;
		}
	}

	/**
	 * Invokes the method on the object, passing the arguments to it and calling
	 * beginExecution before and endExecution after it, which commits all
	 * changes after execution.
	 */
	public static Object invoke(Callable callable, Object obj, Object... args) {
		boolean started = beginExecution(null, null);
		// Retrieve wrapper object for the native java object, and
		// call the function on it.
		Throwable throwable = null;
		try {
			return callable.call(obj, args);
		} catch (Throwable t) {
			throwable = t;
		} finally {
			// commit all changed objects after a scripting function
			// has been called!
			if (started)
				endExecution();
		}
		if (throwable != null)
			handleException(throwable, null);
		return null;
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
		com.scratchdisk.script.Script script = engine.compile(file);
		if (script == null)
			throw new ScriptException("Unable to compile script " + file);
		boolean started = false;
		Object ret = null;
		Throwable throwable = null;
		try {
			if (scope == null)
				scope = script.getEngine().createScope();
			started = beginExecution(file, scope);
			ret = script.execute(scope);
			if (started) {
				// handle onStart / onStop
				Script scriptObj = (Script) scope.get("script");
				Callable onStart = scriptObj.getOnStart();
				if (onStart != null)
					onStart.call(scriptObj);
				if (scriptObj.getOnStop() != null) {
					// add this scope to the scopes that want onStop to be called
					// when the stop button is hit by the user
					stopScripts.add(scriptObj);
				}
			}
		} catch (Throwable t) {
			throwable = t;
		} finally {
			// commit all the changes, even when script has crashed,
			// to synch with direct changes such as creation of paths,
			// etc
			if (started)
				endExecution();
		}
		if (throwable != null)
			handleException(throwable, file);
		return ret;
	}

	protected static void handleException(Throwable t, File file) {
		// Do not allow script cancellation during error reporting,
		// as this is now handled by scripts too
		allowScriptCancelation = false;
		// Unwrap ScriptCanceledExceptions
		Throwable cause = t.getCause();
		if (cause instanceof ScriptCanceledException)
			t = cause;
		if (t instanceof ScriptException) {
			ScriptographerEngine.reportError(t);
		} else if (t instanceof ScriptCanceledException) {
			System.out.println(file != null ? file.getName() + " canceled"
					: "Execution canceled");
		}
		allowScriptCancelation = true;
	}

	/**
	 * Executes all scripts named __init__.* in the given folder and collects the resulting scopes for callback
	 *
	 * @param dir
	 * @throws IOException 
	 * @throws ScriptException 
	 */
	public static void collectInitScripts(File dir, ArrayList<Scope> scopes) {
		File []files = dir.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				String name = file.getName();
				if (file.isDirectory() && !name.startsWith(".")
						&& !name.equals("CVS")) {
					collectInitScripts(file, scopes);
				} else if (name.startsWith("__init__")) {
					try {
						ScriptEngine engine = ScriptEngine.getEngineByFile(file);
						if (engine == null)
							throw new ScriptException("Unable to find script engine for " + file);
						// Execute in the tool's scope so setIdleEventInterval can be called
						Scope scope = engine.createScope();
						execute(file, scope);
						scopes.add(scope);
					} catch (Exception e) {
						reportError(e);
					}
				}
			}
		}
	}

	public static void stopAll() {
		Timer.stopAll();
		// Walk through all the stop scopes and call onStop on them:
		for (Iterator it = stopScripts.iterator(); it.hasNext();) {
			Script script = (Script) it.next();
			Callable onStop = script.getOnStop();
			if (onStop != null) {
				try {
					onStop.call(script);
				} catch (ScriptException e) {
					reportError(e);
				}
			}
		}
		stopScripts.clear();
	}


	/**
	 * Launches the filename with the default associated editor.
	 * 
	 * @param filename
	 */
	public static native boolean launch(String filename);

	public static boolean launch(File file) {
		return launch(file.getPath());
	}

	/**
	 * Returns the current system time in nano seconds.
	 * This is very useful for high resolution time measurements.
	 * @return the current system time.
	 */
	public static native long getNanoTime();

	private static long progressCurrent;
	private static long progressMax;
	private static boolean progressAutomatic = false;
	private static boolean progressVisible = false;

	private static native void nativeSetProgressText(String text);

	public static void showProgress() {
		progressVisible = true;
		progressAutomatic = true;
		progressCurrent = 0;
		progressMax = 1 << 8;
		nativeUpdateProgress(progressCurrent, progressMax, true);
	}

	public static void showProgress(String text) {
		showProgress();
		nativeSetProgressText(text);
	}
	
	private static native boolean nativeUpdateProgress(long current, long max, boolean visible);

	public static  boolean updateProgress(long current, long max) {
		if (progressVisible) {
			progressCurrent = current;
			progressMax = max;
			progressAutomatic = false;
		}
		boolean ret = nativeUpdateProgress(current, max, progressVisible);
		return !allowScriptCancelation || ret;
	}

	public static boolean updateProgress() {
		if (isMainThreadActive()) {
			boolean ret = nativeUpdateProgress(progressCurrent, progressMax, progressVisible);
			if (progressVisible && progressAutomatic) {
				progressCurrent++;
				progressMax++;
			}
			return !allowScriptCancelation || ret;
		} else {
			return true;
		}
	}

	private static native void nativeCloseProgress();

	public static void closeProgress() {
		progressVisible  = false;
		nativeCloseProgress();
	}

	public static boolean isMainThreadActive() {
		return Thread.currentThread().equals(mainThread);
	}

	/**
	 * @jshide
	 */
	public static native void dispatchNextEvent();

	private static final boolean isWindows, isMacintosh;

	static {
		String os = System.getProperty("os.name").toLowerCase();
		isWindows = (os.indexOf("windows") != -1);
		isMacintosh = (os.indexOf("mac os x") != -1);
	}

	public static boolean isWindows() {
		return isWindows;
	}

	public static boolean isMacintosh() {
		return isMacintosh;
	}

	public static native String getApplicationVersion();

	public static native int getApplicationRevision();

	private static String version = null;
	private static int revision = -1;

	public static String getPluginVersion() {
		if (version == null)
			readVersion();
		return version;
	}

	public static int getPluginRevision() {
		if (revision == -1)
			readVersion();
		return revision;
	}

	private static void readVersion() {
		String[] lines = ClassUtils.getServiceInformation(ScriptographerEngine.class);
		if (lines != null) {
			version = lines[0];
			revision = Integer.parseInt(lines[1]);
		}
	}

	/**
	 * To be called from the native environment.
	 */

	@SuppressWarnings("unused")
	private static void onHandleEvent(int type) throws Exception {
		// Loop through all compiled init script scopes and see if a callback
		// function for the given event type exists. If so, call it.
		// This is used to install tools onStartup and GUI stuff onPostStartup
		// It is also used to call onActivate / onDeactivate
		for (Scope scope : initScopes) {
			Callable callable = scope.getCallable(callbackNames[type]);
			if (callable != null) {
				try {
					callable.call(scope);
				} catch (ScriptException e) {
					reportError(e);
				}
			}
		}
		// Explicitly initialize all dialogs after startup, as otherwise
		// funny things will happen on CS3 -> see comment in initializeAll
		if (type == EVENT_APP_STARTUP)
			Dialog.initializeAll();
	}
}