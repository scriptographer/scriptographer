/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2005 Juerg Lehni, http://www.scratchdisk.com.
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
 * $RCSfile: NotificationHandler.java,v $
 * $Author: lehni $
 * $Revision: 1.2 $
 * $Date: 2005/04/04 17:06:13 $
 */

package com.scriptographer.adm;

import org.mozilla.javascript.Function;

import com.scriptographer.js.FunctionHelper;
import com.scriptographer.js.Unsealed;

abstract class NotificationHandler extends ADMObject implements Unsealed {
	private Tracker tracker = new Tracker();
 	private Drawer drawer = new Drawer();

 	protected static Object[] zeroArgs = new Object[0];
	protected Object[] oneArg = new Object[1];
	protected Object[] twoArgs = new Object[2];
	
	protected abstract void onNotify(int notifier) throws Exception;
	protected abstract boolean onTrack(Tracker tracker) throws Exception;
	protected abstract void onDraw(Drawer drawer) throws Exception;

	protected final void onNotify(String notifier) throws Exception {
//		System.out.println(this + " " + notifier);
		onNotify(Notifier.lookup(notifier));
	}
	
	protected Object callFunction(String name) throws Exception {
		if (wrapper != null)
			return FunctionHelper.callFunction(wrapper, name, zeroArgs);
		return null;
	}
	
	protected Object callFunction(String name, Object param1) throws Exception {
		if (wrapper != null) {
			oneArg[0] = param1;
			return FunctionHelper.callFunction(wrapper, name, oneArg);
		}
		return null;
	}
	
	protected Object callFunction(String name, Object param1, Object param2) throws Exception {
		if (wrapper != null) {
			twoArgs[0] = param1;
			twoArgs[1] = param2;
			return FunctionHelper.callFunction(wrapper, name, twoArgs);
		}
		return null;
	}
	
	protected Object callFunction(Function function, Object param1) throws Exception {
		if (wrapper != null) {
			oneArg[0] = param1;
			return FunctionHelper.callFunction(wrapper, function, oneArg);
		}
		return null;
	}
	
	protected Object callFunction(Function function, Object param1, Object param2) throws Exception {
		if (wrapper != null) {
			twoArgs[0] = param1;
			twoArgs[1] = param2;
			return FunctionHelper.callFunction(wrapper, function, twoArgs);
		}
		return null;
	}
}
