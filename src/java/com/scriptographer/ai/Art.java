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
 * File created on 02.12.2004.
 * 
 * $Id$
 */

package com.scriptographer.ai;

import java.util.ArrayList;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import com.scriptographer.CommitManager;
import com.scriptographer.util.SoftIntMap;

/**
 * @author lehni
 */
public abstract class Art extends DictionaryObject {
	
	// the internal version. this is used for internally reflected data,
	// such as segmentList, pathStyle, and so on. Everytime an object gets
	// modified, ScriptographerEngine.selectionChanged() gets fired that
	// increases the version of all involved art objects.
	// update-commit related code needs to check against this variable
	protected int version = 0;
	
	// the reference to the dictionary that contains this Art object, if any
	protected int dictionaryRef = 0;
	
	// internal hash map that keeps track of already wrapped objects. defined
	// as soft.
	private static SoftIntMap artItems = new SoftIntMap();
	
	/* TODO: needed?
	// The same, but for the children of one object, and not weak,
	// so they're kept alive as long as the parent lives:
	private ArrayList childrenWrappers = new ArrayList();
	*/

	private PathStyle style = null;
	
	protected Document document = null;

	// from AIArt.h
	
	// AIArtType
	protected final static short
		// The special type kAnyArt is never returned as an art object type, but
		// is used as a parameter to the Matching Art suite function
		// GetMatchingArt.
		TYPE_ANY = -1,

		// The type kUnknownArt is reserved for objects that are not supported
		// in the plug-in interface. You should anticipate unknown art objects
		// and ignore them gracefully. For example graph objects return
		// kUnkownType.
		//
		// If a plug-in written for an earlier version of the plug-in API calls
		// GetArt- Type with an art object of a type unknown in its version,
		// this function will map the art type to either an appropriate type or
		// to kUnknownArt.
		TYPE_UNKNOWN = 0,
		TYPE_GROUP = 1,
		TYPE_PATH = 2,
		TYPE_COMPOUNDPATH = 3,

		// Pre-AI11 text art type. No longer supported but remains as a place
		// holder so that the segmentValues for other art types remain the same.
		TYPE_TEXT = 4,

		// Pre-AI11 text art type. No longer supported but remains as a place
		// holder so that the segmentValues for other art types remain the same.
		TYPE_TEXTPATH = 5,

		// Pre-AI11 text art type. No longer supported but remains as a place
		// holder so that the segmentValues for other art types remain the same.
		TYPE_TEXTRUN = 6,
		TYPE_PLACED = 7,

		// The special type kMysteryPathArt is never returned as an art object
		// type, it is an obsolete parameter to GetMatchingArt. It used to match
		// paths inside text objects without matching the text objects
		// themselves. In AI11 and later the kMatchTextPaths flag is used to
		// indicate that text paths should be returned.
		TYPE_MYSTERYPATH = 8,
		TYPE_RASTER = 9,
		TYPE_PLUGIN = 10,
		TYPE_MESH = 11,
		TYPE_TEXTFRAME = 12,
		TYPE_SYMBOL = 13,

		// A foreign object is a "black box" containing drawing commands.
		// Construct using AIForeignObjectSuite::New(... rather than
		// AIArtSuite::NewArt(.... See AIForeignObjectSuite.
		TYPE_FOREIGN = 14,

		// A text object read from a legacy file (AI10, AI9, AI8 ....
		TYPE_LEGACYTEXT = 15,

		// Lehni: self defined type for layer groups:
		TYPE_LAYER = 100,
		TYPE_TRACING = 101;

	// AIArtUserAttr:
	// used in Document.getMatchingArt:
	public final static Integer
		ATTR_SELECTED = new Integer(0x00000001),
		ATTR_LOCKED = new Integer(0x00000002),
		ATTR_HIDDEN = new Integer(0x00000004),
		ATTR_FULLY_SELECTED = new Integer(0x00000008),

		// Valid only for groups and plugin groups. Indicates whether the
		// contents of the object are expanded in the layers palette.
		ATTR_EXPANDED = new Integer(0x00000010),
		ATTR_TARGETED = new Integer(0x00000020),

		// Indicates that the object defines a clip mask. This can only be set on
		// paths), compound paths), and text frame objects. This property can only be
		// set on an object if the object is already contained within a clip group.
		ATTR_IS_CLIPMASK = new Integer(0x00001000),

		// Indicates that text is to wrap around the object. This property cannot be
		// set on an object that is part of compound group), it will return
		// kBadParameterErr. private final int ATTR_IsTextWrap has to be set to the
		// ancestor compound group in this case.
		ATTR_IS_TEXTWRAP = new Integer(0x00010000),

		// Meaningful only to GetMatchingArt passing to SetArtUserAttr will cause an error. Only one
		// of kArtSelectedTopLevelGroups), kArtSelectedLeaves or kArtSelectedTopLevelWithPaint can
		// be passed into GetMatchingArt), and they cannot be combined with anything else. When
		// passed to GetMatchingArt causes only fully selected top level objects to be returned
		// and not their children.
		ATTR_SELECTED_TOPLEVEL_GROUPS = new Integer(0x00000040),
		// Meaningful only to GetMatchingArt passing to SetArtUserAttr will cause an error. When passed
		// to GetMatchingArt causes only leaf selected objects to be returned and not their containers.
		// See also kArtSelectedTopLevelGroups
		ATTR_SELECTED_LAYERS = new Integer(0x00000080),
		// Meaningful only to GetMatchingArt passing to SetArtUserAttr will cause an error. When passed
		// to GetMatchingArt causes only top level selected objects that have a stroke or fill to be
		// returned. See also kArtSelectedTopLevelGroups
		ATTR_SELECTED_TOPLEVEL_WITH_PAINT = new Integer(0x00000100),	// Top level groups that have a stroke or fill), or leaves

		// Valid only for GetArtUserAttr and GetMatchingArt passing to
		// SetArtUserAttr will cause an error. true if the art object has a simple
		// style.
		ATTR_HAS_SIMPLE_STYLE = new Integer(0x00000200),

		// Valid only for GetArtUserAttr and GetMatchingArt passing to
		// SetArtUserAttr will cause an error. true if the art object has an active
		// style.
		ATTR_HAS_ACTIVE_STYLE = new Integer(0x00000400),

		// Valid only for GetArtUserAttr and GetMatchingArt passing to
		// SetArtUserAttr will cause an error. true if the art object is a part of a
		// compound path.
		ATTR_PART_OF_COMPOUND = new Integer(0x00000800),

		// On GetArtUserAttr), reports whether the object has an art style that is
		// pending re-execution. On SetArtUserAttr), marks the art style dirty
		// without making any other changes to the art or to the style.
		ATTR_STYLE_IS_DIRTY = new Integer(0x00040000);

	// AIBlendingModeValues:
	public final static int
		BLEND_NORMAL			= 0,
		BLEND_MULTIPLY			= 1,
		BLEND_SCREEN			= 2,
		BLEND_OVERLAY			= 3,
		BLEND_SOFTLIGHT			= 4,
		BLEND_HARDLIGHT			= 5,
		BLEND_COLORDODGE		= 6,
		BLEND_COLORBURN			= 7,
		BLEND_DARKEN			= 8,
		BLEND_LIGHTEN			= 9,
		BLEND_DIFFERENCE		= 10,
		BLEND_EXCLUSION			= 11,
		BLEND_HUE				= 12,
		BLEND_SATURATION		= 13,
		BLEND_COLOR				= 14,
		BLEND_LUMINOSITY		= 15,
		BLEND_NUMS				= 16;

	// AIKnockout:
	public final static int
		KNOCKOUT_UNKNOWN	= -1,
		KNOCKOUT_OFF		= 0,
		KNOCKOUT_ON			= 1,
		KNOCKOUT_INHERIT	= 2;

	public static final int 
		TRANSFORM_OBJECTS			= 1 << 0,
		TRANSFORM_FILL_GRADIENTS	= 1 << 1,
		TRANSFORM_FILL_PATTERNS		= 1 << 2,
		TRANSFORM_STROKE_PATTERNS	= 1 << 3,
		TRANSFORM_LINES				= 1 << 4,
		TRANSFORM_LINKED_MASKS		= 1 << 5,
		TRANSFORM_CHILDREN			= 1 << 6,
		TRANSFORM_SELECTION_ONLY	= 1 << 7,
		// self defined:
		TRANSFORM_DEEP				= 1 << 10;
	
	// AIArtOrder:
	public final static int
		ORDER_UNKNOWN = 0,
		ORDER_BEFORE = 1,
		ORDER_AFTER = 2,
		ORDER_INSIDE = 3,
		ORDER_ANCHESTOR = 4;
	
	// AIExpandFlagValue:
	public final static int
		EXPAND_PLUGINART	    = 0x0001,
		EXPAND_TEXT			    = 0x0002,
		EXPAND_STROKE		    = 0x0004,
		EXPAND_PATTERN		    = 0x0008,
		EXPAND_GRADIENTTOMESH   = 0x0010,
		EXPAND_GRADIENTTOPATHS	= 0x0020,
		EXPAND_SYMBOLINSTANCES	= 0x0040,
	
		EXPAND_ONEATATIME	    = 0x4000,
		EXPAND_SHOWPROGRESS	    = 0x8000,
		// By default objects that are locked such as those on a locked layer
		// cannot be expanded. Setting this flag allows them to be expanded.
		EXPAND_LOCKEDOBJECTS    = 0x10000;

	/**
	 * Creates an Art object that wraps an existing AIArtHandle. Make sure the
	 * right constructor is used (Path, Raster). Use wrapArtHandle instead of
	 * directly calling this constructor (it is called from the anchestor's
	 * constructors). Integer is used instead of int so Art(int handle) can be
	 * distinguised from the Art(Integer handle) constructor
	 * 
	 * @param handle
	 */
	protected Art(int handle) {
		super(handle);
		// keep track of this object from now on, see wrapArtHandle
		artItems.put(this.handle, this);
		/*
		// store the wrapper also in the paren'ts childrenWrappers segmentList,
		// so it becomes permanent as long the object itself exists.
		// see definitions of artItems and childrenWrappers.
		Art parent = getParent();
		if (parent != null)
			parent.childrenWrappers.add(this);
		*/
		// store reference to the active document
		this.document = Document.getActiveDocument();
	}

	private native static int nativeCreate(short type);

	/**
	 * Creates a new AIArtHandle of the specified type and wraps it in a Art
	 * object
	 * 
	 * @param type Art.TYPE_*
	 */
	protected Art(short type) {
		this(nativeCreate(type));
	}

	/**
	 * Wraps an AIArtHandle of given type (determined by
	 * sAIArt->GetType(artHandle)) by the correct Art anchestor class:
	 * 
	 * @param artHandle
	 * @param type
	 * @return the wrapped art object
	 */
	protected static Art wrapHandle(int artHandle, short type, int textType,
			int docHandle, int dictionaryRef, boolean wrapped) {
		// first see wether the object was already wrapped before:
		Art art = null;
		// only try to use the previous wrapper for this adress if the object
		// was marked wrapped otherwise we might get wrong wrappers for objects
		// that reuse a previous address
		if (wrapped)
			art = (Art) artItems.get(artHandle);
		// if it wasn't wrapped yet, do it now:
		// TODO: don't forget to add all types also to the native
		// Art_getType function in com_scriptographer_ai_Art.cpp!
		if (art == null) {
			switch (type) {
				case TYPE_PATH:
					art = new Path(artHandle);
					break;
				case TYPE_GROUP:
					art = new Group(artHandle);
					break;
				case TYPE_RASTER:
					art = new Raster(artHandle);
					break;
				case TYPE_PLACED:
					art = new PlacedItem(artHandle);
					break;
				case TYPE_LAYER:
					art = new Layer(artHandle);
					break;
				case TYPE_COMPOUNDPATH:
					art = new CompoundPath(artHandle);
					break;
				case TYPE_TEXTFRAME:
					switch (textType) {
						case TextFrame.TEXTTYPE_POINT:
							art = new PointText(artHandle);
							break;
						case TextFrame.TEXTTYPE_AREA:
							art = new AreaText(artHandle);
							break;
						case TextFrame.TEXTTYPE_PATH:
							art = new PathText(artHandle);
							break;
					}
					break;
				case TYPE_TRACING:
					art = new Tracing(artHandle);
					break;
				case TYPE_SYMBOL:
					art = new SymbolItem(artHandle);
				}
		}
		if (art != null) {
			art.dictionaryRef = dictionaryRef;
			art.document = Document.wrapHandle(docHandle);
			art.millis = System.currentTimeMillis();
		}
		return art;
	}

	/**
	 * returns the wrapper, if the object has one
	 * 
	 * @param artHandle
	 * @return the wrapper for the artHandle
	 */
	protected static Art getIfWrapped(int artHandle) {
		return (Art) artItems.get(artHandle);
	}

	/**
	 * Increases the version of the art objects associated with artHandles, if
	 * there are any. It does not wrap the artHandles if they weren't already.
	 * 
	 * @param artHandles
	 */
	protected static void updateIfWrapped(int[] artHandles) {
		// reuse one object for lookups, instead of creating a new one
		// for every artHandle
		for (int i = 0; i < artHandles.length; i+=2) {
			// artHandles contains two entries for every object:
			// the current handle, and the initial handle that was stored
			// in the art object's dictionary when it was wrapped. 
			// see the native side for more explanations
			// (ScriptographerEngine::wrapArtHandle,
			// ScriptographerEngine::selectionChanged)
			int curHandle = artHandles[i];
			int prevHandle = artHandles[i + 1];
			Art art = null;
			if (prevHandle != 0) {
				// in case there was already a art object with the initial handle
				// before, udpate it now:
				art = (Art) artItems.get(prevHandle);
				if (art != null) {
					// remove the old reference
					artItems.remove(prevHandle);
					// update object
					art.handle = curHandle;
					// and store the new reference
					artItems.put(curHandle, art);
				}
			}
			if (art == null) {
				art = (Art) artItems.get(curHandle);
			}
			// now update it if it was found
			if (art != null) {
				art.version++;
			}
		}
		CommitManager.version++;
	}
	
	protected void changeHandle(int newHandle, int newDictionaryRef,
			int docHandle) {
		// remove the object at the old handle
		if (handle != newHandle) {
			artItems.remove(handle);
			// change the handles
			handle = newHandle;
			// and insert it again
			artItems.put(newHandle, this);
		}
		dictionaryRef = newDictionaryRef;
		if (docHandle != 0)
			document = Document.wrapHandle(docHandle);
		// udpate
		version++;
	}
	
	/**
	 * Returns the document of the art item.
	 * 
	 * @return the art item's document.
	 */
	public Document getDocument() {
		return document;
	}

	private native boolean nativeRemove(int handle, int docHandle,
			int dictionaryRef);

	/**
	 * Removes the Art object from the document. If the Art object has children,
	 * they are also removed.
	 * 
	 * @return <code>true</code> if the Art object was removed, false
	 *         otherwise
	 */
	public boolean remove() {
		boolean ret = false;
		if (handle != 0) {
			ret = nativeRemove(handle, document.handle, dictionaryRef);
			artItems.remove(handle);
			handle = 0;			
		}
		return ret;
	}
	
	protected native void finalize();

	/**
	 * Copies the art object to another document, or duplicates it within the
	 * same document.
	 * 
	 * @param document the document to copy the new art to
	 * @return the new copy
	 */
	public native Art copyTo(Document document);

	/**
	 * Copy art object into another art object
	 * 
	 * @param art
	 * @return
	 */
	public native Art copyTo(Art art);

	/**
	 * Clones art object within the same document.
	 * 
	 * @return the newly cloned art object
	 */
	public Object clone() {
		return copyTo(document);
	}

	public native Art getParent();

	public native Art getFirstChild();
	public native Art getLastChild();
	public native Art getNextSibling();
	public native Art getPreviousSibling();

	// don't implement this in native as the number of Art objects is not known
	// in advance and like this, a java ArrayList can be used:
	public Art[] getChildren() {
		ArrayList list = new ArrayList();
		Art child = getFirstChild();
		while (child != null) {
			list.add(child);
			child = child.getNextSibling();
		}
		Art[] children = new Art[list.size()];
		list.toArray(children);
		return children;
	}

	/**
	 * Checks if the Art object has children.
	 * 
	 * @return true if it has one or more children, false otherwise
	 */
	public boolean hasChildren() {
		return getFirstChild() != null;
	}

	public native Rectangle getBounds();
	
	public native Rectangle getControlBounds();
	
	public native Rectangle getGeometricBounds();

	public native void setName(String name);
	
	public native String getName();
	
	/**
	 * Checks if the Art object's name as it appears in the layers palette is a
	 * default descriptive name, rather then a user-assigned name.
	 * 
	 * @return <code>true</code> if it's name is default, false otherwise.
	 */
	public native boolean isDefaultName();

	public PathStyle getStyle() {
		if (style == null)
			style = new PathStyle(this);
		return style;
	}

	public void setStyle(PathStyle style) {
		getStyle(); // make sure it's created
		this.style.init(style);
		this.style.markDirty();
	}

	public native boolean isCenterVisible();
	public native void setCenterVisible(boolean centerVisible);

	protected native void setAttribute(int attribute, boolean value);
	protected native boolean getAttribute(int attribute);

	/**
	 * Checks if the Art object is selected or partially selected (groups with
	 * some selected objects/partially selected paths)
	 * 
	 * @return <code>true</code> if it is selected or partially selected,
	 *         false otherwise
	 */
	public boolean isSelected() {
		return getAttribute(ATTR_SELECTED.intValue());
	}

	public void setSelected(boolean selected) {
		setAttribute(ATTR_SELECTED.intValue(), selected);
	}

	/**
	 * Checks if the Art object is fully selected. For paths this means that all
	 * segments are selected, for container objects all children are selected
	 * 
	 * @return <code>true</code> if it is fully selected, false otherwise
	 */
	public boolean isFullySelected() {
		return getAttribute(ATTR_FULLY_SELECTED.intValue());
	}

	public void setFullySelected(boolean selected) {
		setAttribute(ATTR_FULLY_SELECTED.intValue(), selected);
	}

	/**
	 * Checks if the Art object is locked
	 * 
	 * @return <code>true</code> if it is locked, false otherwise
	 */
	public boolean isLocked() {
		return getAttribute(ATTR_LOCKED.intValue());
	}

	public void setLocked(boolean locked) {
		setAttribute(ATTR_LOCKED.intValue(), locked);
	}

	/**
	 * Checks if the Art object is hidden
	 * 
	 * @return <code>true</code> if it is hidden, false otherwise
	 */
	public boolean isHidden() {
		return getAttribute(ATTR_HIDDEN.intValue());
	}

	public void setHidden(boolean hidden) {
		setAttribute(ATTR_HIDDEN.intValue(), hidden);
	}

	/**
	 * Returns the art object's blend mode.
	 * 
	 * @return any of Art.BLEND_*
	 */
	public native int getBlendMode();

	/**
	 * Set the art object's blend mode:
	 * 
	 * @param mode Art.BLEND_*
	 */
	public native void setBlendMode(int mode);

	/**
	 * @return the opacity of the art object as a value between 0 and 1.
	 */
	public native float getOpacity();

	/**
	 * Sets the art object's opacity.
	 * 
	 * @param opacity the opacity of the art object as a value between 0 and 1.
	 */
	public native void setOpacity(float opacity);

	public native boolean getIsolated();

	public native void setIsolated(boolean isolated);

	public native boolean getKnockout();

	public native boolean getKnockoutInherited();

	public native void setKnockout(int knockout);

	public native boolean getAlphaIsShape();

	public native void setAlphaIsShape(boolean isShape);

	public native boolean isValid();
	
	public native boolean appendChild(Art art);
	
	/**
	 * Moves the art object above the specified art object
	 * 
	 * @param art The art object above which it should be moved
	 * @return true if it was moved, false otherwise
	 */
	public native boolean moveAbove(Art art);
	
	/**
	 * Moves the art object below the specified art object
	 * 
	 * @param art the art object below which it should be moved
	 * @return true if it was moved, false otherwise
	 */
	public native boolean moveBelow(Art art);

	/**
	 * Transforms the art object with custom flags to be set.
	 * 
	 * @param at
	 * @param flags Art. TRANSFORM_*
	 */
	public native void transform(AffineTransform at, int flags);

	/**
	 * Transforms the art object with the flags Art.TRANSFORM_OBJECTS and
	 * Art.TRANSFORM_DEEP set
	 * 
	 * @param at
	 */
	public void transform(AffineTransform at) {
		transform(at, TRANSFORM_OBJECTS | TRANSFORM_DEEP);
	}

	/**
	 * scales the object by creating a scale Matrix and executing transform()
	 * 
	 * @param sx
	 * @param sy
	 * @see Matrix#scale(double, double)
	 */
	public void scale(double sx, double sy) {
		transform(AffineTransform.getScaleInstance(sx, sy));
	}

	public void scale(double scale) {
		scale(scale, scale);
	}

	/**
	 * Translates (moves) the object by the given offsets
	 * 
	 * @param tx
	 * @param ty
	 * @see Matrix#translate(double, double)
	 */
	public void translate(double tx, double ty) {
		transform(AffineTransform.getTranslateInstance(tx, ty));
	}

	/**
	 * Translates (moves) the object by the given offset point
	 * 
	 * @param t
	 */
	public void translate(Point2D t) {
		translate(t.getX(), t.getY());
	}

	/**
	 * Rotates the object around an anchor point by a given angle
	 * 
	 * @param theta the rotation angle in radians
	 * @see Matrix#rotate(double, double, double)
	 */
	public void rotate(double theta, float x, float y) {
		transform(AffineTransform.getRotateInstance(theta, x, y));
	}

	public void rotate(double theta, Point2D anchor) {
		transform(AffineTransform.getRotateInstance(theta, anchor.getX(), anchor.getY()));
	}

	/**
	 * @param shx
	 * @param shy
	 * @see Matrix#shear(double, double)
	 */
	public void shear(double shx, double shy) {
		transform(AffineTransform.getShearInstance(shx, shy));
	}

	/**
	 * Rotates the object by a given angle
	 * 
	 * @param theta the rotation angle in radians
	 */
	public void rotate(double theta) {
		transform(AffineTransform.getRotateInstance(theta));
	}

	public String toString() {
		String name = getClass().getName();
		StringBuffer str = new StringBuffer();
		str.append(name.substring(name.lastIndexOf('.') + 1));
		str.append(" (");
		if (isDefaultName()) {
			str.append("@").append(Integer.toHexString(handle));
		} else {
			str.append(getName());
		}
		str.append(")");
		return str.toString();
	}
		
	public native Raster rasterize(int type, float resolution,
			int antialiasing, float width, float height);
	
	public Raster rasterize(int type, float resolution, int antialiasing) {
		return rasterize(type, resolution, antialiasing, -1, -1);
	}
	
	public Raster rasterize(int type) {
		return rasterize(type, 0, 4, -1, -1);
	}
	
	public Raster rasterize() {
		return rasterize(-1, 0, 4, -1, -1);
	}

	public HitTest hitTest(Point point, int type, float tolerance) {
		return document.nativeHitTest(point, type, tolerance, this);
	}

	public HitTest hitTest(Point point, int type) {
		return document.nativeHitTest(point, type,
				HitTest.DEFAULT_TOLERANCE, this);
	}

	public HitTest hitTest(Point point) {
		return document.nativeHitTest(point, HitTest.TEST_ALL,
				HitTest.DEFAULT_TOLERANCE, this);
	}
	
	/**
	 * Breaks artwork up into individual parts and works just like calling
	 * "expand" from the Object menu in Illustrator.
	 * 
	 * It outlines stroked lines, text objects, gradients, patterns, etc.
	 * 
	 * The art item itself is removed, and the newly created item containing the
	 * expanded artwork is returned.
	 * 
	 * @param flags #EXPAND_*
	 * @param steps the amount of steps for gradient, when the
	 *        #EXPAND_GRADIENTTOPATHS flag is set
	 * @return the newly created item containing the expanded artwork
	 */
	public native Art expand(int flags, int steps);

	/**
	 * Calls {@link #expand(int, int)} with these flags set: #EXPAND_PLUGINART,
	 * #EXPAND_TEXT, #EXPAND_STROKE, #EXPAND_PATTERN, #EXPAND_SYMBOLINSTANCES
	 * 
	 * @return the newly created item containing the expanded artwork
	 */
	public Art expand() {
		return expand(EXPAND_PLUGINART | EXPAND_TEXT | EXPAND_STROKE |
				EXPAND_PATTERN | EXPAND_SYMBOLINSTANCES, 0);
	}
	
	public native int getOrder(Art art);
	
	/**
	 * Checks if the Art object is before the specified Art object
	 * 
	 * @param art The Art object to check against
	 * @return <code>true</code> if it is before the specified Art object,
	 *         false otherwise
	 */
	public boolean isBefore(Art art) {
		return getOrder(art) == ORDER_BEFORE;		
	}
	
	/**
	 * Checks if the Art object is after the specified Art object
	 * 
	 * @param art The Art object to check against
	 * @return <code>true</code> if it is after the specified Art object,
	 *         false otherwise
	 */
	public boolean isAfter(Art art) {
		return getOrder(art) == ORDER_AFTER;		
	}
	
	/**
	 * Checks if the Art object is inside the specified Art object
	 * 
	 * @param art The Art object to check against
	 * @return <code>true</code> if it is inside the specified Art object,
	 *         false otherwise
	 */
	public boolean isInside(Art art) {
		return getOrder(art) == ORDER_INSIDE;		
	}
	
	public boolean isAnchestor(Art art) {
		return getOrder(art) == ORDER_ANCHESTOR;		
	}

	protected native void nativeGetDictionary(Dictionary dictionary);
	protected native void nativeSetDictionary(Dictionary dictionary);

	/* TODO:
	{"equals",			artEquals,				0},
	{"hasEqualPath",	artHasEqualPath,		1},
	{"hasFill",			artHasFill,				0},
	{"hasStroke",		artHasStroke,			0},
	{"isClipping",		artIsClipping,			0},
	*/
	
	protected int getVersion() {
		return version;
	}
	
	private long millis = 0;
	
	public long getMillis() {
		return millis;
	}
}