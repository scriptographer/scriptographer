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
 * File created on 06.03.2005.
 * 
 * $Id$
 */

package com.scriptographer;

import org.mozilla.javascript.*;
import org.mozilla.javascript.tools.debugger.ScopeProvider;

import com.scriptographer.js.ExtendedJavaClass;
import com.scriptographer.ai.*;
import com.scriptographer.adm.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.prefs.Preferences;

/**
 * @author lehni
 */
public class GlobalObject extends ImporterTopLevel implements ScopeProvider {

	final static Class classes[] = {
		// ADM, alphabetically
		Button.class,
		ChasingArrows.class,
		Dial.class,
		CheckBox.class,
		Dialog.class,
		DialogGroupInfo.class,
		Drawer.class,
		FloatingDialog.class,
		FontInfo.class,
		Frame.class,
		HierarchyList.class,
		HierarchyListEntry.class,
		Image.class,
		ImageButton.class,
		ImageCheckBox.class,
		ImageRadioButton.class,
		ImageStatic.class,
		Item.class,
		ItemContainer.class,
		ItemGroup.class,
		Key.class,
		List.class,
		ListEntry.class,
		ListItem.class,
		MenuGroup.class,
		MenuItem.class,
		ModalDialog.class,
		PopupDialog.class,
		PopupList.class,
		PopupMenu.class,
		ProgressBar.class,
		RadioButton.class,
		ScrollBar.class,
		Slider.class,
		Spacer.class,
		SpinEdit.class,
		Static.class,
		TableLayout.class,
		TextEdit.class,
		TextItem.class,
		TextValueItem.class,
		ToggleItem.class,
		Tracker.class,

		// AWT Layout classes
		java.awt.BorderLayout.class,
		java.awt.Dimension.class,
		java.awt.FlowLayout.class,
		java.awt.Insets.class,

		// AI, alphabetically
		Annotator.class,
		AreaText.class,
		Art.class,
		ArtSet.class,
		CharacterStyle.class,
		CMYKColor.class,
		Color.class,
		CompoundPath.class,
		Curve.class,
		CurveList.class,
		Document.class,
		DocumentList.class,
		DocumentView.class,
		DocumentViewList.class,
		Event.class,
		FileFormat.class,
		FillStyle.class,
		FontFamily.class,
		FontList.class,
		FontWeight.class,
		Gradient.class,
		GradientColor.class,
		GradientList.class,
		GradientStop.class,
		GradientStopList.class,
		GrayColor.class,
		Group.class,
		HitTest.class,
		Layer.class,
		LayerList.class,
		LiveEffect.class,
		Matrix.class,
		ParagraphStyle.class,
		Path.class,
		Pathfinder.class,
		PathStyle.class,
		PathText.class,
		Pattern.class,
		PatternColor.class,
		PatternList.class,
		Point.class,
		PointText.class,
		Raster.class,
		Rectangle.class,
		RGBColor.class,
		Segment.class,
		SegmentList.class,
		StrokeStyle.class,
		Swatch.class,
		SwatchList.class,
		Symbol.class,
		SymbolItem.class,
		SymbolList.class,
		TabletValue.class,
		TextFrame.class,
		TextRange.class,
		TextStory.class,
		Timer.class,
		Tool.class,
		Tracing.class,

		// Java
		File.class
	};

	protected GlobalObject(Context context) {
		super(context);

		// define classes. the createPrototypes flag is set so
		// the classes' constructors can now wether an object
		// is created as prototype or as real object through
		// isCreatingPrototypes()

		for (int i = 0; i < classes.length; i++)
			new ExtendedJavaClass(this, classes[i]);

		// define some global functions and objects:
		String[] names = { "print", "include", "execute", "evaluate", "commit",
			"getNanoTime", "updateProgress" };
		defineFunctionProperties(names, GlobalObject.class,
			ScriptableObject.READONLY | ScriptableObject.DONTENUM);

		// properties:
		defineProperty("documents", DocumentList.getInstance(),
			ScriptableObject.READONLY | ScriptableObject.DONTENUM);
		defineProperty("fonts", FontList.getInstance(),
			ScriptableObject.READONLY | ScriptableObject.DONTENUM);
		defineProperty(this, "activeDocument", "getActiveDocument", null);
		defineProperty(this, "scriptDir", "getScriptDirectory", null);
	}

	protected static void defineProperty(ScriptableObject obj, String name,
			String getter, String setter) {
		try {
			Method getterMethod = getter != null ?
				GlobalObject.class.getDeclaredMethod(getter,
					new Class[] { ScriptableObject.class }) : null;
			Method setterMethod = setter != null ?
				GlobalObject.class.getDeclaredMethod(setter, new Class[] {
					ScriptableObject.class, Object.class }) : null;
			obj.defineProperty(name, null, getterMethod, setterMethod,
				ScriptableObject.DONTENUM);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String getClassName() {
		return "global";
	}

	protected Scriptable createScope(File scriptFile) {
		ScriptableObject scope = new NativeObject();
		scope.setPrototype(this);
		scope.setParentScope(null);
		scope.defineProperty("scriptFile", scriptFile,
			ScriptableObject.READONLY | ScriptableObject.DONTENUM);
		defineProperty(scope, "preferences", "getPreferences", null);
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

	protected static Object getActiveDocument(ScriptableObject obj) {
		return ScriptographerEngine.javaToJS(Document.getActiveDocument());
	}

	protected static Object getScriptDirectory(ScriptableObject obj) {
		return ScriptographerEngine.javaToJS(
			ScriptographerEngine.getScriptDirectory());
	}

	protected static Object getPreferences(ScriptableObject obj)
			throws IOException {
		// determine preferences for the current executing script
		// by walking up the file path to the script directory and using each
		// folder
		// as a preference node.
		File file = (File) obj.get("scriptFile", obj);
		Preferences prefs = ScriptographerEngine.getPreferences(false).node(
			"scripts");
		ArrayList parts = new ArrayList();
		File root = ScriptographerEngine.getScriptDirectory();
		// collect the directory parts up to root
		do {
			parts.add(file.getName());
			file = file.getParentFile();
		} while (file != null && !file.equals(root));

		for (int i = parts.size() - 1; i >= 0; i--) {
			prefs = prefs.node((String) parts.get(i));
		}
		// now replace it with the result so getPreferences is only called once:
		obj.defineProperty("preferences", prefs, ScriptableObject.READONLY
			| ScriptableObject.DONTENUM);

		return ScriptographerEngine.javaToJS(prefs);
	}

	/**
	 * Print the string segmentValues of its arguments.
	 * 
	 * This method is defined as a JavaScript function. Note that its arguments
	 * are of the "varargs" form, which allows it to handle an arbitrary number
	 * of arguments supplied to the JavaScript function.
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
		File baseDir = getDirectory(thisObj);
		for (int i = 0; i < args.length; i++) {
			ScriptographerEngine.executeFile(new File(baseDir,
				Context.toString(args[i])), thisObj);
		}
	}

	/**
	 * Loads and executes a set of JavaScript source files in a newly created
	 * scope.
	 */
	public static void execute(Context cx, Scriptable thisObj, Object[] args,
			Function funObj) throws Exception {
		File baseDir = getDirectory(thisObj);
		for (int i = 0; i < args.length; i++) {
			ScriptographerEngine.executeFile(new File(baseDir,
				Context.toString(args[i])), null);
		}
	}

	/**
	 * Evaluates the given javascript string in the current scope. Similar to
	 * eval(), but it allows the use of another object than the global scope:
	 * e.g.: <code>
	 * var obj = {
	 *     eval: evaluate
	 * };
	 * obj.eval("print(this);");
	 * </code>
	 */
	public static void evaluate(Context cx, Scriptable thisObj, Object[] args,
			Function funObj) throws Exception {
		ScriptographerEngine.executeString(Context.toString(args[0]), thisObj);
	}

	/**
	 * 
	 */
	public static void commit(Context cx, Scriptable thisObj, Object[] args,
			Function funObj) {
		// call with key set to null so the commit version is not increased
		// (see CommitManager.commit()
		CommitManager.commit(null);
	}

	public static long getNanoTime(Context cx, Scriptable thisObj,
			Object[] args, Function funObj) {
		return ScriptographerEngine.getNanoTime();
	}

	public static Point getMousePoint(Context cx, Scriptable thisObj,
			Object[] args, Function funObj) {
		return null;
	}

	public static boolean updateProgress(Context cx, Scriptable thisObj,
			Object[] args, Function funObj) {
		return ScriptographerEngine.updateProgress(
			(long) Context.toNumber(args[0]),
			(long) Context.toNumber(args[1])
		);
	}

	public Scriptable getScope() {
		return this;
	}
}
