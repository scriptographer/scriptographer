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
 * File created on 16.03.2005.
 *
 * $Id$
 */

package com.scriptographer.ui;

/**
 * The NotificationListener just recieves notifictaions from native code.
 * It can be a Dialog, Item or ListEntry
 * 
 * It implements Wrappable and Unsealed in order to provide the JS environment
 * a means of setting callback functions and calling these from Java.
 * 
 * @author lehni
 */
abstract class NotificationHandler extends NativeObject {
	@SuppressWarnings("unused")
	private Tracker tracker = new Tracker();
 	@SuppressWarnings("unused")
	private Drawer drawer = new Drawer();

	protected abstract void onNotify(Notifier notifier) throws Exception;
	protected abstract boolean onTrack(Tracker tracker) throws Exception;
	protected abstract void onDraw(Drawer drawer) throws Exception;

	protected final void onNotify(String notifier) throws Exception {
		onNotify(Notifier.get(notifier));
	}
}
