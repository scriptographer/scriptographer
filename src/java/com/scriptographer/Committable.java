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
 * File created on 14.01.2005.
 */

package com.scriptographer;

/**
 * @author lehni
 */
public interface Committable {
	/**
	 * Commits the cached change to the native object.
	 * 
	 * @jshide
	 */
	public abstract void commit(boolean endExecution);
}
