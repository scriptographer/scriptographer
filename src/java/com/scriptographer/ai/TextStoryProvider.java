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
 * File created on May 19, 2010.
 */

package com.scriptographer.ai;

/**
 * @author lehni
 *
 */
public interface TextStoryProvider {
	/**
	 * Returns a handle for a story just to be used internally to get a stories
	 * object from. Any code that uses this needs to dispose of the handle right
	 * after. This is only used in connection with document.getStories().
	 * 
	 * It's a shame we need to make this public, but Java offers no protected
	 * interfaces. Dumb.
	 * 
	 * @jshide
	 */
	public int getStoryHandle();
}
