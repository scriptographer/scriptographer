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
 * File created on 16.03.2005.
 */

package com.scriptographer.adm;

import java.util.HashMap;
import java.util.Map;

import com.scratchdisk.script.Callable;
import com.scriptographer.ai.Timer;
import com.scriptographer.script.RunnableCallable;
import com.scriptographer.ui.NativeObject;

/**
 * The NotificationListener just receives notifications from native code.
 * It can be a Dialog, Item or ListEntry
 * 
 * @author lehni
 */
abstract class NotificationHandler extends NativeObject {
	@SuppressWarnings("unused")
	private Tracker tracker = new Tracker();
 	@SuppressWarnings("unused")
	private Drawer drawer = new Drawer();
	private Map data;

	protected abstract void onNotify(Notifier notifier);
	protected abstract boolean onTrack(Tracker tracker);
	protected abstract boolean onDraw(Drawer drawer);

	public abstract boolean defaultTrack(Tracker tracker);
	public abstract void defaultDraw(Drawer drawer);

	protected final void onNotify(String name) {
		Notifier notifier = Notifier.get(name);
		// Use invokeLater to call onInitialize, so it is called after the
		// native onInit callback.
		if (notifier == Notifier.INITIALIZE) {
			invokeLater(new Runnable() {
				public void run() {
					onNotify(Notifier.INITIALIZE);
				}
			});
		} else {
			onNotify(notifier);
		}
	}

	/**
	 * Allow UI items to store data along with them.
	 */
	public Map getData() {
		if (data == null)
			data = new HashMap();
		return data;
	}

	public void setData(Map data) {
		this.data = data;
	}

	private class RunnableTimer extends Timer {
		Runnable runnable;

		RunnableTimer(Runnable runnable) {
			super(0, false);
			this.runnable = runnable; 
		}

		protected boolean onExecute() {
			runnable.run();
			// Do not redraw document
			return false;
		}
	}

	/**
	 * @jshide
	 */
	public boolean invokeLater(Runnable runnable) {
		try {
			new RunnableTimer(runnable);
			return true;
		} catch (Throwable e) {
			return false;
		}
	}

	/**
	 * @jshide
	 */
	public boolean invokeLater(Callable function) {
		return invokeLater(new RunnableCallable(function, this));
	}
}
