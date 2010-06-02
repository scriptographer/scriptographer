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

package com.scriptographer.adm;

import java.io.File;

import com.scriptographer.ai.Color;
import com.scriptographer.ui.Component;
import com.scriptographer.ui.UiFactory;

/**
 * @author lehni
 * 
 * @jshide
 */
public class AdmUiFactory extends UiFactory {

	public void alert(String title, String message) {
		Dialog.alert(title, message);
	}

	public Color chooseColor(Color color) {
		return Dialog.chooseColor(color);
	}

	public File chooseDirectory(String message, File selectedDir) {
		return Dialog.chooseDirectory(message, selectedDir);
	}

	public boolean confirm(String title, String message) {
		return Dialog.confirm(title, message);
	}

	public File fileOpen(String message, String[] filters, File selectedFile) {
		return Dialog.fileOpen(message, filters, selectedFile);
	}

	public File fileSave(String message, String[] filters, File selectedFile) {
		return Dialog.fileSave(message, filters, selectedFile);
	}

	public Object[] prompt(String title, Component[] components) {
		return null;
		// return Dialog.prompt(title, components);
	}
}
