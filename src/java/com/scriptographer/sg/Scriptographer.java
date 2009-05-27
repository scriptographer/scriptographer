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
 * File created on May 6, 2007.
 *
 * $Id$
 */

package com.scriptographer.sg;

import java.io.File;

import com.scriptographer.ScriptographerEngine;

/**
 * @author lehni
 *
 * @jsnostatic
 */
public class Scriptographer {

	private Scriptographer() {
		// Do not let anyone to instantiate this class.
	}

	/**
	 * Returns Scriptographer's main directory.
	 */
	public File getPluginDirectory() {
		return ScriptographerEngine.getPluginDirectory();
	}

	/**
	 * Returns Scriptographer's script directory.
	 */
	public File getScriptDirectory() {
		return ScriptographerEngine.getScriptDirectory();
	}

	/**
	 * Returns Scriptographer's version description.
	 */
	public String getVersion() {
		return ScriptographerEngine.getPluginVersion();
	}

	/**
	 * Returns Scriptographer's revision number.
	 */
	public int getRevision() {
		return ScriptographerEngine.getPluginRevision();
	}
	
	private static Scriptographer scriptographer = null;

	/**
	 * @jshide
	 */
	public static Scriptographer getInstance() {
		if (scriptographer == null)
			scriptographer = new Scriptographer();
		return scriptographer;
	}

}
