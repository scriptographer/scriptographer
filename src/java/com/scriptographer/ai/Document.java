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
 *
 * $Id$
 */

package com.scriptographer.ai;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

import com.scratchdisk.list.Lists;
import com.scratchdisk.list.ReadOnlyList;
import com.scratchdisk.script.ChangeListener;
import com.scratchdisk.util.ArrayList;
import com.scratchdisk.util.ConversionUtils;
import com.scratchdisk.util.IntegerEnumUtils;
import com.scratchdisk.util.SoftIntMap;
import com.scriptographer.CommitManager;
import com.scriptographer.ScriptographerEngine;
import com.scriptographer.ScriptographerException;
import com.scriptographer.script.EnumUtils;

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
public class Document extends NativeObject implements ChangeListener {
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

	protected ArrayList<Item> createdItems = new ArrayList<Item>();
	protected ArrayList<Item> modifiedItems = new ArrayList<Item>();
	protected ArrayList<Item> removedItems = new ArrayList<Item>();

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
						"Unable to create document from non existing file: " + file);
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
	public Document(String title, float width, float height, ColorModel colorModel,
			DialogStatus dialogStatus) {
		this(nativeCreate(title, width, height, 
				(colorModel != null ? colorModel : ColorModel.CMYK).value,
				(dialogStatus != null ? dialogStatus : DialogStatus.NONE).value));
	}

	public Document(String title, float width, float height, ColorModel colorModel) {
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
	 * Undo / Redo History Stuff
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

	private void setHistoryLevels(int undoLevel, int redoLevel, boolean checkLevel) {
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
				// Update newly created and modified items, after new historyVersion
				// is set.
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
					for (Item item : createdItems)
						item.creationVersion = item.modificationVersion = historyVersion;
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
		// We first need to check that document handle is not 0, because if it is
		// all items inside are invalid. handle is set to 0 in onClosed().
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

	protected void onClosed() {
		// Since AI reused document handles, we have to manually remove wrappers
		// when documents get closed. This happens through a kDocumentClosedNotifier
		// on the native side.
		documents.remove(handle);
		handle = 0;
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
	protected void onSelectionChanged(int[] artHandles, int undoLevel, int redoLevel) {
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
			// Check if we were going back to a previous branch, and if so, switch
			// back.
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

	private static native void nativeBeginExecution(int[] values);

	/**
	 * Called before AI functions are executed.
	 * 
	 * @return The current undo level
	 * 
	 * @jshide
	 */
	public static void beginExecution() {
		int[] values = new int[3]; // docHandle, undoLevel, redoLevel
		nativeBeginExecution(values);
		Document document = wrapHandle(values[0]);
		if (document != null) {
			document.setHistoryLevels(values[1], values[2], true);
		}
	}

	/**
	 * Called after AI functions are executed
	 * 
	 * @jshide
	 */
	public static native void endExecution();

	private native void nativeActivate(boolean focus, boolean forCreation);

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
	
	private Item getCurrentStyleItem() {
		// This is a bit of a hack: We use a special handle HANDLE_CURRENT_STYLE
		// to tell the native side that this is in fact the current style, not an
		// item handle...
		if (currentStyleItem == null)
			currentStyleItem = new Item(Item.HANDLE_CURRENT_STYLE, this, false, true);
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
		setSize(size.width, size.height);
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
		if (data == null)
			data = Dictionary.wrapHandle(nativeGetData(), this);
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
	
	private native int nativeGetStories();
	
	private TextStoryList stories = null;
	
	/**
	 * The stories contained within the document.
	 */
	public TextStoryList getStories() {
		// We need to version TextStoryLists, since document handles seem to not be unique:
		// When there is only one document, closing it and opening a new one results in the
		// same document handle. Versioning seems the only way to keep story lists updated.
		if (stories == null) {
			int handle = nativeGetStories();
			if (handle != 0)
				stories = new TextStoryList(handle, this);
		} else if (stories.version != CommitManager.version) {
			int handle = nativeGetStories();
			if (handle != 0)
				stories.changeHandle(handle);
			else
				stories = null;
		}
		return stories;
	}

	private native void nativePrint(int status);

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
		invalidate((float) rect.x, (float) rect.y, (float) rect.width, (float) rect.height);
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
	 * @jshide
	 */
	public ItemList getSelectedItems(Class[] types) {
		if (types == null) {
			return getSelectedItems();
		} else {
			HashMap<Object, Object> map = new HashMap<Object, Object>();
			map.put(ItemAttribute.SELECTED, true);
			return getItems(types, map);
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
	 * @jshide
	 */
	public ItemList getSelectedItems(Class type) {
		return getSelectedItems(new Class[] { type });
	}
	
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

	private native ItemList getMatchingItems(Class type,
			HashMap<Integer, Boolean> attributes);

	/**
	 * @jshide
	 */
	public ItemList getItems(Class[] types, Map<Object, Object> attributes) {
		// Convert the attributes list to a new HashMap containing only
		// integer -> boolean pairs.
		HashMap<Integer, Boolean> converted = new HashMap<Integer, Boolean>();
		if (attributes != null) {
			for (Map.Entry entry : attributes.entrySet()) {
				Object key = entry.getKey();
				if (!(key instanceof ItemAttribute)) {
					key = EnumUtils.get(ItemAttribute.class, key.toString());
					if (key == null)
						throw new ScriptographerException("Undefined attribute: "
								+ entry.getKey());
				}
				converted.put(((ItemAttribute) key).value,
						ConversionUtils.toBoolean(entry.getValue()));
			}
		}
		ItemList items = new ItemList();
		for (int i = 0; i < types.length; i++) {
			Class type = types[i];
			// Expand PathItem -> Path / CompoundPath
			if (PathItem.class.equals(type)) {
				items.addAll(getMatchingItems(Path.class, converted));
				items.addAll(getMatchingItems(CompoundPath.class, converted));
			} else {
				ItemList list = getMatchingItems(type, converted);
				// Filter out TextItems that do not match the given type.
				// This is needed since nativeGetMatchingItems returns all TextItems...
				// TODO: Move this to the native side maybe?
				if (TextItem.class.isAssignableFrom(type))
					for (Item item : list)
						if (!type.isInstance(item))
							list.remove(item);
				items.addAll(list);
			}
		}
		// Filter out matched children when the parent matches too
		for (int i = items.size() - 1; i >= 0; i--) {
			Item item = items.get(i);
			if (items.contains(item.getParent()))
				items.remove(i);
		}
		return items;
	}

	/**
	 * @jshide
	 */
	public ItemList getItems(Class[] types) {
		return getItems(types, (Map<Object, Object>) null);
	}

	/**
	 * @jshide
	 */
	public ItemList getItems(Class type, Map<Object, Object> attributes) {
		return getItems(new Class[] { type }, attributes);
	}
	
	/**
	 * @jshide
	 */
	public ItemList getItems(Class type) {
		return getItems(new Class[] { type });
	}

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
	 * // All hidden Paths contained in the document.
	 * var hiddenItems = document.getItems({
	 *     type: Path,
	 *     hidden: true
	 * });
	 * </code>
	 * 
	 * @param attributes an object containing the various attributes to check
	 *        for. The key {@code type} defines a single prototype, an array of
	 *        prototypes or a comma separated {@String} of prototype
	 *        names to check for. The following keys have {@code Boolean} values
	 *        to check for the state of the matching items: {@enum
	 *        ItemAttribute}.
	 */
	public ItemList getItems(Map<Object, Object> attributes) {
		ArrayList<Class> classes = new ArrayList<Class>();
		// Convert "type" to class array:
		Object types = attributes.get("type");
		if (types != null) {
			// Support comma separated String lists.
			if (types instanceof String)
				types = ((String) types).split(",");
			if (types instanceof Object[]) {
				for (Object type : (Object[]) types) {
					if (type instanceof Class) {
						classes.add((Class) type);
					} else if (type instanceof String) {
						// Try loading class from String name.
						try {
							classes.add(Class.forName(
									Item.class.getPackage().getName()
									+ "." + type));
						} catch (ClassNotFoundException e) {
						}
					}
				}
			} else if (types instanceof Class)
				classes.add((Class) types);
		}
		// Filter out classes again that are not inheriting Item.
		for (int i = classes.size() - 1; i >= 0; i--)
			if (!Item.class.isAssignableFrom((classes.get(i))))
				classes.remove(i);
		// If no class was specified, match them all through Item.
		if (classes.isEmpty())
			classes.add(Item.class);
		// Remove "type" from the cloned map that's passed to getItems.
		HashMap<Object, Object> clone = new HashMap<Object, Object>(attributes);
		clone.remove("type");
		return getItems(classes.toArray(new Class[classes.size()]), clone);
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
	 * var path = new Path.Spiral(firstArcCenter, start, decayPercent, numQuarterTurns, true);
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
	
	protected native HitResult nativeHitTest(Point point, int request,
			float tolerance, Item item);

	/**
	 * @param point
	 * @param request
	 * @param tolerance the hit-test tolerance in view coordinates (pixels at
	 *        the current zoom factor). correct results for large values are not
	 *        guaranteed {@default 2}
	 */
	public HitResult hitTest(Point point, HitRequest request, float tolerance) {
		return nativeHitTest(point,
				(request != null ? request : HitRequest.ALL).value,
				tolerance, null);
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
