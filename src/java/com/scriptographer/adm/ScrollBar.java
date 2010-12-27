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

import com.scriptographer.ScriptographerEngine; 
import com.scratchdisk.script.Callable;

/**
 * @author lehni
 */
public class ScrollBar extends ValueItem {

	public ScrollBar(Dialog dialog) {
		super(dialog, ItemType.SCROLLBAR);
	}
	
	private Callable onChange = null;

	public Callable getOnChange() {
		return onChange;
	}

	public void setOnChange(Callable onChange) {
		this.onChange = onChange;
	}

	protected void onChange() {
		ScriptographerEngine.invoke(onChange, this);
	}

	protected void onNotify(Notifier notifier, ListEntry entry) {
		// override the default behavior and give onChange for both
		// notifiers:
		switch(notifier) {
			case USER_CHANGED:
			case INTERMEDIATE_CHANGED:
				onChange();
				break;
		}
	}
}
