/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2008 Juerg Lehni, http://www.scratchdisk.com.
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
 * $Id$
 */

package com.scriptographer.ai;

import java.awt.Shape;
import java.io.File;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import com.scratchdisk.list.ExtendedList;
import com.scratchdisk.util.ConversionUtils;
import com.scratchdisk.util.SoftIntMap;
import com.scriptographer.ScriptographerException;
import com.scriptographer.script.EnumUtils;

/**
 * @author lehni
 */
public class Document extends DictionaryObject {

	protected LayerList layers = null;
	protected DocumentViewList views = null;
	protected SymbolList symbols = null;
	protected SwatchList swatches = null;
	protected GradientList gradients = null;

	/**
	 * Opens an existing document.
	 * 
	 * @param file the file to read from
	 * @param colorModel the document's desired color model
	 * @param dialogStatus how dialogs should be handled
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
	 * @param colorModel the document's desired color model
	 * @param dialogStatus how dialogs should be handled
	 */
	public Document(String title, float width, float height, ColorModel colorModel,
			DialogStatus dialogStatus) {
		super(nativeCreate(title, width, height, colorModel.value,
				(dialogStatus != null ? dialogStatus : DialogStatus.NONE).value));
	}

	public Document(String title, float width, float height) {
		this(title, width, height, ColorModel.CMYK, DialogStatus.NONE);
	}

	protected Document(int handle) {
		super(handle);
	}

	private static native int nativeCreate(File file, int colorModel,
			int dialogStatus);

	private static native int nativeCreate(String title, float width,
			float height, int colorModel, int dialogStatus);
	
	// use a SoftIntMap to keep track of already wrapped documents:
	private static SoftIntMap<Document> documents = new SoftIntMap<Document>();
	
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
	
	private static native int getActiveDocumentHandle();
	
	/**
	 * @jshide
	 */
	public static Document getActiveDocument() {
		return Document.wrapHandle(getActiveDocumentHandle());
	}

	/**
	 * Called before ai functions are executed
	 * @jshide
	 */
	public static native void beginExecution();
	
	/**
	 * Called after ai functions are executed
	 * @jshide
	 */
	public static native void endExecution();

	/**
	 * Activates this document, so all newly created items will be placed
	 * in it.
	 * 
	 * @param focus When set to true, the document window is brought to the
	 *        front, otherwise the window sequence remains the same.
	 * @param forCreation if set to true, the internal pointer gWorkingDoc will
	 *        not be modified, but gCreationDoc will be set, which then is only
	 *        used once in the next call to Document_activate() (native stuff).
	 */
	private native void activate(boolean focus, boolean forCreation);

	/**
	 * Activates this document, so all newly created items will be placed
	 * in it.
	 * 
	 * @param focus When set to <code>true</code>, the document window is
	 *        brought to the front, otherwise the window sequence remains the
	 *        same.
	 */
	public void activate(boolean focus) {
		activate(focus, false);
	}
	
	/**
	 * Activates this document and brings its window to the front
	 */
	public void activate() {
		activate(true, false);
	}
	
	public LayerList getLayers() {
		if (layers == null)
			layers = new LayerList(this);
		return layers;
	}

	public native Layer getActiveLayer();
	
	public DocumentViewList getViews() {
		if (views == null)
			views = new DocumentViewList(this);
		return views;
	}
	
	// getActiveView can not be native as there is no wrapViewHandle defined
	// nativeGetActiveView returns the handle, that still needs to be wrapped
	// here. as this is only used once, that's the prefered way (just like
	// DocumentList.getActiveDocument
	
	private native int getActiveViewHandle(); 

	public DocumentView getActiveView() {
		return DocumentView.wrapHandle(getActiveViewHandle());
	}
	
	public SymbolList getSymbols() {
		if (symbols == null)
			symbols = new SymbolList(this);
		return symbols;
	}
	
	private native int getActiveSymbolHandle(); 

	public Symbol getActiveSymbol() {
		return (Symbol) Symbol.wrapHandle(getActiveSymbolHandle(), this);
	}

	public SwatchList getSwatches() {
		if (swatches == null)
			swatches = new SwatchList(this);
		return swatches;
	}

	public GradientList getGradients() {
		if (gradients == null)
			gradients = new GradientList(this);
		return gradients;
	}

	// TODO: getActiveSwatch, getActiveGradient
	
	public native Point getPageOrigin();
	
	public native void setPageOrigin(Point pt);

	public native Point getRulerOrigin();
	
	public native void setRulerOrigin(Point pt);

	public native Size getSize();

	/**
	 * setSize only works while reading a document!
	 * 
	 * @param width
	 * @param height
	 */
	public native void setSize(float width, float height);
	
	public void setSize(Size size) {
		setSize(size.width, size.height);
	}

	public native Rectangle getCropBox();
	
	public native void setCropBox(Rectangle cropBox);

	public native boolean isModified();
	
	public native void setModified(boolean modified);

	public native File getFile();

	private native int nativeGetFileFormat();

	private native void nativeSetFileFormat(int handle);
	
	public FileFormat getFileFormat() {
		return FileFormat.getFormat(nativeGetFileFormat());
	}

	public void setFileFormat(FileFormat format) {
		nativeSetFileFormat(format != null ? format.handle : 0);
	}
	/**
	 * Prints the document
	 * 
	 * @param dialogStatus <tt>Document.DIALOG_*</tt>
	 */
	public native void print(int dialogStatus);
	
	/**
	 * Saves the document
	 */
	public native void save();
	
	/**
	 * Closes the document
	 */
	public native void close();
	
	/**
	 * Forces the document to be redrawn
	 */
	public native void redraw();
	
	/**
	 * Copies the selected items to the clipboard
	 */
	public native void copy();
	
	/**
	 * Cuts the selected items to the clipboard
	 */
	public native void cut();
	
	/**
	 * Pastes the selected items to the clipboard
	 */
	public native void paste();

	/**
	 * Places a file in the document
	 * 
	 * @param file the file to place
	 * @param linked when set to <code>true</code>, the placed object is a
	 *        link to the file, otherwise it is embedded within the document
	 */
	public native Item place(File file, boolean linked);
	
	public Item place(File file) {
		return place(file, true);
	}

	/**
	 * Invalidates the rectangle in artwork coordinates. This will cause all
	 * views of the document that contain the given rectangle to update at the
	 * next opportunity.
	 */
	public native void invalidate(float x, float y, float width, float height);
	
	public void invalidate(Rectangle rect) {
		invalidate(rect.x, rect.y, rect.width, rect.height);
	}

	private native boolean nativeWrite(File file, int formatHandle, boolean ask);
	
	public boolean write(File file, FileFormat format, boolean ask) {
		if (format == null)
			format = this.getFileFormat();
		return nativeWrite(file, format != null ? format.handle : 0, ask);
	}

	public boolean write(File file, FileFormat format) {
		return write(file, format, false);
	}

	public boolean write(File file) {
		return write(file, null, false);
	}

	/**
	 * Checks whether the document contains any selected items.
	 * 
	 * @return <code>true</code> if the document contains selected items,
	 *         false otherwise.
	 */	
	public native boolean hasSelectedItems();

	public native ItemSet getSelectedItems();
	
	/**
	 * Deselects all the selected items in the document.
	 */
	public native void deselectAll();
	
	private native ItemSet nativeGetMatchingItems(Class type, HashMap<Integer, Boolean> attributes);

	/**
	 * Returns all items of a given class that match a set of attributes, as specified by the
	 * passed map. For each of the keys in the map, the demanded value can either be true or false.
	 * 
	 * @param type
	 * @param attributes
	 * @return
	 * @jshide
	 */
	@SuppressWarnings("unchecked")
	public ItemSet getMatchingItems(Class type, EnumMap<ItemAttribute, Boolean> attributes) {
		return getMatchingItems(type, (Map) attributes);
	}

	/**
	 * Returns all items of a given class that match a set of attributes, as specified by the
	 * passed map. For each of the keys in the map, the demanded value can either be true or false.
	 * 
	 * @param type
	 * @param attributes
	 * @return
	 */
	public ItemSet getMatchingItems(Class type, Map<Object, Object> attributes) {
		// Convert the attributes list to a new HashMap containing only
		// integer -> boolean pairs.
		HashMap<Integer, Boolean> converted = new HashMap<Integer, Boolean>();
		for (Map.Entry entry : attributes.entrySet()) {
			Object key = entry.getKey();
			if (!(key instanceof ItemAttribute)) {
				key = EnumUtils.get(ItemAttribute.class, key.toString());
				if (key == null)
					throw new ScriptographerException("Undefined attribute: " + key);
			}
			converted.put(((ItemAttribute) key).value,
					ConversionUtils.toBoolean(entry.getValue()));
		}
		return nativeGetMatchingItems(type, converted);
	}

	@SuppressWarnings("unchecked")
	public ItemSet getMatchingItems(Class type) {
		return getMatchingItems(type, (Map) null);
	}
	
	public ItemSet getPaths() throws ScriptographerException {
		return getMatchingItems(Path.class);
	}

	public ItemSet getCompoundPaths() throws ScriptographerException {
		return getMatchingItems(CompoundPath.class);
	}

	public ItemSet getGroups() throws ScriptographerException {
		return getMatchingItems(Group.class);
	}

	public ItemSet getTextFrames() throws ScriptographerException {
		return getMatchingItems(TextFrame.class);
	}

	public ItemSet getRasters() throws ScriptographerException {
		return getMatchingItems(Raster.class);
	}
	
	/* TODO: make these
	public Item getInsertionItem();
	public int getInsertionOrder();
	public boolean isInsertionEditable();
	*/

	public Path createLine(Point pt1, Point pt2) {
		Path path = this.createPath();
		path.moveTo(pt1);
		path.lineTo(pt2);
		return path;
	}

	/**
	 * Creates a rectangular path
	 * 
	 * @param rect
	 * @return the newly created path
	 */
	public native Path createRectangle(Rectangle rect);

	/**
	 * Creates a rectangular path with rounded corners
	 * 
	 * @param rect
	 * @param hor the horizontal size of the rounded corners
	 * @param ver the vertical size of the rounded corners
	 * @return the newly created path
	 */
	public native Path createRoundRectangle(Rectangle rect, float hor, float ver);

	/**
	 * Creates an oval shaped path
	 * 
	 * @param rect
	 * @param circumscribed if this is set to true the oval shaped path will be
	 *        created so the rectangle fits into it. If it's set to false the
	 *        oval path will fit within the rectangle.
	 * @return the newly created path
	 */
	public native Path createOval(Rectangle rect, boolean circumscribed);
	
	/**
	 * Creates a regular polygon shaped path
	 * 
	 * @param numSides the number of sides of the polygon
	 * @param center
	 * @param radius
	 * @return the newly created path
	 */
	public native Path createRegularPolygon(int numSides, Point center,
			float radius);

	/**
	 * Created a star shaped path
	 * 
	 * The largest of <code>radius1</code> and <code>radius2</code> will be
	 * the outer radius of the star. The smallest of radius1 and radius2 will be
	 * the inner radius.
	 * 
	 * @param numPoints the number of points of the star
	 * @param center
	 * @param radius1
	 * @param radius2
	 * @return the newly created path
	 */
	public native Path createStar(int numPoints, Point center, float radius1,
			float radius2);

	/**
	 * Creates a spiral shaped path
	 * 
	 * @param firstArcCenter the center point of the first arc
	 * @param start the starting point of the spiral
	 * @param decayPercent the percentage by which each succeeding arc will be
	 *        scaled
	 * @param numQuarterTurns the number of quarter turns (arcs)
	 * @param clockwiseFromOutside if this is set to <code>true</code> the
	 *        spiral will spiral in a clockwise direction from the first point.
	 *        If it's set to <code>false</code> it will spiral in a counter
	 *        clockwise direction
	 * @return the newly created path
	 */
	public native Path createSpiral(Point firstArcCenter, Point start,
			float decayPercent, int numQuarterTurns,
			boolean clockwiseFromOutside);

	/**
	 * Creates an oval shaped path
	 * @param rect
	 * @return the newly created path
	 */
	public Path createOval(Rectangle rect) {
		return createOval(rect, false);
	}

	public Path createCircle(float x, float y, float radius) {
		return createCircle(new Point(x, y), radius);
	}

	public Path createCircle(Point center, float radius) {
		return createOval(new Rectangle(
				center.subtract(radius, radius),
				center.add(radius, radius)));
	}

	/**
	 * Creates a new path
	 * @return the newly created path
	 */
	public Path createPath() {
		activate(false, true);
		return new Path();
	}
	
	public Path createPath(ExtendedList segments) {
		activate(false, true);
		return new Path(segments);
	}
	
	public Path createPath(Object[] segments) {
		activate(false, true);
		return new Path(segments);
	}
	
	public Raster createRaster(ColorType type, int width, int height) {
		activate(false, true);
		return new Raster(type, width, height);
	}
	
	public Raster createRaster(int type) {
		activate(false, true);
		return new Raster(type);
	}
	
	public Raster createRaster() {
		activate(false, true);
		return new Raster();
	}
	
	public CompoundPath createCompoundPath() {
		activate(false, true);
		return new CompoundPath();
	}
	
	public CompoundPath createCompoundPath(ExtendedList children) {
		activate(false, true);
		return new CompoundPath(children);
	}
	
	public CompoundPath createCompoundPath(Item[] children) {
		activate(false, true);
		return new CompoundPath(children);
	}
	
	public CompoundPath createCompoundPath(Shape shape) {
		activate(false, true);
		return new CompoundPath(shape);
	}
	
	public Group createGroup() {
		activate(false, true);
		return new Group();
	}
	
	public Group createGroup(ExtendedList children) {
		activate(false, true);
		return new Group(children);
	}
	
	public Group createGroup(Item[] children) {
		activate(false, true);
		return new Group(children);
	}
	
	public AreaText createAreaText(Path area, TextOrientation orientation) {
		activate(false, true);
		return new AreaText(area, orientation);
	}

	public AreaText createAreaText(Path area) {
		activate(false, true);
		return new AreaText(area);
	}
	
	public PointText createPointText(Point point, TextOrientation orientation) {
		activate(false, true);
		return new PointText(point, orientation);
	}

	public PointText createPointText(Point point) {
		activate(false, true);
		return new PointText(point);
	}
	
	public PathText createPathText(Path path, TextOrientation orientation) {
		activate(false, true);
		return new PathText(path, orientation);
	}

	public PathText createPathText(Path path) {
		activate(false, true);
		return new PathText(path);
	}
	
	public Layer createLayer() {
		activate(false, true);
		return new Layer();
	}
	
	protected native HitTest nativeHitTest(Point point, int request,
			float tolerance, Item item); 

	
	/**
	 * @param point
	 * @param request
	 * @param tolerance specified in view coordinates (i.e pixels at the current
	 *        zoom factor). The default value is 2. The algorithm is not
	 *        guaranteed to produce correct results for large values.
	 * @return
	 */
	public HitTest hitTest(Point point, HitRequest request, float tolerance) {
		return this.nativeHitTest(point, (request != null ? request : HitRequest.ALL).value, tolerance, null);
	}

	public HitTest hitTest(Point point, HitRequest request) {
		return this.hitTest(point, request, HitTest.DEFAULT_TOLERANCE);
	}

	public HitTest hitTest(Point point) {
		return this.hitTest(point, HitRequest.ALL, HitTest.DEFAULT_TOLERANCE);
	}
	
	private native int nativeGetStories();
	
	/**
	 * Text reflow is suspended during script execution. when reflowText() is
	 * called, the reflow of text is forced.
	 */
	public native void reflowText();

	private TextStoryList stories = null;
	
	public TextStoryList getStories() {
		if (stories == null) {
			int handle = nativeGetStories();
			if (handle != 0)
				stories = new TextStoryList(handle, this);
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
