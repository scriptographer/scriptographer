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
 * File created on May 6, 2007.
 */

package com.scriptographer.sg;

import java.io.File;

import com.scriptographer.ScriptographerEngine;

/**
 * The Scriptographer object represents the Scriptographer plugin and can be
 * accessed through the global {@code scriptographer} variable.
 * 
 * @author lehni
 * 
 * @jsnostatic
 */
public class Scriptographer {

	private Scriptographer() {
		// Do not let anyone to instantiate this class.
	}

	/**
	 * Scriptographer's main directory.
	 */
	public File getPluginDirectory() {
		return ScriptographerEngine.getPluginDirectory();
	}

	/**
	 * Scriptographer's version description.
	 */
	public double getVersion() {
		return ScriptographerEngine.getPluginVersion();
	}

	/**
	 * Scriptographer's revision number.
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
