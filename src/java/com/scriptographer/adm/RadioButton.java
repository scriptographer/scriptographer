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
 * File created on 03.01.2005.
 */

package com.scriptographer.adm;

/**
 * A RadioButton is by default text based.
 * Only if it is created with an image passed to the constructor,
 * It is picture based.
 * Picture based items (CheckBox, Static, PushButton, RadioButton),
 * this policy has been chosen to avoid 4 more classes.
 * 
 * @author lehni
 */
public class RadioButton extends ToggleItem {
	
	protected RadioButton(Dialog dialog, ItemType type) {
		super(dialog, type);
	}

	public RadioButton(Dialog dialog) {
		super(dialog, ItemType.TEXT_RADIOBUTTON);
	}
}
