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
 * $RCSfile: GlobalScope.java,v $
 * $Author: lehni $
 * $Revision: 1.1 $
 * $Date: 2005/03/07 13:36:38 $
 */

package com.scriptographer;

import org.mozilla.javascript.*;
import com.scriptographer.js.ScriptographerWrapFactory;
import com.scriptographer.js.ExtendedJavaClass;
import com.scriptographer.ai.*;
import com.scriptographer.adm.*;

import java.io.File;

public class GlobalScope extends ImporterTopLevel {

	private Context context;

	protected GlobalScope(Context context) {
		super(context);

		this.context = context;

		// context.setCompileFunctionsWithDynamicScope(true);

        WrapFactory wrapper = new ScriptographerWrapFactory();
        wrapper.setJavaPrimitiveWrap(false);
		context.setApplicationClassLoader(getClass().getClassLoader());
        context.setWrapFactory(wrapper);

		context.setOptimizationLevel(9);
		// define some global functions and objects:
		String[] names = { "print", "include", "execute", "evaluate", "commit" };
		defineFunctionProperties(names, GlobalScope.class, ScriptableObject.DONTENUM);

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
		new ExtendedJavaClass(this, Spacer.class);

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
		new ExtendedJavaClass(this, Tool.class);

		new ExtendedJavaClass(this, ArtSet.class);
		new ExtendedJavaClass(this, SegmentList.class);
		new ExtendedJavaClass(this, CurveList.class);

		// Java
		new ExtendedJavaClass(this, File.class);

		new ExtendedJavaClass(this, Event.class);

		defineProperty("documents", DocumentList.getInstance(), ScriptableObject.READONLY | ScriptableObject.DONTENUM);
	}

	public String getClassName() {
		return "global";
	}

	protected Scriptable createScope() {
		Scriptable scope = context.newObject(this);
		scope.setPrototype(this);
		scope.setParentScope(null);
		return scope;
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
	 * Loads and executes a set of JavaScript source files in the current scope.
	 */
	public static void include(Context cx, Scriptable thisObj, Object[] args,
		Function funObj) throws Exception {
		ScriptographerEngine engine = ScriptographerEngine.getInstance();
		for (int i = 0; i < args.length; i++) {
			engine.executeFile(Context.toString(args[i]), thisObj);
		}
	}

	/**
	 * Loads and executes a set of JavaScript source files in a newly created scope.
	 */
	public static void execute(Context cx, Scriptable thisObj, Object[] args,
		Function funObj) throws Exception {
		ScriptographerEngine engine = ScriptographerEngine.getInstance();
		for (int i = 0; i < args.length; i++) {
			engine.executeFile(Context.toString(args[i]), null);
		}
	}

	/**
	 * Loads and executes a set of JavaScript source files in a newly created scope.
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
}
