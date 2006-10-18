/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2006 Juerg Lehni, http://www.scratchdisk.com.
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
 * $RCSfile: WrappableObject.java,v $
 * $Author: lehni $
 * $Revision: 1.2 $
 * $Date: 2006/10/18 14:12:51 $
 */

package com.scriptographer.js;

import org.mozilla.javascript.Scriptable;
import com.scriptographer.js.Wrappable;

/**
 * 
 * A simple class that implements Wrappable and provides a mechanism for storing
 * its JS wrapper internally, in order to make shure the same wrapper is reussed
 * if the object is wrapped again. Also, the wrapper object can be used in order
 * to execute JS functions on the object from within Java.
 * 
 * @author Lehni
 */
public class WrappableObject implements Wrappable {
 	protected Scriptable wrapper;
	
	public void setWrapper(Scriptable wrapper) {
		this.wrapper = wrapper;
	}
	
	public Scriptable getWrapper() {
		return wrapper;
	}
}
