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
 * File created on 06.03.2005.
 * 
 * $RCSfile: GlobalObject.java,v $
 * $Author: lehni $
 * $Revision: 1.9 $
 * $Date: 2005/10/10 08:39:21 $
 */

package com.scriptographer;

import org.mozilla.javascript.*;

import com.scriptographer.js.ScriptographerWrapFactory;
import com.scriptographer.js.ExtendedJavaClass;
import com.scriptographer.ai.*;
import com.scriptographer.adm.*;

import java.io.File;
import java.lang.reflect.Method;

public class GlobalObject extends ImporterTopLevel {

	protected GlobalObject(Context context) {
		super(context);

        WrapFactory wrapper = new ScriptographerWrapFactory();
        wrapper.setJavaPrimitiveWrap(false);
		context.setApplicationClassLoader(getClass().getClassLoader());
        context.setWrapFactory(wrapper);

		context.setOptimizationLevel(9);

		// define classes. the createPrototypes flag is set so
		// the classes' constructors can now wether an object
		// is created as prototype or as real object through
		// isCreatingPrototypes()

		// ADM
		new ExtendedJavaClass(this, Dialog.class);
		new ExtendedJavaClass(this, ModalDialog.class);
		new ExtendedJavaClass(this, FloatingDialog.class);
		new ExtendedJavaClass(this, PopupDialog.class);
		new ExtendedJavaClass(this, Drawer.class);
		new ExtendedJavaClass(this, Tracker.class);
		new ExtendedJavaClass(this, Image.class);
		new ExtendedJavaClass(this, ListItem.class);
		new ExtendedJavaClass(this, ListEntry.class);
		new ExtendedJavaClass(this, HierarchyList.class);
		new ExtendedJavaClass(this, HierarchyListEntry.class);
		// all item classes:
		new ExtendedJavaClass(this, Frame.class);
		new ExtendedJavaClass(this, ItemGroup.class);
		new ExtendedJavaClass(this, List.class);
		new ExtendedJavaClass(this, HierarchyList.class);
		new ExtendedJavaClass(this, Button.class);
		new ExtendedJavaClass(this, CheckBox.class);
		new ExtendedJavaClass(this, RadioButton.class);
		new ExtendedJavaClass(this, Static.class);
		new ExtendedJavaClass(this, ScrollBar.class);
		new ExtendedJavaClass(this, Slider.class);
		new ExtendedJavaClass(this, ProgressBar.class);
		new ExtendedJavaClass(this, TextEdit.class);
		new ExtendedJavaClass(this, Dial.class);
		new ExtendedJavaClass(this, ChasingArrows.class);
		new ExtendedJavaClass(this, PopupList.class);
		new ExtendedJavaClass(this, PopupMenu.class);
		new ExtendedJavaClass(this, SpinEditPopup.class);
		new ExtendedJavaClass(this, TextEditPopup.class);

		// layout specific classes
		new ExtendedJavaClass(this, ItemContainer.class);
		new ExtendedJavaClass(this, Spacer.class);
		new ExtendedJavaClass(this, TableLayout.class);
		new ExtendedJavaClass(this, java.awt.FlowLayout.class);
		new ExtendedJavaClass(this, java.awt.BorderLayout.class);

		// AI
		new ExtendedJavaClass(this, Rectangle.class);
		new ExtendedJavaClass(this, Point.class);
		new ExtendedJavaClass(this, Matrix.class);

		new ExtendedJavaClass(this, Color.class);
		new ExtendedJavaClass(this, Grayscale.class);
		new ExtendedJavaClass(this, RGBColor.class);
		new ExtendedJavaClass(this, CMYKColor.class);

		new ExtendedJavaClass(this, Art.class);
		new ExtendedJavaClass(this, Path.class);
		new ExtendedJavaClass(this, Group.class);
		new ExtendedJavaClass(this, Raster.class);
		new ExtendedJavaClass(this, Layer.class);
		new ExtendedJavaClass(this, CompoundPath.class);

		new ExtendedJavaClass(this, Segment.class);
		new ExtendedJavaClass(this, Curve.class);
		new ExtendedJavaClass(this, CurveParameter.class);
		new ExtendedJavaClass(this, TabletValue.class);
		new ExtendedJavaClass(this, Pathfinder.class);
		new ExtendedJavaClass(this, Document.class);
		new ExtendedJavaClass(this, LiveEffect.class);
		new ExtendedJavaClass(this, MenuItem.class);
		new ExtendedJavaClass(this, MenuGroup.class);
		new ExtendedJavaClass(this, Tool.class);
		new ExtendedJavaClass(this, Timer.class);
		new ExtendedJavaClass(this, Annotator.class);

		new ExtendedJavaClass(this, ArtSet.class);
		new ExtendedJavaClass(this, SegmentList.class);
		new ExtendedJavaClass(this, CurveList.class);

		// Java
		new ExtendedJavaClass(this, File.class);

		// define some global functions and objects:
		String[] names = { "print", "include", "execute", "evaluate", "commit", "getNanoTime", "getMousePoint" };
		defineFunctionProperties(names, GlobalObject.class, ScriptableObject.DONTENUM);

		// properties:
		defineProperty("documents", DocumentList.getInstance(), ScriptableObject.READONLY | ScriptableObject.DONTENUM);
		defineProperty("scriptDir", ScriptographerEngine.getScriptDirectory(), ScriptableObject.READONLY | ScriptableObject.DONTENUM);
		try {
			Method getter = GlobalObject.class.getDeclaredMethod("getActiveDocument", new Class[] { ScriptableObject.class });
			defineProperty("activeDocument", null, getter, null, ScriptableObject.DONTENUM);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getClassName() {
		return "global";
	}

	protected Scriptable createScope(File scriptFile) {
		ScriptableObject scope = new org.mozilla.javascript.NativeObject();
		scope.setPrototype(this);
		scope.setParentScope(null);
		scope.defineProperty("scriptFile", scriptFile, ScriptableObject.READONLY | ScriptableObject.DONTENUM);
		return scope;
	}
	
	/**
	 * Determines the directory of a script by reading it's scriptFile property
	 * in the main scope. If script file is empty (e.g. for console),
	 * Scriptographer's base directory is used
	 * 
	 * @param scope
	 * @return
	 */
	protected static File getDirectory(Scriptable scope) {
		File file = (File) scope.get("scriptFile", scope);
		if (file != null)
			file = file.getParentFile();
		else
			file = ScriptographerEngine.getScriptDirectory();
		return file;
	}

	/*
	 * JavaScript functions
	 *
	 */

	static Object getActiveDocument(ScriptableObject obj) {
		return DocumentList.getActive();
	}
	
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
	 * Loads and executes a set of JavaScript source files in the current scope.
	 */
	public static void include(Context cx, Scriptable thisObj, Object[] args,
		Function funObj) throws Exception {
		ScriptographerEngine engine = ScriptographerEngine.getInstance();
		File baseDir = getDirectory(thisObj);
		for (int i = 0; i < args.length; i++) {
			engine.executeFile(new File(baseDir, Context.toString(args[i])), thisObj);
		}
	}

	/**
	 * Loads and executes a set of JavaScript source files in a newly created scope.
	 */
	public static void execute(Context cx, Scriptable thisObj, Object[] args,
		Function funObj) throws Exception {
		ScriptographerEngine engine = ScriptographerEngine.getInstance();
		File baseDir = getDirectory(thisObj);
		for (int i = 0; i < args.length; i++) {
			engine.executeFile(new File(baseDir, Context.toString(args[i])), null);
		}
	}

	/**
	 * Evaluates the given javascript string in the current scope.
	 * Similar to eval(), but it allows the use of another object than
	 * the global scope:
	 * e.g.:
	 * <code>
	 * var obj = {
	 *     eval: evaluate
	 * };
	 * obj.eval("print(this);");
	 * </code>
	 */
	public static void evaluate(Context cx, Scriptable thisObj, Object[] args,
		Function funObj) throws Exception {
		ScriptographerEngine.getInstance().executeString(Context.toString(args[0]), thisObj);
	}

	/**
	 *
	 */
	public static void commit(Context cx, Scriptable thisObj, Object[] args,
		Function funObj) {
		CommitManager.commit();
	}
	
	public static long getNanoTime(Context cx, Scriptable thisObj, Object[] args,
		Function funObj) {
		return ScriptographerEngine.getNanoTime();
	}
	
	public static Point getMousePoint(Context cx, Scriptable thisObj, Object[] args,
		Function funObj) {
		return ScriptographerEngine.getMousePoint();
	}
}
