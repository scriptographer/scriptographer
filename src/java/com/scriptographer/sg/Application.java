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

package com.scriptographer.sg;

import java.io.File;

import com.scriptographer.CommitManager;
import com.scriptographer.ScriptographerEngine;
import com.scriptographer.ai.Document;
import com.scriptographer.ai.DocumentList;
import com.scriptographer.ai.FontList;

/**
 * @author lehni
 * @jsnostatic
 */
public class Application {

	private Application() {
	}

	public String getVersion() {
		return ScriptographerEngine.getApplicationVersion();
	}

	public String getRevision() {
		return ScriptographerEngine.getApplicationRevision();
	}

	/**
	 * Returns the active document.
	 */
	public Document getActiveDocument() {
		return Document.getActiveDocument();
	}

	/**
	 * Returns a list of open documents.
	 */
	public DocumentList getDocuments() {
		return DocumentList.getInstance();
	}

	/**
	 * Returns a list of available fonts in the system.
	 */
	public FontList getFonts() {
		return FontList.getInstance();
	}

	public void commit() {
		CommitManager.commit(null);
	}

	boolean isWindows() {
		return ScriptographerEngine.isWindows();
	}

	boolean isMacintosh() {
		return ScriptographerEngine.isMacintosh();
	}
	
	public boolean launch(String filename) {
		return ScriptographerEngine.launch(filename);
	}

	public boolean launch(File file) {
		return ScriptographerEngine.launch(file);
	}

	/**
	 * Returns the current system time in nano seconds.
	 * This is very useful for high resolution time measurements.
	 * @return the current system time.
	 */
	public long getNanoTime() {
		return ScriptographerEngine.getNanoTime();
	}

	/**
	 * 
	 * @param current the current slider position
	 * @param max the maximum slidper position
	 * @return
	 */
	public boolean updateProgress(long current, long max) {
		return ScriptographerEngine.updateProgress(current, max);
	}

	private static Application app = null;
	
	/**
	 * @jshide all
	 */
	public static Application getInstance() {
		if (app == null)
			app = new Application();
		return app;
	}
}
