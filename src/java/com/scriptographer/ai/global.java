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
 * File created on 10.06.2006.
 * 
 * $Id$
 */

package com.scriptographer.ai;

import java.io.File;

/*
 * This class is just a dummy to get Javadoc to generate documentation for
 * the global scope
 */

/**
 * Objects and functions present in the global scope. These can be used anywhere
 * in scripts.
 * 
 * @author lehni
 */
public class global {
	private global() {
		// do not initiate
	}

	/**
	 * Prints one or more passed parameters to the console.
	 */
	public void print() {
		// dummy
	}

	/**
	 * Includes and evaluates one or more other javascripts.
	 * <p>e.g:
	 * <pre>include("raster.js", "mysql.js");</pre>
	 */
	public void include() {
		// dummy
	}

	/**
	 * Returns the current mouse position.
	 * @return the current mouse position
	 */
	public Point getMousePoint() {
		// dummy
		return null;
	}

	/**
	 * Returns the current system time in nano seconds.
	 * This is very useful for high resolution time measurements.
	 * @return the current system time.
	 */
	public long getNanoTime() {
		// dummy
		return 0;
	}

	// TODO: commit, evaluate

	/**
	 * The active document.
	 */
	public Document activeDocument;

	/**
	 * The list of open documents.
	 */
	public DocumentList documents;

	/**
	 * The list of available fonts in the system.
	 */
	public FontList fonts;

	/**
	 * The Scriptographer base directory where all the scripts are stored.
	 */
	public File scriptDir;

	/**
	 * The file of the current script.
	 */
	public File scriptFile;
}
