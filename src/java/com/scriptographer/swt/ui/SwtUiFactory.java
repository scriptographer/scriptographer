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
 * File created on Oct 18, 2013.
 */

package com.scriptographer.swt.ui;

import java.io.File;
//import java.io.IOException;
//import java.util.HashMap;

import com.scriptographer.ai.Color;
import com.scriptographer.ui.Component;
import com.scriptographer.ui.Palette;
import com.scriptographer.ui.PaletteProxy;
import com.scriptographer.ui.UiFactory;

/**
 * @author Olga
 *
 */
public class SwtUiFactory extends UiFactory {

	/* (non-Javadoc)
	 * @see com.scriptographer.ui.UiFactory#alert(java.lang.String, java.lang.String)
	 */
	@Override
	public void alert(String title, String message) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.scriptographer.ui.UiFactory#chooseColor(com.scriptographer.ai.Color)
	 */
	@Override
	public Color chooseColor(Color color) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.scriptographer.ui.UiFactory#chooseDirectory(java.lang.String, java.io.File)
	 */
	@Override
	public File chooseDirectory(String message, File selectedDir) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.scriptographer.ui.UiFactory#confirm(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean confirm(String title, String message) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.scriptographer.ui.UiFactory#fileOpen(java.lang.String, java.lang.String[], java.io.File)
	 */
	@Override
	public File fileOpen(String message, String[] filters, File selectedFile) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.scriptographer.ui.UiFactory#fileSave(java.lang.String, java.lang.String[], java.io.File)
	 */
	@Override
	public File fileSave(String message, String[] filters, File selectedFile) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.scriptographer.ui.UiFactory#prompt(java.lang.String, com.scriptographer.ui.Component[])
	 */
	@Override
	public Object[] prompt(String title, Component[] components) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.scriptographer.ui.UiFactory#createPalette(com.scriptographer.ui.Palette, com.scriptographer.ui.Component[])
	 */
	@Override
	public PaletteProxy createPalette(Palette palette, Component[] components) {
		// TODO Auto-generated method stub
		return null;
	}

}
