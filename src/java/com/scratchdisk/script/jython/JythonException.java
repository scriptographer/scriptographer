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
 * File created on Apr 14, 2007.
 */

package com.scratchdisk.script.jython;

import com.scratchdisk.script.ScriptException;

/**
 * @author lehni
 *
 */
public class JythonException extends ScriptException {
	public JythonException(Throwable cause) {
		// TODO: format a message
		super(cause);
	}

	public String getFullMessage() {
		// TODO: stack traces?
		return getMessage();
	}
}
