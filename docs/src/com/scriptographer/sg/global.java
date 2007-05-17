/*
 * Scriptographer
 * 
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 * 
 * Copyright (c) 2002-2007 Juerg Lehni, http://www.scratchdisk.com.
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
 * File created on 10.06.2006.
 * 
 * $Id: global.java 317 2007-05-01 17:21:24Z lehni $
 */

package com.scriptographer.sg;

/*
 * This class is just a dummy to get Javadoc to generate documentation for
 * the global scope
 */

/**
 * Objects and functions present in the global scope. These can be used anywhere
 * in scripts.
 * 
 * @author lehni
 */
public class global {

	private global() {
	}

	/**
	 * The global scriptographer object.
	 */
	public Scriptographer scriptographer;

	/**
	 * The global application object.
	 */
	public Application app;

	/**
	 * The global script object.
	 */
	public Script script;

	/**
	 * Prints one or more passed parameters to the console.
	 */
	public void print() {
	}

	/**
	 * Includes and evaluates one or more other javascripts.
	 * <p>e.g:
	 * <pre>include("raster.js", "mysql.js");</pre>
	 */
	public void include() {
	}
}
