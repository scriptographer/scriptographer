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
 * File created on 23.01.2005.
 *
 * $RCSfile: Document.java,v $
 * $Author: lehni $
 * $Revision: 1.4 $
 * $Date: 2005/03/25 17:09:15 $
 */

package com.scriptographer.ai;

import java.awt.geom.Point2D;
import java.io.File;
import java.util.Map;

import org.mozilla.javascript.NativeObject;

import com.scriptographer.js.FunctionHelper;

public class Document extends AIObject {

	// TODO: move this to app.DIALOG_* and have a global function set / getDialogStatus,
	// that controls the general handling of dialogs on a global setting level.
	// remove the parameter from  the constructors.
	
	// ActionDialogStatus
	public static final int
		DIALOG_NONE = 0,
		DIALOG_ON = 1,
		DIALOG_PARTIAL_ON = 2,
		DIALOG_OFF = 3;

	protected int documentHandle = 0;

	protected LayerList layers = null;

	/**
	 * Opens an existing document.
	 *
	 * @param file the file to read from
	 * @param colorModel the document's desired color model, Color.MODEL_* values
	 * @param dialogStatus how dialogs should be handled, Document.DIALOG_* values
	 */
	public Document(File file, int colorModel, int dialogStatus) {
		super(nativeCreate(file, colorModel, dialogStatus));
	}

	/**
	 * Creates a new document.
	 *
	 * @param title the title of the document
	 * @param width the width of the document
	 * @param height the height of the document
	 * @param colorModel the document's desired color model, Color.MODEL_* values
	 * @param dialogStatus how dialogs should be handled, Document.DIALOG_* values
	 */
	public Document(String title, float width, float height, int colorModel, int dialogStatus) {
		super(nativeCreate(title, width, height, colorModel, dialogStatus));
	}

	private static native int nativeCreate(File file, int colorModel, int dialogStatus);
	
	private static native int nativeCreate(String title, float width, float height, int colorModel, int dialogStatus);

	protected Document(int handle) {
		super(handle);
	}
	
	public LayerList getLayers() {
		if (layers == null)
			layers = new LayerList(this);
		return layers;
	}

	public native Point getPageOrigin();
	
	public native void setPageOrigin(Point pt);

	public native Point getRulerOrigin();
	
	public native void setRulerOrigin(Point pt);

	public native Point getSize();

	/**
	 * SetSize only works while reading a document!
	 *
	 * @param width
	 * @param height
	 */
	public native void setSize(float width, float height);
	
	public void setSize(Point2D size) {
		setSize((float) size.getX(), (float) size.getY());
	}

	public native Rectangle getCropBox();
	
	public native void setCropBox(Rectangle cropBox);

	public native boolean isModified();
	
	public native void setModified(boolean modified);

	public native File getFile();

	private static String[] formats = null;
	
	private static native String[] nativeGetFormats();
	
	public static String[] getFileFormats() {
		if (formats == null)
			formats = nativeGetFormats();
		return (String[]) formats.clone();
	}

	public native void activate();
	
	public native void print(int dialogStatus);
	
	public native void save();
	
	public native void close();
	
	public native void redraw();
	
	public native void copy();
	
	public native void cut();
	
	public native void paste();
	
	public native boolean write(File file, String format, boolean ask);

	public boolean write(File file, String format) {
		return write(file, format, false);
	}

	public boolean write(File file) {
		return write(file, null, false);
	}

	public native ArtSet getSelectedArt();
	
	public native ArtSet getMatchingArt(Class type, Map attributes);

	public ArtSet getMatchingArt(Class type, NativeObject attributes) {
		return getMatchingArt(type, FunctionHelper.convertToMap(attributes));
	}
}
