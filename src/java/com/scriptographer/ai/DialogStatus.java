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
 * File created on Apr 13, 2008.
 */

package com.scriptographer.ai;

import com.scratchdisk.util.IntegerEnum;

/**
 * ActionDialogStatus
 * 
 * @author lehni
 */

/*
 * TODO: Instead of passing it as a parameter, implement global 
 * app.dialogStatus, that defines handling for all such functions,
 * also Document#write, etc.
 */
public enum DialogStatus implements IntegerEnum {
	NONE(0),
	ON(1),
	PARTIAL_ON(2),
	OFF(3);

	protected int value;

	private DialogStatus(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
