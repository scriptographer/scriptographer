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
 * File created on 04.12.2004.
 *
 * $RCSfile: ScriptographerEngine.java,v $
 * $Author: lehni $
 * $Revision: 1.2 $
 * $Date: 2005/03/05 21:21:16 $
 */

package com.scriptographer;

import com.scriptographer.adm.*;
import com.scriptographer.ai.*;
import com.scriptographer.js.*;
import org.mozilla.javascript.*;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;

public class ScriptographerEngine extends ScriptableObject {

	private static ScriptographerEngine engine = null;
	private Context context;
	private Tool[] tools;
	public static final boolean isWindows, isMacOSX;

	static {
		// immediatelly redirect system streams.
		ConsoleOutputStream.getConsole().enableRedirection(true);
		// getSystem variables
		String os = System.getProperty("os.name").toLowerCase();
		isWindows = (os.indexOf("windows") != -1);
		isMacOSX = (os.indexOf("mac os x") != -1);
	}

	public ScriptographerEngine() throws Exception {
		// create the context
		context = Context.enter();
		// context.setCompileFunctionsWithDynamicScope(true);

        WrapFactory wrapper = new ScriptographerWrapFactory();
        wrapper.setJavaPrimitiveWrap(false);
		context.setApplicationClassLoader(getClass().getClassLoader());
        context.setWrapFactory(wrapper);

		context.setOptimizationLevel(9);
		// init the global scope and seal it. Use dynamic scopes?
		context.initStandardObjects(this, true);
		// define some global functions and objects:
		String[] names = { "print", "include", "commit" };
		defineFunctionProperties(names, ScriptographerEngine.class, ScriptableObject.DONTENUM);

		// define classes. the createPrototypes flag is set so
		// the classes' constructors can now wether an object
		// is created as prototype or as real object through
		// isCreatingPrototypes()

		// ADM
		new ExtendedJavaClass(this, Dialog.class);
		new ExtendedJavaClass(this, Drawer.class);
		new ExtendedJavaClass(this, Tracker.class);
		new ExtendedJavaClass(this, Image.class);
		new ExtendedJavaClass(this, List.class);
		new ExtendedJavaClass(this, ListEntry.class);
		new ExtendedJavaClass(this, HierarchyList.class);
		new ExtendedJavaClass(this, HierarchyListEntry.class);
		// all item classes:
		new ExtendedJavaClass(this, Frame.class);
		new ExtendedJavaClass(this, ItemGroup.class);
		new ExtendedJavaClass(this, ListBox.class);
		new ExtendedJavaClass(this, HierarchyListBox.class);
		new ExtendedJavaClass(this, PushButton.class);
		new ExtendedJavaClass(this, CheckBox.class);
		new ExtendedJavaClass(this, RadioButton.class);
		new ExtendedJavaClass(this, Static.class);
		new ExtendedJavaClass(this, ScrollBar.class);
		new ExtendedJavaClass(this, Slider.class);
		new ExtendedJavaClass(this, ProgressBar.class);
		new ExtendedJavaClass(this, TextEdit.class);
		new ExtendedJavaClass(this, Dial.class);
		new ExtendedJavaClass(this, ChasingArrows.class);

		new ExtendedJavaClass(this, ItemContainer.class);

		// AI
		new ExtendedJavaClass(this, Rectangle.class);
		new ExtendedJavaClass(this, Point.class);
		new ExtendedJavaClass(this, Matrix.class);

		new ExtendedJavaClass(this, Color.class);
		new ExtendedJavaClass(this, Grayscale.class);
		new ExtendedJavaClass(this, RGBColor.class);
		new ExtendedJavaClass(this, CMYKColor.class);

		new ExtendedJavaClass(this, Path.class);
		new ExtendedJavaClass(this, Group.class);
		new ExtendedJavaClass(this, Raster.class);
		new ExtendedJavaClass(this, Layer.class);

		new ExtendedJavaClass(this, Segment.class);
		new ExtendedJavaClass(this, Curve.class);
		new ExtendedJavaClass(this, SegmentPosition.class);
		new ExtendedJavaClass(this, Pathfinder.class);
		new ExtendedJavaClass(this, Document.class);
		new ExtendedJavaClass(this, LiveEffect.class);
		new ExtendedJavaClass(this, MenuItem.class);
		new ExtendedJavaClass(this, MenuGroup.class);

		new ExtendedJavaClass(this, ArtSet.class);
		new ExtendedJavaClass(this, SegmentList.class);
		new ExtendedJavaClass(this, CurveList.class);
		// not needed: new ExtendedJavaClass(this, DocumentList.class);
		// not needed: new ExtendedJavaClass(this, LayerList.class);

		// Java
		new ExtendedJavaClass(this, File.class);

		new ExtendedJavaClass(this, Event.class);

		defineProperty("layers", LayerList.getInstance(), ScriptableObject.READONLY | ScriptableObject.DONTENUM);
		defineProperty("documents", DocumentList.getInstance(), ScriptableObject.READONLY | ScriptableObject.DONTENUM);

		tools = new Tool[2];
	}

	public static void init() {
		ConsoleOutputStream.enableOutput(true);
	}

	public static void destroy() {
		Dialog.destroyAll();
		LiveEffect.removeAll();
		MenuItem.removeAll();
		ConsoleOutputStream.getConsole().enableRedirection(false);
	}

	public static ScriptographerEngine getEngine() throws Exception {
		if (engine == null)
			engine = new ScriptographerEngine();
		return engine;
	}

	/**
	 * @see org.mozilla.javascript.ScriptableObject#getClassName()
	 */
	public String getClassName() {
		return "global";
	}

	private Scriptable createScope() {
		Scriptable scope = context.newObject(this);
		scope.setPrototype(this);
		scope.setParentScope(null);
		return scope;
	}

	public Script compileFile(String filename) {
		FileReader in = null;
		Script script = null;
		try {
			in = new FileReader(filename);
			script = context.compileReader(in, filename, 1, null);
		} catch (RhinoException re) {
			System.err.println(re.sourceName() + ":" + re.lineNumber() + "," + re.columnNumber() + ": " + re.getMessage());
		} catch (FileNotFoundException ex) {
			Context.reportError("Couldn't open file \"" + filename + "\".");
		} catch (IOException ioe) {
			System.err.println(ioe.toString());
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ioe) {
					System.err.println(ioe.toString());
				}
			}
		}
		return script;
	}

	public Script compileString(String string) {
		try {
			return context.compileString(string, "console", 1, null);
		} catch (RhinoException re) {
			System.err.println(re.sourceName() + ":" + re.lineNumber() + "," + re.columnNumber() + ": " + re.getMessage());
		}
		return null;
	}

	public Scriptable execScript(Script script) {
		Scriptable ret = null;
		try {
			// This is needed on mac, where there is more than one thread and the Loader is initiated on startup
			// in the second thread. The ScriptographerEngine get loaded through the Loader, so getting the
			// ClassLoader from there is save:
			Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
			Scriptable scope = createScope();
			ConsoleOutputStream.enableOutput(false);
			script.exec(context, scope);
			CommitManager.commit();
			ret = scope;
		} catch (WrappedException we) {
			System.err.println(we.getMessage());
			we.getWrappedException().printStackTrace();
		} catch (RhinoException re) {
			System.err.println(re.sourceName() + ":" + re.lineNumber() + "," + re.columnNumber() + ": " + re.getMessage());
		} finally {
			ConsoleOutputStream.enableOutput(true);
		}
		return ret;
	}

	public Scriptable evaluateFile(String filename) {
		// TODO: use cashing of compiled script files, watch changes!
		Script script = compileFile(filename);
		if (script != null)
			return execScript(script);
		return null;
	}

	public Scriptable evaluateString(String string) {
		Script script = compileString(string);
		if (script != null)
			return execScript(script);
		return null;
	}

	public Tool getTool(int index) {
		Tool tool = tools[index];
		if (tool == null) tools[index] = tool = new Tool();
		return tool;
	}

	/*
	 * JavaScript functions
	 * 
	 */

	/**
	 * Print the string segmentValues of its arguments.
	 *
	 * This method is defined as a JavaScript function.
	 * Note that its arguments are of the "varargs" form, which
	 * allows it to handle an arbitrary number of arguments
	 * supplied to the JavaScript function.
	 *
	 */
	public static void print(Context cx, Scriptable thisObj, Object[] args,
		Function funObj) {
		for (int i = 0; i < args.length; i++) {
			if (i > 0)
				System.out.print(", ");

			// Convert the arbitrary JavaScript value into a string form.
			String s = Context.toString(args[i]);
			System.out.print(s);
		}
		System.out.println();
	}

	/**
	 * Load and execute a set of JavaScript source files.
	 *
	 * This method is defined as a JavaScript function.
	 *
	 */
	public static void include(Context cx, Scriptable thisObj, Object[] args,
		Function funObj) throws Exception {
		ScriptographerEngine engine = getEngine();
		for (int i = 0; i < args.length; i++) {
			engine.evaluateFile(Context.toString(args[i]));
		}
	}

	public static void commit(Context cx, Scriptable thisObj, Object[] args,
		Function funObj) {
		CommitManager.commit();
	}

	public static void main(String args[]) throws Exception {
		ConsoleOutputStream.getConsole().enableRedirection(false);
	 	getEngine().evaluateFile("/Users/Lehni/Development/C & C++/Java Scriptographer/Scriptographer Scripts/test.js");
	}
}