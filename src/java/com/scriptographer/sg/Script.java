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
	private Callable onStart;
	private Callable onStop;
	private boolean showProgress = true;

	/**
	 * @jshide
	 */
	public Script(File file) {
		this.file = file;
	}

	/**
	 * Returns the script's preferences, as an object in which data
	 * can be stored and retrieved from:
	 * <pre>
	 * script.preferences.value = 10;
	 * </pre>
	 * @return the script's preferences object.
	 */
	public Preferences getPreferences() {
		if (prefs == null)
			prefs = new Preferences(ScriptographerEngine.getPreferences(file));
		return prefs;
	}

	/**
	 * Returns the script file.
	 * @return the script file.
	 */
	public File getFile() {
		return file;
	}

	/**
	 * Returns the directory in which the script is stored in.
	 * @return the script's parent directory.
	 */
	public File getDirectory() {
		return file.getParentFile();
	}

	public Callable getOnStart() {
		return onStart;
	}

	public void setOnStart(Callable onStart) {
		this.onStart = onStart;
	}

	public Callable getOnStop() {
		return onStop;
	}

	public void setOnStop(Callable onStop) {
		this.onStop = onStop;
	}
	
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
