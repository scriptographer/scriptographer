/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2008 Juerg Lehni, http://www.scratchdisk.com.
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
 * File created on 11.03.2005.
 *
 * $Id$
 */

package com.scriptographer.adm;

/**
 * @author lehni
 */
public class PopupList extends ListItem {
	// Options
	public static final int
	// self defined pseudo options, for creation of the right TYPE:
		OPTION_SCROLLING = 1 << 1;
	
	protected PopupList(Dialog dialog, long handle) {
		super(dialog, handle);
	}

	public PopupList(Dialog dialog, int options) {
		super(dialog, getType(options), OPTION_NONE);
	}

	public PopupList(Dialog dialog) {
		this(dialog, OPTION_NONE);
	}

	private static int getType(int options) {
		// abuse the ADM's password style for creating it as a type...
		return (options & OPTION_SCROLLING) != 0 ? TYPE_SCROLLING_POPUP_LIST
			: TYPE_POPUP_LIST;
	}
}
