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

package com.scriptographer.ai;

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
	private static boolean onExecute(int handle) {
		Timer timer = getTimer(handle);
		if (timer != null) {
			try {
				Document document = Document.getActiveDocument();
				// Produce a normal undo cycle if in the previous cycle we have
				// created or removed items. Otherwise just merge the changes of
				// this cycle with the previous one.
				// It is important to create new cycles when such changes
				// happen, as they affect the live span of items within the undo
				// history, and if all cycles were merged, the history tracking
				// code in Document would not be able to track their life span.
				int undoType = document == null || document.hasCreatedState()
						|| document.hasRemovedState() 
								? Document.UNDO_STANDARD
								: Document.UNDO_MERGE;
				// Clear changed states now to track for new changes in
				// onExecute()
				if (document != null)
					document.clearChangedStates();
				boolean changed = timer.onExecute()
						|| document != null && document.hasChangedSates();
				// Only change undo type if the document have actually changed,
				// or if the type is MERGE, in which case we will not add new
				// unto levels. This is actually needed to prevent a weird bug
				// that occasionally happens where Ai keeps adding new levels.
				// To not set STANDARD in all cases prevents issues with
				// situations where Timers are used for ADM interface stuff,
				// e.g. invokeLater().
				if (document != null
						&& (changed || undoType == Document.UNDO_MERGE)) {
					document.setUndoType(undoType);
				}
				return changed;
			} finally {
				// Simulate one shot timers by aborting:
				if (!timer.periodic)
					timer.abort();
			}
		}
		return false;
	}

	private static Timer getTimer(int handle) {
		return timers.get(handle);
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
