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
 * File created on Apr 11, 2008.
 *
 * $Id$
 */

package com.scriptographer.ai;

import com.scriptographer.NamedOption;

/**
 * AIKnockout
 * 
 * @author lehni
 */
public class Knockout extends NamedOption {

	public static final Knockout
		UNKNOWN		= new Knockout("unknown",	-1),
		OFF			= new Knockout("off",		0),
		ON 			= new Knockout("on", 		1),
		INHERIT		= new Knockout("inherit", 	2);


	private Knockout(String name, int value) {
		super(name, value);
	}

	protected static Knockout get(int value) {
		return (Knockout) get(Knockout.class, value);
	}
}
