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
 * File created on 02.12.2004.
 * 
 * $Id$
 */

package com.scriptographer.ai;

import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.Stack;

import com.scratchdisk.list.Lists;
import com.scratchdisk.list.ReadOnlyList;
import com.scratchdisk.script.ChangeReceiver;
import com.scratchdisk.util.ArrayList;
import com.scratchdisk.util.IntegerEnumUtils;
import com.scratchdisk.util.SoftIntMap;
import com.scriptographer.CommitManager;
import com.scriptographer.ScriptographerEngine;
import com.scriptographer.ScriptographerException;
import com.scriptographer.adm.Image;

/**
 * The Item type allows you to access and modify the artwork items in
 * Illustrator documents. Its functionality is inherited by different document
 * item types such as {@link Path}, {@link CompoundPath}, {@link Group},
 * {@link Layer} and {@link Raster}. They each add a layer of functionality that
 * is unique to their type, but share the underlying properties and functions
 * that they inherit from Item.
 * 
 * @author lehni
 * 
 * @jsreference {@type field} {@name document} {@reference Item#document} {@after data}
 */
public class Item extends DocumentObject implements Style, ChangeReceiver {

	/**
	 * The internal version. this is used for internally reflected data, such as
	 * segmentList, pathStyle, and so on. Every time an object gets modified,
	 * ScriptographerEngine.selectionChanged() gets fired that increases the
	 * version of all involved items. update-commit related code needs to check
	 * against this variable
	 */
	protected int version = 0;

	/**
	 * Handle history, to keep track of version / handle pairs in a stack.
	 */
	protected Stack<HandleHistoryEntry> handleHistory = null;

	/**
	 * The history version at the time of the creation of this item. This is
	 * used for isValid checks. If historyVersion is below creationVersion, the
	 * item does not exist anymore and is invalid (happens through undo's). Set
	 * to -1 if creation version is unknown, for existing items that are
	 * wrapped, rather than newly created. These version values include both
	 * branch and level information. See Document.
	 */
	protected long creationVersion;

	/**
	 * The history version at which this item was removed. Set to an invalid
	 * value as long as it is not removed. This is used in isValid checks.
	 */
	protected long deletionVersion = Long.MAX_VALUE;

	/**
	 * Then history version of the last modification of this item. Refetching is
	 * needed for any item for which the current historyVersion is between
	 * between creationVersion and modificationVersion. These version values
	 * include both branch and level information. See Document.
	 */
	protected long modificationVersion;

	/**
	 * The handle for the dictionary that contains this item, if any
	 */
	protected int dictionaryHandle = 0;

	/**
	 * The key under which this item was inserted into the dictionary
	 */
	protected int dictionaryKey = 0;

	/**
	 * The art item's dictionary
	 */
	private Dictionary data = null;

	/**
	 * Internal hash map that keeps track of already wrapped objects. defined
	 * as soft.
	 */
	private static SoftIntMap<Item> items = new SoftIntMap<Item>();

	private PathStyle style = null;

	/**
	 * For Document#currentStyleItem
	 */
	protected final static int HANDLE_CURRENT_STYLE = -1;

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

	private static native int nativeCreate(short type);

	/**
	 * Creates a wrapper for a AIArtHandle. Make sure the right constructor is
	 * used (Path, Raster). Use wrapArtHandle instead of directly calling this
	 * constructor (it is called from the anchestor's constructors).
	 * 
	 * @param handle
	 */
	protected Item(int handle, Document doc, boolean created,
			boolean unversioned) {
		super(handle, doc);
		if (document == null)
			throw new ScriptographerException(
					"Unable to create item. There is no open document.");
		if (unversioned) {
			creationVersion = 0;
		} else if (created) {
			// Set the creation level to the current level for this newly
			// created item, so that isValid works right away. After the history
			// cycle, these values are set one higher, since this items really
			// belongs to the document.historyVersion + 1 cycle.
			creationVersion = document.historyVersion;
			// This item's versions need to be updated after the history cycle
			// is finished.
			document.addCreatedItem(this);
		} else {
			// This is an existing item of which the creation level is unknown.
			// set levels to -1
			creationVersion = -1;
			// Since creationLevel for this item is not known, add it to
			// the items to check on each undo.
			document.checkItems.add(new SoftReference<Item>(this));
		}
		// Use the current history level for the modification level, to force
		// updates bellow this level, since we do not know when exactly
		// the item was created or last modified.
		modificationVersion = document.historyVersion;
		// Keep track of this object from now on, see wrapArtHandle
		if (handle != 0)
			items.put(handle, this);

		// Collect new items. Used for effects
		if (created && newItems != null)
			newItems.add(this);
	}

	protected Item(int handle, int docHandle, boolean created) {
		this(handle, Document.wrapHandle(docHandle), created, false);
	}

	/**
	 * Creates a new AIArtHandle of the specified type in the active document
	 * and wraps it in a item.
	 * 
	 * @param type Item.TYPE_
	 */
	protected Item(short type) {
		// Create with false handle, to get document pointer and have time to
		// activate with forCreation = true, to make sure currentStyle gets
		// committed, etc.
		this(0, null, true, false);
		document.activate(false, true);
		// Now set the handle
		handle = nativeCreate(type);
		// Keep track of this object from now on, see wrapArtHandle
		items.put(handle, this);
	}

	/**
	 * Creates a wrapper for newly created items in the active document.
	 * Do not use to wrap existing ones, since it passes created = true!
	 */
	protected Item(int handle) {
		this(handle, null, true, false);
	}

	/**
	 * Wraps an AIArtHandle of given type (determined by
	 * sAIArt->GetType(artHandle)) by the correct Item ancestor class:
	 * 
	 * @param artHandle
	 * @param type
	 * @return the wrapped item
	 */
	protected static Item wrapHandle(int artHandle, short type, int textType,
			int docHandle, boolean wrapped, boolean created) {
		// ScriptographerEngine.logConsole("wrapHandle: @" + Integer.toHexString(artHandle));
		// First see whether the object was already wrapped before:
		Item item = null;
		// Only try to use the previous wrapper for this address if the object
		// was marked wrapped otherwise we might get wrong wrappers for objects
		// that reuse a previous address
		if (wrapped) {
			item = items.get(artHandle);
			// Make sure this item is still valid. It might be a reused art
			// handle too...
			if (item != null && !item.isValid())
				item = null;
		}
		// If it wasn't wrapped yet, do it now:
		// TODO: Don't forget to add all types also to the native
		// Item_getType function in com_scriptographer_ai_Item.cpp!
		if (item == null) {
			switch (type) {
			case TYPE_PATH:
				item = new Path(artHandle, docHandle, created);
				break;
			case TYPE_GROUP:
				item = new Group(artHandle, docHandle, created);
				break;
			case TYPE_RASTER:
				item = new Raster(artHandle, docHandle, created);
				break;
			case TYPE_PLACED:
				item = new PlacedFile(artHandle, docHandle, created);
				break;
			case TYPE_LAYER:
				item = new Layer(artHandle, docHandle, created);
				break;
			case TYPE_COMPOUNDPATH:
				item = new CompoundPath(artHandle, docHandle, created);
				break;
			case TYPE_TEXTFRAME:
				switch (textType) {
				case TextItem.TEXTTYPE_POINT:
					item = new PointText(artHandle, docHandle, created);
					break;
				case TextItem.TEXTTYPE_AREA:
					item = new AreaText(artHandle, docHandle, created);
					break;
				case TextItem.TEXTTYPE_PATH:
					item = new PathText(artHandle, docHandle, created);
					break;
				}
				break;
			case TYPE_TRACING:
				item = new Tracing(artHandle, docHandle, created);
				break;
			case TYPE_SYMBOL:
				item = new PlacedSymbol(artHandle, docHandle, created);
			}
		}
		return item;
	}

	protected static native Item wrapHandle(int artHandle, int docHandle,
			boolean created, boolean checkWrapped);

	/**
	 * Returns the wrapper, if the object has one
	 * 
	 * @param artHandle
	 * @return the wrapper for the artHandle
	 */
	protected static Item getIfWrapped(int artHandle) {
		Item item = items.get(artHandle);
		// Make sure this item is still valid. It might be a reused art handle
		// too...
		if (item != null && !item.isValid()) {
			// Remove invalid ones so they dont come back, as their handle was 
			// now reused in the meantime by a new item. We can do this safely
			// as getIfWrapped is only used for now valid items, so there is no
			// way we could go back to the previous item through undoing, as
			// Illustrator would not have given us the handle otherwise.
			items.remove(artHandle);
			item = null;
		}
		return item;
	}

	/**
	 * Increases the version of the items associated with artHandles, if
	 * there are any. It does not wrap the artHandles if they weren't already.
	 * 
	 * Called from the native environment.
	 */
	protected static void updateIfWrapped(int[] artHandles) {
		// Reuse item objects for lookups, instead of creating a new one
		// for every artHandle
		for (int i = 0; i < artHandles.length; i+=2) {
			// artHandles contains two entries for every item:
			// The current handle, and the initial handle that was stored
			// in the item's dictionary when it was wrapped. 
			// See the native side for more explanations
			// (ScriptographerEngine::wrapArtHandle,
			// ScriptographerEngine::onSelectionChanged)
			int curHandle = artHandles[i];
			int prevHandle = artHandles[i + 1];
			Item item = null;
			// Only change the handle if it has changed.
			// Items where the handle has not changed are also included,
			// since their version numbers needs to increase.
			if (prevHandle != 0 && prevHandle != curHandle) {
				// In case there was already an item with the initial handle
				// before, change its handle now:
				item = items.get(prevHandle);
				if (item != null)
					item.changeHandle(curHandle, 0, false);
			} else {
				item = items.get(curHandle);
				// Now update it if it was found
				if (item != null)
					item.version++;
			}
		}
	}

	/**
	 * @jshide
	 */
	public static void removeIfWrapped(int[] artHandles, boolean removeHandles) {
		for (int i = 0; i < artHandles.length; i++) {
			Item item = items.get(artHandles[i]);
			if (item != null)
				item.remove(removeHandles);
		}
	}

	/**
	 * Simple helper class for handle history entries, to keep track of
	 * version / handle pairs in a stack.
	 */
	static class HandleHistoryEntry {
		long version;
		int handle;

		HandleHistoryEntry(long version, int handle) {
			this.version = version;
			this.handle = handle;
		}
	};
	
	protected void changeHandle(int newHandle, int docHandle,
			boolean clearDictionary) {
		// Remove the object at the old handle
		if (handle != newHandle) {
			// If there was no handle history yet, add the creation version to
			// the history as a first element.
			if (handleHistory == null) {
				handleHistory = new Stack<HandleHistoryEntry>();
				handleHistory.push(
						new HandleHistoryEntry(creationVersion, handle));
			}
			items.remove(handle);
			// Change the handles...
			handle = newHandle;
			// ...and insert it again
			items.put(newHandle, this);
			// Now add the new handle to the history.
			handleHistory.push(
					new HandleHistoryEntry(document.historyVersion, newHandle));
			// Mark this item as modified. This will update modificationVersion
			// after the cycle through #updateModified(), which will also adjust
			// the version of the last HistoryEntry.
			setModified();
		}
		if (docHandle != 0)
			document = Document.wrapHandle(docHandle);
		if (clearDictionary) {
			dictionaryHandle = 0;
			dictionaryKey = 0;
		}
		// Update
		version++;
	}

	/**
	 * Call to update the item's modification level
	 */
	protected void setModified() {
		modificationVersion = document.historyVersion;
		// This item's modification date needs updating after the cycle.
		document.addModifiedItem(this);
	}

	/**
	 * Called by document to update items in document.modifiedItems after cycle.
	 */
	protected void updateModified(long version) {
		// Adjust last HistoryEntry as well if it was created in the current
		// cycle.
		if (handleHistory != null) {
			HandleHistoryEntry last = handleHistory.peek();
			if (last.version == modificationVersion) {
				if (Document.reportUndoHistory)
					ScriptographerEngine.logConsole("Updating handleHistory version"
							+ Integer.toString(last.handle, 16) + ", " + last.version);
				last.version = version;
			}
		}
		modificationVersion = version;
	}

	/**
	 * Checks a cached version number against the internal version and decides
	 * if the cached data needs an update. Also takes history levels into
	 * account.
	 */
	protected boolean needsUpdate(int version) {
		// Before checking if this item is valid or needs updates, check handle
		// history, and swap back to previous handles if version went back
		// bellow the last entry on the stack.
		// TODO: Should this be moved to isValid() instead?
		if (handleHistory != null) {
			HandleHistoryEntry last = handleHistory.peek();
			if (document.historyVersion < last.version) {
				handleHistory.pop();
				HandleHistoryEntry previous = handleHistory.peek();
				// Just like in #changeHandle(), remove old handle...
				items.remove(handle);
				if (Document.reportUndoHistory)
					ScriptographerEngine.logConsole("Switching back to handleHistory version for handle "
							+ Integer.toString(handle, 16) + ": "
							+ Integer.toString(previous.handle, 16) + ", " + previous.version);
				// ...change the handle to the previous one...
				handle = previous.handle;
				// ...and add it again.
				items.put(handle, this);
				if (handleHistory.size() <= 1) {
					// We've reached the end of the history,
					// switch back to simple mode.
					handleHistory = null;
				}
			}
		}
		return isValid() && (version != this.version
				|| document.historyVersion < modificationVersion);
	}

	/**
	 * Returns true if this item needs an update regardless of the cached
	 * version, due to history changes.
	 */
	protected boolean needsUpdate() {
		return this.needsUpdate(version);
	}

	/**
	 * Called by native methods through commitIfWrapped if all cached changes
	 * need to be committed before the objects are modified.
	 * 
	 * The version is then increased to invalidate the cached values, as they
	 * were just changed.
	 */
	protected boolean commit(boolean invalidate) {
		boolean committed = CommitManager.commit(this);
		// Increasing version by one causes refetching of cached data:
		if (invalidate)
			version++;
		return committed;
	}

	/**
	 * Called by native methods if all cached changes need to be committed
	 * before the objects are modified.
	 */
	protected static boolean commitIfWrapped(int handle, boolean invalidate) {
		Item item = getIfWrapped(handle);
		if (item != null) 
			return item.commit(invalidate);
		return false;
	}

	private static native boolean nativeRemove(int handle, int docHandle,
			int dictionaryHandle);

	protected boolean remove(boolean removeHandle) {
		if (handle != 0 && (!removeHandle
				|| nativeRemove(handle, document.handle, dictionaryHandle))) {
			// Do not remove from items since undoing can bring them back, and
			// we don't want to loose the undo history tracking ionformation for
			// them: items.remove(handle);
			deletionVersion = document.historyVersion;
			// This item's versions need to be updated after the history cycle
			// is finished.
			document.addRemovedItem(this);
			return true;
		}
		return false;
	}

	/**
	 * Removes the item from the document. If the item has children,
	 * they are also removed.
	 * 
	 * @return {@true if the item was removed}
	 */
	public boolean remove() {
		return remove(true);
	}
	
	/**
	 * Removes all the children items contained within the item.
	 * 
	 * @return {@true if removing was successful}
	 */
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
	 */
	public native Item copyTo(Item item);

	/**
	 * Clones the item within the same document.
	 * 
	 * @return the newly cloned item
	 */
	public Object clone() {
		Item parent = getParent();
		// TODO: parent == null: dictionary item -> return valid parent?
		return parent != null ? copyTo(parent) : copyTo(document);
	}

	/**
	 * The name of the item as it appears in the layers palette.
	 * 
	 * Sample code:
	 * <code>
	 * var layer = new Layer(); // a layer is an item
	 * print(layer.name); // null
	 * layer.name = 'A nice name';
	 * print(layer.name); // 'A nice name'
	 * </code>
	 */
	public native String getName();

	public native void setName(String name);

	/**
	 * The item's position within the art board. This is the
	 * {@link Rectangle#getCenter()} of the {@link Item#getBounds()} rectangle.
	 * 
	 * Sample code: <code>
	 * // Create a circle at position { x: 10, y: 10 }
	 * var circle = new Path.Circle(new Point(10, 10), 10);
	 * 
	 * // Move the circle to { x: 20, y: 20 }
	 * circle.position = new Point(20, 20);
	 * 
	 * // Move the circle 10 points to the right
	 * circle.position += new Point(10, 0);
	 * print(circle.position); // { x: 30, y: 20 }
	 * </code>
	 */
	public native Point getPosition();

	public void setPosition(Point pt) {
		translate(pt.subtract(getPosition()));
	}

	/**
	 * @jshide
	 */
	public void setPosition(double x, double y) {
		setPosition(new Point(x, y));
	}
	
	/**
	 * The path style of the item.
	 * 
	 * Sample code:
	 * <code>
	 * var circle = new Path.Circle(new Point(10, 10), 10);
	 * circle.style = {
	 * 	fillColor: new RGBColor(1, 0, 0),
	 * 	strokeColor: new RGBColor(0, 1, 0),
	 * 	strokeWidth: 5
	 * };
	 * </code>
	 */
	public PathStyle getStyle() {
		if (style == null) {
			style = new PathStyle(this);
		} else {
			style.update();
		}
		return style;
	}

	public void setStyle(PathStyle style) {
		// Make sure it's created and fetched
		getStyle();
		this.style.init(style);
		this.style.markDirty();
	}

	/**
	 * A boolean value that specifies whether the center point of the item is
	 * visible.
	 * 
	 * @jshide
	 */
	public native boolean isCenterVisible();

	/**
	 * @jshide
	 */
	public native void setCenterVisible(boolean centerVisible);

	private native int nativeGetAttributes(int attributes);

	private native void nativeSetAttributes(int attributes, int values);

	/*
	 * This can be used to retrieve all user attributes and restore them again
	 * after an operation that would change them.
	 */
	protected int getAttributes() {
		return nativeGetAttributes(0xffffffff);
	}

	protected void setAttributes(int attributes) {
		// Setting all attributes at once does not seem to work, so loop
		// through attributes and set separately.
		//	nativeSetAttributes(0xffffffff, attributes);
		for (ItemAttribute attribute : ItemAttribute.values())
			nativeSetAttributes(attribute.value, attributes & attribute.value);
	}

	/**
	 * @jshide
	 */
	public boolean getAttribute(ItemAttribute attribute) {
		if (attribute != null)
			return (nativeGetAttributes(attribute.value) & attribute.value) != 0;
		return false;
	}

	/**
	 * @jshide
	 */
	public void setAttribute(ItemAttribute attribute, boolean value) {
		if (attribute != null) {
			if (attribute == ItemAttribute.SELECTED
					|| attribute == ItemAttribute.FULLY_SELECTED)
				document.commitCurrentStyle();
			nativeSetAttributes(attribute.value, value ? attribute.value : 0);
		}
	}

	/**
	 * Specifies whether an item is selected.
	 * 
	 * Sample code:
	 * <code>
	 * print(document.selectedItems.length); // 0
	 * var path = new Path.Circle(new Size(50, 50), 25);
	 * path.selected = true; // Select the path
	 * print(document.selectedItems.length) // 1
	 * </code>
	 * 
	 * @return {true if the item is selected or partially selected (groups with
	 * some selected items/partially selected paths)}
	 */
	public boolean isSelected() {
		return getAttribute(ItemAttribute.SELECTED);
	}

	public void setSelected(boolean selected) {
		setAttribute(ItemAttribute.SELECTED, selected);
	}

	/**
	 * Specifies whether the item is fully selected. For paths this means that
	 * all segments are selected, for container items (groups/layers) all
	 * children are selected.
	 * 
	 * @return {@true if the item is fully selected}
	 */
	public boolean isFullySelected() {
		return getAttribute(ItemAttribute.FULLY_SELECTED);
	}

	public void setFullySelected(boolean selected) {
		setAttribute(ItemAttribute.FULLY_SELECTED, selected);
	}

	/**
	 * Specifies whether the item is locked.
	 * 
	 * Sample code:
	 * <code>
	 * var path = new Path();
	 * print(path.locked) // false
	 * path.locked = true; // Locks the path
	 * </code>
	 * 
	 * @return {@true if the item is locked}
	 */
	public boolean isLocked() {
		return getAttribute(ItemAttribute.LOCKED);
	}

	public void setLocked(boolean locked) {
		setAttribute(ItemAttribute.LOCKED, locked);
	}

	/**
	 * Specifies whether the item is visible.
	 * 
	 * Sample code:
	 * <code>
	 * var path = new Path();
	 * print(path.visible) // true
	 * path.visible = false; // Hides the path
	 * </code>
	 * 
	 * @return {@true if the item is visible}
	 */
	public boolean isVisible() {
		return !getAttribute(ItemAttribute.HIDDEN);
	}

	public void setVisible(boolean visible) {
		setAttribute(ItemAttribute.HIDDEN, !visible);
	}

	/**
	 * Specifies whether the item is hidden.
	 * 
	 * Sample code:
	 * <code>
	 * var path = new Path();
	 * print(path.hidden); // false
	 * path.hidden = true; // Hides the path
	 * </code>
	 * 
	 * @return {@true if the item is hidden}
	 * 
	 * @jshide
	 */
	public final boolean isHidden() {
		return !isVisible();
	}

	/**
	 * @jshide
	 */
	public final void setHidden(boolean hidden) {
		setVisible(!hidden);
	}

	/**
	 * Specifies whether the item defines a clip mask. This can only be set on
	 * paths, compound paths, and text frame objects, and only if the item is
	 * already contained within a clipping group.
	 * 
	 * Sample code:
	 * <code>
	 * var group = new Group();
	 * group.appendChild(path);
	 * group.clipped = true;
	 * path.clipMask = true;
	 * </code>
	 * 
	 * @return {@true if the item defines a clip mask}
	 */
	public boolean isClipMask() {
		return getAttribute(ItemAttribute.CLIP_MASK);
	}

	@SuppressWarnings("deprecation")
	public void setClipMask(boolean clipMask) {
		setAttribute(ItemAttribute.CLIP_MASK, clipMask);
		if (clipMask) {
			PathStyle style = getStyle();
			style.setFillColor(Color.NONE);
			style.setStrokeColor(Color.NONE);
			// We need to reflect the clip mask status in the deprectted clip
			// property too, otherwise the delayed commmit() through the
			// CommitManager would override whatever we set here.
			style.clip = clipMask;
		}
	}

	/**
	 * Specifies whether the item is targeted.
	 * 
	 * Sample code:
	 * <code>
	 * var path = new Path();
	 * print(path.targeted) // false
	 * path.targeted = true; // Marks the the path as targeted
	 * </code>
	 * 
	 * @return {@true if the item is locked}
	 */
	public boolean isTargeted() {
		return getAttribute(ItemAttribute.TARGETED);
	}

	public void setTargeted(boolean targeted) {
		setAttribute(ItemAttribute.TARGETED, targeted);
	}

	private native int nativeGetBlendMode();

	private native void nativeSetBlendMode(int mode);

	/**
	 * The blend mode of the item.
	 * 
	 * Sample code:
	 * <code>
	 * var circle = new Path.Circle(new Point(50, 50), 10);
	 * print(circle.blendMode); // normal
	 * 
	 * // Change the blend mode of the path item:
	 * circle.blendMode = 'multiply';
	 * </code>
	 */
	public BlendMode getBlendMode() {
		return (BlendMode) IntegerEnumUtils.get(BlendMode.class,
				nativeGetBlendMode());
	}

	public void setBlendMode(BlendMode blend) {
		nativeSetBlendMode(blend.value);
	}

	/**
	 * The opacity of the item.
	 * 
	 * Sample code:
	 * <code>
	 * // Create a circle at position { x: 10, y: 10 } 
	 * var circle = new Path.Circle(new Point(10, 10), 10);
	 * 
	 * // Change the opacity of the circle to 50%:
	 * circle.opacity = 0.5;
	 * </code>
	 * 
	 * @return the opacity of the item as a value between 0 and 1.
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
	
	private native int nativeGetData();

	/**
	 * @jshide
	 */
	public LiveEffectPosition getEffectPosition(LiveEffect effect,
			Map<String, Object> parameters) {
		if (effect != null)
			return IntegerEnumUtils.get(LiveEffectPosition.class,
					nativeGetEffectPosition(effect, parameters));
		return null;
	}

	private native int nativeGetEffectPosition(LiveEffect effect,
			Map<String, Object> parameters);

	/**
	 * @jshide
	 */
	public boolean hasEffect(LiveEffect effect, Map<String, Object> parameters) {
		return getEffectPosition(effect, parameters) != null;
	}

	/**
	 * @jshide
	 */
	public boolean addEffect(LiveEffect effect, Map<String, Object> parameters,
			LiveEffectPosition position) {
		if (effect != null) {
			LiveEffectParameters params = parameters != null
			? parameters instanceof LiveEffectParameters
					? (LiveEffectParameters) parameters
					: new LiveEffectParameters(parameters)
				: new LiveEffectParameters();
			// The moment we pass params to nativeAddEffect, Illustrator
			// will take care of the releasing, so set release to false.
			params.release = false;
			return nativeAddEffect(effect, params, position != null
					? position.value : effect.getPosition().value);
		}
		return false;
	}

	/**
	 * @jshide
	 */
	public boolean addEffect(LiveEffect effect, Map<String, Object> parameters) {
		return addEffect(effect, parameters, effect.getPosition());
	}

	/**
	 * @jshide
	 */
	public boolean addEffect(LiveEffect effect) {
		return addEffect(effect, null);
	}

	private native boolean nativeAddEffect(LiveEffect effect,
			LiveEffectParameters data, int position);

	/**
	 * @jshide
	 */
	public native boolean editEffect(LiveEffect effect,
			Map<String, Object> parameters);

	/**
	 * @jshide
	 */
	public native boolean removeEffect(LiveEffect effect,
			Map<String, Object> parameters);

	/**
	 * An object contained within the item which can be used to store data.
	 * The values in this object can be accessed even after the file has been
	 * closed and opened again. Since these values are stored in a native
	 * structure, only a limited amount of value types are supported: Number,
	 * String, Boolean, Item, Point, Matrix.
	 * 
	 * Sample code:
	 * <code>
	 * var path = new Path.Circle(new Point(50, 50), 50);
	 * path.data.point = new Point(50, 50);
	 * print(path.data.point); // {x: 50, y: 50}
	 * </code>
	 */
	public Dictionary getData() {
		// We need to check if existing data references are still valid,
		// as Dictionary.releaseAll() is invalidating them after each
		// history cycle. See Dictionary.releaseAll() for more explanations
		if (data == null || !data.isValid())
			data = Dictionary.wrapHandle(nativeGetData(), document, this);
		return data;	
	}

	public void setData(Map<String, Object> map) {
		Dictionary data = getData();
		if (map != data) {
			data.clear();
			data.putAll(map);
		}
	}

	/**
	 * {@grouptitle Document Hierarchy}
	 * 
	 * The document that the item belongs to.
	 */
	public Document getDocument() {
		// This is only here for the API document.
		// It does exactly the same as the definition in DocumentObject
		return document;
	}

	/**
	 * The item's parent layer, if any.
	 */
	public native Layer getLayer();
	
	/**
	 * The item that this item is contained within.
	 * 
	 * Sample code:
	 * <code>
	 * var path = new Path();
	 * print(path.parent) // Layer (Layer 1)
	 * 
	 * var group = new Group();
	 * group.appendTop(path);
	 * print(path.parent); // Group (@31fbbe00)
	 * </code>
	 */
	public native Item getParent();

	/**
	 * The children items contained within this item.
	 * 
	 * Sample code:
	 * <code>
	 * var group = new Group();
	 * 
	 * // the group doesn't have any children yet
	 * print(group.children.length); // 0
	 * 
	 * var path = new Path();
	 * path.name = 'pathName';
	 * 
	 * // append the path in the group
	 * group.appendTop(path);
	 * 
	 * print(group.children.length); // 1
	 * 
	 * // access children by index:
	 * print(group.children[0]); // Path (pathName)
	 * 
	 * // access children by name:
	 * print(group.children['pathName']); // Path (pathName)
	 * </code>
	 */
	public ItemList getChildren() {
		// Don't implement this in native as the number of items is not known
		// in advance and like this, a java ArrayList can be used:
		// TODO: Cache the result. Invalidate cached version when version
		// changes, or when appendChild / moveAbove / bellow affects this
		// children list.
		ItemList list = new ItemList();
		Item child = getFirstChild();
		while (child != null) {
			list.add(child);
			child = child.getNextSibling();
		}
		return list;
	}

	public void setChildren(ReadOnlyList<Item> children) {
		removeChildren();
		for (Item child : children)
			appendBottom(child);
	}

	public void setChildren(Item[] children) {
		setChildren(Lists.asList(children));
	}

	/**
	 * Reverses the order of this item's children
	 */
	public boolean reverseChildren() {
		boolean changed = false;
		for (Item child : getChildren())
			changed = appendTop(child) | changed;
		return changed;
	}

	/**
	 * The first item contained within this item.
	 */
	public native Item getFirstChild();

	/**
	 * The last item contained within this item.
	 */
	public native Item getLastChild();
	
	/**
	 * The next item on the same level as this item.
	 */
	public native Item getNextSibling();

	/**
	 * The previous item on the same level as this item.
	 */
	public native Item getPreviousSibling();

	/**
	 * The index of this item within the list of it's parent's children.
	 */
	public int getIndex() {
		Item item = getPreviousSibling();
		int i = 0;
		while (item != null) {
			item = item.getPreviousSibling();
			i++;
		}
		return i;
	}
	
	/**
	 * {@grouptitle Bounding Rectangles}
	 * 
	 * The bounding rectangle of the item excluding stroke width.
	 */
	public native Rectangle getBounds();

	/**
	 * @jshide
	 */
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
	}

	public void setBounds(Rectangle rect) {
		setBounds(rect.x, rect.y, rect.width, rect.height);
	}

	/**
	 * The bounding rectangle of the item including stroke width.
	 */
	public native Rectangle getStrokeBounds();

	/**
	 * The bounding rectangle of the item including stroke width and controls.
	 */
	public native Rectangle getControlBounds();

	/*
	 * Stroke Styles
	 */

	/**
	 * @copy PathStyle#getStrokeColor()
	 */
	public Color getStrokeColor() {
		return getStyle().getStrokeColor();
	}

	public void setStrokeColor(Color color) {
		getStyle().setStrokeColor(color);
	}

	public void setStrokeColor(java.awt.Color color) {
		getStyle().setStrokeColor(color);
	}

	/**
	 * @copy PathStyle#getStrokeWidth()
	 */
	public Float getStrokeWidth() {
		return getStyle().getStrokeWidth();
	}

	public void setStrokeWidth(Float width) {
		getStyle().setStrokeWidth(width);
	}

	/**
	 * @copy PathStyle#getStrokeCap()
	 */
	public StrokeCap getStrokeCap() {
		return getStyle().getStrokeCap();
	}

	public void setStrokeCap(StrokeCap cap) {
		getStyle().setStrokeCap(cap);
	}

	/**
	 * @copy PathStyle#getStrokeJoin()
	 */
	public StrokeJoin getStrokeJoin() {
		return getStyle().getStrokeJoin();
	}

	public void setStrokeJoin(StrokeJoin join) {
		getStyle().setStrokeJoin(join);
	}

	/**
	 * @copy PathStyle#getDashOffset()
	 */
	public Float getDashOffset() {
		return getStyle().getDashOffset();
	}

	public void setDashOffset(Float offset) {
		getStyle().setDashOffset(offset);
	}

	/**
	 * @copy PathStyle#getDashArray()
	 */
	public float[] getDashArray() {
		return getStyle().getDashArray();
	}

	public void setDashArray(float[] array) {
		getStyle().setDashArray(array);
	}
	
	/**
	 * @copy PathStyle#getMiterLimit()
	 */
	public Float getMiterLimit() {
		return getStyle().getMiterLimit();
	}

	public void setMiterLimit(Float limit) {
		getStyle().setMiterLimit(limit);
	}

	/**
	 * @copy PathStyle#getStrokeOverprint()
	 */
	public Boolean getStrokeOverprint() {
		return getStyle().getStrokeOverprint();
	}

	public void setStrokeOverprint(Boolean overprint) {
		getStyle().setStrokeOverprint(overprint);
	}

	/*
	 * Fill Style
	 */

	/**
	 * @copy PathStyle#getFillColor()
	 */
	public Color getFillColor() {
		return getStyle().getFillColor();
	}

	public void setFillColor(Color color) {
		getStyle().setFillColor(color);
	}

	public void setFillColor(java.awt.Color color) {
		getStyle().setFillColor(color);
	}

	/**
	 * @copy PathStyle#getFillOverprint()
	 */
	public Boolean getFillOverprint() {
		return getStyle().getFillOverprint();
	}

	public void setFillOverprint(Boolean overprint) {
		getStyle().setFillOverprint(overprint);
	}

	/*
	 * Path Style
	 */
	/**
	 * {@grouptitle Path Style}
	 * 
	 * @copy PathStyle#getWindingRule()
	 */
	public WindingRule getWindingRule() {
		return getStyle().getWindingRule();
	}

	public void setWindingRule(WindingRule rule) {
		getStyle().setWindingRule(rule);
	}

	/**
	 * @copy PathStyle#getResolution()
	 */
	public Float getResolution() {
		return getStyle().getResolution();
	}

	public void setResolution(Float resolution) {
		getStyle().setResolution(resolution);
	}

	/*
	 * End of Style
	 */

	public HitResult hitTest(Point point, HitRequest request, float tolerance) {
		return document.hitTest(point, request, tolerance, this);
	}

	public HitResult hitTest(Point point, HitRequest request) {
		return hitTest(point, request, HitResult.DEFAULT_TOLERANCE);
	}

	public HitResult hitTest(Point point) {
		return hitTest(point, HitRequest.ALL, HitResult.DEFAULT_TOLERANCE);
	}

	public HitResult hitTest(Point point, float tolerance) {
		return hitTest(point, HitRequest.ALL, tolerance);
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
	 *       {@code 'gradient-to-paths'} flag is passed
	 * @return the newly created item containing the expanded artwork
	 */
	public Item expand(EnumSet<ExpandFlag> flags, int steps) {
		return nativeExpand(IntegerEnumUtils.getFlags(flags), steps);
	}

	public Item expand(EnumSet<ExpandFlag> flags) {
		return expand(flags, 0);
	}

	public Item expand(ExpandFlag[] flags, int steps) {
		return expand(EnumSet.copyOf(Arrays.asList(flags)), steps);
	}

	public Item expand(ExpandFlag[] flags) {
		return expand(flags, 0);
	}

	private static int defaultExpandFlags =
		IntegerEnumUtils.getFlags(EnumSet.of(ExpandFlag.PLUGIN_ART,
				ExpandFlag.TEXT, ExpandFlag.STROKE, ExpandFlag.PATTERN,
				ExpandFlag.SYMBOL_INSTANCES));

	/**
	 * Calls {@link #expand(int, int)} with these flags set:
	 * ExpandFlag#PLUGIN_ART, ExpandFlag#TEXT, ExpandFlag#STROKE,
	 * ExpandFlag#PATTERN, ExpandFlag#SYMBOL_INSTANCES
	 * 
	 * @return the newly created item containing the expanded artwork
	 */
	public Item expand() {
		return nativeExpand(defaultExpandFlags, 0);
	}

	private native Raster nativeRasterize(int type, float resolution,
			int antialiasing, float width, float height);

	/**
	 * Rasterizes the item into a newly created Raster object. The item itself
	 * is not removed after rasterization.
	 * 
	 * @param type the color mode of the raster {@default same as document}
	 * @param resolution the resolution of the raster in dpi {@default 72}
	 * @param antialiasing the amount of anti-aliasing {@default 4}
	 * @param width {@default automatic}
	 * @param height {@default automatic}
	 * @return the newly created Raster item
	 */
	public Raster rasterize(ColorType type, float resolution, int antialiasing,
			float width, float height) {
		return nativeRasterize(type != null ? type.value : -1, resolution,
				antialiasing, width, height);
	}

	public Raster rasterize(ColorType type, float resolution, int antialiasing) {
		return rasterize(type, resolution, antialiasing, -1, -1);
	}

	public Raster rasterize(ColorType type, float resolution) {
		return rasterize(type, resolution, 4, -1, -1);
	}
	
	public Raster rasterize(ColorType type) {
		return rasterize(type, 72, 4, -1, -1);
	}
	
	public Raster rasterize() {
		return rasterize(null, 72, 4, -1, -1);
	}

	private static native Raster nativeRasterize(Item[] items, int type,
			float resolution, int antialiasing, float width, float height);

	/**
	 * Rasterizes the passed items into a newly created Raster object. The items
	 * are not removed after rasterization.
	 * 
	 * @param type the color mode of the raster {@default same as document}
	 * @param resolution the resolution of the raster in dpi {@default 72}
	 * @param antialiasing the amount of anti-aliasing {@default 4}
	 * @param width {@default automatic}
	 * @param height {@default automatic}
	 * @return the newly created Raster item
	 */
	public static Raster rasterize(Item[] items, ColorType type,
			float resolution, int antialiasing, float width, float height) {
		return nativeRasterize(items, type != null ? type.value : -1,
				resolution, antialiasing, width, height);
	}

	public static Raster rasterize(Item[] items, ColorType type,
			float resolution, int antialiasing) {
		return rasterize(items, type, resolution, antialiasing, -1, -1);
	}
	
	public static Raster rasterize(Item[] items, ColorType type) {
		return rasterize(items, type, 0, 4, -1, -1);
	}
	
	public static Raster rasterize(Item[] items) {
		return rasterize(items, null, 0, 4, -1, -1);
	}

	private native void nativeDraw(Image image, int width, int height);

	/**
	 * @jshide
	 */
	public void draw(Image image) {
		nativeDraw(image, image.getWidth(), image.getHeight());
	}
	
	/**
	 * {@grouptitle Tests}
	 * 
	 * Checks if the item contains any children items.
	 * 
	 * @return {@true if it has one or more children}
	 */
	public boolean hasChildren() {
		return getFirstChild() != null;
	}

	/**
	 * Checks whether the item is editable.
	 * 
	 * @return {@true when neither the item, nor it's parents are locked or
	 * hidden}
	 */
	public native boolean isEditable();
	
	/**
	 * Checks whether the item is valid, i.e. it hasn't been removed.
	 * 
	 * Sample code: <code>
	 * var path = new Path();
	 * print(path.isValid()); // true
	 * path.remove();
	 * print(path.isValid()); // false
	 * </code>
	 * 
	 * @return {@true if the item is valid}
	 */
	public boolean isValid() {
		if (!Document.trackUndoHistory)
			return Item.isValid(handle);
		boolean valid = handle != 0
				&& document.isValidVersion(creationVersion)
				&& !document.isValidVersion(deletionVersion);
		// Weird stacking of if statements, just so Eclipse does not warn about
		// dead code
		if (Document.reportUndoHistory)
			if (!valid)
				ScriptographerEngine.logConsole(getId() + " is invalid { branch: "
						+ ((creationVersion >> 32) & 0xffffffffl) + ", level: " 
						+ (creationVersion & 0xffffffffl) + " }, isValid: "
						+ Item.isValid(handle));
		return valid;
	}

	protected void checkValid() {
		if (!isValid())
			throw new ScriptographerException("The item is no longer valid, either due to deletion or undoing. Use isValid() checks to avoid this error.");
	}

	private static native boolean[] nativeCheckItems(int[] values,
			int length);

	protected static void checkItems(Document document, long version) {
		ArrayList<SoftReference<Item>> checkItems = document.checkItems;
		if (!checkItems.isEmpty()) {
			int[] values = new int[checkItems.size() * 3];
			// Check all these handles in one go, for increased performance
			// We need to pass dictionaryHandle and key as well, so these
			// art items can be checked for validity differently.
			int j = 0;
			for (int i = 0, l = checkItems.size(); i < l; i++) {
				Item item = checkItems.get(i).get();
				if (item != null) {
					values[j++] = item.handle;
					values[j++] = item.dictionaryHandle;
					values[j++] = item.dictionaryKey;
				}
			}
			boolean[] valid = nativeCheckItems(values, j);
			// Update historyVersion to one that is not valid anymore
			for (int i = valid.length - 1; i >= 0; i--) {
				if (!valid[i]) {
					// Retrieve the item to update through its soft reference.
					Item item = checkItems.get(i).get();
					// Check for null as the soft reference might have been
					// released
					if (item != null) {
						item.creationVersion = version;
						if (Document.reportUndoHistory)
							ScriptographerEngine.logConsole("Marking " + item
									+ " as invalid before version: " + version);
					}
					// Remove it from the list
					checkItems.remove(i);
				}
			}
		}
	}

	/**
	 * {@grouptitle Hierarchy Operations}
	 * 
	 * Inserts the specified item as a child of the item by appending it to the
	 * list of children and moving it above all other children. You can use this
	 * function for groups, compound paths and layers.
	 * 
	 * Sample code: <code>
	 * var group = new Group();
	 * var path = new Path();
	 * group.appendTop(path);
	 * print(path.isDescendant(group)); // true
	 * </code>
	 * 
	 * @param item The item that will be appended as a child
	 */
	public native boolean appendTop(Item item);

	/*
	public boolean appendTop(Item... items) {
		boolean ok = true;
		for (Item item : items) {
			if (!appendTop(item))
				ok = false;
		}
		return ok;
	}
	*/

	/**
	 * Inserts the specified item as a child of this item by appending it to the
	 * list of children and moving it below all other children. You can use this
	 * function for groups, compound paths and layers.
	 * 
	 * Sample code: <code>
	 * var group = new Group();
	 * var path = new Path();
	 * group.appendBottom(path);
	 * print(path.isDescendant(group)); // true
	 * </code>
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
	 * 
	 * Sample code: <code>
	 * var firstPath = new Path();
	 * var secondPath = new Path();
	 * print(firstPath.isAbove(secondPath)); // false
	 * firstPath.moveAbove(secondPath);
	 * print(firstPath.isAbove(secondPath)); // true
	 * </code>
	 * 
	 * @param item The item above which it should be moved
	 * @return true if it was moved, false otherwise
	 */
	public native boolean moveAbove(Item item);

	/**
	 * Moves the item below the specified item.
	 * 
	 * Sample code: <code>
	 * var firstPath = new Path();
	 * var secondPath = new Path();
	 * print(secondPath.isBelow(firstPath)); // false
	 * secondPath.moveBelow(firstPath);
	 * print(secondPath.isBelow(firstPath)); // true
	 * </code>
	 * 
	 * @param item the item below which it should be moved
	 * @return true if it was moved, false otherwise
	 */
	public native boolean moveBelow(Item item);

	/**
	 * {@grouptitle Hierarchy Tests}
	 * 
	 * Checks if this item is above the specified item in the stacking order of
	 * the document.
	 * 
	 * Sample code: <code>
	 * var firstPath = new Path();
	 * var secondPath = new Path();
	 * print(secondPath.isAbove(firstPath)); // true
	 * </code>
	 * 
	 * @param item The item to check against
	 * @return {@true if it is above the specified item}
	 */
	public native boolean isAbove(Item item);

	/**
	 * Checks if the item is below the specified item in the stacking order of
	 * the document.
	 * 
	 * Sample code: <code>
	 * var firstPath = new Path();
	 * var secondPath = new Path();
	 * print(firstPath.isBelow(secondPath)); // true
	 * </code>
	 * 
	 * @param item The item to check against
	 * @return {@true if it is below the specified item}
	 */
	public native boolean isBelow(Item item);

	public boolean isParent(Item item) {
		return getParent() == item;
	}

	public boolean isChild(Item item) {
		return item != null && item.getParent() == this;
	}

	/**
	 * Checks if the item is contained within the specified item.
	 * 
	 * Sample code: <code>
	 * var group = new Group();
	 * var path = new Path();
	 * group.appendTop(path);
	 * print(path.isDescendant(group)); // true
	 * </code>
	 * 
	 * @param item The item to check against
	 * @return {@true if it is inside the specified item}
	 */
	public native boolean isDescendant(Item item);

	/**
	 * Checks if the item is an ancestor of the specified item.
	 * 
	 * Sample code: <code>
	 * var group = new Group();
	 * var path = new Path();
	 * group.appendChild(path);
	 * print(group.isAncestor(path)); // true
	 * print(path.isAncestor(group)); // false
	 * </code>
	 * 
	 * @param item the item to check against
	 * @return {@true if the item is an ancestor of the specified item}
	 */
	public native boolean isAncestor(Item item);

	/**
	 * Checks whether the item is grouped with the specified item.
	 * 
	 * @param item
	 * @return {@true if the items are grouped together}
	 */
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
	
	/**
	 * {@grouptitle Transform Functions}
	 * 
	 * Scales the item by the given values from its center point, or optionally
	 * by a supplied point.
	 * 
	 * @param sx
	 * @param sy
	 * @param center {@default the center point of the item}
	 * 
	 * @see Matrix#scale(double, double, Point center)
	 */
	public void scale(double sx, double sy, Point center) {
		transform(new Matrix().scale(sx, sy, center));
	}

	public void scale(double sx, double sy) {
		scale(sx, sy, getPosition());
	}

	/**
	 * Scales the item by the given value from its center point, or optionally
	 * by a supplied point.
	 * 
	 * Sample code:
	 * <code>
	 * // Create a circle at position { x: 10, y: 10 } 
	 * var circle = new Path.Circle(new Point(10, 10), 10);
	 * print(circle.bounds.width); // 20
	 * 
	 * // Scale the path by 200% around its center point
	 * circle.scale(2);
	 * 
	 * print(circle.bounds.width); // 40
	 * </code>
	 * 
	 * <code>
	 * // Create a circle at position { x: 10, y: 10 } 
	 * var circle = new Path.Circle(new Point(10, 10), 10);
	 * 
	 * // Scale the path 200% from its bottom left corner
	 * circle.scale(2, circle.bounds.bottomLeft);
	 * </code>
	 * 
	 * @param scale the scale factor
	 * @param center {@default the center point of the item}
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
	 * Sample code:
	 * <code>
	 * // Create a circle at position { x: 10, y: 10 } 
	 * var circle = new Path.Circle(new Point(10, 10), 10);
	 * circle.translate(new Point(5, 10));
	 * print(circle.position); // {x: 15, y: 20}
	 * </code>
	 * 
	 * Alternatively you can also add to the {@link #getPosition()} of the item:
	 * <code>
	 * // Create a circle at position { x: 10, y: 10 } 
	 * var circle = new Path.Circle(new Point(10, 10), 10);
	 * circle.position += new Point(5, 10);
	 * print(circle.position); // {x: 15, y: 20}
	 * </code>
	 * 
	 * @param t
	 */
	public void translate(Point t) {
		transform(new Matrix().translate(t));
	}

	/**
	 * Rotates the item by a given angle around the given point.
	 * 
	 * @param angle the rotation angle in radians
	 * @see Matrix#rotate(double, Point)
	 */
	public void rotate(double angle, Point center) {
		transform(new Matrix().rotate(angle, center));
	}

	/**
	 * Rotates the item by a given angle around its center point.
	 * 
	 * @param theta the rotation angle in radians
	 */
	public void rotate(double angle) {
		rotate(angle, getPosition());
	}

	/**
	 * Shears the item with a given amount around its center point.
	 * 
	 * @param shx
	 * @param shy
	 * @see Matrix#shear(double, double)
	 */
	public void shear(double shx, double shy) {
		Point pos = getPosition();
		Matrix matrix = new Matrix();
		matrix.translate(pos.x, pos.y);
		matrix.shear(shx, shy);
		matrix.translate(-pos.x, -pos.y);
		transform(matrix);
	}

	/* Matrix Transform */
	
	private native void nativeTransform(Matrix matrix, int flags);

	/**
	 * @jshide
	 */
	public void transform(Matrix matrix, EnumSet<TransformFlag> flags) {
		nativeTransform(matrix, IntegerEnumUtils.getFlags(flags));
	}

	/**
	 * Transforms the item with custom flags to be set.
	 * 
	 * @param matrix
	 * @param flags
	 */
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

	/* TODO:
	{"equals",			artEquals,				0},
	{"hasEqualPath",	artHasEqualPath,		1},
	{"hasFill",			artHasFill,				0},
	{"hasStroke",		artHasStroke,			0},
	{"isClipping",		artIsClipping,			0},
	*/
	
	/**
	 * @jshide
	 */
	public native int getItemType();

	/**
	 * @jshide
	 */
	public static native int getItemType(Class cls);

	public String toString() {
		if (isValid()) {
			String name = getName();
			if (name != null)
				return getClass().getSimpleName() + " '" + name + "'";
		}
		return super.toString();
	}

	private static ItemList newItems = null;
	
	/**
	 * @jshide
	 */
	public static void collectNewItems() {
		newItems = new ItemList();
	}

	/**
	 * @jshide
	 */
	public static ItemList retreiveNewItems() {
		ItemList items = newItems;
		newItems = null;
		for (int i = items.size() - 1; i >= 0; i--) {
			Item item = items.get(i);
			// Make sure we're not returning any invalid new items.
			if (!item.isValid())
				items.remove(i);
		}
		return items;
	}

	/**
	 * @jshide
	 */
	public static boolean debug(Item item) {
		return item.isValid();
	}

	/*
	 * This is just here for debugging the undo history code. It can be removed
	 * once that works well.
	 */
	protected static native boolean isValid(int handle);
}