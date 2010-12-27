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
 * File created on Feb 9, 2010.
 */

package com.scriptographer.sg;

import com.scriptographer.ui.KeyModifiers;

/**
 * @author lehni
 * 
 * @jshide
 */
public abstract class Event {
	private int modifiers;

	public Event(int modifiers) {
		this.modifiers = modifiers;
	}

	/**
	 * Returns an object representing the state of various modifiers keys. These
	 * properties are supported:
	 * {@code shift, control, option, meta, capsLock}.
	 * 
	 * Sample code:
	 * <code>
	 * function onMouseDown(event) {
	 *     if(event.modifiers.shift) {
	 *     	print('The shift key is down');
	 *     };
	 * }
	 * </code>
	 */
	public KeyModifiers getModifiers() {
		return new KeyModifiers(modifiers);
	}
}
