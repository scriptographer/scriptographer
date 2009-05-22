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
 * File created on Apr 22, 2007.
 *
 * $Id$
 */

package com.scriptographer.sg;

import java.io.File;

import com.scratchdisk.script.Callable;
import com.scriptographer.ScriptographerEngine;

/**
 * @author lehni
 *
 */
public class Script {
	private File file;
	private Preferences prefs = null;
	private Callable onStop;
	private boolean showProgress = true;

	/**
	 * @jshide
	 */
	public Script(File file) {
		this.file = file;
	}

	/**
	 * @jsbean Returns the script's preferences, as an object in which data
	 * @jsbean can be stored and retrieved from:
	 * @jsbean <pre>
	 * @jsbean script.preferences.value = 10;
	 * @jsbean </pre>
	 */
	public Preferences getPreferences() {
		if (prefs == null)
			prefs = new Preferences(ScriptographerEngine.getPreferences(file));
		return prefs;
	}

	/**
	 * @jsbean Returns the script file.
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @jsbean Returns the directory in which the script is stored in.
	 */
	public File getDirectory() {
		return file.getParentFile();
	}

	/**
	 * @jsbean The handler function to be called when the script is stopped
	 * @jsbean through the stop button in the Scriptographer GUI.
	 */
	public Callable getOnStop() {
		return onStop;
	}

	public void setOnStop(Callable onStop) {
		this.onStop = onStop;
	}

	/**
	 * @jsbean Determines wether the scripts wants to display the progress bar
	 * @jsbean or not. <code>true</code> by default.
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
}
