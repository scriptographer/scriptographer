/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2007 Juerg Lehni, http://www.scratchdisk.com.
 * All rights reserved.
 *
 * Please visit http://scriptographer.com/ for updates and contact.
 *
 * -- GPL LICENSE NOTICE --
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * -- GPL LICENSE NOTICE --
 *
 * File created on 03.01.2005.
 *
 * $Id$
 */

package com.scriptographer.adm;

import com.scriptographer.script.ScriptMethod;

/**
 * @author lehni
 */
public class ScrollBar extends ValueItem {

	public ScrollBar(Dialog dialog) {
		super(dialog, TYPE_SCROLLBAR, OPTION_NONE);
	}
	
	private ScriptMethod onChange = null;

	public ScriptMethod getOnChange() {
		return onChange;
	}

	public void setOnChange(ScriptMethod onChange) {
		this.onChange = onChange;
	}

	protected void onChange() throws Exception {
		onChange.execute(this);
	}
	
	protected void onNotify(int notifier, ListEntry entry) throws Exception {
		// override the default behavior and give onChange for both
		// notifiers:
		switch(notifier) {
			case Notifier.NOTIFIER_USER_CHANGED:
			case Notifier.NOTIFIER_INTERMEDIATE_CHANGED:
				onChange();
				break;
		}
	}
}
