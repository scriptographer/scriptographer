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
 * File created on 16.02.2005.
 *
 * $Id$
 */

package com.scriptographer.ai;

import java.util.ArrayList;

import com.scratchdisk.util.IntMap;

/**
 * @author lehni
 * 
 * @jshide
 */
public class FileFormat extends NativeObject {

	protected final static long
	/** Read the file, creating artwork in a new document.
		The format is included in the File > Open file types.
		Use when adding a format. */
		OPTION_READ =							(1L<<0),
	/** Write the document's contents to a file
		in a non-Illustrator format.
		The format is included in the File > Export file types.
		Use when adding a format. */
		OPTION_EXPORT =							(1L<<1),
	/** Write the document's contents to a file
		in a format from which is can be read back into
		Illustrator without loss of data.
		The format is included in the File > Save As file types.
		Use when adding a format.*/
		OPTION_WRITE =							(1L<<9),
	/** Read the file and embed artwork to the current document.
		The format is included in the File > Import file types.
		Use when adding a format.*/
		OPTION_IMPORT_ART =						(1L<<2),
	/** Read the file and embed artwork to the current document.
		This is the same as \c #	OPTION_IMPORTART.
		Use when adding a format.*/
		OPTION_PLACE_ART	=					(1L<<3),
	/** Allows "Template" to be checked in the Place dialog when
		linking or embedding a file of this type, so the art is
		placed in a template layer.
		Use when adding a format.*/
		OPTION_CONVERT_TEMPLATE =				(1L<<7),
	/** Make a link from the contents of a file to an Illustrator
		document.
		Use when adding a format.*/
		OPTION_LINK_ART =						(1L<<8),
	/** Not used. */
		OPTION_IMPORT_STYLES =					(1L<<4),
	/** When reading, the plug-in sets the print record. See
		\c #AIDocumentSuite::SetDocumentPrintRecord().
		Use when adding a format.*/
		OPTION_SUPPLIES_PRINT_RECORD =			(1L<<5),

	/** Makes this the default format for all documents. If specified
		by more than one plug-in, the last one becomes the default.
		Use when adding a format.*/
		OPTION_IS_DEFAULT =						(1L<<6),

	/** The plug-in will not respond to the \c #kSelectorAIChec	OPTION_
		selector. (For example, the PhotoShop adapter plug-in always returns
		\c #kNoErr.)
		Use when adding a format.*/
		OPTION_NO_AUTO_CHECK_FOR_MAT =			(1L<<10),
	/** Read the file, creating artwork in a new template layer in
		the current document.
		Not used for adding a format. */
		OPTION_CREATE_TEMPLATE_LAYER =			(1L<<11),
	/** Handle the extended data passed in a Go message
		for a placement request.
		Use when adding a format.*/
		OPTION_HAS_EXTENDED_DATA =				(1L<<12),

	/** This file format supplies its own startup objects (colors, patterns,
		and so on), Illustrator does not copy the startup file
		Use when adding a format.*/
		OPTION_SKIP_STARTUP_OBJECTS =			(1L<<13),

	/** Disable warning dialogs upon read and write.
		Not used for adding a format.*/
		OPTION_NO_WARNING =						(1L<<14),
	/** Write the current document to a copy of the file it was
		loaded from.
		Not used for adding a format. */
		OPTION_SAVE_COPY =						(1L<<15),

	/**  Prevents this file format from appearing in the file
		selection menu of the Open and Place dialogs.
		Use when adding a format. */
		OPTION_SUPPRESS_UI =					(1L<<21),
	/** Set in combination with \c #	OPTION_WRITE for a
		Save As operation, to distinguish it from Save.
		Not used for adding a format. */
		OPTION_WRITE_AS =						(1L<<22),

	/** Always receive the Check message, even for operations this plug-in
		does not support. Allows an opportunity to explicitly reject operations
		on files matching this plug-in's type.
		Use when adding a format.*/
		OPTION_CHECK_ALWAYS =					(1L<<23),

	/** Handle additional parameters passed in \c #AIFileFormatMessage::actionParm.
		These supplement the usual parameters of the file format,
		and may not be complete. Can be used, for instance, for scripting or optimizing.

		If set in the Go message for a plug-in that does not handle the option,
		you can ignore it.
		Not used for adding a format. */
		OPTION_CONTAINS_PARTIAL_PARAMETERS =	(1L<<24),

	/** Import only the SLO composite fonts.
		Do not import artwork or other global objects, and
		do not perform font fauxing.
		Not used for adding a format.*/
		OPTION_IMPORT_COMPOSITE_FONTS	=		(1L<<25),

	/** Treat the file as stationary--that is, open a copy with an Untitled name.
		Use only in conjunction with \c #	OPTION_READ
		Not used for adding a format. */
		OPTION_OPEN_UNTITLED_COPY =			(1L<<26),

	/** An option for the native (PGF) AI File Format Writer,
		telling it to write out only the indicated palettes and the global objects,
		directly or indirectly. Not used for adding a format. */
		OPTION_WRITE_SWATCH_LIBRARY =			(1L<<27),
	/** An option for the native (PGF) AI File Format Writer,
		telling it to write out only the indicated palettes and the global objects,
		directly or indirectly. Not used for adding a format. */
		OPTION_WRITE_BRUSH_LIBRARY =			(1L<<28),
	/** An option for the native (PGF) AI File Format Writer,
		telling it to write out only the indicated palettes and the global objects,
		directly or indirectly. Not used for adding a format. */
		OPTION_WRITE_STYLE_LIBRARY =			(1L<<29),
	/** An option for the native (PGF) AI File Format Writer,
		telling it to write out only the indicated palettes and the global objects,
		directly or indirectly. Not used for adding a format. */
		OPTION_WRITE_SYMBOL_LIBRARY =			(1L<<30);

	private String name;
	private String title;
	private String[] extensions;
	private long options;

	protected FileFormat(int handle, String name, String title, String extension, long options) {
		super(handle);
		this.name = name;
		this.title = title;
		this.extensions = extension.split("\\s*,\\s*");
		this.options = options;
		lookup.put(handle, this);
	}

	private static IntMap<FileFormat> lookup = new IntMap<FileFormat>();

	protected static native ArrayList<FileFormat> getFileFormats();

	public String getName() {
		return name;
	}

	public String getTitle() {
		return title;
	}

	public String[] getExtensions() {
		return extensions;
	}

	public String toString() {
		String str = "";
		if ((options & OPTION_WRITE) != 0)
			str = "writes: ";
		else if ((options & OPTION_EXPORT) != 0)
			str = "exports: ";
		for (int i = 0; i < extensions.length; i++) {
			if (i > 0)
				str += ", ";
			str += extensions[i];
		}
		return "FileFormat \""+ name + "\": " + title + " (" + str + ")";
	}

	protected static FileFormat getFormat(int handle) {
		return (FileFormat) lookup.get(handle);
	}
}
