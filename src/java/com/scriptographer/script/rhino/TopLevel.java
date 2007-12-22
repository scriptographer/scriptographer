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

package com.scriptographer.script.rhino;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import com.scratchdisk.script.Script;
import com.scratchdisk.script.ScriptEngine;
import com.scratchdisk.script.rhino.ExtendedJavaClass;
import com.scriptographer.ScriptographerEngine;
import com.scriptographer.adm.*;
import com.scriptographer.ai.*;
import com.scriptographer.sg.Application;
import com.scriptographer.sg.Scriptographer;
/**
 * @author lehni
 */
public class TopLevel extends com.scratchdisk.script.rhino.TopLevel {

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
		Margins.class,
		Size.class,

		// AWT Layout classes
		BorderLayout.class,
		Dimension.class,
		FlowLayout.class,

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
		PlacedItem.class,
		com.scriptographer.ai.Point.class,
		PointText.class,
		Raster.class,
		com.scriptographer.ai.Rectangle.class,
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
		com.scriptographer.sg.File.class
	};

	public TopLevel(Context context) {
		super(context);

		// define classes. the createPrototypes flag is set so
		// the classes' constructors can now wether an object
		// is created as prototype or as real object through
		// isCreatingPrototypes()

		for (int i = 0; i < classes.length; i++) {
			ExtendedJavaClass cls = new ExtendedJavaClass(this, classes[i], true);
			// Put it in the global scope:
			ScriptableObject.defineProperty(this, cls.getClassName(), cls,
				ScriptableObject.PERMANENT | ScriptableObject.READONLY
					| ScriptableObject.DONTENUM);
		}

		// Define some global functions and objects:
		String[] names = { "include", "execute", "evaluate" };
		defineFunctionProperties(names, TopLevel.class,
			ScriptableObject.READONLY | ScriptableObject.DONTENUM);

		// Properties:

		defineProperty("documents", DocumentList.getInstance(),
			ScriptableObject.READONLY | ScriptableObject.DONTENUM);

		defineProperty("fonts", FontList.getInstance(),
			ScriptableObject.READONLY | ScriptableObject.DONTENUM);

		try {
			defineProperty(this, "document", "getActiveDocument", null);
			defineProperty(this, "activeDocument", "getActiveDocument", null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		defineProperty("app", Application.getInstance(),
				ScriptableObject.READONLY | ScriptableObject.DONTENUM);

		defineProperty("scriptographer", Scriptographer.getInstance(),
				ScriptableObject.READONLY | ScriptableObject.DONTENUM);
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
		Object obj = scope.get("script", scope);
		if (obj == Scriptable.NOT_FOUND) obj = null;
		com.scriptographer.sg.Script script = (com.scriptographer.sg.Script) obj;
		if (script != null)
			return script.getFile().getParentFile();
		else
			return ScriptographerEngine.getScriptDirectory();
	}

	/*
	 * JavaScript functions
	 */
	protected static Object getActiveDocument(ScriptableObject obj) {
		return Context.javaToJS(Document.getActiveDocument(), obj);
	}

	/**
	 * Loads and executes a set of JavaScript source files in the current scope.
	 */
	public static void include(Context cx, Scriptable thisObj, Object[] args,
			Function funObj) throws Exception {
		File baseDir = getDirectory(thisObj);
		ScriptEngine engine = ScriptEngine.getEngineByName("JavaScript");
		for (int i = 0; i < args.length; i++) {
			File file = new File(baseDir, Context.toString(args[i]));
			Script script = engine.compile(file);
			if (script != null)
				script.execute(engine.getScope(thisObj));
		}
	}

	/**
	 * Loads and executes a set of JavaScript source files in a newly created
	 * scope.
	 */
	public static void execute(Context cx, Scriptable thisObj, Object[] args,
			Function funObj) throws Exception {
		File baseDir = getDirectory(thisObj);
		ScriptEngine engine = ScriptEngine.getEngineByName("JavaScript");
		for (int i = 0; i < args.length; i++) {
			File file = new File(baseDir, Context.toString(args[i]));
			Script script = engine.compile(file);
			if (script != null)
				script.execute(engine.createScope());
		}
	}

	/**
	 * Evaluates the given javascript string in the current scope. Similar to
	 * eval(), but it allows the use of another object than the global scope:
	 * e.g.:
	 * <pre>
	 * var obj = {
	 *     eval: evaluate
	 * };
	 * obj.eval("print(this);");
	 * </pre>
	 */
	public static void evaluate(Context cx, Scriptable thisObj, Object[] args,
			Function funObj) throws Exception {
		ScriptEngine engine = ScriptEngine.getEngineByName("JavaScript");
		engine.evaluate(Context.toString(args[0]), engine.getScope(thisObj));
	}
}
