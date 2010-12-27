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
 * File created on 31.07.2005.
 */

package com.scriptographer.ui;

import com.scriptographer.ScriptographerEngine;

/**
 * @author lehni
 */
public class Key {

	private Key() {
		// Don't let anyone instantiate this class.
	}

	/**
	 * Checks whether the specified key is pressed.
	 * 
	 * Sample code:
	 * <code>
	 * function onMouseDown(event) {
	 * 	if(Key.isDown('shift')) {
	 * 		print('The shift key is currently pressed.')
	 * 	}
	 * }
	 * </code>
	 * @return {@true if the key is pressed}
	 */
	public static boolean isDown(KeyCode key) {
		return ScriptographerEngine.isKeyDown(key);
	}
}
