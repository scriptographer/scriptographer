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
 * File created on 23.01.2005.
 */

package com.scriptographer.ai;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

import com.scratchdisk.list.Lists;
import com.scratchdisk.list.ReadOnlyList;
import com.scratchdisk.script.ChangeReceiver;
import com.scratchdisk.util.ArrayList;
import com.scratchdisk.util.IntegerEnumUtils;
import com.scratchdisk.util.SoftIntMap;
import com.scriptographer.CommitManager;
import com.scriptographer.ScriptographerEngine;
import com.scriptographer.ScriptographerException;

/**
 * The Document item refers to an Illustrator document.
 * 
 * The currently active document can be accessed through the global {@code
 * document} variable.
 * 
 * An array of all open documents is accessible through the global {@code
 * documents} variable.
 * 
 * @author lehni
 */
public class Document extends NativeObject implements ChangeReceiver {
	/*
	 * These flags are just here to test the undo history code. They
	 * should be removed once that works well.
	 */
	protected static final boolean trackUndoHistory = true;
	protected static final boolean reportUndoHistory = false;

	private LayerList layers = null;
	private DocumentViewList views = null;
	private SymbolList symbols = null;
	private SwatchList swatches = null;
	private ArtboardList artboards = null;
	private Dictionary data = null;
	private Item currentStyleItem = null;

	/**
	 * The current level in the undo history.
	 */
	private int undoLevel;

	/**
	 * The "future" of the undo history, in case the user went back through undos.
	 */
	private int redoLevel;

	protected long historyVersion;

	private int maxHistoryBranch;

	protected HashMap<Long, HistoryBranch> history;

	private HistoryBranch historyBranch;

	/**
	 * Internal list that keeps track of wrapped objects that have no clear
	 * creation level. These need to be checked if they are valid in each undo.
	 */
	protected ArrayList<SoftReference<Item>> checkItems =
			new ArrayList<SoftReference<Item>>();

	private ArrayList<Item> createdItems = new ArrayList<Item>();
	private ArrayList<Item> modifiedItems = new ArrayList<Item>();
	private ArrayList<Item> removedItems = new ArrayList<Item>();

	// Keep track of state changes
	private boolean createdState = false;
	private boolean modifiedState = false;
	private boolean removedState = false;

	protected Document(int handle) {
		super(handle);
		// Initialise history data for this document.
		resetHistory();
	}

	/**
	 * Opens an existing document.
	 * 
	 * Sample code:
	 * <code>
	 * var file = new File('/path/to/poster.ai');
	 * var poster = new Document(file);
	 * </code>
	 * 
	 * @param file the file to read from
	 * @param colorModel the document's desired color model {@default 'cmyk'}
	 * @param dialogStatus how dialogs should be handled {@default 'none'}
	 * @throws FileNotFoundException 
	 */
	public Document(File file, ColorModel colorModel, DialogStatus dialogStatus)
			throws FileNotFoundException {
		this(nativeCreate(file,
				(colorModel != null ? colorModel : ColorModel.CMYK).value,
				(dialogStatus != null ? dialogStatus : DialogStatus.NONE).value));
		if (handle == 0) {
			if (!file.exists())
				throw new FileNotFoundException(
						"Unable to create document from non existing file: "
						+ file);
			throw new ScriptographerException(
					"Unable to create document from file: " + file);
		}
	}

	public Document(File file, ColorModel colorModel)
			throws FileNotFoundException {
		this(file, colorModel, null);
	}

	public Document(File file) throws FileNotFoundException {
		this(file, null, null);
	}

	/**
	 * Creates a new document.
	 * 
	 * Sample code:
	 * <code>
	 * // Create a new document named 'poster'
	 * // with a width of 100pt and a height of 200pt:
	 * var doc = new Document('poster', 100, 200);;
	 * </code>
	 * 
	 * <code>
	 * // Create a document with a CMYK color mode
	 * // and show Illustrator's 'New Document' dialog:
	 * var doc = new Document('poster', 100, 200, 'cmyk', 'on');
	 * </code>
	 * 
	 * @param title the title of the document
	 * @param width the width of the document
	 * @param height the height of the document
	 * @param colorModel the document's desired color model {@default 'cmyk'}
	 * @param dialogStatus how dialogs should be handled {@default 'none'}
	 */
	public Document(String title, float width, float height,
			ColorModel colorModel, DialogStatus dialogStatus) {
		this(nativeCreate(title, width, height, 
				(colorModel != null ? colorModel : ColorModel.CMYK).value,
				(dialogStatus != null ? dialogStatus : DialogStatus.NONE).value));
	}

	public Document(String title, float width, float height,
			ColorModel colorModel) {
		this(title, width, height, colorModel, null);
	}

	public Document(String title, float width, float height) {
		this(title, width, height, null, null);
	}

	private static native int nativeCreate(java.io.File file, int colorModel,
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

	private static native int nativeGetActiveDocumentHandle();
	
	private static native int nativeGetWorkingDocumentHandle();

	/**
	 * @jshide
	 */
	public static Document getActiveDocument() {
		return Document.wrapHandle(nativeGetActiveDocumentHandle());
	}

	/**
	 * @jshide
	 */
	public static Document getWorkingDocument() {
		return Document.wrapHandle(nativeGetWorkingDocumentHandle());
	}

	/*
	 * Undo / Redo History Tracking
	 * 
	 * Illustrator's native handles are not versioned. Through the undo / redo
	 * functionality, an item that we have a handle to might become invalid at a
	 * certain point, or a redo command might make a previously invalid item
	 * valid again.
	 * 
	 * Unfortunately, Illustrator does not give us a way to tie into this system
	 * and to easily know when and if a certain item is valid. In order to solve
	 * this, Scriptographer implements its own undo history tracking system that
	 * internally represents the history in a tree structure, with branches both
	 * representing future possible changes (through redo) and past changes that
	 * are undoable. Going back in history and branching off on a different
	 * branch makes whole future branches invalid.
	 * 
	 * All this is kept track of through Illustrator's facility to find out the
	 * current amount of undo transactions:
	 * sAIUndo->CountTransactions(&undoLevel, &redoLevel);
	 * 
	 * In the code below we tie into Illustrator's internal undo processes
	 * through a row of callbacks: onClosed, onRevert, onSelectionChanged,
	 * onUndo, onRedo, onClear. The native side uses different approaches to get
	 * these notifications. On each of them, the history tree is kept up to date
	 * and checked.
	 * 
	 * At the same time, Scriptographer keeps track of creation, deletion and
	 * modification of items, and marks these with a versioned id at the end of
	 * an undo cycle. These numbers consist of 64bit of information: 32bit for
	 * the branch number, and 32bit for the level within that branch at which
	 * the change happened. These ids then offer an easy and efficient way to
	 * check at any time if an item is currently valid or not.
	 * 
	 * The same mechanism then is also used by the Timer class to know of items
	 * have changed in the meantime and decide how to handle / define the undo
	 * cycle type.
	 */

	private class HistoryBranch {
		long branch; // the branch number
		long level; // the current level within this branch, if it is active
		long start; // the start level of this branch
		long end; // the maximum level available in this branch
		HistoryBranch previous;
		HistoryBranch next;

		HistoryBranch(HistoryBranch previous, long start) {
			// make sure we're not reusing branch numbers that were used for
			// previous branches before, by continuously adding to
			// maxHistoryBranch.
			this.branch = ++maxHistoryBranch;
			this.previous = previous;
			if (previous != null) {
				this.start = start;
				// If a previous "future" branch is cleared, remove it from the
				// history.
				if (previous.next != null)
					history.remove(previous.next.branch);
				previous.next = this;
			} else {
				start = 0;
			}
		}

		long getVersion(long level) {
			return (branch << 32) | level;
		}

		public String toString() {
			return "{ branch: " + branch + ", level: " + level
					+ ", start: " + start + ", end: " + end + " }";
		}
	};

	private void resetHistory() {
		undoLevel = -1;
		redoLevel = -1;
		historyVersion = 0;
		maxHistoryBranch = -1;
		historyBranch = new HistoryBranch(null, 0);
		history = new HashMap<Long, HistoryBranch>();
		history.put(historyBranch.branch, historyBranch);
	}

	private void setHistoryLevels(int undoLevel, int redoLevel,
			boolean checkLevel) {
		if (undoLevel != this.undoLevel || redoLevel != this.redoLevel) {
			boolean updateItems = false;
			if (checkLevel && undoLevel > this.undoLevel) {
				// A new history cycle was completed.
				if (this.redoLevel > redoLevel) {
					// A previous branch was broken off by going back in the
					// undo history first and then starting a new branch.
					// Store the level of the current history branch first.
					historyBranch.level = this.undoLevel;
					// Create a new branch
					historyBranch = new HistoryBranch(historyBranch, undoLevel);
					history.put(historyBranch.branch, historyBranch);
				}
				// Update the current historyEntry's future to the new level
				// This is the maximum possible level for a branch
				historyBranch.end = undoLevel;
				// Update newly created and modified items, after new
				// historyVersion is set.
				updateItems = true;
			}
			// Set new history version, to be used by items for setting of
			// creation / modification version and execution of checks.
			historyVersion = historyBranch.getVersion(undoLevel);
			if (updateItems) {
				// Scan through newly created, modified and deleted items and
				// update versions. We cannot set this while they are created or
				// modified, since that will still be during the old
				// history cycle, with the old version, and anticipating
				// the new version would break isValid and needsUpdate calls
				// during that cycle.
				if (!createdItems.isEmpty()) {
					for (Item item : createdItems) {
						item.creationVersion = historyVersion;
						item.modificationVersion = historyVersion;
					}
					createdItems.clear();
				}
				if (!modifiedItems.isEmpty()) {
					for (Item item : modifiedItems)
						item.updateModified(historyVersion);
					modifiedItems.clear();
				}
				if (!removedItems.isEmpty()) {
					for (Item item : removedItems)
						item.deletionVersion = historyVersion;
					removedItems.clear();
				}
			}
			this.undoLevel = undoLevel;
			this.redoLevel = redoLevel;
			// Update the current historyEntry level to the current level
			historyBranch.level = undoLevel;
			if (reportUndoHistory)
				ScriptographerEngine.logConsole("undoLevel = " + undoLevel
						+ ", redoLevel = " + redoLevel
						+ ", current = " + historyBranch
						+ ", previous = " + historyBranch.previous
						+ ", version = " + historyVersion);
		}
	}

	protected boolean isValidVersion(long version) {
		// We first need to check that document handle is not 0, because if it
		// is all items inside are invalid. handle is set to 0 in onClosed().
		if (handle == 0)
			return false;
		if (version == -1 || !trackUndoHistory)
			return true;
		// Branch = upper 32 bits
		long branch = (version >> 32) & 0xffffffffl;
		// First see if this branch is still around
		HistoryBranch entry = history.get(branch);
		if (entry != null) {
			// Level = lower 32 bits
			long level = version & 0xffffffffl;
			// See if the item is valid by comparing levels. If it is above
			// the current branch level, it will only be valid in the future,
			// if the user would go back there through redos.
			// But most of all the main undoLevel needs to be matched, as
			// otherwise we would also validate objects in future branches
			boolean validLevel = level <= undoLevel
					&& level <= entry.level && level >= entry.start;
			return validLevel;
		}
		return false;
	}

	/*
	 * Methods to be called by the Item class to keep track of created, modified
	 * and removed items, and to mark them accordingly at the end of the current
	 * undo cycle. 
	 */
	protected void addCreatedItem(Item item) {
		createdItems.add(item);
		createdState = true;
	}

	protected void addModifiedItem(Item item) {
		modifiedItems.add(item);
		modifiedState = true;
	}

	protected void addRemovedItem(Item item) {
		removedItems.add(item);
		removedState = true;
	}

	/*
	 * Methods to access the current internal state since the last time it was
	 * accessed, used by the Timer class to decide how to define the undo cycle
	 * type.
	 */
	protected boolean hasCreatedState() {
		return createdState;
	}

	protected boolean hasModifiedState() {
		return modifiedState;
	}

	protected boolean hasRemovedState() {
		return removedState;
	}

	protected boolean hasChangedSates() {
		return createdState || modifiedState || removedState;
	}

	protected void clearChangedStates() {
		createdState = false;
		modifiedState = false;
		removedState = false;
	}

	/*
	 * Undo History Tracking related callbacks
	 */

	protected void onClosed() {
		// Since AI reused document handles, we have to manually remove wrappers
		// when documents get closed. This happens through a
		// kDocumentClosedNotifier on the native side.
		documents.remove(handle);
		handle = 0;
		// Closing this document has invalidated depending Dictionaries.
		// We need to call releaseInvalid now before the invalid dictionaries
		// would cause crashes when released next time this method is called.
		Dictionary.releaseInvalid();
	}

	protected void onRevert() {
		if (reportUndoHistory)
			ScriptographerEngine.logConsole("Revert");
		resetHistory();
		Item.checkItems(this, Long.MAX_VALUE);
	}

	/**
	 * Called from the native environment.
	 */
	protected void onSelectionChanged(int[] artHandles, int undoLevel,
			int redoLevel) {
		if (artHandles != null)
			Item.updateIfWrapped(artHandles);
		// TODO: Look into making CommitManager.version document dependent?
		CommitManager.version++;
		if (trackUndoHistory) {
			setHistoryLevels(undoLevel, redoLevel, true);
		}
	}
	
	protected void onUndo(int undoLevel, int redoLevel) {
		if (reportUndoHistory)
			ScriptographerEngine.logConsole("Undo");
		if (trackUndoHistory) {
			// Check if we were going back to a previous branch, and if so,
			// switch back.
			if (historyBranch.previous != null
					&& undoLevel <= historyBranch.previous.level)
				historyBranch = historyBranch.previous;
	
			long previousVersion = historyVersion;
	
			// Set levels. This also sets historyVersion correctly
			setHistoryLevels(undoLevel, redoLevel, false);

			// Scan through all the wrappers without a defined creationLevel and
			// set it to the previous historyVersion if they are not valid
			// anymore. Do this after the new historyLevel is set, as this
			// updates handles form the handleHistory in modified items.
			Item.checkItems(this, previousVersion);
		}
	}

	protected void onRedo(int undoLevel, int redoLevel) {
		if (reportUndoHistory)
			ScriptographerEngine.logConsole("Redo");
		if (trackUndoHistory) {
			// Check if we were going forward to a "future" branch, and if so,
			// switch again.
			if (historyBranch.next != null && undoLevel > historyBranch.end) {
				if (reportUndoHistory)
					ScriptographerEngine.logConsole("Back to the future: "
							+ historyBranch.next);
				historyBranch = historyBranch.next;
			}
			setHistoryLevels(undoLevel, redoLevel, false);
		}
	}

	protected void onClear(int[] artHandles) {
		if (reportUndoHistory)
			ScriptographerEngine.logConsole("Clear");
		if (artHandles != null)
			Item.removeIfWrapped(artHandles, false);
	}

	private static native void nativeBeginExecution(int[] returnValues);

	/**
	 * Called before AI functions are executed.
	 * 
	 * @return The current undo level
	 * 
	 * @jshide
	 */
	public static void beginExecution() {
		// Use an array as a simple way to receive values back from the native
		// side.
		int[] returnValues = new int[3]; // docHandle, undoLevel, redoLevel
		nativeBeginExecution(returnValues);
		Document document = wrapHandle(returnValues[0]);
		if (document != null) {
			document.setHistoryLevels(returnValues[1], returnValues[2], true);
		}
	}

	/**
	 * Called after AI functions are executed
	 * 
	 * @jshide
	 */
	public static native void endExecution();

	/*
	 * Undo Cycle manipulation
	 */

	// AIUndoContextKind
	protected static final int
		/** 
		 * A standard context results in the addition of a new transaction which
		 * can be undone/redone by the user.
		 */
		UNDO_STANDARD = 0,
		/** 
		 * A silent context does not cause redos to be discarded and is skipped
		 * over when undoing and redoing. An example is a selection change.
		 */
		UNDO_SILENT = 1,
		/** 
		 * An appended context is like a standard context, except that it is
		 * combined with the preceding transaction. It does not appear as a
		 * separate transaction. Used, for example, to collect sequential
		 * changes to the color of an object into a	single undo/redo transaction.
		 */
		UNDO_MERGE = 2;

	protected native void setUndoType(int ype);

	/*
	 * Normal document methods
	 */
	
	/**
	 * Activates this document, so all newly created items will be placed
	 * in it.
	 * 
	 * @param focus When set to true, the document window is brought to the
	 *        front, otherwise the window sequence remains the same.
	 * @param forCreation if set to true, the internal pointer gActiveDoc will
	 *        not be modified, but gCreationDoc will be set, which then is only
	 *        used once in the next call to Document_activate() (native stuff).
	 */
	protected void activate(boolean focus, boolean forCreation) {
		nativeActivate(focus, forCreation);
		if (forCreation)
			commitCurrentStyle();
	}

	private native void nativeActivate(boolean focus, boolean forCreation);

	/**
	 * Activates this document, so all newly created items will be placed
	 * in it.
	 * 
	 * @param focus When set to {@code true}, the document window is
	 *        brought to the front, otherwise the window sequence remains the
	 *        same. Default is {@code true}.
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
	
	/**
	 * Checks whether the document contains any selected items.
	 * 
	 * @return {@code true} if the document contains selected items,
	 *         false otherwise.
	 * 
	 * @jshide
	 */
	public native boolean hasSelectedItems();

	/**
	 * The selected items contained within the document.
	 */
	public native ItemList getSelectedItems();


	protected native ItemList getMatchingItems(Class type, int whichAttributes,
			int attributes);

	/**
	 * Returns all items that match a set of attributes, as specified by the
	 * passed map. For each of the keys in the map, the demanded value can
	 * either be true or false.
	 * 
	 * Sample code: <code>
	 * // All selected paths and rasters contained in the document.
	 * var selectedItems = document.getItems({ 
	 *     type: [Path, Raster], 
	 *     selected: true
	 * });
	 * 
	 * // All visible Paths contained in the document.
	 * var visibleItems = document.getItems({
	 *     type: Path,
	 *     hidden: false
	 * });
	 * </code>
	 * 
	 * @param attributes an object containing the various attributes to check
	 *        for.
	 */
	public ItemList getItems(ItemAttributes attributes) {
		return attributes != null ? attributes.getItems(this) : null;
	}
	
	/**
	 * @jshide
	 */
	public ItemList getItems(Class type) {
		ItemAttributes attributes = new ItemAttributes();
		attributes.setType(type);
		return getItems(attributes);
	}

	/**
	 * @jshide
	 */
	public ItemList getItems(Class[] types) {
		ItemAttributes attributes = new ItemAttributes();
		attributes.setType(types);
		return getItems(attributes);
	}

	/**
	 * @deprecated
	 */
	public ItemList getItems(Class[] types, ItemAttributes attributes) {
		if (attributes != null) {
			if (types != null)
				attributes.setType(types);
			return attributes.getItems(this);
		}
		return null;
	}

	/**
	 * @deprecated
	 */
	public ItemList getItems(Class type, ItemAttributes attributes) {
		if (attributes != null) {
			if (type != null)
				attributes.setType(type);
			return attributes.getItems(this);
		}
		return null;
	}

	/**
	 * Returns the selected items that are instances of one of the passed classes.
	 * 
	 * Sample code:
	 * <code>
	 * // Get all selected groups and paths:
	 * var items = document.getSelectedItems([Group, Path]);
	 * </code>
	 * 
	 * @param types
	 * 
	 * @deprecated
	 */
	public ItemList getSelectedItems(Class[] types) {
		if (types == null) {
			return getSelectedItems();
		} else {
			ItemAttributes attributes = new ItemAttributes();
			attributes.setSelected(true);
			return getItems(types, attributes);
		}
	}

	/**
	 * Returns the selected items that are an instance of the passed class.
	 * 
	 * Sample code:
	 * <code>
	 * // Get all selected rasters:
	 * var items = document.getSelectedItems(Raster);
	 * </code>
	 * 
	 * @param types
	 * 
	 * @deprecated
	 */
	public ItemList getSelectedItems(Class type) {
		return getSelectedItems(new Class[] { type });
	}

	private Item getCurrentStyleItem() {
		// This is a bit of a hack: We use a special handle HANDLE_CURRENT_STYLE
		// to tell the native side that this is in fact the current style, not
		// an item handle...
		if (currentStyleItem == null)
			currentStyleItem = new Item(Item.HANDLE_CURRENT_STYLE, this, false,
					true);
		// Update version so style gets refetched from native side.
		currentStyleItem.version = CommitManager.version;
		return currentStyleItem;
	}

	/**
	 * The currently active Illustrator path style. All selected items and newly
	 * created items will be styled with this style.
	 */
	public PathStyle getCurrentStyle() {
		return getCurrentStyleItem().getStyle();
	}

	public void setCurrentStyle(PathStyle style) {
		getCurrentStyleItem().setStyle(style);
	}

	protected void commitCurrentStyle() {
		// Make sure style change gets committed before selection changes,
		// since it affects the selection.
		if (currentStyleItem != null)
			CommitManager.commit(currentStyleItem);
	}
	
	/**
	 * The point of the lower left corner of the imageable page, relative to the
	 * ruler origin.
	 */
	public native Point getPageOrigin();
	
	public native void setPageOrigin(Point pt);

	/**
	 * The point of the ruler origin of the document, relative to the bottom
	 * left of the artboard.
	 */
	public native Point getRulerOrigin();
	
	public native void setRulerOrigin(Point pt);

	/**
	 * The size of the document.
	 * Setting size only works while reading a document!
	 */
	public native Size getSize();

	/**
	 * @jshide
	 */
	public native void setSize(double width, double height);
	
	public void setSize(Size size) {
		if (size != null)
			setSize(size.width, size.height);
	}

	public Rectangle getBounds() {
		return new Rectangle(getRulerOrigin().negate(), getSize());
	}

	public void setBounds(Rectangle bounds) {
		if (bounds != null) {
			setRulerOrigin(bounds.getPoint().negate());
			setSize(bounds.getSize());
		}
	}

	private native int nativeGetColormodel();
	private native void nativeSetColormodel(int model);

	public ColorModel getColorModel() {
		return IntegerEnumUtils.get(ColorModel.class, nativeGetColormodel());
	}

	public void setColorModel(ColorModel model) {
		if (model != null)
			nativeSetColormodel(model.value);
	}

	/**
	 * Specifies if the document has been edited since it was last saved. When
	 * set to {@code true}, closing the document will present the user
	 * with a dialog box asking to save the file.
	 */
	public native boolean isModified();
	
	public native void setModified(boolean modified);

	/**
	 * The file associated with the document.
	 */
	public native File getFile();

	private native int nativeGetFileFormat();

	private native void nativeSetFileFormat(int handle);
	
	public FileFormat getFileFormat() {
		return FileFormat.getFormat(nativeGetFileFormat());
	}

	public void setFileFormat(FileFormat format) {
		nativeSetFileFormat(format != null ? format.handle : 0);
	}
	
	private native int nativeGetData();

	/**
	 * An object contained within the document which can be used to store data.
	 * The values in this object can be accessed even after the file has been
	 * closed and opened again. Since these values are stored in a native
	 * structure, only a limited amount of value types are supported: Number,
	 * String, Boolean, Item, Point, Matrix.
	 * 
	 * Sample code:
	 * <code>
	 * document.data.point = new Point(50, 50);
	 * print(document.data.point); // {x: 50, y: 50}
	 * </code>
	 * 
	 */
	public Dictionary getData() {
		// We need to check if existing data references are still valid,
		// as Dictionary.releaseAll() is invalidating them after each
		// history cycle. See Dictionary.releaseAll() for more explanations
		if (data == null || !data.isValid())
			data = Dictionary.wrapHandle(nativeGetData(), this, this);
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
	 * The layers contained within the document.
	 * 
	 * Sample code:
	 * <code>
	 *  // When you create a new Document it always contains
	 *  // a layer called 'Layer 1'
	 *  print(document.layers); // Layer (Layer 1)
	 *
	 *  // Create a new layer called 'test' in the document
	 *  var newLayer = new Layer();
	 *  newLayer.name = 'test';
	 *
	 *  print(document.layers); // Layer (test), Layer (Layer 1)
	 *  print(document.layers[0]); // Layer (test)
	 *  print(document.layers.test); // Layer (test)
	 *  print(document.layers['Layer 1']); // Layer (Layer 1)
	 * </code>
	 */
	public LayerList getLayers() {
		if (layers == null)
			layers = new LayerList(this);
		return layers;
	}

	/**
	 * The layer which is currently active. The active layer is indicated in the
	 * Layers palette by a black triangle. New items will be created on this
	 * layer by default.
	 * @return The layer which is currently active
	 */
	public native Layer getActiveLayer();
	
	/**
	 * The symbols contained within the document.
	 */
	public SymbolList getSymbols() {
		if (symbols == null)
			symbols = new SymbolList(this);
		return symbols;
	}

	private native int getActiveSymbolHandle(); 

	/**
	 * The symbol which is selected in the Symbols menu.
	 */
	public Symbol getActiveSymbol() {
		return (Symbol) Symbol.wrapHandle(getActiveSymbolHandle(), this);
	}

	/**
	 * The swatches contained within the document.
	 * 
	 * Sample code:
	 * <code>
	 * var firstSwatch = document.swatches[0];
	 * var namedSwatch = document.swatches['CMYK Blue'];
	 * </code>
	 */
	public SwatchList getSwatches() {
		if (swatches == null)
			swatches = new SwatchList(this);
		return swatches;
	}

	/**
	 * The artboards contained in the document.
	 */
	public ArtboardList getArtboards() {
		if (artboards == null)
			artboards = new ArtboardList(this);
		else
			artboards.update();
		return artboards;
	}

	public void setArtboards(ReadOnlyList<Artboard> boards) {
		ArtboardList artboards = getArtboards();
		for (int i = 0, l = boards.size(); i < l; i++)
			artboards.set(i, boards.get(i));
		artboards.setSize(boards.size());
	}

	public void setArtboards(Artboard[] boards) {
		setArtboards(Lists.asList(boards));
	}

	private native int getActiveArtboardIndex();

	private native void setActiveArtboardIndex(int index);

	public Artboard getActiveArtboard() {
		return getArtboards().get(getActiveArtboardIndex());
	}

	public void setActiveArtboard(Artboard board) {
		setActiveArtboardIndex(board.getIndex());
	}

	/**
	 * The document views contained within the document.
	 */
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

	/**
	 * The document view which is currently active.
	 */
	public DocumentView getActiveView() {
		return DocumentView.wrapHandle(getActiveViewHandle(), this);
	}
	
	// TODO: getActiveSwatch, getActiveGradient

	private TextStoryList stories = null;
	
	/**
	 * The stories contained within the document.
	 */
	public TextStoryList getStories() {
		// See getStories(int storyHandle, boolean dispose) for explanations:
		ItemList items = getItems(TextItem.class);
		TextItem item = items.size() > 0 ? (TextItem) items.getFirst() : null;
		return getStories(item, true);
	}

	protected TextStoryList getStories(TextStoryProvider storyProvider,
			boolean release) {
		// We need to have a storyHandle to fetch the document's stories from.
		// We could use document.getItems() to get one, but there are situations
		// where this code seems to not work, e.g. when a text item was just
		// removed from the document (but is still valid during the cycle and
		// could be introduced in the DOM again)
		// So let's be on the save side when directly working with existing
		// items and always provide the context.
		// Also we need to version TextStoryLists, since document handles seem
		// to not be unique:
		// When there is only one document, closing it and opening a new one
		// results in the same document handle. Versioning seems the only way to
		// keep story lists updated.
		if (stories == null || stories.version != CommitManager.version) {
			int handle = storyProvider != null
					? nativeGetStories(storyProvider.getStoryHandle(), release)
					: 0;
			if (stories == null)
				stories = new TextStoryList(handle, this);
			else
				stories.changeHandle(handle);
		}
		return stories;
	}

	private native int nativeGetStories(int storyHandle, boolean release);

	/**
	 * Prints the document.
	 * 
	 * @param status
	 */
	public void print(DialogStatus status) {
		nativePrint(status.value);
	}

	public void print() {
		print(DialogStatus.OFF);
	}

	private native void nativePrint(int status);

	/**
	 * Saves the document.
	 */
	public native void save();
	
	/**
	 * Closes the document.
	 */
	public native void close();
	
	/**
	 * Forces the document to be redrawn.
	 */
	public native void redraw();

	public native void undo();

	public native void redo();

	/**
	 * Places a file in the document.
	 * 
	 * Sample code:
	 * <code>
	 * var file = new File('/path/to/image.jpg');
	 * var item = document.place(file);
	 * </code>
	 * 
	 * @param file the file to place
	 * @param linked when set to {@code true}, the placed object is a
	 *        link to the file, otherwise it is embedded within the document
	 */
	public native Item place(File file, boolean linked);
	
	public Item place(File file) {
		return place(file, true);
	}

	/**
	 * @jshide
	 */
	public native void invalidate(float x, float y, float width, float height);
	
	/**
	 * Invalidates the rectangle in artwork coordinates. This will cause all
	 * views of the document that contain the given rectangle to update at the
	 * next opportunity.
	 */
	public void invalidate(Rectangle rect) {
		invalidate((float) rect.x, (float) rect.y, (float) rect.width, 
				(float) rect.height);
	}

	private native boolean nativeWrite(File file, int formatHandle, boolean ask);

	/**
	 * @jshide
	 */
	public boolean write(File file, FileFormat format, boolean ask) {
		if (format == null) {
			// Try to get format by extension
			String name = file.getName();
			int pos = name.lastIndexOf('.');
			format = FileFormatList.getInstance().get(name.substring(pos + 1));
			if (format == null)
				format = this.getFileFormat();
		}
		return nativeWrite(file, format != null ? format.handle : 0, ask);
	}

	/**
	 * @jshide
	 */
	public boolean write(File file, FileFormat format) {
		return write(file, format, false);
	}

	/**
	 * @jshide
	 */
	public boolean write(File file, String format, boolean ask) {
		return write(file, FileFormatList.getInstance().get(format), ask);
	}

	/**
	 * @jshide
	 */
	public boolean write(File file, String format) {
		return write(file, format, false);
	}

	public boolean write(File file, boolean ask) {
		return write(file, (FileFormat) null, ask);
	}

	public boolean write(File file) {
		return write(file, false);
	}

	/**
	 * The selected text as a text range.
	 * 
	 * Sample code:
	 * <code>
	 * var range = document.selectedTextRange;
	 * 
	 * // Check if there is a selected range:
	 * if(range) {
	 * 	range.characterStyle.fontSize += 15;
	 * }
	 * </code>
	 */
	public native TextRange getSelectedTextRange();

	private native void nativeSelectAll();

	/**
	 * Selects all items in the document.
	 */
	public void selectAll() {
		commitCurrentStyle();
		nativeSelectAll();
	}

	private native void nativeDeselectAll();

	/**
	 * Deselects all selected items in the document.
	 */
	public void deselectAll() {
		commitCurrentStyle();
		nativeDeselectAll();
	}

	/* TODO: make these
	public Item getInsertionItem();
	public int getInsertionOrder();
	public boolean isInsertionEditable();
	*/

	private Path createPath() {
		activate(false, true);
		return new Path();
	}

	/**
	 * Creates a Path Item with two anchor points forming a line.
	 * 
	 * Sample code:
	 * <code>
	 * var path = new Path.Line(new Point(20, 20, new Point(100, 100));
	 * </code>
	 * 
	 * @param pt1 the first anchor point of the path
	 * @param pt2 the second anchor point of the path
	 * @return the newly created path
	 * 
	 * @jshide
	 */
	public Path createLine(Point pt1, Point pt2) {
		Path path = this.createPath();
		path.moveTo(pt1);
		path.lineTo(pt2);
		return path;
	}

	/**
	 * Creates a Path Item with two anchor points forming a line.
	 * 
	 * Sample code:
	 * <code>
	 * var path = new Path.Line(20, 20, 100, 100);
	 * </code>
	 * 
	 * @param x1 the x position of the first point
	 * @param y1 the y position of the first point
	 * @param x2 the x position of the second point
	 * @param y2 the y position of the second point
	 * @return the newly created path
	 * 
	 * @jshide
	 */
	public Path createLine(double x1, double y1, double x2, double y2) {
		return createLine(new Point(x1, y1), new Point(x2, y2));
	}

	private native Path nativeCreateRectangle(Rectangle rect);

	/**
	 * Creates a rectangular shaped Path Item.
	 * 
	 * Sample code:
	 * <code>
	 * var rectangle = new Rectangle(new Point(100, 100), new Size(100, 100));
	 * var path = new Path.Rectangle(rectangle);
	 * </code>
	 * 
	 * @param rect
	 * @return the newly created path
	 * 
	 * @jshide
	 */
	public Path createRectangle(Rectangle rect) {
		activate(false, true);
		return nativeCreateRectangle(rect);
	}

	/**
	 * Creates a rectangular shaped Path Item.
	 * 
	 * Sample code:
	 * <code>
	 * var path = new Path.Rectangle(100, 100, 10, 10);
	 * </code>
	 * 
	 * @jshide
	 */
	public Path createRectangle(double x, double y, double width, double height) {
		return createRectangle(new Rectangle(x, y, width, height));
	}

	/**
	 * Creates a rectangle shaped Path Item.
	 * 
	 * Sample code:
	 * <code>
	 * var path = new Path.Rectangle(new Point(100, 100), new Size(10, 10));
	 * </code>
	 * 
	 * @param point the bottom left point of the rectangle
	 * @param size the size of the rectangle
	 * @return the newly created path
	 * 
	 * @jshide
	 */
	public Path createRectangle(Point point, Size size) {
		return createRectangle(new Rectangle(point, size));
	}

	private native Path nativeCreateRoundRectangle(Rectangle rect, Size size);

	/**
	 * Creates a rectangular Path Item with rounded corners.
	 * 
	 * Sample code:
	 * <code>
	 * var rectangle = new Rectangle(new Point(100, 100), new Size(100, 100));
	 * var path = new Path.RoundRectangle(rectangle, new Size(30, 30));
	 * </code>
	 * 
	 * @param rect
	 * @param size the size of the rounded corners
	 * @return the newly created path
	 * 
	 * @jshide
	 */
	public Path createRoundRectangle(Rectangle rect, Size size) {
		activate(false, true);
		return nativeCreateRoundRectangle(rect, size);
	}

	/**
	 * Creates a rectangular Path Item with rounded corners.
	 * 
	 * Sample code:
	 * <code>
	 * var path = new Path.RoundRectangle(50, 50, 100, 100, 30, 30);
	 * </code>
	 * 
	 * @param x the left position of the rectangle
	 * @param y the bottom position of the rectangle
	 * @param width the width of the rectangle
	 * @param height the height of the rectangle
	 * @param hor the horizontal size of the rounder corners
	 * @param ver the vertical size of the rounded corners
	 * @return the newly created path
	 * 
	 * @jshide
	 */
	public Path createRoundRectangle(double x, double y, double width,
			double height, float hor, float ver) {
		return createRoundRectangle(new Rectangle(x, y, width, height),
				new Size(hor, ver));
	}

	private native Path nativeCreateOval(Rectangle rect, boolean circumscribed);

	/**
	 * Creates an oval shaped Path Item.
	 * 
	 * Sample code:
	 * <code>
	 * var rectangle = new Rectangle(new Point(100, 100), new Size(150, 100));
	 * var path = new Path.Oval(rectangle);
	 * </code>
	 * 
	 * @param rect
	 * @param circumscribed if this is set to true the oval shaped path will be
	 *        created so the rectangle fits into it. If it's set to false the
	 *        oval path will fit within the rectangle. {@default false}
	 * @return the newly created path
	 * 
	 * @jshide
	 */
	public Path createOval(Rectangle rect, boolean circumscribed) {
		activate(false, true);
		return nativeCreateOval(rect, circumscribed);
	}

	/**
	 * @jshide
	 */
	public Path createOval(Rectangle rect) {
		return createOval(rect, false);
	}

	/**
	 * Creates an oval shaped Path Item.
	 * 
	 * Sample code:
	 * <code>
	 * var rectangle = new Rectangle(100, 100, 150, 100);
	 * var path = new Path.Oval(rectangle);
	 * </code>
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param circumscribed if this is set to true the oval shaped path will be
	 *        created so the rectangle fits into it. If it's set to false the
	 *        oval path will fit within the rectangle. {@default false}
	 * @return the newly created path
	 * 
	 * @jshide
	 */
	public Path createOval(double x, double y, double width, double height,
			boolean circumscribed) {
		return createOval(new Rectangle(x, y, width, height), circumscribed);
	}

	/**
	 * @jshide
	 */
	public Path createOval(double x, double y, double width, double height) {
		return createOval(x, y, width, height);
	}

	/**
	 * Creates a circle shaped Path Item.
	 * 
	 * Sample code:
	 * <code>
	 * var path = new Path.Circle(new Point(100, 100), 50);
	 * </code>
	 * 
	 * @param center the center point of the circle
	 * @param radius the radius of the circle
	 * @return the newly created path
	 * 
	 * @jshide
	 */
	public Path createCircle(Point center, float radius) {
		return createOval(new Rectangle(center.subtract(radius, radius), center
				.add(radius, radius)));
	}

	/**
	 * Creates a circle shaped Path Item.
	 * 
	 * Sample code:
	 * 
	 * <code>
	 * var path = new Path.Circle(100, 100, 50);
	 * </code>
	 * 
	 * @param x the horizontal center position of the circle
	 * @param y the vertical center position of the circle
	 * @param radius the radius of the circle
	 * @return the newly created path
	 * 
	 * @jshide
	 */
	public Path createCircle(float x, float y, float radius) {
		return createCircle(new Point(x, y), radius);
	}

	private native Path nativeCreateRegularPolygon(Point center, int numSides,
			float radius);

	/**
	 * Creates a regular polygon shaped Path Item.
	 * 
	 * Sample code:
	 * <code>
	 * // Create a triangle shaped path
	 * var triangle = new Path.RegularPolygon(new Point(100, 100), 3, 50);
	 * 
	 * // Create a decahedron shaped path
	 * var decahedron = new Path.RegularPolygon(new Point(200, 100), 10, 50);
	 * </code>
	 * 
	 * @param center the center point of the polygon
	 * @param numSides the number of sides of the polygon
	 * @param radius the radius of the polygon
	 * @return the newly created path
	 * 
	 * @jshide
	 */
	public Path createRegularPolygon(Point center, int numSides, float radius) {
		activate(false, true);
		return nativeCreateRegularPolygon(center, numSides, radius);
	}

	private native Path nativeCreateStar(Point center, int numPoints,
			float radius1, float radius2);

	/**
	 * Creates a star shaped Path Item.
	 * 
	 * The largest of {@code radius1} and {@code radius2} will be the outer
	 * radius of the star. The smallest of radius1 and radius2 will be the inner
	 * radius.
	 * 
	 * Sample code:
	 * <code>
	 * var center = new Point(100, 100);
	 * var points = 6;
	 * var innerRadius = 20;
	 * var outerRadius = 50;
	 * var path = new Path.Star(center, points, innerRadius, outerRadius);
	 * </code>
	 * 
	 * @param center the center point of the star
	 * @param numPoints the number of points of the star
	 * @param radius1
	 * @param radius2
	 * @return the newly created path
	 * 
	 * @jshide
	 */
	public Path createStar(Point center, int numPoints, float radius1,
			float radius2) {
		activate(false, true);
		return nativeCreateStar(center, numPoints, radius1, radius2);
	}

	private native Path nativeCreateSpiral(Point firstArcCenter, Point start,
			float decayPercent, int numQuarterTurns,
			boolean clockwiseFromOutside);

	/**
	 * Creates a spiral shaped Path Item.
	 * 
	 * Sample code:
	 * <code>
	 * var firstArcCenter = new Point(100, 100);
	 * var start = new Point(50, 50);
	 * var decayPercent = 90;
	 * var numQuarterTurns = 25;
	 * 
	 * var path = new Path.Spiral(firstArcCenter, start, decayPercent,
	 *         numQuarterTurns, true);
	 * </code>
	 * 
	 * @param firstArcCenter the center point of the first arc
	 * @param start the starting point of the spiral
	 * @param decayPercent the percentage by which each succeeding arc will be
	 *        scaled
	 * @param numQuarterTurns the number of quarter turns (arcs)
	 * @param clockwiseFromOutside if this is set to {@code true} the spiral
	 *        will spiral in a clockwise direction from the first point. If it's
	 *        set to {@code false} it will spiral in a counter clockwise
	 *        direction
	 * @return the newly created path
	 * 
	 * @jshide
	 */
	public Path createSpiral(Point firstArcCenter, Point start,
			float decayPercent, int numQuarterTurns,
			boolean clockwiseFromOutside) {
		activate(false, true);
		return nativeCreateSpiral(firstArcCenter, start, decayPercent,
				numQuarterTurns, clockwiseFromOutside);
	}
	
	private native HitResult nativeHitTest(Point point, int request,
			float tolerance, Item item);

	protected HitResult hitTest(Point point, HitRequest request,
			float tolerance, Item item) {
		return nativeHitTest(point,
				(request != null ? request : HitRequest.ALL).value,
				tolerance, item);
	}

	/**
	 * @param point
	 * @param request
	 * @param tolerance the hit-test tolerance in view coordinates (pixels at
	 *        the current zoom factor). correct results for large values are not
	 *        guaranteed {@default 2}
	 */
	public HitResult hitTest(Point point, HitRequest request, float tolerance) {
		return hitTest(point, request, tolerance, null);
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
	
	/**
	 * Text reflow is suspended during script execution. when reflowText() is
	 * called, the reflow of text is forced.
	 */
	public native void reflowText();

	/**
	 * Checks whether the document is valid, i.e. it hasn't been closed.
	 * 
	 * Sample code:
	 * <code>
	 * var doc = document;
	 * print(doc.isValid()); // true
	 * doc.close();
	 * print(doc.isValid()); // false
	 * </code>
	 * 
	 * @return {@true if the document is valid}
	 */
	public boolean isValid() {
		return handle != 0;
	}

	/**
	 * {@grouptitle Clipboard Functions}
	 * 
	 * Cuts the selected items to the clipboard.
	 */
	public native void cut();
	
	/**
	 * Copies the selected items to the clipboard.
	 */
	public native void copy();
	
	/**
	 * Pastes the contents of the clipboard into the active layer of the
	 * document.
	 */
	public native void paste();
}
