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
 * File created on 04.12.2004.
 */

package com.scriptographer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Stack;
import java.util.prefs.Preferences;

import com.scratchdisk.script.Callable;
import com.scratchdisk.script.Scope;
import com.scratchdisk.script.ScriptCanceledException;
import com.scratchdisk.script.ScriptEngine;
import com.scratchdisk.script.ScriptException;
import com.scratchdisk.util.ClassUtils;
import com.scratchdisk.util.ConversionUtils;
import com.scriptographer.ai.Annotator;
import com.scriptographer.ai.Dictionary;
import com.scriptographer.ai.Document;
import com.scriptographer.ai.LiveEffect;
import com.scriptographer.sg.Script;
import com.scriptographer.ui.Dialog;
import com.scriptographer.ui.KeyCode;
import com.scriptographer.ui.KeyEvent;
import com.scriptographer.ui.MenuItem;
import com.scriptographer.ui.Timer;

/**
 * @author lehni
 */
public class ScriptographerEngine {
	private static File pluginDir = null;
	private static File coreDir = null;
	private static File[] scriptDirectories = null;
	private static PrintStream errorLogger = null;
	private static PrintStream consoleLogger = null;
	private static Thread mainThread;

	private static HashMap<String, ArrayList<Scope>> callbackScopes;

	// All callback functions to be found and collected in the compiled scopes.
	private static String[] callbackNames = {
		"onStartup",
		"onShutdown",
		"onActivate",
		"onDeactivate",
		"onAbout",

		"onOwlDragBegin",
		"onOwlDragEnd",

		"onKeyDown",
		"onKeyUp",

		"onStop"
	};

	// App Events. Their numbers need to match calbackNames indices.
	public static final int EVENT_APP_STARTUP = 0;
	public static final int EVENT_APP_SHUTDOWN = 1;
	public static final int EVENT_APP_ACTIVATED = 2;
	public static final int EVENT_APP_DEACTIVATED = 3;
	public static final int EVENT_APP_ABOUT = 4;
	public static final int EVENT_OWL_DRAG_BEGIN = 5;
	public static final int EVENT_OWL_DRAG_END = 6;

	// Key Events. Their numbers need to match calbackNames indices.
	public static final int EVENT_KEY_DOWN = 7;
	public static final int EVENT_KEY_UP = 8;

	/**
	 * Don't let anyone instantiate this class.
	 */
	private ScriptographerEngine() {
	}

	public static void init(String pluginPath) {
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
		// Compile all core init scripts
		callbackScopes = new HashMap<String, ArrayList<Scope>>();
		coreDir = new File(new File(pluginDir, "Core"), "JavaScript");
		if (coreDir.isDirectory()) {
			// Load the core libraries first.
			loadLibraries(new File(coreDir, "lib"));
			compileInitScripts(coreDir);
		}
	}

	public static void destroy() {
		// We're shutting down, so do not display console stuff any more
		ConsoleOutputStream.enableOutput(false);
		ConsoleOutputStream.enableRedirection(false);
		stopAll(true, true);
		LiveEffect.removeAll();
		MenuItem.removeAll();
		Annotator.disposeAll();
		try {
			// This is needed on some versions on Mac CS (CFM?)
			// as the JVM seems to not shoot down properly, and the
			// preferences would then not be flushed to file otherwise.
			getPreferences(null).flush();
		} catch (java.util.prefs.BackingStoreException e) {
			throw new RuntimeException(e);
		}
	}

	public static File getPluginDirectory() {
		return pluginDir;
	}

	public static void setScriptDirectories(File[] directories) {
		scriptDirectories = directories;
		// When setting script directories for error reporting, also compile
		// init scripts within them.
		for (int i = 0, l = scriptDirectories.length; i < l; i++) {
			compileInitScripts(scriptDirectories[i]);
		}
	}

	public static String[] getScriptPath(File file, boolean hideCore) {
		ArrayList<String> parts = new ArrayList<String>();
		boolean loop = true;
		while (loop) {
			parts.add(0, file.getName());
			file = file.getParentFile();
			if (file == null || file.equals(pluginDir))
				break;
			if (hideCore && file.equals(coreDir))
				return null;
			if (scriptDirectories != null) {
				for (int i = 0, l = scriptDirectories.length; i < l; i++) {
					if (file.equals(scriptDirectories[i])) {
						// Add the script directory name itself too.
						parts.add(0, file.getName());
						loop = false;
						break;
					}
				}
			}
					
		}
		return parts.toArray(new String[parts.size()]);
	}

	/**
	 * Executes all scripts named __init__.* in the given folder
	 * 
	 * @param dir
	 * @throws IOException
	 * @throws ScriptException
	 */
	protected static void compileInitScripts(File dir) {
		File []files = dir.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				String name = file.getName();
				if (file.isDirectory() && !name.startsWith(".")
						&& !name.equals("CVS")) {
					compileInitScripts(file);
				} else if (name.startsWith("__init__")) {
					try {
						ScriptEngine engine =
								ScriptEngine.getEngineByFile(file);
						if (engine == null)
							throw new ScriptException(
									"Unable to find script engine for " + file);
						execute(file, engine.createScope());
					} catch (Exception e) {
						reportError(e);
					}
				}
			}
		}
	}

	protected static void loadLibraries(File dir) {
		File[] files = dir.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				String name = file.getName();
				if (file.isDirectory() && !name.startsWith(".")
						&& !name.equals("CVS")) {
					loadLibraries(file);
				} else {
					try {
						ScriptEngine engine =
								ScriptEngine.getEngineByFile(file);
						if (engine != null)
							execute(file, engine.getGlobalScope());
					} catch (Exception e) {
						reportError(e);
					}
				}
			}
		}
	}

	public static Preferences getPreferences(Script script) {
		// The base preferences for Scriptographer are:
		// com.scriptographer.preferences on Mac, three nodes seem
		// to be necessary, otherwise things get mixed up...
		Preferences prefs = Preferences.userNodeForPackage(
				ScriptographerEngine.class).node("preferences");
		if (script == null)
			return prefs;
		// Determine preferences for the current executing script
		// by walking up the file path to the script directory and 
		// using each folder as a preference node.
		File file = script.getFile();
		/*
		ArrayList<String> parts = new ArrayList<String>();
		// Collect the directory parts up to either coreDir or pluginDir and see
		// which preference node is to be used. Non-core script files are placed
		// in the "scripts" node, all others use the main node.
		boolean useScriptsPrefs = true;
		while (useScriptsPrefs && file != null) {
			parts.add(file.getName());
			file = file.getParentFile();
			if (file != null && (file.equals(pluginDir) || file.equals(coreDir)))
				useScriptsPrefs = false;
		}
		if (useScriptsPrefs)
			prefs = prefs.node("scripts");
		// Now walk backwards per added folder element and produce sub nodes
		for (int i = parts.size() - 1; i >= 0; i--)
			prefs = prefs.node(parts.get(i));
		*/
		String[] parts = getScriptPath(file, false);
		for (int i = 0, l = parts.length; i < l; i++)
			prefs = prefs.node(parts[i]);
		return prefs;
	}

	private static PrintStream getLogger(String name) {
		try {
			File logDir = new File(pluginDir, "Logs");
			if (!logDir.exists())
				logDir.mkdir();
			return new PrintStream(
					new FileOutputStream(new File(logDir, name)), true);
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
			errorLogger.flush();
		}
	}
	
	public static void logError(String str) {
		if (errorLogger != null) {
			errorLogger.println(new Date());
			errorLogger.println(str);
			errorLogger.flush();
		}
	}

	public static void logConsole(String str) {
		if (consoleLogger != null) {
			consoleLogger.println(str);
			consoleLogger.flush();
		}
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
			/*
			// TODO:
			if (scriptsDir != null)
				error = StringUtils.replace(error, scriptsDir.getAbsolutePath()
						+ System.getProperty("file.separator"), "");
			*/
			// Add a line break at the end if the error does
			// not contain one already.
			// TODO: find out why this regular expression does not work and make
			// it work instead:
			// if (!Pattern.compile("(?:\\n\\r|\\n|\\r)$").matcher(error).matches())
			String lineBreak = System.getProperty("line.separator");
			if (!error.endsWith(lineBreak))
				error +=  lineBreak;
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
		stopAll(true, true);
		reloadCount++;
		return nativeReload();
	}

	public static native String nativeReload();

	public static void setCallback(ScriptographerCallback cb) {
		ConsoleOutputStream.setCallback(cb);
	}
	
	private static Stack<Script> scriptStack = new Stack<Script>();
	private static boolean allowScriptCancelation = true;
	private static boolean executionHasCommitted = false;

	public static Script getCurrentScript() {
		// There can be 'holes' in the script stack, so find the first non-null
		// entry and return it.
		for (int i = scriptStack.size() - 1; i >= 0; i--) {
			Script last = scriptStack.get(i);
			if (last != null)
				return last;
		}
		return null;
	}


	/**
	 * To be called before AI functions are executed as scripts
	 */
	public static void beginExecution(File file, Scope scope) {
		// Since the interface is done in scripts too and we receive being /
		// endExecution events for all UI notifications as well, we need to
		// cheat a bit here.
		// When file is set, we ignore the current state of "executing",
		// as we're about to to execute a new script...
		Script script = scope != null ? (Script) scope.get("script") : null;
		// Only call Document.beginExecution if it has not already
		// been called through the UI notification callback.
		if (scriptStack.empty()) {
			executionHasCommitted  = false;
			Document.beginExecution();
			// Disable output to the console while the script is
			// executed as it won't get updated anyway
			// ConsoleOutputStream.enableOutput(false);
		}
		if (file != null) {
			Dialog.destroyAll(false, false);
			Timer.abortAll(false, false);
			// Put a script object in the scope to offer the user
			// access to information about it.
			if (script == null) {
				script = new Script(file, file.getPath().startsWith(coreDir.getPath()));
				scope.put("script", script, true);
			}
		}
		if (scriptStack.empty() || file != null) {
			if (script != null && !script.getShowProgress()) {
				closeProgress();
			} else if (file == null || !file.getName().startsWith("__")) {
				showProgress(file != null ? "Executing " + file.getName()
						+ "..." : "Executing...");
			}
		}
		// Push script even if it is null, as we're always popping again in
		// endExecution.
		scriptStack.push(script);
	}

	public static void beginExecution() {
		beginExecution(null, null);
	}

	/**
	 * To be called after AI functions were executed.
	 * 
	 * @return if any changes to the document were committed.
	 */
	public static boolean endExecution() {
		if (!scriptStack.empty())
			scriptStack.pop();
		if (scriptStack.empty()) {
			try {
				executionHasCommitted = CommitManager.commit();
			} catch(Throwable t) {
				ScriptographerEngine.reportError(t);
			}
			Dictionary.releaseInvalid();
			Document.endExecution();
			closeProgress();
		}
		return executionHasCommitted;
	}

	public static boolean executionHasCommitted() {
		return executionHasCommitted;
	}

	/**
	 * Invokes the method on the object, passing the arguments to it and calling
	 * beginExecution before and endExecution after it, which commits all
	 * changes after execution.
	 */
	public static Object invoke(Callable callable, Object obj, Object... args) {
		Scope scope;
		if (obj instanceof Scope) {
			scope = (Scope) obj;
			obj = scope.getScope();
		} else {
			scope = callable.getScope();
		}
		beginExecution(null, scope);
		// Retrieve wrapper object for the native java object, and
		// call the function on it.
		Throwable throwable = null;
		try {
			return callable.call(obj, args);
		} catch (Throwable t) {
			throwable = t;
		} finally {
			// Commit all changed objects after a scripting function
			// has been called!
			endExecution();
		}
		if (throwable != null)
			handleException(throwable, null);
		return null;
	}

	/**
	 * Compiles the script file and throws errors if it cannot be compiled.
	 * 
	 * @param file
	 * @throws ScriptException
	 * @throws IOException
	 */
	public static com.scratchdisk.script.Script compile(File file)
			throws ScriptException, IOException {
		ScriptEngine engine = ScriptEngine.getEngineByFile(file);
		if (engine == null)
			throw new ScriptException("Unable to find script engine for " + file);
		com.scratchdisk.script.Script script = engine.compile(file);
		if (script == null)
			throw new ScriptException("Unable to compile script " + file);
		return script;
	}

	/**
	 * Executes the specified script file.
	 *
	 * @param file
	 * @throws ScriptException 
	 * @throws IOException 
	 */
	public static Object execute(File file, Scope scope)
			throws ScriptException, IOException {
		return execute(compile(file), file, scope);
	}

	/**
	 * Executes the compiled script.
	 * 
	 * @param script
	 * @param file
	 * @param scope
	 * @throws ScriptException
	 * @throws IOException
	 */
	public static Object execute(com.scratchdisk.script.Script script,
			File file, Scope scope) throws ScriptException, IOException {
		Object ret = null;
		Throwable throwable = null;
		try {
			if (scope == null)
				scope = script.getEngine().createScope();
			beginExecution(file, scope);
			ret = script.execute(scope);
			addCallbacks(scope, file);
		} catch (Throwable t) {
			throwable = t;
		} finally {
			// Commit all the changes, even when script has caused an error, to
			// sync with direct changes such as creation of paths, etc.
			endExecution();
		}
		if (throwable != null)
			handleException(throwable, file);
		return ret;
	}

	private static void handleException(Throwable t, File file) {
		// Do not allow script cancellation during error reporting, as this is
		// now handled by scripts too
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

	private static Script getScript(Scope scope) {
		return (Script) scope.get("script");
	}

	private static void addCallbacks(Scope scope, File file) {
		// Scan through callback names and add to callback scope sublists if
		// found.
		for (String name : callbackNames) {
			Callable callback = scope.getCallable(name);
			if (callback != null) {
				ArrayList<Scope> list = callbackScopes.get(name);
				if (list == null) {
					list = new ArrayList<Scope>();
					callbackScopes.put(name, list);
				} else {
					// Remove old scope for this script before adding new one
					for (int i = list.size() - 1; i >= 0; i--) {
						if (getScript(list.get(i)).getFile().equals(file)) {
							list.remove(i);
							break;
						}
					}
				}
				list.add(scope);
			}
		}
	}

	private static void removeCallbacks(String name, boolean ignoreKeepAlive) {
		ArrayList<Scope> list = callbackScopes.get(name);
		if (list != null) {
			for (int i = list.size() - 1; i >= 0; i--) {
				if (getScript(list.get(i)).canRemove(ignoreKeepAlive))
					list.remove(i);
			}
		}
	}

	private static void removeCallbacks(boolean ignoreKeepAlive) {
		for (String name : callbackScopes.keySet())
			removeCallbacks(name, ignoreKeepAlive);
	}
	
	private static boolean callCallbacks(String name, Object[] args) {
		ArrayList<Scope> list = callbackScopes.get(name);
		// The first callback handler that returns true stops the others
		// (and in the case of keyDown / up also the native one!)
		if (list != null) {
			for (Scope scope : list) {
				Callable callback = scope.getCallable(name);
				Object res = invoke(callback, scope, args);
				if (ConversionUtils.toBoolean(res))
					return true;
			}
		}
		return false;
	}

	private static void callCallbacks(String name) {
		callCallbacks(name, new Object[0]);
	}

	public static void stopAll(boolean ignoreKeepAlive, boolean force) {
		Timer.abortAll(ignoreKeepAlive, force);
		callCallbacks("onStop");
		Dialog.destroyAll(ignoreKeepAlive, force);
		removeCallbacks(ignoreKeepAlive);
	}

	/**
	 * To be called from the native environment.
	 */
	public static void onHandleEvent(int type) {
		// TODO: There is currently no way to use these callbacks in a Java-only
		// use of the API. Find one?
		callCallbacks(callbackNames[type]);
		// Explicitly initialize all dialogs after startup, as otherwise
		// funny things will happen on CS3 and above. See comment in initializeAll
		if (type == EVENT_APP_STARTUP)
			Dialog.initializeAll();
	}

	/**
	 * To be called from the native environment.
	 */
	@SuppressWarnings("unused")
	private static boolean onHandleKeyEvent(int type, int keyCode,
			char character, int modifiers) {
		// TODO: There is currently no way to use these callbacks in a Java-only
		// use of the API. Find one?
		return callCallbacks(callbackNames[type],
				new Object[] { new KeyEvent(type, keyCode, character, modifiers) });
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

	private static native boolean nativeIsDown(int keyCode);

	public static boolean isKeyDown(KeyCode key) {
		return key != null ? nativeIsDown(key.value()) : false;
	}

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
	
	private static native boolean nativeUpdateProgress(long current, long max,
			boolean visible);

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
			boolean ret =
					nativeUpdateProgress(progressCurrent, progressMax,
							progressVisible);
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

	public static boolean getProgressVisible() {
		return progressVisible;
	}

	public static void setProgressVisible(boolean visible) {
		if (progressVisible ^ visible) {
			if (visible) {
				progressVisible = true;
				nativeUpdateProgress(progressCurrent, progressMax, true);
			} else {
				closeProgress();
			}
			
		}
	}

	public static boolean isMainThreadActive() {
		return Thread.currentThread().equals(mainThread);
	}

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

	public static native boolean isActive();

	public static native double getApplicationVersion();

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
		String[] lines = ClassUtils.getServiceInformation(
				ScriptographerEngine.class);
		if (lines != null) {
			version = lines[0];
			revision = Integer.parseInt(lines[1]);
		}
	}

	public static void debug() {
	}
}