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

/**
 * @author lehni
 *
 */
public class ScriptException extends RuntimeException {
	public String text;

	public ScriptException(Throwable cause) {
		super(cause);
	}

	public ScriptException(String message, Throwable cause) {
		super(message, cause);
	}

	public ScriptException(String message) {
		super(message);
	}

	public String getFullMessage() {
		return getMessage();
	}

	public Throwable getWrappedException() {
		return getCause();
	}
}
