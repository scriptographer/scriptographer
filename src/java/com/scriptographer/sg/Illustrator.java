/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Scripting Plugin for Adobe Illustrator
 * http://scriptographer.org/
 *
 * Copyright (c) 2002-2010, Juerg Lehni
 * http://scratchdisk.com/
 *
 * All rights reserved. See LICENSE file for details.
 * 
 * File created on Apr 22, 2007.
 */

package com.scriptographer.sg;

import java.io.File;

import com.scriptographer.CommitManager;
import com.scriptographer.ScriptographerEngine;
import com.scriptographer.ai.FileFormatList;
import com.scriptographer.ai.FontList;

/**
 * The Illustrator object represents the Illustrator application and can be
 * accessed through the global {@code illustrator} variable.
 * 
 * @author lehni
 * 
 * @jsnostatic
 */
public class Illustrator {

	private Illustrator() {
		// Do not let anyone to instantiate this class.
	}

	/**
	 * Adobe Illustrator's version description.
	 */
	public double getVersion() {
		return ScriptographerEngine.getIllustratorVersion();
	}

	/**
	 * Adobe Illustrator's revision number.
	 */
	public int getRevision() {
		return ScriptographerEngine.getIllustratorRevision();
	}

	/**
	 * An array of all installed fonts.
	 */
	public FontList getFonts() {
		return FontList.getInstance();
	}

	/**
	 * An array of all file formats available for writing of documents.
	 * 
	 * @jshide
	 */
	public String[] getFileFormats() {
		return FileFormatList.getInstance().getExtensions();
	}

	/**
	 * Commits all pending cached changes to the native objects.
	 * This should usually not be necessary to be called since Scriptographer
	 * handles this transparently behind the scenes. It might be useful for
	 * temporary bug fixes though for cases where the automatic execution
	 * was forgotten.
	 * 
	 * @jshide
	 */
	public void commit() {
		CommitManager.commit(null);
	}

	/**
	 * Returns {@true if the system is Windows}.
	 */
	public boolean isWindows() {
		return ScriptographerEngine.isWindows();
	}

	/**
	 * Returns {@true if the system is Macintosh}.
	 */
	public boolean isMacintosh() {
		return ScriptographerEngine.isMacintosh();
	}

	/**
	 * Returns {@true if the Illustrator application is in front and ready to
	 * receive user input}.
	 */
	public boolean isActive() {
		return ScriptographerEngine.isActive();
	}

	/**
	 * {@grouptitle File Launching}
	 * 
	 * Launches the given filename or URL through the operating system.
	 */
	public boolean launch(String filename) {
		return ScriptographerEngine.launch(filename);
	}

	/**
	 * Launches the given file through the operating system.
	 */
	public boolean launch(File file) {
		return ScriptographerEngine.launch(file);
	}

	/**
	 * Returns the current system time in nanoseconds. This is very
	 * useful for high resolution time measurements.
	 * 
	 * @jshide
	 */
	public long getNanoTime() {
		return ScriptographerEngine.getNanoTime();
	}

	/**
	 * {@grouptitle Progress Bar}
	 * 
	 * Updates the progress bar dialog.
	 * 
	 * @param current the current progress bar position
	 * @param max the maximum progress bar position
	 * @return false if the user aborted script execution, true otherwise.
	 */
	public boolean updateProgress(long current, long max) {
		return ScriptographerEngine.updateProgress(current, max);
	}

	/**
	 * Displays the progress bar dialog.
	 * 
	 * @param text the optional text to display above the progress bar.
	 */
	public void showProgress(String text) {
		ScriptographerEngine.showProgress(text);
	}

	public void showProgress() {
		ScriptographerEngine.showProgress();
	}

	/**
	 * Closes the progress bar dialog.
	 */
	public void closeProgress() {
		ScriptographerEngine.closeProgress();
	}

	private static Illustrator instance = null;

	/**
	 * @jshide
	 */
	public static Illustrator getInstance() {
		if (instance == null)
			instance = new Illustrator();
		return instance;
	}
}
