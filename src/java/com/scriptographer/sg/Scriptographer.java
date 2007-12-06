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
 * File created on May 6, 2007.
 *
 * $Id$
 */

package com.scriptographer.sg;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.scratchdisk.script.ScriptEngine;
import com.scriptographer.ScriptographerEngine;

/**
 * @author lehni
 *
 */
public class Scriptographer {

	private Scriptographer() {
	}

	public File getPluginDirectory() {
		return ScriptographerEngine.getPluginDirectory();
	}

	public File getScriptDirectory() {
		return ScriptographerEngine.getScriptDirectory();
	}

	private String version = null;
	private int revision = -1;

	public String getVersion() {
		if (version == null)
			readVersion();
		return version;
	}

	public int getRevision() {
		if (revision == -1)
			readVersion();
		return revision;
	}

	private void readVersion() {
		// Read the version from the file...
		InputStream in = ScriptEngine.class.getResourceAsStream(
				"/META-INF/version");
		if (in != null) {
			try {
				BufferedReader buffer = new BufferedReader(
						new InputStreamReader(in));
				version = buffer.readLine();
				revision = Integer.parseInt(buffer.readLine());
				in.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
	
	private static Scriptographer scripto = null;

	/**
	 * @jshide all
	 */
	public static Scriptographer getInstance() {
		if (scripto == null)
			scripto = new Scriptographer();
		return scripto;
	}

}
