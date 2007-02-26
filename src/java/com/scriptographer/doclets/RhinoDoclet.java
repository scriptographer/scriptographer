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
 * File created on Jan 23, 2007.
 * 
 * $IdRhinoDoclet.java,v $
 * $Author: lehni lehni $
 * $Revision: 209 $
 * $Date: 2006-12-20 14:37:20 +0100 (Wed, 20 Dec 2006) Jan 23, 2007 $
 */

package com.scriptographer.doclets;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.mozilla.javascript.*;

import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.Doclet;
import com.sun.javadoc.RootDoc;

public class RhinoDoclet extends Doclet {
	static NativeObject options;
	static File file;
	
	public static boolean start(RootDoc root) {
		ContextFactory.initGlobal(new DocletContextFactory());
		Context cx = Context.enter();
		try {
		    Scriptable scope = new GlobalObject(cx);
		    scope.put("root", scope, root);
		    scope.put("options", scope, options);
		    FileReader in = new FileReader(file);
		    cx.evaluateReader(scope, in, file.getName(), 1, null);
		} catch (RhinoException e) {
			System.err.println(e.details());
			System.err.print(e.getScriptStackTrace());
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
		    Context.exit();
		}
		return true;
	}

	public static int optionLength(String option) {
		// The parameters used in JS Doclets can only have a name and one value
		return 2;
	}
	
	static public boolean validOptions(String[][] args, DocErrorReporter err) {
		options = new NativeObject();
		for (int i = 0; i < args.length; i++) {
			String[] arg = args[i];
			// cut away the "-" from passed options
			// not specifying a value for any given parameter equals to true
			options.put(arg[0].substring(1), options, arg.length > 1 ? arg[1] : "true");
		}
		Object value = options.get("script", options);
		if (value != Scriptable.NOT_FOUND) {
			file = new File((String) value);
			if (!file.exists()) {
				err.printError("File " + file + " does not exist.");
				return false;
			}
		} else {
			err.printError("Please specify a script file.");
			return false;
		}
		return true;
	}
}