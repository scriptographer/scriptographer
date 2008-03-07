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
 * File created on 02.12.2004.
 * 
 * $Id$
 */

package com.scriptographer.ai;

import java.util.ArrayList;

import com.scratchdisk.list.List;
import com.scratchdisk.list.Lists;
import com.scratchdisk.util.SoftIntMap;
import com.scriptographer.CommitManager;

/**
 * @author lehni
 */
public abstract class Art extends DictionaryObject {
	
	// the internal version. this is used for internally reflected data,
	// such as segmentList, pathStyle, and so on. Every time an object gets
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
		TRANSFORM_SELECTION_ONLY	= 1 << 7;
	
	// AIArtOrder:
	public final static int
		ORDER_UNKNOWN = 0,
		ORDER_ABOVE = 1,
		ORDER_BELOW = 2,
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
	 * sAIArt->GetType(artHandle)) by the correct Art ancestor class:
	 * 
	 * @param artHandle
	 * @param type
	 * @return the wrapped art object
	 */
	protected static Art wrapHandle(int artHandle, short type, int textType,
			int docHandle, int dictionaryRef, boolean wrapped) {
		// first see whether the object was already wrapped before:
		Art art = null;
		// only try to use the previous wrapper for this address if the object
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
			if (art.millis == 0)
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
		// Remove the object at the old handle
		if (handle != newHandle) {
			artItems.remove(handle);
			// Change the handles...
			handle = newHandle;
			// ...and insert it again
			artItems.put(newHandle, this);
		}
		dictionaryRef = newDictionaryRef;
		if (docHandle != 0)
			document = Document.wrapHandle(docHandle);
		// Update
		version++;
	}

	/**
	 * Called by native methods that need all cached changes to be
	 * commited before the objects are modified. The version is then
	 * increased to invalidate the cached values, as they were just 
	 * changed.
	 */
	protected void commit(boolean invalidate) {
		CommitManager.commit(this);
		// Increasing version by one causes refetching of cached data:
		if (invalidate)
			version++;
	}

	/**
	 * @jsbean Returns the document that the art item belongs to.
	 */
	public Document getDocument() {
		return document;
	}

	private native boolean nativeRemove(int handle, int docHandle,
			int dictionaryRef);

	/**
	 * Removes the art item from the document. If the art item has children,
	 * they are also removed.
	 * 
	 * @return <code>true</code> if the art item was removed, false
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
	 * Copies the art item to another document, or duplicates it within the
	 * same document.
	 * 
	 * @param document the document to copy the art item to
	 * @return the new copy of the art item
	 */
	public native Art copyTo(Document document);

	/**
	 * Copies the art item into the specified art item.
	 * 
	 * @param art
	 * @return
	 */
	public native Art copyTo(Art art);

	/**
	 * Clones the art item within the same document.
	 * 
	 * @return the newly cloned art object
	 */
	public Object clone() {
		return copyTo(document);
	}
	
	/**
	 * @jsbean Returns the art item that this art item is contained within.
	 */
	public native Art getParent();

	/**
	 * @jsbean Returns the first art item contained within this art item.
	 */
	public native Art getFirstChild();

	/**
	 * @jsbean Returns the last art item contained within this art item.
	 */
	public native Art getLastChild();
	
	/**
	 * @jsbean Returns the next art item on the same level as this art item.
	 */
	public native Art getNextSibling();

	/**
	 * @jsbean Returns the previous art item on the same level as this art item.
	 */
	public native Art getPreviousSibling();

	// don't implement this in native as the number of art objects is not known
	// in advance and like this, a java ArrayList can be used:
	/**
	 * @jsbean An array of art items contained within this art item
	 */
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

	public void setChildren(List elements) {
		removeChildren();
		for (int i = 0, size = elements.size(); i < size; i++) {
			Object obj = elements.get(i);
			if (obj instanceof Art)
				appendChild((Art) obj);
		}
	}

	public void setChildren(Art[] children) {
		setChildren(Lists.asList(children));
	}

	public boolean removeChildren() {
		Art child = getFirstChild();
		boolean removed = false;
		while (child != null) {
			Art next = child.getNextSibling();
			child.remove();
			child = next;
			removed = true;
		}
		return removed;
	}

	/**
	 * Checks if the art object has children.
	 * 
	 * @return true if it has one or more children, false otherwise
	 */
	public boolean hasChildren() {
		return getFirstChild() != null;
	}

	protected native Rectangle nativeGetBounds();

	private ArtRectangle bounds = null;

	/**
	 * @jsbean The bounds of the art item excluding stroke width.
	 */
	public Rectangle getBounds() {
		if (bounds == null)
			bounds = new ArtRectangle(this);
		else
			bounds.update();
		return bounds;
	}

	public void setBounds(float x, float y, float width, float height) {
		Rectangle rect = getBounds();
		Matrix matrix = new Matrix();
		// Read this from bottom to top:
		// Translate to new center:
		matrix.translate(
				x + width * 0.5f,
				y + height * 0.5f);
		// Scale to new Size:
		matrix.scale(
				width / rect.width,
				height / rect.height);
		// Translate to center:
		matrix.translate(
				-(rect.x + rect.width * 0.5f),
				-(rect.y + rect.height * 0.5f));
		// Now execute the transformation:
		transform(matrix);
		// This is always defined now since we're using getBounds above
		bounds.update();
	}

	public void setBounds(Rectangle rect) {
		setBounds(rect.x, rect.y, rect.width, rect.height);
	}

	/**
	 * @jsbean The bounds of the art item including stroke width.
	 */
	public native Rectangle getStrokeBounds();

	/**
	 * @jsbean The bounds of the art item including stroke width and controls.
	 */
	public native Rectangle getControlBounds();

	protected native Point nativeGetPosition();

	private ArtPoint position = null;

	public Point getPosition() {
		if (position == null)
			position = new ArtPoint(this);
		else
			position.update();
		return position;
	}

	public void setPosition(float x, float y) {
		Point point = getPosition();
		translate(x - point.x, y - point.y);
		// This is always defined now since we're using getPosition above
		position.update();
	}

	public void setPosition(Point pt) {
		setPosition(pt.x, pt.y);
	}

	/**
	 * @jsbean The name of the art item as it appears in the layers palette.
	 * @jsbean Sample code:
	 * @jsbean
	 * @jsbean <pre>
	 * @jsbean var layer = new Layer(); // a layer is an art item
	 * @jsbean print(layer.name); // returns '<Layer 2>'
	 * @jsbean layer.name = "A nice name";
	 * @jsbean print(layer.name); // returns 'A nice name'
	 * @jsbean </pre>
	 */
	public native String getName();

	public native void setName(String name);
	
	/**
	 * Checks if the art item's name as it appears in the layers palette is a
	 * default descriptive name, rather then a user-assigned name.
	 * 
	 * @return <tt>true</tt> if it's name is default, <tt>false</tt> otherwise.
	 * @jshide bean
	 */
	public native boolean isDefaultName();

	/**
	 * The path style of the art item.
	 * @return
	 */
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
	 * @jsbean A boolean value that specifies whether an art item is selected.
	 * @jsbean Returns true if the art item is selected or partially selected (groups with
	 * @jsbean some selected objects/partially selected paths), false otherwise.
	 * @jsbean Sample code:
	 * @jsbean <pre>
	 * @jsbean print(activeDocument.selectedItems.length) // returns 0
	 * @jsbean var path = new Path(); // new art items are always created in the active layer
	 * @jsbean path.selected = true; // select the path
	 * @jsbean print(activeDocument.selectedItems.length) // returns 1
	 * @jsbean </pre>
	 */
	public boolean isSelected() {
		return getAttribute(ATTR_SELECTED.intValue());
	}

	public void setSelected(boolean selected) {
		setAttribute(ATTR_SELECTED.intValue(), selected);
	}

	/**
	 * @jsbean A boolean value that specifies whether the art item is fully
	 * @jsbean selected. For paths this means that all segments are selected,
	 * @jsbean for container objects all children are selected.
	 */
	public boolean isFullySelected() {
		return getAttribute(ATTR_FULLY_SELECTED.intValue());
	}

	public void setFullySelected(boolean selected) {
		setAttribute(ATTR_FULLY_SELECTED.intValue(), selected);
	}

	/**
	 * @jsbean A boolean value that specifies whether the art item is locked.
	 * @jsbean Sample code:
	 * @jsbean <pre>
	 * @jsbean var path = new Path();
	 * @jsbean print(path.locked) // returns false
	 * @jsbean path.locked = true; // locks the path
	 * @jsbean print(path.locked) // returns true
	 * @jsbean </pre>
	 */
	public boolean isLocked() {
		return getAttribute(ATTR_LOCKED.intValue());
	}

	public void setLocked(boolean locked) {
		setAttribute(ATTR_LOCKED.intValue(), locked);
	}

	/**
	 * @jsbean A boolean value that specifies whether the art item is hidden.
	 * @jsbean Sample code:
	 * @jsbean
	 * @jsbean <pre>
	 * @jsbean var path = new Path();
	 * @jsbean print(path.hidden) // returns false
	 * @jsbean path.hidden = true; // hides the path
	 * @jsbean print(path.hidden) // returns true
	 * @jsbean </pre>
	 */
	public boolean isHidden() {
		return getAttribute(ATTR_HIDDEN.intValue());
	}

	public void setHidden(boolean hidden) {
		setAttribute(ATTR_HIDDEN.intValue(), hidden);
	}

	// Indicates that the object defines a clip mask. 

	/**
	 * @jsbean A boolean value that specifies whether the art item defines a clip mask.
	 * @jsbean This can only be set on paths, compound paths, and text frame objects,
	 * @jsbean and only if the item is already contained within a clip group.
	 * @jsbean Sample code:
	 * @jsbean
	 * @jsbean <pre>
	 * @jsbean var group = new Group();
	 * @jsbean group.appendChild(path);
	 * @jsbean group.clipped = true;
	 * @jsbean path.clipMask = true;
	 * @jsbean </pre>
	 */
	public boolean isClipMask() {
		return getAttribute(ATTR_HIDDEN.intValue());
	}

	public void setClipMask(boolean clipMask) {
		setAttribute(ATTR_IS_CLIPMASK.intValue(), clipMask);
	}

	/**
	 * @jsbean Returns <code>true</code> when neither the art item, nor it's parents are locked or hidden.
	 */
	public native boolean isEditable();

	/**
	 * @jsbean The art item's blend mode as specified by the <code>Art.BLEND_*</code> static
	 * @jsbean properties.
	 * 
	 * @return any of Art.BLEND_*
	 */
	public native int getBlendMode();

	/**
	 * Set the art item's blend mode:
	 * 
	 * @param mode Art.BLEND_*
	 */
	public native void setBlendMode(int mode);

	/**
	 * @jsbean A value between 0 and 1 that specifies the opacity of the art item.
	 */
	public native float getOpacity();

	public native void setOpacity(float opacity);

	public native boolean getIsolated();

	public native void setIsolated(boolean isolated);

	public native boolean getKnockout();

	public native boolean getKnockoutInherited();

	public native void setKnockout(int knockout);

	public native boolean getAlphaIsShape();

	public native void setAlphaIsShape(boolean isShape);

	public native boolean isValid();

	/**
	 * Appends the specified art item as a child of this art item.
	 * You can use this function for groups, compound paths and layers.
	 * Sample code:
	 * <pre>
	 * var group = new Group();
	 * var path = new Path();
	 * group.appendChild(path);
	 * print(path.isInside(group)) // returns true
	 * </pre>
	 * 
	 * @param art The art item that will be appended as a child
	 */
	public native boolean appendChild(Art art);
	
	/**
	 * Moves this art item above the specified art item.
	 * Sample code:
	 * <pre>
	 * var firstPath = new Path();
	 * var secondPath = new Path();
	 * print(firstPath.isAbove(secondPath)) // returns false
	 * firstPath.moveAbove(secondPath);
	 * print(firstPath.isAbove(secondPath)) // returns true
	 * </pre>
	 * 
	 * @param art The art item above which it should be moved
	 * @return true if it was moved, false otherwise
	 */
	public native boolean moveAbove(Art art);
	
	/**
	 * Moves the art item below the specified art object.
	 * <pre>
	 * var firstPath = new Path();
	 * var secondPath = new Path();
	 * print(secondPath.isBelow(firstPath)) // returns false
	 * secondPath.moveBelow(firstPath);
	 * print(secondPath.isBelow(firstPath)) // returns true
	 * </pre>
	 * 
	 * @param art the art item below which it should be moved
	 * @return true if it was moved, false otherwise
	 */
	public native boolean moveBelow(Art art);

	/**
	 * Transforms the art item with custom flags to be set.
	 * 
	 * @param at
	 * @param flags Art. TRANSFORM_*
	 */
	public native void transform(Matrix matrix, int flags);

	/**
	 * Transforms the art item with the flags Art.TRANSFORM_OBJECTS and
	 * Art.TRANSFORM_DEEP set
	 * 
	 * @param matrix
	 */
	public void transform(Matrix matrix) {
		transform(matrix, TRANSFORM_OBJECTS | TRANSFORM_CHILDREN);
	}

	protected Matrix centered(Matrix matrix) {
		Matrix centered = new Matrix();
		Point pos = getPosition();
		centered.translate(pos.x, pos.y);
		centered.concatenate(matrix);
		centered.translate(-pos.x, -pos.y);
		return centered;
	}
	
	/**
	 * Scales the art item by the given values from its center point.
	 * 
	 * @param sx
	 * @param sy
	 * @see Matrix#scale(double, double)
	 */
	public void scale(double sx, double sy) {
		transform(centered(new Matrix().scale(sx, sy)));
	}

	public void scale(double scale) {
		scale(scale, scale);
	}

	/**
	 * Translates (moves) the art item by the given offsets.
	 * 
	 * @param tx
	 * @param ty
	 * @see Matrix#translate(double, double)
	 */
	public void translate(double tx, double ty) {
		transform(new Matrix().translate(tx, ty));
	}

	/**
	 * Translates (moves) the art item by the given offset point.
	 * 
	 * @param t
	 */
	public void translate(Point t) {
		translate(t.x, t.y);
	}

	/**
	 * Rotates the art item by a given angle around its center point.
	 * 
	 * @param theta the rotation angle in radians
	 */
	public void rotate(double theta) {
		transform(new Matrix().rotate(theta, getPosition()));
	}

	/**
	 * Rotates the art item around an anchor point by a given angle around
	 * the given point.
	 * 
	 * @param theta the rotation angle in radians
	 * @see Matrix#rotate(double, double, double)
	 */
	public void rotate(double theta, float x, float y) {
		transform(new Matrix().rotate(theta, x, y));
	}

	public void rotate(double theta, Point anchor) {
		transform(new Matrix().rotate(theta, anchor));
	}

	/**
	 * Shears the art item with a given amount around its center point.
	 * @param shx
	 * @param shy
	 * @see Matrix#shear(double, double)
	 */
	public void shear(double shx, double shy) {
		transform(centered(new Matrix().shear(shx, shy)));
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
	
	protected native int getOrder(Art art);
	
	/**
	 * Checks if this art item is above the specified art item in the stacking
	 * order of the document.
	 * Sample code:
	 * <pre>
	 * var firstPath = new Path();
	 * var secondPath = new Path();
	 * print(secondPath.isAbove(firstPath)) // returns true
	 * </pre>
	 * 
	 * @param art The art item to check against
	 * @return <code>true</code> if it is above the specified art item, false
	 *         otherwise
	 */
	public boolean isAbove(Art art) {
		return getOrder(art) == ORDER_ABOVE;		
	}
	
	/**
	 * Checks if the art item is below the specified art item in the stacking
	 * order of the document
	 * Sample code:
	 * <pre>
	 * var firstPath = new Path();
	 * var secondPath = new Path();
	 * print(firstPath.isBelow(secondPath)) // returns true
	 * </pre>
	 * 
	 * @param art The art item to check against
	 * @return <code>true</code> if it is below the specified art item, false
	 *         otherwise
	 */
	public boolean isBelow(Art art) {
		return getOrder(art) == ORDER_BELOW;		
	}
	
	/**
	 * Checks if the art item is contained within the specified art item
	 * Sample code:
	 * <pre>
	 * var group = new Group();
	 * var path = new Path();
	 * group.appendChild(path);
	 * print(path.isInside(group)) // returns true
	 * </pre>
	 *
	 * @param art The art item to check against
	 * @return <code>true</code> if it is inside the specified art item,
	 *         false otherwise
	 */
	public boolean isInside(Art art) {
		return getOrder(art) == ORDER_INSIDE;		
	}

	/**
	 * Checks if this art item is an ancestor of the specified art item.
	 * Sample code:
	 * <pre>
	 * var group = new Group();
	 * var path = new Path();
	 * group.appendChild(path);
	 * print(group.isAncestor(path)) // returns true
	 * </pre>
	 * 
	 * @param art the art item to check against
	 * @return <code>true</code> if it is an ancestor of the specified art
	 *         item, false otherwise
	 */
	public boolean isAncestor(Art art) {
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
	
	/**
	 * This is only there for hunting one of the dreaded bugs.
	 * @jshide all
	 */
	public long getMillis() {
		return millis;
	}
}