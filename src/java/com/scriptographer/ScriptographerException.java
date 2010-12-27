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
 * File created on 08.12.2004.
 */

package com.scriptographer;

/**
 * @author lehni
 */
public class ScriptographerException extends RuntimeException {

	public ScriptographerException(String msg) {
		super(msg);
	}

	public ScriptographerException(Throwable t) {
		super(t);
	}
}