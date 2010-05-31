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
 * File created on May 29, 2010.
 */

package com.scriptographer.ui;

import com.scratchdisk.script.Callable;
import com.scratchdisk.util.ConversionUtils;
import com.scratchdisk.util.IntMap;
import com.scriptographer.ScriptographerEngine;
import com.scriptographer.ScriptographerException;
import com.scriptographer.sg.Script;

/**
 * @author lehni
 *
 */
public class Timer extends NativeObject {

	private int period;
	private boolean periodic;

	private Script script = null;

	private static IntMap<Timer> timers = new IntMap<Timer>();

	/**
	 * Creates a timer object.
	 * 
	 * @param period The timer's period in milliseconds.
	 * @param periodic Controls whether the timer is one-shop or periodic. 
	 */
	public Timer(int period, boolean periodic) {
		script = ScriptographerEngine.getCurrentScript();
		this.period = period;
		this.periodic = periodic;
		handle = nativeCreate(period);
		if (handle == 0)
			throw new ScriptographerException("Unable to create Timer.");
		timers.put(handle, this);
	}

	public Timer(int period) {
		this(period, true);
	}

	public void abort() {
		if (handle != 0) {
			timers.remove(handle);
			nativeAbort(handle);
			handle = 0;
		}
	}

	private native int nativeCreate(int period);
	private native void nativeAbort(int handle);

	public double getPeriod() {
		return period;
	}
	
	public boolean isPeriodic() {
		return periodic;
	}
	
	protected boolean canAbort(boolean ignoreKeepAlive) {
		return script == null || script.canRemove(ignoreKeepAlive);
	}

	public static void abortAll(boolean ignoreKeepAlive, boolean force) {
		// As abort() modifies the Map, using an iterator is not possible here:
		Object[] timers = Timer.timers.values().toArray();
		for (int i = 0; i < timers.length; i++) {
			Timer timer = (Timer) timers[i];
			if (force || timer.canAbort(ignoreKeepAlive))
				timer.abort();
		}
	}
	
	private Callable onExecute = null;

	public void setOnExecute(Callable onExecute) {
		this.onExecute = onExecute;
	}
	
	public Callable getOnExecute() {
		return onExecute;
	}

	/**
	 * The timer callback.
	 * 
	 * Return true if document should be redrawn automatically.
	 */
	protected boolean onExecute() {
		if (onExecute != null) {
			Object result = ScriptographerEngine.invoke(onExecute, this);
			if (result != null)
				return ConversionUtils.toBoolean(result);
		}
		return false;
	}

	/**
	 * To be called from the native environment:
	 */
	@SuppressWarnings("unused")
	private static boolean onExecute(int handle) {
		Timer timer = getTimer(handle);
		if (timer != null) {
			try {
				return timer.onExecute();
			} finally {
				// Simulate one shot timers by aborting:
				if (!timer.periodic)
					timer.abort();
			}
		}
		return false;
	}

	private static Timer getTimer(int handle) {
		return (Timer) timers.get(handle);
	}

	protected void finalize() {
		abort();
	}

	public Object getId() {
		// Return the integer handle as id instead of a formated string, as the
		// script side wants to work with numeric timer ids.
		return handle;
	}
}
