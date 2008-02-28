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
 * File created on Apr 14, 2007.
 *
 * $Id$
 */

package com.scriptographer.script.jython;

import org.python.core.Py;
import org.python.core.PyInteger;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.core.PyTuple;
import org.python.core.__builtin__;

import com.scriptographer.ScriptographerEngine;

/**
 * @author lehni
 *
 */
public class JythonEngine extends com.scratchdisk.script.jython.JythonEngine {
	public JythonEngine() {
		// TODO: why is this needed? It is added in the classloader already!
		PySystemState.add_classdir(ScriptographerEngine.getPluginDirectory() + "/java/classes");
		globals.__setitem__(new PyString("test"), new PyInteger(100));
	    PyTuple all = new PyTuple(new PyString[] { Py.newString('*') });
	    __builtin__.__import__("com.scriptographer.ai", globals, globals, all);
	    __builtin__.__import__("com.scriptographer.adm", globals, globals, all);
//        __builtin__.eval(new PyString("from com.scriptographer.ai import *"), globals);
//        __builtin__.eval(new PyString("from com.scriptographer.adm import *"), globals);
	}
}
