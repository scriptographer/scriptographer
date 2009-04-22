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

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;

import com.scratchdisk.list.List;
import com.scratchdisk.list.Lists;
import com.scratchdisk.util.IntegerEnumUtils;
import com.scratchdisk.util.SoftIntMap;
import com.scriptographer.CommitManager;

/**
 * @author lehni
 */
public abstract class Item extends DocumentObject {
	
	// the internal version. this is used for internally reflected data,
	// such as segmentList, pathStyle, and so on. Every time an object gets
	// modified, ScriptographerEngine.selectionChanged() gets fired that
	// increases the version of all involved items.
	// update-commit related code needs to check against this variable
	protected int version = 0;
	
	// The handle for the dictionary that contains this item, if any
	protected int dictionaryHandle = 0;

	// The art item's dictionary
	private Dictionary data = null;

	// Internal hash map that keeps track of already wrapped objects. defined
	// as soft.
	private static SoftIntMap<Item> items = new SoftIntMap<Item>();

	private PathStyle style = null;

	// from AIArt.h

	// AIArtType
	protected final static short
		// The special type kAnyArt is never returned as an item type, but
		// is used as a parameter to the Matching Item suite function
		// GetMatchingArt.
		TYPE_ANY = -1,

		// The type kUnknownArt is reserved for objects that are not supported
		// in the plug-in interface. You should anticipate unknown items
		// and ignore them gracefully. For example graph objects return
		// kUnkownType.
		//
		// If a plug-in written for an earlier version of the plug-in API calls
		// GetArt- Type with an item of a type unknown in its version,
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

		// The special type kMysteryPathArt is never returned as an item
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

	/**
	 * Creates an item that wraps an existing AIArtHandle. Make sure the
	 * right constructor is used (Path, Raster). Use wrapArtHandle instead of
	 * directly calling this constructor (it is called from the anchestor's
	 * constructors). Integer is used instead of int so Item(int handle) can be
	 * distinguished from the Item(Integer handle) constructor
	 * 
	 * @param handle
	 */
	protected Item(int handle) {
		// We are setting document to null by default, since it will be
		// set in wrapHandle.
		super(handle, null); 
		// Keep track of this object from now on, see wrapArtHandle
		items.put(handle, this);
	}

	private native static int nativeCreate(short type);

	/**
	 * Wraps an AIArtHandle of given type (determined by
	 * sAIArt->GetType(artHandle)) by the correct Item ancestor class:
	 * 
	 * @param artHandle
	 * @param type
	 * @return the wrapped item
	 */
	protected static Item wrapHandle(int artHandle, short type, int textType,
			int docHandle, int dictionaryHandle, boolean wrapped) {
		// first see whether the object was already wrapped before:
		Item item = null;
		// only try to use the previous wrapper for this address if the object
		// was marked wrapped otherwise we might get wrong wrappers for objects
		// that reuse a previous address
		Item prev = items.get(artHandle);
		if (wrapped)
			item = items.get(artHandle);
		// if it wasn't wrapped yet, do it now:
		// TODO: don't forget to add all types also to the native
		// Item_getType function in com_scriptographer_ai_Item.cpp!
		if (item == null) {
			switch (type) {
				case TYPE_PATH:
					item = new Path(artHandle);
					break;
				case TYPE_GROUP:
					item = new Group(artHandle);
					break;
				case TYPE_RASTER:
					item = new Raster(artHandle);
					break;
				case TYPE_PLACED:
					item = new PlacedFile(artHandle);
					break;
				case TYPE_LAYER:
					item = new Layer(artHandle);
					break;
				case TYPE_COMPOUNDPATH:
					item = new CompoundPath(artHandle);
					break;
				case TYPE_TEXTFRAME:
					switch (textType) {
						case TextItem.TEXTTYPE_POINT:
							item = new PointText(artHandle);
							break;
						case TextItem.TEXTTYPE_AREA:
							item = new AreaText(artHandle);
							break;
						case TextItem.TEXTTYPE_PATH:
							item = new PathText(artHandle);
							break;
					}
					break;
				case TYPE_TRACING:
					item = new Tracing(artHandle);
					break;
				case TYPE_SYMBOL:
					item = new PlacedSymbol(artHandle);
				}
		}
		if (item != null) {
			if (item.getItemType() != Item.getItemType(item.getClass()) && Item.getItemType(item.getClass()) < 100) {
				int i = 0;
			}
			item.dictionaryHandle = dictionaryHandle;
			item.document = Document.wrapHandle(docHandle);
			if (item.millis == 0)
				item.millis = System.currentTimeMillis();
		}
		return item;
	}

	/**
	 * Returns the wrapper, if the object has one
	 * 
	 * @param artHandle
	 * @return the wrapper for the artHandle
	 */
	protected static Item getIfWrapped(int artHandle) {
		return (Item) items.get(artHandle);
	}

	/**
	 * Increases the version of the items associated with artHandles, if
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
			// in the item's dictionary when it was wrapped. 
			// see the native side for more explanations
			// (ScriptographerEngine::wrapArtHandle,
			// ScriptographerEngine::selectionChanged)
			int curHandle = artHandles[i];
			int prevHandle = artHandles[i + 1];
			Item item = null;
			if (prevHandle != 0) {
				// in case there was already an item with the initial handle
				// before, udpate it now:
				item = (Item) items.get(prevHandle);
				if (item != null) {
					// remove the old reference
					items.remove(prevHandle);
					// update object
					item.handle = curHandle;
					// and store the new reference
					items.put(curHandle, item);
				}
			}
			if (item == null) {
				item = (Item) items.get(curHandle);
			}
			// now update it if it was found
			if (item != null) {
				item.version++;
			}
		}
		CommitManager.version++;
	}
	
	protected void changeHandle(int newHandle, int docHandle, int newDictionaryHandle) {
		// Remove the object at the old handle
		if (handle != newHandle) {
			items.remove(handle);
			// Change the handles...
			handle = newHandle;
			// ...and insert it again
			items.put(newHandle, this);
		}
		dictionaryHandle = newDictionaryHandle;
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

	private native boolean nativeRemove(int handle, int docHandle,
			int dictionaryHandle);

	/**
	 * Removes the item from the document. If the item has children,
	 * they are also removed.
	 * 
	 * @return <code>true</code> if the item was removed, false
	 *         otherwise
	 */
	public boolean remove() {
		boolean ret = false;
		if (handle != 0) {
			ret = nativeRemove(handle, document.handle, dictionaryHandle);
			items.remove(handle);
			handle = 0;			
		}
		return ret;
	}
	
	/**
	 * Creates a new AIArtHandle of the specified type and wraps it in a item
	 * 
	 * @param type Item.TYPE_*
	 */
	protected Item(short type) {
		super(nativeCreate(type));
		// Keep track of this object from now on, see wrapArtHandle
		items.put(handle, this);
	}

	protected native void finalize();

	/**
	 * Copies the item to another document, or duplicates it within the
	 * same document.
	 * 
	 * @param document the document to copy the item to
	 * @return the new copy of the item
	 */
	public native Item copyTo(Document document);

	/**
	 * Copies the item into the specified item.
	 * 
	 * @param item
	 * @return
	 */
	public native Item copyTo(Item item);

	/**
	 * Clones the item within the same document.
	 * 
	 * @return the newly cloned item
	 */
	public Object clone() {
		return copyTo(getParent());
	}
	
	/**
	 * @jsbean Returns the item that this item is contained within.
	 */
	public native Item getParent();

	/**
	 * @jsbean Returns the item's parent layer, if any,
	 */
	public native Layer getLayer();

	/**
	 * @jsbean Returns the first item contained within this item.
	 */
	public native Item getFirstChild();

	/**
	 * @jsbean Returns the last item contained within this item.
	 */
	public native Item getLastChild();
	
	/**
	 * @jsbean Returns the next item on the same level as this item.
	 */
	public native Item getNextSibling();

	/**
	 * @jsbean Returns the previous item on the same level as this item.
	 */
	public native Item getPreviousSibling();

	// don't implement this in native as the number of items is not known
	// in advance and like this, a java ArrayList can be used:
	// TODO: Cache the result. Invalidate cached version when version changes,
	// or when appendChild / moveAbove / bellow affects this children list.
	/**
	 * @jsbean An array of items contained within this item
	 */
	public ItemList getChildren() {
		ItemList list = new ItemList();
		Item child = getFirstChild();
		while (child != null) {
			list.add(child);
			child = child.getNextSibling();
		}
		return list;
	}

	public void setChildren(List elements) {
		removeChildren();
		for (int i = 0, size = elements.size(); i < size; i++) {
			Object obj = elements.get(i);
			if (obj instanceof Item)
				appendBottom((Item) obj);
		}
	}

	public void setChildren(Item[] children) {
		setChildren(Lists.asList(children));
	}

	public boolean removeChildren() {
		Item child = getFirstChild();
		boolean removed = false;
		while (child != null) {
			Item next = child.getNextSibling();
			child.remove();
			child = next;
			removed = true;
		}
		return removed;
	}

	/**
	 * Checks if the item has children.
	 * 
	 * @return true if it has one or more children, false otherwise
	 */
	public boolean hasChildren() {
		return getFirstChild() != null;
	}
	
	protected native Rectangle nativeGetBounds();

	private ItemRectangle bounds = null;

	/**
	 * @jsbean The bounds of the item excluding stroke width.
	 */
	public Rectangle getBounds() {
		commit(false);
		if (bounds == null)
			bounds = new ItemRectangle(this);
		else
			bounds.update();
		return bounds;
	}

	public void setBounds(double x, double y, double width, double height) {
		Rectangle rect = getBounds();
		Matrix matrix = new Matrix();
		// Read this from bottom to top:
		// Translate to new center:
		matrix.translate(
				x + width * 0.5f,
				y + height * 0.5f);
		// Scale to new Size, if size changes and avoid divisions by 0:
		if (width != rect.width || height != rect.height)
			matrix.scale(
					rect.width != 0 ? width / rect.width : 1,
					rect.height != 0 ? height / rect.height : 1);
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
	 * @jsbean The bounds of the item including stroke width.
	 */
	public native Rectangle getStrokeBounds();

	/**
	 * @jsbean The bounds of the item including stroke width and controls.
	 */
	public native Rectangle getControlBounds();

	protected native Point nativeGetPosition();

	private ItemPoint position = null;

	/**
	 * @jsbean The item's position. This is the center of the bounds rectangle.
	 */
	public Point getPosition() {
		if (position == null)
			position = new ItemPoint(this);
		else
			position.update();
		return position;
	}

	public void setPosition(Point pt) {
		translate(pt.subtract(getPosition()));
		// This is always defined now since we're using getPosition above
		position.update();
	}

	public void setPosition(double x, double y) {
		setPosition(new Point(x, y));
	}


	/**
	 * @jsbean The name of the item as it appears in the layers palette.
	 * @jsbean Sample code:
	 * @jsbean
	 * @jsbean <pre>
	 * @jsbean var layer = new Layer(); // a layer is an item
	 * @jsbean print(layer.name); // returns '<Layer 2>'
	 * @jsbean layer.name = "A nice name";
	 * @jsbean print(layer.name); // returns 'A nice name'
	 * @jsbean </pre>
	 */
	public native String getName();

	public native void setName(String name);
	
	/**
	 * Checks if the item's name as it appears in the layers palette is a
	 * default descriptive name, rather then a user-assigned name.
	 * 
	 * @return <tt>true</tt> if it's name is default, <tt>false</tt> otherwise.
	 * @jshide bean
	 */
	public native boolean isDefaultName();

	/**
	 * The path style of the item.
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

	private native void nativeSetAttribute(int attribute, boolean value);
	private native boolean nativeGetAttribute(int attribute);

	public void setAttribute(ItemAttribute attribute, boolean value) {
		nativeSetAttribute(attribute.value, value);
	}

	public boolean getAttribute(ItemAttribute attribute) {
		return nativeGetAttribute(attribute.value);
	}

	/**
	 * @jsbean A boolean value that specifies whether an item is selected.
	 * @jsbean Returns true if the item is selected or partially selected (groups with
	 * @jsbean some selected objects/partially selected paths), false otherwise.
	 * @jsbean Sample code:
	 * @jsbean <pre>
	 * @jsbean print(activeDocument.selectedItems.length) // returns 0
	 * @jsbean var path = new Path(); // new items are always created in the active layer
	 * @jsbean path.selected = true; // select the path
	 * @jsbean print(activeDocument.selectedItems.length) // returns 1
	 * @jsbean </pre>
	 */
	public boolean isSelected() {
		return getAttribute(ItemAttribute.SELECTED);
	}

	public void setSelected(boolean selected) {
		setAttribute(ItemAttribute.SELECTED, selected);
	}

	/**
	 * @jsbean A boolean value that specifies whether the item is fully
	 * @jsbean selected. For paths this means that all segments are selected,
	 * @jsbean for container objects all children are selected.
	 */
	public boolean isFullySelected() {
		return getAttribute(ItemAttribute.FULLY_SELECTED);
	}

	public void setFullySelected(boolean selected) {
		setAttribute(ItemAttribute.FULLY_SELECTED, selected);
	}

	/**
	 * @jsbean A boolean value that specifies whether the item is locked.
	 * @jsbean Sample code:
	 * @jsbean <pre>
	 * @jsbean var path = new Path();
	 * @jsbean print(path.locked) // returns false
	 * @jsbean path.locked = true; // locks the path
	 * @jsbean print(path.locked) // returns true
	 * @jsbean </pre>
	 */
	public boolean isLocked() {
		return getAttribute(ItemAttribute.LOCKED);
	}

	public void setLocked(boolean locked) {
		setAttribute(ItemAttribute.LOCKED, locked);
	}

	/**
	 * @jsbean A boolean value that specifies whether the item is visible.
	 * @jsbean Sample code:
	 * @jsbean
	 * @jsbean <pre>
	 * @jsbean var path = new Path();
	 * @jsbean print(path.visible) // returns true
	 * @jsbean path.visible = false; // hides the path
	 * @jsbean print(path.visible) // returns false
	 * @jsbean </pre>
	 */
	public boolean isVisible() {
		return !getAttribute(ItemAttribute.HIDDEN);
	}

	public void setVisible(boolean visible) {
		setAttribute(ItemAttribute.HIDDEN, !visible);
	}

	public final boolean isHidden() {
		return !isVisible();
	}

	public final void setHidden(boolean hidden) {
		setVisible(!hidden);
	}

	// Indicates that the object defines a clip mask. 

	/**
	 * @jsbean A boolean value that specifies whether the item defines a clip mask.
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
		return getAttribute(ItemAttribute.HIDDEN);
	}

	public void setClipMask(boolean clipMask) {
		setAttribute(ItemAttribute.CLIPMASK, clipMask);
	}

	/**
	 * @jsbean Returns <code>true</code> when neither the item, nor it's parents are locked or hidden.
	 */
	public native boolean isEditable();

	/**
	 * @jsbean The item's blend mode as specified by the <code>Item.BLEND_*</code> static
	 * @jsbean properties.
	 * 
	 * @return any of Item.BLEND_*
	 */
	private native int nativeGetBlendMode();

	/**
	 * Set the item's blend mode:
	 * 
	 * @param mode Item.BLEND_*
	 */
	private native void nativeSetBlendMode(int mode);

	public BlendMode getBlendMode() {
		return (BlendMode) IntegerEnumUtils.get(BlendMode.class,
				nativeGetBlendMode());
	}

	public void setBlendMode(BlendMode blend) {
		nativeSetBlendMode(blend.value);
	}

	/**
	 * @jsbean A value between 0 and 1 that specifies the opacity of the item.
	 */
	public native float getOpacity();

	public native void setOpacity(float opacity);

	public native boolean getIsolated();

	public native void setIsolated(boolean isolated);

	private native int nativeGetKnockout(boolean inherited);

	private native void nativeSetKnockout(int knockout);

	public Knockout getKnockout(boolean inherited) {
		return (Knockout) IntegerEnumUtils.get(Knockout.class,
				nativeGetKnockout(inherited));
	}

	public Knockout getKnockout() {
		return getKnockout(false);
	}

	public void setKnockout(Knockout knockout) {
		nativeSetKnockout(knockout.value);
	}

	public native boolean getAlphaIsShape();

	public native void setAlphaIsShape(boolean isShape);

	public native boolean isValid();

	/**
	 * Inserts the specified item as a child of this item by appending it to the
	 * list of children and moving it above all other children.
	 * 
	 * You can use this function for groups, compound paths and layers. Sample
	 * code:
	 * 
	 * <pre>
	 * var group = new Group();
	 * var path = new Path();
	 * group.appendTop(path);
	 * print(path.isDescendant(group)) // returns true
	 * </pre>
	 * 
	 * @param item The item that will be appended as a child
	 */
	public native boolean appendTop(Item item);

	/**
	 * Inserts the specified item as a child of this item by appending it to the
	 * list of children and moving it bellow all other children.
	 * 
	 * You can use this function for groups, compound paths and layers. Sample
	 * code:
	 * 
	 * <pre>
	 * var group = new Group();
	 * var path = new Path();
	 * group.appendTop(path);
	 * print(path.isDescendant(group)) // returns true
	 * </pre>
	 * 
	 * @param item The item that will be appended as a child
	 */
	public native boolean appendBottom(Item item);

	/**
	 * A link to {@link #appendTop}
	 * 
	 * @deprecated use {@link #appendTop} or {@link #appendBottom} instead.
	 */
	public boolean appendChild(Item item) {
		return appendTop(item);
	}
	
	/**
	 * Moves this item above the specified item.
	 * Sample code:
	 * <pre>
	 * var firstPath = new Path();
	 * var secondPath = new Path();
	 * print(firstPath.isAbove(secondPath)) // returns false
	 * firstPath.moveAbove(secondPath);
	 * print(firstPath.isAbove(secondPath)) // returns true
	 * </pre>
	 * 
	 * @param item The item above which it should be moved
	 * @return true if it was moved, false otherwise
	 */
	public native boolean moveAbove(Item item);
	
	/**
	 * Moves the item below the specified item.
	 * <pre>
	 * var firstPath = new Path();
	 * var secondPath = new Path();
	 * print(secondPath.isBelow(firstPath)) // returns false
	 * secondPath.moveBelow(firstPath);
	 * print(secondPath.isBelow(firstPath)) // returns true
	 * </pre>
	 * 
	 * @param item the item below which it should be moved
	 * @return true if it was moved, false otherwise
	 */
	public native boolean moveBelow(Item item);

	/**
	 * Checks if this item is above the specified item in the stacking
	 * order of the document.
	 * Sample code:
	 * <pre>
	 * var firstPath = new Path();
	 * var secondPath = new Path();
	 * print(secondPath.isAbove(firstPath)) // returns true
	 * </pre>
	 * 
	 * @param item The item to check against
	 * @return <code>true</code> if it is above the specified item, false
	 *         otherwise
	 */
	public native boolean isAbove(Item item);
	
	/**
	 * Checks if the item is below the specified item in the stacking
	 * order of the document
	 * Sample code:
	 * <pre>
	 * var firstPath = new Path();
	 * var secondPath = new Path();
	 * print(firstPath.isBelow(secondPath)) // returns true
	 * </pre>
	 * 
	 * @param item The item to check against
	 * @return <code>true</code> if it is below the specified item, false
	 *         otherwise
	 */
	public native boolean isBelow(Item item);

	public boolean isParent(Item item) {
		return getParent() == item;
	}

	public boolean isChild(Item item) {
		return item != null && item.getParent() == this;
	}

	/**
	 * Checks if the item is contained within the specified item
	 * Sample code:
	 * <pre>
	 * var group = new Group();
	 * var path = new Path();
	 * group.appendChild(path);
	 * print(path.isDescendant(group)) // returns true
	 * </pre>
	 *
	 * @param item The item to check against
	 * @return <code>true</code> if it is inside the specified item,
	 *         false otherwise
	 */
	public native boolean isDescendant(Item item);

	/**
	 * Checks if this item is an ancestor of the specified item.
	 * Sample code:
	 * <pre>
	 * var group = new Group();
	 * var path = new Path();
	 * group.appendChild(path);
	 * print(group.isAncestor(path)) // returns true
	 * </pre>
	 * 
	 * @param item the item to check against
	 * @return <code>true</code> if it is an ancestor of the specified 
	 *         item, false otherwise
	 */
	public native boolean isAncestor(Item item);

	public boolean isGroupedWith(Item item) {
		Item parent = getParent();
		while (parent != null) {
			// Find group parents
			if ((parent instanceof Group || parent instanceof CompoundPath) 
					&& item.isDescendant(parent))
				return true;
			// Keep walking up otherwise
			parent = parent.getParent();
		}
		return false;
	}

	private native void nativeTransform(Matrix matrix, int flags);

	/**
	 * Transforms the item with custom flags to be set.
	 * 
	 * @param at
	 * @param flags
	 */
	public void transform(Matrix matrix, EnumSet<TransformFlag> flags) {
		nativeTransform(matrix, IntegerEnumUtils.getFlags(flags));
	}

	public void transform(Matrix matrix, TransformFlag[] flags) {
		transform(matrix, EnumSet.copyOf(Arrays.asList(flags)));
	}

	private static int defaultTransformFlags =
			IntegerEnumUtils.getFlags(EnumSet.of(TransformFlag.OBJECTS,
					TransformFlag.CHILDREN));

	/**
	 * Transforms the item with the flags TransformFlag.OBJECTS, and
	 * TransformFlag.CHILDREN set
	 * 
	 * @param matrix
	 */
	public void transform(Matrix matrix) {
		nativeTransform(matrix, defaultTransformFlags);
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
	 * Scales the item by the given values from its center point.
	 * 
	 * @param sx
	 * @param sy
	 * @see Matrix#scale(double, double, Point center)
	 */
	public void scale(double sx, double sy, Point center) {
		transform(new Matrix().scale(sx, sy, center));
	}

	public void scale(double sx, double sy) {
		scale(sx, sy, getPosition());
	}

	/**
	 * Scales the item by the given values from its center point.
	 * 
	 * @param scale
	 * @see Matrix#scale(double, Point center)
	 */
	public void scale(double scale, Point center) {
		scale(scale, scale, center);
	}

	public void scale(double scale) {
		scale(scale, scale);
	}

	/**
	 * Translates (moves) the item by the given offset point.
	 * 
	 * @param t
	 */
	public void translate(Point t) {
		transform(new Matrix().translate(t));
	}

	/**
	 * Rotates the item around an anchor point by a given angle around
	 * the given point.
	 * 
	 * @param theta the rotation angle in radians
	 * @see Matrix#rotate(double, Point)
	 */
	public void rotate(double theta, Point anchor) {
		transform(new Matrix().rotate(theta, anchor));
	}

	/**
	 * Rotates the item by a given angle around its center point.
	 * 
	 * @param theta the rotation angle in radians
	 */
	public void rotate(double theta) {
		rotate(theta, getPosition());
	}

	/**
	 * Shears the item with a given amount around its center point.
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

	public HitTest hitTest(Point point, HitRequest type, float tolerance) {
		return document.nativeHitTest(point, (type != null ? type
				: HitRequest.ALL).value, tolerance, this);
	}

	public HitTest hitTest(Point point, HitRequest type) {
		return hitTest(point, type, HitTest.DEFAULT_TOLERANCE);
	}

	public HitTest hitTest(Point point) {
		return hitTest(point, HitRequest.ALL, HitTest.DEFAULT_TOLERANCE);
	}

	private native Item nativeExpand(int flags, int steps);
	/**
	 * Breaks artwork up into individual parts and works just like calling
	 * "expand" from the Object menu in Illustrator.
	 * 
	 * It outlines stroked lines, text objects, gradients, patterns, etc.
	 * 
	 * The item itself is removed, and the newly created item containing the
	 * expanded artwork is returned.
	 * 
	 * @param flags
	 * @param steps the amount of steps for gradient, when the
	 *        ExpandFlag#GRADIENT_TO_PATHS flag is set
	 * @return the newly created item containing the expanded artwork
	 */
	public Item expand(EnumSet<ExpandFlag> flags, int steps) {
		return nativeExpand(IntegerEnumUtils.getFlags(flags), steps);
	}

	public Item expand(EnumSet<ExpandFlag> flags) {
		return expand(flags, 0);
	}

	public Item expand(ExpandFlag[] flags) {
		return expand(EnumSet.copyOf(Arrays.asList(flags)));
	}

	private static int defaultExpandFlags =
		IntegerEnumUtils.getFlags(EnumSet.of(ExpandFlag.PLUGIN_ART,
				ExpandFlag.TEXT, ExpandFlag.STROKE, ExpandFlag.PATTERN,
				ExpandFlag.SYMBOL_INSTANCES));

	/**
	 * Calls {@link #expand(int, int)} with these flags set: ExpandFlag#PLUGIN_ART,
	 * ExpandFlag#TEXT, ExpandFlag#STROKE, ExpandFlag#PATTERN, ExpandFlag#SYMBOL_INSTANCES
	 * 
	 * @return the newly created item containing the expanded artwork
	 */
	public Item expand() {
		return nativeExpand(defaultExpandFlags, 0);
	}

	private native int nativeGetData();

	public Dictionary getData() {
		if (data == null)
			data = Dictionary.wrapHandle(nativeGetData(), document);
		return data;	
	}

	public void setData(Map<String, Object> map) {
		Dictionary data = getData();
		if (map != data) {
			data.clear();
			data.putAll(map);
		}
	}

	/* TODO:
	{"equals",			artEquals,				0},
	{"hasEqualPath",	artHasEqualPath,		1},
	{"hasFill",			artHasFill,				0},
	{"hasStroke",		artHasStroke,			0},
	{"isClipping",		artIsClipping,			0},
	*/
	
	private long millis = 0;
	
	/**
	 * This is only there for hunting one of the dreaded bugs.
	 * @jshide all
	 */
	public long getMillis() {
		return millis;
	}

	public native int getItemType();

	public static native int getItemType(Class cls);
}