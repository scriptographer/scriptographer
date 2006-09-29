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
 * $Revision: 1.20 $
 * $Date: 2006/09/29 22:35:26 $
 */

package com.scriptographer.ai;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.Map;

import org.mozilla.javascript.NativeObject;

import com.scriptographer.js.FunctionHelper;
import com.scriptographer.util.ExtendedList;
import com.scriptographer.util.ReadOnlyList;
import com.scriptographer.util.SoftIntMap;

public class Document extends DictionaryObject {

	// TODO: move this to app.DIALOG_* and have a global function set / getDialogStatus,
	// that controls the general handling of dialogs on a global setting level.
	// remove the parameter from  the constructors.
	
	// ActionDialogStatus
	public static final int
		DIALOG_NONE = 0,
		DIALOG_ON = 1,
		DIALOG_PARTIAL_ON = 2,
		DIALOG_OFF = 3;

	protected LayerList layers = null;
	protected ViewList views = null;

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

	protected Document(int handle) {
		super(handle);
	}

	private static native int nativeCreate(File file, int colorModel, int dialogStatus);
	
	private static native int nativeCreate(String title, float width, float height, int colorModel, int dialogStatus);
	
	// use a SoftIntMap to keep track of already wrapped documents:
	private static SoftIntMap documents = new SoftIntMap();
	
	protected static Document wrapHandle(int handle) {
		if (handle == 0)
			return null;
		Document doc = (Document) documents.get(handle);
		if (doc == null) {
			doc = new Document(handle);
			documents.put(handle, doc);
		}
		return doc;
	}
	
	public LayerList getLayers() {
		if (layers == null)
			layers = new LayerList(this);
		return layers;
	}
	
	public Layer getActiveLayer() {
		return getLayers().getActiveLayer();
	}
	
	public ViewList getViews() {
		if (views == null)
			views = new ViewList(this);
		return views;
	}
	
	public View getActiveView() {
		return getViews().getActiveView();
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
	
	/**
	 * @param dialogStatus <tt>Document.DIALOG_*</tt>
	 */
	public native void print(int dialogStatus);
	
	public native void save();
	
	public native void close();
	
	public native void redraw();
	
	public native void copy();
	
	public native void cut();
	
	public native void paste();

	/**
	 * Invalidates the rectangle in artwork coordinates. This will cause all views of the
	 * document that contain the given rectangle to update at the next opportunity.
	 */
	public native void redraw(float x, float y, float width, float height);
	
	public void redraw(Rectangle2D rect) {
		redraw((float) rect.getX(), (float) rect.getY(), (float) rect.getWidth(), (float) rect.getHeight());
	}
	
	public native boolean write(File file, String format, boolean ask);

	public boolean write(File file, String format) {
		return write(file, format, false);
	}

	public boolean write(File file) {
		return write(file, null, false);
	}
	
	public native boolean hasSelectedItems();

	public native ArtSet getSelectedItems();
	
	public native void deselectAll();
	
	public native ArtSet getMatchingItems(Class type, Map attributes);

	public ArtSet getMatchingItems(Class type, NativeObject attributes) {
		return getMatchingItems(type, FunctionHelper.convertToMap(attributes));
	}

	public ArtSet getPathItems() {
		return getMatchingItems(Path.class, (Map) null);
	}

	public ArtSet getCompoundPathItems() {
		return getMatchingItems(CompoundPath.class, (Map) null);
	}

	public ArtSet getGroupItems() {
		return getMatchingItems(Group.class, (Map) null);
	}

	public ArtSet getTextItems() {
		return getMatchingItems(TextFrame.class, (Map) null);
	}

	public ArtSet getRasterItems() {
		return getMatchingItems(Raster.class, (Map) null);
	}

	public native Path createRectangle(Rectangle rect);

	public native Path createRoundRectangle(Rectangle rect, float hor, float ver);
	
	public native Path createOval(Rectangle rect, boolean circumscribed);
	
	public native Path createRegularPolygon(int numSides, Point center, float radius);
	
	public native Path createStar(int numPoints, Point center, float radius1, float radius2);
	
	public native Path createSpiral(Point firstArcCenter, Point start, float decayPercent, int numQuarterTurns, boolean clockwiseFromOutside);

	public Path createOval(Rectangle rect) {
		return createOval(rect, false);
	}
	
	public Path createPath() {
		return new Path(this);
	}
	
	public Path createPath(ExtendedList segments) {
		return new Path(this, segments);
	}
	
	public Path createPath(Object[] segments) {
		return new Path(this, segments);
	}
	
	public Raster createRaster(int type, int width, int height) {
		return new Raster(this, type, width, height);
	}
	
	public Raster createRaster(int type) {
		return new Raster(this, type);
	}
	
	public Raster createRaster() {
		return new Raster(this);
	}
	
	public CompoundPath createCompoundPath() {
		return new CompoundPath(this);
	}
	
	public CompoundPath createCompoundPath(ExtendedList children) {
		return new CompoundPath(this, children);
	}
	
	public CompoundPath createCompoundPath(Art[] children) {
		return new CompoundPath(this, children);
	}
	
	public CompoundPath createCompoundPath(Shape shape) {
		return new CompoundPath(this, shape);
	}
	
	public Group createGroup() {
		return new Group(this);
	}
	
	public Group createGroup(ExtendedList children) {
		return new Group(this, children);
	}
	
	public Group createGroup(Art[] children) {
		return new Group(this, children);
	}
	
	public AreaText createAreaText(Path area, int orient) {
		return new AreaText(this, area, orient);
	}

	public AreaText createAreaText(Path area) {
		return new AreaText(this, area);
	}
	
	public PointText createPointText(Point2D point, int orient) {
		return new PointText(this, point, orient);
	}

	public PointText createPointText(Point2D point) {
		return new PointText(this, point);
	}
	
	public PathText createPathText(Path path, int orient) {
		return new PathText(this, path, orient);
	}

	public PathText createPathText(Path path) {
		return new PathText(this, path);
	}
	
	public Layer createLayer() {
		return new Layer(this);
	}
	
	protected native HitTest nativeHitTest(Point point, int type, float tolerance, Art art); 

	
	/**
	 * @param point
	 * @param type HitTest.TEST_*
	 * @param tolerance specified in view coordinates (i.e pixels at the current
		zoom factor). The default value is 2. The algorithm is not guaranteed to produce
		correct results for large values.
	 * @return
	 */
	public HitTest hitTest(Point point, int type, float tolerance) {
		return this.nativeHitTest(point, type, tolerance, null);
	}

	public HitTest hitTest(Point point, int type) {
		return this.hitTest(point, type, HitTest.DEFAULT_TOLERANCE);
	}

	public HitTest hitTest(Point point) {
		return this.hitTest(point, HitTest.TEST_ALL, HitTest.DEFAULT_TOLERANCE);
	}
	
	private native int nativeGetStories();
	
	public static native void suspendTextReflow();
	
	public static native void resumeTextReflow();
	
	/**
	 * reflow is suspended during script execution.
	 * when reflow() is called, it's quickly turned on and off again
	 * immediatelly afterwards.
	 */
	public void reflowText() {
		// TODO: test if this does the trick? does resumeTextReflow immediatelly reflow the text?
		resumeTextReflow();
		suspendTextReflow();
	}
	
	private TextStoryList stories = null;
	
	public ReadOnlyList getStories() {
		if (stories == null) {
			int handle = nativeGetStories();
			if (handle != 0)
				stories = new TextStoryList(handle);
		}
		return stories;
	}
	
	protected int getVersion() {
		// TODO: getVersion is used for keeping Dictionaries up to date.
		// But right now document is not version aware. This means that once
		// the Dictionary is created, it will ignore changes to the
		// document's dictionary from other parts of illustrator.
		// this should be changed!
		return 0;
	}

	protected native void nativeGetDictionary(Dictionary dictionary);

	protected native void nativeSetDictionary(Dictionary dictionary);
}
