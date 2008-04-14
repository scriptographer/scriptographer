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
 * File created on 03.01.2005.
 *
 * $Id$
 */

package com.scriptographer.adm;

import java.util.HashMap;

/**
 * @author lehni
 */
public class Notifier {
	public static final int
		NOTIFIER_USER_CHANGED = 0,
		NOTIFIER_INTERMEDIATE_CHANGED = 1,
		NOTIFIER_BOUNDS_CHANGED = 2,
		NOTIFIER_VIEW_BOUNDS_CHANGED = 3,
		NOTIFIER_ENTRY_TEXT_CHANGED = 4,
		NOTIFIER_CLOSE_HIT = 5,
		NOTIFIER_ZOOM_HIT = 6,
		NOTIFIER_CYCLE = 7,
		NOTIFIER_COLLAPSE = 8,
		NOTIFIER_EXPAND = 9,
		NOTIFIER_CONTEXT_MENU_CHANGED = 10,
		NOTIFIER_WINDOW_SHOW = 11,
		NOTIFIER_WINDOW_HIDE = 12,
		NOTIFIER_GROUP_SHOW = 13,
		NOTIFIER_GROUP_HIDE = 14,
		NOTIFIER_WINDOW_ACTIVATE = 15,
		NOTIFIER_WINDOW_DEACTIVATE = 16,
		NOTIFIER_NUMBER_OUT_OF_BOUNDS = 17,
		NOTIFIER_WINDOW_DRAG_MOVED = 18,
		NOTIFIER_PRE_CLIPBOARD_CUT = 19,
		NOTIFIER_POST_CLIPBOARD_CUT = 20,
		NOTIFIER_PRE_CLIPBOARD_COPY = 21,
		NOTIFIER_POST_CLIPBOARD_COPY = 22,
		NOTIFIER_PRE_CLIPBOARD_PASTE = 23,
		NOTIFIER_POST_CLIPBOARD_PASTE = 24,
		NOTIFIER_PRE_CLIPBOARD_CLEAR = 25,
		NOTIFIER_POST_CLIPBOARD_CLEAR = 26,
		NOTIFIER_PRE_TEXT_SELECTION_CHANGED = 27,
		NOTIFIER_TEXT_SELECTION_CHANGED = 28,
		NOTIFIER_PRE_CLIPBOARD_REDO = 29,
		NOTIFIER_POST_CLIPBOARD_REDO = 30,
		NOTIFIER_PRE_CLIPBOARD_UNDO = 31,
		NOTIFIER_POST_CLIPBOARD_UNDO = 32,
		// Pseudo notifiers:
		NOTIFIER_INITIALIZE = 33,
		NOTIFIER_DESTROY = 34;

	private final static String[] notifierTypes = {
		"ADM User Changed Notifier",
		"ADM Intermediate Changed Notifier",
		"ADM Bounds Changed Notifier",
		"ADM View Bounds Changed Notifier",
		"ADM Entry Text Changed Notifier",
		"ADM Close Hit Notifier",
		"ADM Zoom Hit Notifier",
		"ADM Cycle Notifier",
		"ADM Collapse Notifier",
		"ADM Expand Notifier",
		"ADM Context Menu Changed Notifier",
		"ADM Show Window Notifier",
		"ADM Hide Window Notifier",
		"ADM Show Group Notifier",
		"ADM Hide Group Notifier",
		"ADM Activate Window Notifier",
		"ADM Deactivate Window Notifier",
		"ADM Number Out Of Bounds Notifier",
		"ADM Window Moved By Drag",
		"ADM Pre Clipboard Cut Notifier",
		"ADM Post Clipboard Cut Notifier",
		"ADM Pre Clipboard Copy Notifier",
		"ADM Post Clipboard Copy Notifier",
		"ADM Pre Clipboard Paste Notifier",
		"ADM Post Clipboard Paste Notifier",
		"ADM Pre Clipboard Clear Notifier",
		"ADM Post Clipboard Clear Notifier",
		"ADM Pre Selection Change Notification",
		"ADM Text Selection Change Notification",
		"ADM Pre Clipboard Redo Notifier",
		"ADM Post Clipboard Redo Notifier",
		"ADM Pre Clipboard Undo Notifier",
		"ADM Post Clipboard Undo Notifier",
		// Pseudo notifiers:
		"ADM Initialize Window Notifier",
		"ADM Destroy Notifier"
	};

	// hashmap for conversation to unique ids that can be compared with ==
	// instead of .equals
	private static HashMap<String, Integer> notifiers = new HashMap<String, Integer>();

	static {
		for (int i = 0; i < notifierTypes.length; i++)
			notifiers.put(notifierTypes[i], i);
	}
	
	public static int lookup(String notifier) {
		Integer value = (Integer) notifiers.get(notifier);
		if (value != null)
			return value.intValue();
		else {
			System.out.println("notfound " + notifier);
			return -1;
		}
	}
}
