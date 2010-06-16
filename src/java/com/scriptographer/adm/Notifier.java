/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2010 Juerg Lehni, http://www.scratchdisk.com.
 * All rights reserved.
 *
 * Please visit http://scriptographer.org/ for updates and contact.
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
 */

package com.scriptographer.adm;

import java.util.HashMap;

/**
 * @author lehni
 * 
 * @jshide
 */
public enum Notifier {
	USER_CHANGED("ADM User Changed Notifier"),
	INTERMEDIATE_CHANGED("ADM Intermediate Changed Notifier"),
	BOUNDS_CHANGED("ADM Bounds Changed Notifier"),
	VIEW_BOUNDS_CHANGED("ADM View Bounds Changed Notifier"),
	ENTRY_TEXT_CHANGED("ADM Entry Text Changed Notifier"),
	CLOSE_HIT("ADM Close Hit Notifier"),
	ZOOM_HIT("ADM Zoom Hit Notifier"),
	CYCLE("ADM Cycle Notifier"),
	COLLAPSE("ADM Collapse Notifier"),
	EXPAND("ADM Expand Notifier"),
	CONTEXT_MENU_CHANGED("ADM Context Menu Changed Notifier"),
	WINDOW_SHOW("ADM Show Window Notifier"),
	WINDOW_HIDE("ADM Hide Window Notifier"),
	GROUP_SHOW("ADM Show Group Notifier"),
	GROUP_HIDE("ADM Hide Group Notifier"),
	WINDOW_ACTIVATE("ADM Activate Window Notifier"),
	WINDOW_DEACTIVATE("ADM Deactivate Window Notifier"),
	NUMBER_OUT_OF_BOUNDS("ADM Number Out Of Bounds Notifier"),
	WINDOW_DRAG_MOVED("ADM Window Moved By Drag"),
	PRE_CLIPBOARD_CUT("ADM Pre Clipboard Cut Notifier"),
	POST_CLIPBOARD_CUT("ADM Post Clipboard Cut Notifier"),
	PRE_CLIPBOARD_COPY("ADM Pre Clipboard Copy Notifier"),
	POST_CLIPBOARD_COPY("ADM Post Clipboard Copy Notifier"),
	PRE_CLIPBOARD_PASTE("ADM Pre Clipboard Paste Notifier"),
	POST_CLIPBOARD_PASTE("ADM Post Clipboard Paste Notifier"),
	PRE_CLIPBOARD_CLEAR("ADM Pre Clipboard Clear Notifier"),
	POST_CLIPBOARD_CLEAR("ADM Post Clipboard Clear Notifier"),
	PRE_TEXT_SELECTION_CHANGED("ADM Pre Selection Change Notification"),
	TEXT_SELECTION_CHANGED("ADM Text Selection Change Notification"),
	PRE_CLIPBOARD_REDO("ADM Pre Clipboard Redo Notifier"),
	POST_CLIPBOARD_REDO("ADM Post Clipboard Redo Notifier"),
	PRE_CLIPBOARD_UNDO("ADM Pre Clipboard Undo Notifier"),
	POST_CLIPBOARD_UNDO("ADM Post Clipboard Undo Notifier"),
	// Pseudo notifiers: 
	INITIALIZE("ADM Initialize Notifier"),
	DESTROY("ADM Destroy Notifier");

	protected String name;

	private Notifier(String name) {
		this.name = name;
	}

	// hash-map for conversation to unique ids that can be compared with ==
	// instead of .equals
	private static HashMap<String, Notifier> notifiers =
		new HashMap<String, Notifier>();

	static {
		for (Notifier notifier : values())
			notifiers.put(notifier.name, notifier);
	}
	
	public static Notifier get(String name) {
		Notifier notifier = notifiers.get(name);
		if (notifier != null)
			return notifier;
		else {
			System.err.println("Notifier not found " + name);
			return null;
		}
	}
}
