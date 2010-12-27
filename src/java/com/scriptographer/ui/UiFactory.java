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
 * File created on Jun 2, 2010.
 */

package com.scriptographer.ui;

import java.io.File;

import com.scriptographer.ScriptographerEngine;
import com.scriptographer.adm.ui.AdmUiFactory;
import com.scriptographer.ai.Color;
import com.scriptographer.ui.Component;

/**
 * @author lehni
 * 
 * @jshide
 */
public abstract class UiFactory {
	private static UiFactory factory;

	public static UiFactory getInstance() {
		if (factory == null) {
			factory = ScriptographerEngine.getIllustratorVersion() < 16
					? new AdmUiFactory()
					// TODO: Implement SwtUiFactory
					// new SwtUiFactory();
					: null;
		}
		return factory;
	}

	public abstract void alert(String title, String message);

	public abstract Color chooseColor(Color color);

	public abstract File chooseDirectory(String message, File selectedDir);

	public abstract boolean confirm(String title, String message);

	public abstract File fileOpen(String message, String[] filters,
			File selectedFile);

	public abstract File fileSave(String message, String[] filters,
			File selectedFile);

	public abstract Object[] prompt(String title, Component[] components);

	public abstract PaletteProxy createPalette(Palette palette,
			Component[] components);
}
