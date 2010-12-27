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
 * File created on Feb 19, 2007.
 */

package com.scratchdisk.script;

import java.io.File;

/**
 * @author lehni
 *
 */
public abstract class Script {
	protected File file;
	private long lastModified;

	public Script(File file) {
		this.file = file;
		if (file != null)
			lastModified = file.lastModified();
	}

	public abstract Object execute(Scope scope) throws ScriptException;

	public abstract ScriptEngine getEngine();

	public File getFile() {
		return file;
	}

	public boolean hasChanged() {
		return file != null && lastModified != file.lastModified();
	}
}
