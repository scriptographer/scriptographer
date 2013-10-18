/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Scripting Plugin for Adobe Illustrator
 * http://scriptographer.org/
 *
 * Copyright (c) 2002-2010, Juerg Lehni
 * http://scratchdisk.com/
 *
 * All rights reserved. See LICENSE file for details.
 * 
 * File created on 06.03.2005.
 */

package com.scriptographer.script.rhino;

import java.io.File;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Wrapper;

import com.scratchdisk.script.Scope;
import com.scratchdisk.script.Script;
import com.scratchdisk.script.ScriptEngine;
import com.scratchdisk.script.ScriptException;
import com.scratchdisk.script.rhino.ExtendedJavaClass;

import com.scriptographer.adm.Border;
import com.scriptographer.adm.Button;
import com.scriptographer.adm.ChasingArrows;
import com.scriptographer.adm.CheckBox;
import com.scriptographer.adm.Dial;
import com.scriptographer.adm.DialogColor;
import com.scriptographer.adm.DialogGroupInfo;
import com.scriptographer.adm.Drawer;
import com.scriptographer.adm.FloatingDialog;
import com.scriptographer.adm.FontInfo;
import com.scriptographer.adm.Frame;
import com.scriptographer.adm.HierarchyListBox;
import com.scriptographer.adm.HierarchyListEntry;
import com.scriptographer.adm.Image;
import com.scriptographer.adm.ImageButton;
import com.scriptographer.adm.ImageCheckBox;
import com.scriptographer.adm.ImagePane;
import com.scriptographer.adm.ImageRadioButton;
import com.scriptographer.adm.ItemGroup;
import com.scriptographer.adm.ListBox;
import com.scriptographer.adm.ListEntry;
import com.scriptographer.adm.ListItem;
import com.scriptographer.adm.ModalDialog;
import com.scriptographer.adm.PopupDialog;
import com.scriptographer.adm.PopupList;
import com.scriptographer.adm.PopupMenu;
import com.scriptographer.adm.ProgressBar;
import com.scriptographer.adm.RadioButton;
import com.scriptographer.adm.ScrollBar;
import com.scriptographer.adm.Slider;
import com.scriptographer.adm.Spacer;
import com.scriptographer.adm.SpinEdit;
import com.scriptographer.adm.TextEdit;
import com.scriptographer.adm.TextPane;
import com.scriptographer.adm.TextValueItem;
import com.scriptographer.adm.ToggleItem;
import com.scriptographer.adm.Tracker;


import com.scriptographer.ai.Annotator;
import com.scriptographer.ai.AreaText;
import com.scriptographer.ai.Artboard;
import com.scriptographer.ai.CMYKColor;
import com.scriptographer.ai.CharacterStyle;
import com.scriptographer.ai.Color;
import com.scriptographer.ai.CompoundPath;
import com.scriptographer.ai.Curve;
import com.scriptographer.ai.Dictionary;
import com.scriptographer.ai.Document;
import com.scriptographer.ai.DocumentList;
import com.scriptographer.ai.DocumentView;
import com.scriptographer.ai.FileFormat;
import com.scriptographer.ai.FillStyle;
import com.scriptographer.ai.FontFamily;
import com.scriptographer.ai.FontList;
import com.scriptographer.ai.FontWeight;
import com.scriptographer.ai.Gradient;
import com.scriptographer.ai.GradientColor;
import com.scriptographer.ai.GradientStop;
import com.scriptographer.ai.GrayColor;
import com.scriptographer.ai.Group;
import com.scriptographer.ai.HitResult;
import com.scriptographer.ai.Layer;
import com.scriptographer.ai.Line;
import com.scriptographer.ai.LiveEffect;
import com.scriptographer.ai.LiveEffectParameters;
import com.scriptographer.ai.Matrix;
import com.scriptographer.ai.ParagraphStyle;
import com.scriptographer.ai.Path;
import com.scriptographer.ai.PathItem;
import com.scriptographer.ai.PathStyle;
import com.scriptographer.ai.PathText;
import com.scriptographer.ai.Pathfinder;
import com.scriptographer.ai.Pattern;
import com.scriptographer.ai.PatternColor;
import com.scriptographer.ai.PlacedFile;
import com.scriptographer.ai.PlacedSymbol;
import com.scriptographer.ai.PointText;
import com.scriptographer.ai.RGBColor;
import com.scriptographer.ai.Raster;
import com.scriptographer.ai.Segment;
import com.scriptographer.ai.StrokeStyle;
import com.scriptographer.ai.Swatch;
import com.scriptographer.ai.Symbol;
import com.scriptographer.ai.TextRange;
import com.scriptographer.ai.TextStory;
import com.scriptographer.ai.Timer;
import com.scriptographer.ai.Tool;
import com.scriptographer.ai.ToolHandler;
import com.scriptographer.ai.Tracing;
import com.scriptographer.sg.Illustrator;
import com.scriptographer.sg.Scriptographer;
import com.scriptographer.ui.Dialog;
import com.scriptographer.ui.Key;
import com.scriptographer.ui.MenuGroup;
import com.scriptographer.ui.MenuItem;
import com.scriptographer.ui.Palette;
/**
 * @author lehni
 */
public class TopLevel extends com.scratchdisk.script.rhino.TopLevel {

	final static Class classes[] = {
		// AI, alphabetically
		Annotator.class,
		AreaText.class,
		Artboard.class,
		CharacterStyle.class,
		Color.class,
		CMYKColor.class,
		DialogColor.class,
		CompoundPath.class,
		Curve.class,
		Dictionary.class,
		Document.class,
		DocumentView.class,
		FileFormat.class,
		FillStyle.class,
		FontFamily.class,
		FontWeight.class,
		Gradient.class,
		GradientColor.class,
		GradientStop.class,
		GrayColor.class,
		Group.class,
		HitResult.class,
		com.scriptographer.ai.Item.class,
		Layer.class,
		Line.class,
		LiveEffect.class,
		LiveEffectParameters.class,
		Matrix.class,
		ParagraphStyle.class,
		Path.class,
		Pathfinder.class,
		PathItem.class,
		PathStyle.class,
		PathText.class,
		Pattern.class,
		PatternColor.class,
		PlacedFile.class,
		com.scriptographer.ai.Point.class,
		PointText.class,
		Raster.class,
		com.scriptographer.ai.Rectangle.class,
		RGBColor.class,
		Segment.class,
		com.scriptographer.ai.Size.class,
		StrokeStyle.class,
		Swatch.class,
		Symbol.class,
		PlacedSymbol.class,
		com.scriptographer.ai.TextItem.class,
		TextRange.class,
		TextStory.class,
		Tool.class,
		ToolHandler.class,
		Tracing.class,
	
		// UI, alphabetically
		Dialog.class,
		Key.class,
		MenuGroup.class,
		MenuItem.class,
		Palette.class,
		
		// ADM, alphabetically
		Border.class,
		Button.class,
		ChasingArrows.class,
		Dial.class,
		CheckBox.class,
		DialogGroupInfo.class,
		Drawer.class,
		FloatingDialog.class,
		FontInfo.class,
		Frame.class,
		HierarchyListBox.class,
		HierarchyListEntry.class,
		Image.class,
		ImageButton.class,
		ImageCheckBox.class,
		ImageRadioButton.class,
		ImagePane.class,
		ItemGroup.class,
		ListBox.class,
		ListEntry.class,
		ListItem.class,
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
		TextPane.class,
		TextEdit.class,
		TextValueItem.class,
		Timer.class,
		ToggleItem.class,
		Tracker.class
	};

	public TopLevel(Context context) {
		super(context);

		// define classes. the createPrototypes flag is set so
		// the classes' constructors can now whether an object
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
		String[] names = { "include", "execute", "mapJavaClass" };
		defineFunctionProperties(names, TopLevel.class,
			ScriptableObject.READONLY | ScriptableObject.DONTENUM);

		// Properties:

		// Define the global reference here, for scripts that get executed
		// directly in the TopLevel scope (libraries)
		// This is overridden by RhinoEngine#createScope for all other scopes.
		defineProperty("global", this,
				ScriptableObject.READONLY | ScriptableObject.DONTENUM);

		defineProperty("documents", DocumentList.getInstance(),
				ScriptableObject.READONLY | ScriptableObject.DONTENUM);

		defineProperty("fonts", FontList.getInstance(),
				ScriptableObject.READONLY | ScriptableObject.DONTENUM);

		try {
			defineProperty(this, "document", "getActiveDocument", null);
			// Expose deprecated activeDocument
			defineProperty(this, "activeDocument", "getActiveDocument", null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		defineProperty("scriptographer", Scriptographer.getInstance(),
				ScriptableObject.READONLY | ScriptableObject.DONTENUM);

		defineProperty("illustrator", Illustrator.getInstance(),
				ScriptableObject.READONLY | ScriptableObject.DONTENUM);

		// deprecated:
		defineProperty("app", Illustrator.getInstance(),
				ScriptableObject.READONLY | ScriptableObject.DONTENUM);
	}

	/**
	 * Determines the directory of a script by reading it's scriptFile property
	 * in the main scope.
	 * 
	 * @param scope
	 */
	protected static File getDirectory(Scriptable scope) {
		Object obj = scope.get("script", scope);
		if (obj instanceof Wrapper)
			obj = ((Wrapper) obj).unwrap();
		if (obj instanceof com.scriptographer.sg.Script)
			return ((com.scriptographer.sg.Script) obj).getFile().getParentFile();
		return null;
	}

	/**
	 * @param script
	 * @param scope
	 * @throws ScriptException 
	 */
	private static void executeScript(Script script, Scope scope)
			throws ScriptException {
		if (script != null) {
			// Temporarily override script with the new one, so includes in
			// other directories work. Also pass it as the parent script to
			// the newly created script, so it can inherit coordinate system
			// / angle unit settings.
			com.scriptographer.sg.Script parent =
					(com.scriptographer.sg.Script) scope.get("script");
			try {
				scope.put("script", new com.scriptographer.sg.Script(
						script.getFile(), parent), true);
				script.execute(scope);
			} finally {
				scope.put("script", parent, true);
			}
		}
	}

	/*
	 * JavaScript functions
	 */
	protected static Object getActiveDocument(Scriptable obj) {
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
			executeScript(engine.compile(file), engine.getScope(thisObj));
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
			executeScript(engine.compile(file), engine.createScope());
		}
	}

	/**
	 * Maps a Java class to a JavaScript prototype, so this can be used
	 * instead for wrapping of returned java types. So far this is only
	 * used for java.io.File in Scriptographer.
	 */
	public static void mapJavaClass(Context cx, Scriptable thisObj,
			Object[] args, Function funObj) {
		if (args.length == 2) {
			for (int i = 0; i < args.length; i++)
				args[i] = Context.jsToJava(args[i], Object.class);
			if (args[0] instanceof Class && args[1] instanceof Function) {
				Class cls = (Class) args[0];
				Function proto = (Function) args[1];
				RhinoWrapFactory factory =
						(RhinoWrapFactory) cx.getWrapFactory();
				factory.mapJavaClass(cls, proto);
			}
		}
	}
}
