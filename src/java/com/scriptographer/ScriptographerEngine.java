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
import com.scriptographer.script.ScriptEngine;

import java.io.*;
import java.util.ArrayList;
import java.util.prefs.Preferences;

/**
 * @author lehni
 */
public class ScriptographerEngine {
	private static long progressCurrent;
	private static long progressMax;
	private static boolean progressAutomatic;
	private static final boolean isWindows, isMacintosh;
	private static ConsoleDialog consoleDialog;
	private static MainDialog mainDialog;
	private static File scriptDir = null;
	private static File pluginDir = null;
	private static PrintStream logger = null;

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

		consoleDialog = new ConsoleDialog();
		mainDialog = new MainDialog(consoleDialog);

		// Execute all scripts in startup folder:
		if (scriptDir != null)
			ScriptEngine.executeAll(new File(scriptDir, "startup"));

		// Explicitly initialize all dialogs on startup, as otherwise
		// funny things will happen on CS3 -> see comment in initializeAll
		Dialog.initializeAll();
	}

	public static void destroy() {
		// We're shuting down, so do not display console stuff any more
		ConsoleOutputStream.enableRedirection(false);
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

	public static boolean isWindows() {
		return isWindows;
	}

	public static boolean isMacintosh() {
		return isMacintosh;
	}
	
	public static Preferences getPreferences(boolean checkFile) {
		if (checkFile && currentFile != null)
			return getPreferences(currentFile);
		// the base prefs for Scriptographer are:
		// com.scriptographer.scriptographer on mac, three nodes seem to be
		// necessary, otherwise things get mixed up...
		return Preferences.userNodeForPackage(ScriptographerEngine.class).node("scriptographer");
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
	
	public static String formatError(Throwable t) {
		String error = t.getMessage();
		PrintStream logger = ScriptographerEngine.getLogger();
		logger.print(error);
		logger.print("Stacktrace: ");
		t.printStackTrace(logger);
		logger.println();
		return error;
	}

	public static void reportError(Throwable t) {
		System.err.print(formatError(t));
	}

	static int reloadCount = 0;

	public static int getReloadCount() {
		return reloadCount;
	}

	public static String reload() {
		ScriptEngine.stopAll();
		reloadCount++;
		return nativeReload();
	}

	public static native String nativeReload();

	public static void onAbout() {
		AboutDialog.show();
	}
	
	private static boolean executing = false;
	private static File currentFile = null;
	
	public static boolean isExecuting() {
		return executing;
	}

	/**
	 * to be called before ai functions are executed
	 */
	public static boolean beginExecution(File file) {
		if (!executing) {
			executing = true;
			Document.beginExecution();
			currentFile = file;
			return true;
		}
		return false;
	}

	public static boolean beginExecution() {
		return beginExecution(null);
	}
		
	/**
	 * to be called before ai functions are executed
	 */
	public static void endExecution() {
		if (executing) {
			CommitManager.commit();
			Document.endExecution();
			currentFile = null;
			executing = false;
		}
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
	
	public static native boolean closeProgress();
}