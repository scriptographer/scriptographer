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
 * File created on 03.01.2005.
 *
 * $RCSfile: Wrappable.java,v $
 * $Author: lehni $
 * $Revision: 1.2 $
 * $Date: 2005/10/23 00:30:13 $
 */

package com.scriptographer.js;

import org.mozilla.javascript.Scriptable;

/**
 * The Wrappable interface allows normal Java object to know about their JS
 * wrapper, if present. They can keep track of their wrapper in an internal
 * wrapper field and use it for calling JS functions.
 * 
 * If the Java class implements the Unsealed interface as well, then the JS
 * wrapper scope will be created as an instance of the class
 * UnsealedJavaObjectWrapper which allows adding of further properties.
 * 
 * This together is very useable for defining callback functions from JS and
 * easily calling them from J.
 * 
 * See ScriptographerWrapFactory for information about how this is all achieved.
 * 
 * Another advantage is that Wrappable objects will reuse their wrapper when
 * wrapped again through the WrapFactory, so the user can be shure to allways
 * access not only the same Java object but also the same wrapper, with
 * eventually additionally set properties (e.g. an Unsealed object).
 * 
 * @author Lehni
 */
public interface Wrappable {
	/**
	 * 
	 * @param scope
	 * @param staticType
	 * @return
	 */
	Scriptable getWrapper();
	
	/**
	 * 
	 * @param wrapper
	 */
	void setWrapper(Scriptable wrapper);
}
