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
 * File created on Feb 19, 2007.
 *
 * $Id$
 */

package com.scratchdisk.script;

/**
 * @author lehni
 *
 */
public abstract class Scope {

	public abstract Object[] getKeys();

	public abstract Object getScope();

	public abstract Object get(String name);

	public abstract Object put(String name, Object value, boolean readOnly);

	public Object put(String name, Object value) {
		return put(name, value, false);
	}

	public Callable getCallable(String name) {
		Object obj = get(name);
		return obj instanceof Callable ? (Callable) obj : null;
	}
}
