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
 * File created on Apr 22, 2007.
 *
 * $Id: $
 */

package com.scriptographer.ai;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.scratchdisk.script.ScriptEngine;
import com.scriptographer.CommitManager;
import com.scriptographer.ScriptographerEngine;

/**
 * @author lehni
 * @jsnostatic
 */
public class Application {

	public static native int getVersion();

	private static int version = -1;

	public static int getScriptVersion() {
		if (version < 0) {
			// Read the version from the file...
			InputStream in = ScriptEngine.class.getResourceAsStream(
					"/META-INF/version");
			if (in != null) {
				try {
					BufferedReader buffer = new BufferedReader(
							new InputStreamReader(in));
					version = Integer.parseInt(buffer.readLine());
					in.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}
		return version;
	}

	/**
	 * Returns the active document.
	 */
	public static Document getActiveDocument() {
		return Document.getActiveDocument();
	}

	/**
	 * Returns a list of open documents.
	 */
	public static DocumentList getDocuments() {
		return DocumentList.getInstance();
	}

	/**
	 * Returns a list of available fonts in the system.
	 */
	public static FontList getFonts() {
		return FontList.getInstance();
	}

	public static void commit() {
		CommitManager.commit(null);
	}

	public static File getScriptDirectory() {
		return ScriptographerEngine.getScriptDirectory();
	}

	public static File getPluginDirectory() {
		return ScriptographerEngine.getPluginDirectory();
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
	private static boolean progressAutomatic;
	private static Application app;

	private static native void nativeShowProgress(String text);
	
	public static void showProgress(String text) {
		progressAutomatic = true;
		progressCurrent = 0;
		progressMax = 1 << 8;
		nativeUpdateProgress(progressCurrent, progressMax);
		nativeShowProgress(text);
	}
	
	private static native boolean nativeUpdateProgress(long current, long max);

	public static  boolean updateProgress(long current, long max) {
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

	public static native boolean closeProgress();

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

	/**
	 * @jshide
	 */
	public static Application getInstance() {
		if (app == null)
			app = new Application();
		return app;
	}
}
