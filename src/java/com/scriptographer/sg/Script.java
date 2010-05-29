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
 * File created on Apr 22, 2007.
 */

package com.scriptographer.sg;

import java.io.File;

import com.scriptographer.ScriptographerEngine;

/**
 * @author lehni
 *
 */
public class Script {
	private File file;
	private Preferences prefs = null;
	private boolean keepAlive = false;
	private boolean showProgress = true;
	protected boolean coreScript = false;

	/**
	 * @jshide
	 */
	public Script(File file, boolean coreScript) {
		this.file = file;
		this.coreScript = coreScript;
	}

	/**
	 * @jshide
	 */
	public Script(File file, Script previous) {
		this(file, previous.coreScript);
	}

	/**
	 * Returns the script's preferences, as an object in which data
	 * can be stored and retrieved from:
	 * <code>
	 * script.preferences.value = 10;
	 * </code>
	 */
	public Preferences getPreferences() {
		if (prefs == null)
			prefs = new Preferences(ScriptographerEngine.getPreferences(this));
		return prefs;
	}

	/**
	 * Returns the script file.
	 */
	public File getFile() {
		return file;
	}

	/**
	 * Returns the directory in which the script is stored in.
	 */
	public File getDirectory() {
		return file.getParentFile();
	}

	/**
	 * Determines whether the scripts wants to display the progress bar
	 * or not. {@code true} by default.
	 */
	public boolean getShowProgress() {
		return showProgress;
	}

	public void setShowProgress(boolean show) {
		showProgress = show;
		if (show)
			ScriptographerEngine.showProgress();
		else
			ScriptographerEngine.closeProgress();
	}

	public boolean getKeepAlive() {
		return keepAlive;
	}

	public void setKeepAlive(boolean keepAlive) {
		this.keepAlive  = keepAlive;
	}

	public boolean canRemove(boolean ignoreKeepAlive) {
		return !coreScript && (ignoreKeepAlive || !keepAlive);
	}
}
