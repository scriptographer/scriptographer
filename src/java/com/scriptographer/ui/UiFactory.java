/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2010 Juerg Lehni, http://www.scratchdisk.com.
 * All rights reserved.
 *
 * Please visit http://scriptographer.org/ for updates and contact.
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
			factory = ScriptographerEngine.getApplicationVersion() < 16
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
