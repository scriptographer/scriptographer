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
 * File created on 02.12.2004.
 * 
 * $RCSfile: Art.java,v $
 * $Author: lehni $
 * $Revision: 1.5 $
 * $Date: 2005/03/25 17:09:15 $
 */

package com.scriptographer.ai;

import java.util.ArrayList;
import java.util.WeakHashMap;
import java.awt.geom.AffineTransform;

import com.scriptographer.util.Handle;

abstract class Art extends AIObject {
	
	// the internal version. this is used for internally reflected data,
	// such as segmentList, pathStyle, and so on. Everytime an object gets
	// modified, ScriptographerEngine.selectionChanged() gets fired that
	// increases the version of all involved art objects.
	// update-commit related code needs to check against this variable
	protected int version = 0;
	
	// internal hash map that keeps track of already wrapped objects. defined
	// as weak.
	private static WeakHashMap artWrappers = new WeakHashMap();
	// The same, but for the children of one object, and not weak,
	// so they're kept alive as long as the parent lives:
	private ArrayList childrenWrappers = new ArrayList();

	private PathStyle style = null;

	// from AIArt.h
	
	// AIArtType
	protected final static int
		// The special type kAnyArt is never returned as an art object type, but
		// is used as a parameter to the Matching Art suite function
		// GetMatchingArt.
		TYPE_ANY = -1,

		// The type kUnknownArt is reserved for objects that are not supported
		// in the plug-in interface. You should anticipate unknown art objects
		// and ignore them gracefully. For example graph objects return
		// kUnkownType.
		//
		// If a plug-in written for an earlier maxVersion of the plug-in API calls
		// GetArt- Type with an art object of a type unknown in its maxVersion,
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
		TYPE_LAYER = 100;

	// AIArtOrder:
	// TODO: is this used at all?
	protected final static int
		// Return segmentValues from GetArtOrder. (This is the order they would be
		// encountered in a GetArtSibling tree traversal. For most objects, "before"
		// means above in the paint order, and "after" means behind in the paint
		// order, but there are exceptions. E.g., the text paths of a text object
		// are linked from bottom to top.)
		ORDER_UNKNOWN = 0,
		ORDER_FIRST_BEFORE_SECOND = 1,
		ORDER_SECOND_AFTER_FIRST = ORDER_FIRST_BEFORE_SECOND,
		ORDER_FIRST_AFTER_SECOND = 2,
		ORDER_SECOND_BEFORE_FIRST = ORDER_FIRST_AFTER_SECOND,
		ORDER_FIRST_INSIDE_SECOND = 3,
		ORDER_SECOND_INSIDE_FIRST = 4;

	// AIArtUserAttr:
	// used in Document.getMatching:
	public final static Integer
		ATTR_SELECTED = new Integer(0x00000001),
		ATTR_LOCKED = new Integer(0x00000002),
		ATTR_HIDDEN = new Integer(0x00000004),
		ATTR_FULLY_SELECTED = new Integer(0x00000008),

		// Valid only for groups and plugin groups. Indicates whether the contents
		// of the object are expanded in the layers palette.
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

	/**
	 * Creates an Art object that wraps an existing AIArtHandle. Make sure the
	 * right constructor is used (Path, Raster). Use wrapArtHandle instead of
	 * directly calling this constructor (it is called from the anchestor's 
	 * constructors).
	 * Integer is used instead of int so Art(int handle) can be distinguised from
	 * the Art(Integer handle) constructor
	 * @param handle
	 */
	protected Art(Handle handle) {
		super(handle);
		// keep track of this object from now on, see wrapArtHandle
		artWrappers.put(handle, this);
		// store the wrapper also in the paren'ts childrenWrappers segmentList, so
		// it becomes permanent as long the object itself exists.
		// see definitions of artWrappers and childrenWrappers.
		Art parent = getParent();
		if (parent != null)
			parent.childrenWrappers.add(this);
	}
	
	/**
	 * Creates a new AIArtHandle of the specified type and wraps it in a Art object
	 * Do not call it from Art object, call the 0 parameter constructor of the anchestor
	 * classes which then call this constructor here.
	 * @param type
	 */
	protected Art(int type) {
		this(new Handle(nativeCreate(type)));
	}

	/**
	 * Wraps an AIArtHandle of given type (determined by sAIArt->GetType(artHandle)) by
	 * the correct Art anchestor class:
	 * @param artHandle
	 * @param type
	 * @return
	 */
	protected static Art wrapHandle(int artHandle, int type) {
		// first see wether the object was already wrapped before:
		Handle handle = new Handle(artHandle);
		Art art = (Art) artWrappers.get(handle);
		// if it wasn't wrapped yet, do it now:
		if (art == null) {
			switch (type) {
			case TYPE_PATH:
				art = new Path(handle);
				break;
			case TYPE_GROUP:
				art = new Group(handle);
				break;
			case TYPE_RASTER:
				art = new Raster(handle);
				break;
			case TYPE_LAYER:
				art = new Layer(handle);
				break;
			}
		}
		return art;
	}
	
	/**
	 * Increases the version of the art object associated with artHandle,
	 * if there is one. It does not wrap the artHandle if it wasn't
	 * already.
	 *
	 * @param artHandle
	 * @return true if the object was updated
	 */
	protected static boolean updateIfWrapped(int artHandle) {
		Art art = (Art) artWrappers.get(new Handle(artHandle));
		if (art != null) {
			art.version++;
			return true;
		}
		return false;
	}

	/**
	 * This gets fired from the native environment if the selection was changed
	 * all the contained artHandles should increase their version counter, if
	 * they're already wrapped:
	 * 
	 * @param artHandles
	 */
	private static void onSelectionChanged(int[] artHandles) {
		// reuse one object for lookups, instead of creating a new one
		// for every artHandle
		Handle handle = new Handle();
		for (int i = 0; i < artHandles.length; i++) {
			handle.handle = artHandles[i];
			Art art = (Art) artWrappers.get(handle);
			if (art != null) {
				art.version++;
			}
		}
	}

	public boolean remove() {
		boolean ret = false;
		if (handle != 0) {
			ret = nativeRemove(handle);
			artWrappers.remove(new Handle(handle));
			handle = 0;			
		}
		return ret;
	}

	public native Object clone();

	/**
	 * Creates an AIArtHandle of the given type. Used in the Art constructor
	 * @param type
	 * @return
	 */
	private native static int nativeCreate(int type);
	private native boolean nativeRemove(int handle);

	public native Art getFirstChild();
	public native Art getLastChild();
	public native Art getNextSibling();
	public native Art getPreviousSibling();
	public native Art getParent();

	// don't implement this in native as the number of Art objects is not known in advance
	// and like this, a java ArrayList can be used:
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

	public boolean hasChildren() {
		return getFirstChild() != null;
	}

	public native Rectangle getBounds();

	public native void setName(String name);
	
	public native String getName();
	
	public native boolean hasDefaultName();

	public PathStyle getStyle() {
		if (style == null)
			style = new PathStyle(this);
		else
			style.checkUpdate();
		return style;
	}

	public void setStyle(PathStyle style) {
		this.style = new PathStyle(style, this);
	}

	public native boolean isCenterVisible();
	public native void setCenterVisible(boolean centerVisible);

	protected native void setAttribute(int attribute, boolean value);
	protected native boolean getAttribute(int attribute);

	public boolean isSelected() {
		return getAttribute(ATTR_SELECTED.intValue());
	}

	public void setSelected(boolean selected) {
		setAttribute(ATTR_SELECTED.intValue(), selected);
	}

	public boolean isLocked() {
		return getAttribute(ATTR_LOCKED.intValue());
	}

	public void setLocked(boolean locked) {
		setAttribute(ATTR_LOCKED.intValue(), locked);
	}

	public boolean isHidden() {
		return getAttribute(ATTR_HIDDEN.intValue());
	}

	public void setHidden(boolean hidden) {
		setAttribute(ATTR_HIDDEN.intValue(), hidden);
	}

	// for text
	/*
	 * {"textType", ART_TEXTTYPE, JSPROP_ENUMERATE},
	 * {"matrix", ART_MATRIX, JSPROP_ENUMERATE},
	 * {"dashOffset", ART_TEXTOFFSET, JSPROP_ENUMERATE},
	 * {"wrapped", ART_TEXTWRAPPED, JSPROP_ENUMERATE},
	 * {"orientation", ART_TEXTORIENTATION, JSPROP_ENUMERATE},
	 * 
	 * // for group
	 * 
	 * {"clipped", ART_CLIPPED, JSPROP_ENUMERATE},
	 */

	public native boolean append(Art art);
	public native boolean moveAbove(Art art);
	public native boolean moveBelow(Art art);

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

	public native void transform(AffineTransform at, int flags);

	public void transform(AffineTransform at) {
		transform(at, TRANSFORM_OBJECTS | TRANSFORM_DEEP);
	}
	
	public String toString() {
		String name = getClass().getName();
		name = name.substring(name.lastIndexOf('.') + 1);
		name += " (";
		if (hasDefaultName()) {
			name += "@" + Integer.toHexString(handle);
		} else {
			name += getName();
		}
		return name + ")";
	}

	/*
	{"toString",		artToString,			0},
	{"equals",			artEquals,				0},
	{"clone",			artClone,				0},
	{"isValid",			artIsValid,				0},
	{"hasEqualPath",	artHasEqualPath,		1},
	{"hasFill",			artHasFill,				0},
	{"hasStroke",		artHasStroke,			0},
	{"isClipping",		artIsClipping,			0},
	*/
	}