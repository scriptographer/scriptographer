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
 * File created on 19.02.2005.
 * 
 * $RCSfile: MenuGroup.java,v $
 * $Author: lehni $
 * $Revision: 1.5 $
 * $Date: 2005/10/23 00:33:04 $
 */

package com.scriptographer.ai;

import com.scriptographer.util.IntMap;

public class MenuGroup extends AIObject {
	// AIMenuGroups.h:
	public static final MenuGroup
		GROUP_ABOUT 							= new MenuGroup("About"),

		GROUP_OPEN 								= new MenuGroup("Open Document"),
		GROUP_RECENT							= new MenuGroup("Recent Files"),
		GROUP_CLOSE 							= new MenuGroup("Close Document"),
		GROUP_SAVE 								= new MenuGroup("Save Document"),
		GROUP_SAVE_FOR							= new MenuGroup("Save For"),
		GROUP_IMPORT 							= new MenuGroup("Import"),
		GROUP_PLACE 							= new MenuGroup("Place Document"),
		GROUP_EXPORT 							= new MenuGroup("Export Document"),
		GROUP_DOCUMENT_UTILITIES				= new MenuGroup("Document Utilities"),
		GROUP_DOCUMENT_INTERCHANGE				= new MenuGroup("Document Interchange"),
		GROUP_PRINT								= new MenuGroup("Print"),
		GROUP_SEND								= new MenuGroup("Send Document"),

		GROUP_APPLICATION_UTILITIES				= new MenuGroup("Application Utilities"),
		GROUP_QUIT								= new MenuGroup("Quit"),

		GROUP_UNDO								= new MenuGroup("Undo"),
		GROUP_PASTE_UTILITIES					= new MenuGroup("Paste Utilities"),
		GROUP_SELECT 							= new MenuGroup("Select"),			// Select menu- internal commands
		GROUP_SELECT_EXTERNAL					= new MenuGroup("SelectExternal"),	// Select menu- external commands,
		GROUP_EDITTEXT							= new MenuGroup("Edit Text"),			// Find/Replace, Spell Check
		GROUP_EDIT 								= new MenuGroup("Edit"),
		GROUP_PRESETS							= new MenuGroup("Presets Group"),		// PDF, Transparency, Print presets

		GROUP_SAME								= new MenuGroup("Same"),
		GROUP_SELECT_OBJECT						= new MenuGroup("SelectObject"),

		GROUP_PREFERENCES						= new MenuGroup("Preferences"),
		GROUP_EDIT_UTILITIES					= new MenuGroup("Edit Utilities"),
		GROUP_CLIPBOARD 						= new MenuGroup("Clipboard"),

		GROUP_REPEAT							= new MenuGroup("Repeat"),
		GROUP_ARRANGE_TRANSFORM					= new MenuGroup("Arrange Transform"),
		GROUP_ARRANGE_MOVE						= new MenuGroup("Arrange Move"),
		GROUP_ARRANGE_GROUP 					= new MenuGroup("Arrange Group"),
		GROUP_ARRANGE_ATTRIBUTES				= new MenuGroup("Arrange Attributes"),

		GROUP_VIEW_MODE							= new MenuGroup("View Mode"),
		GROUP_VIEW_ADORNMENTS					= new MenuGroup("View Adornments"),
		GROUP_VIEW 								= new MenuGroup("View"),
		GROUP_VIEW_UTILITIES 					= new MenuGroup("View Utilities"),
		GROUP_VIEW_EXTENSION 					= new MenuGroup("View Extension"),

		GROUP_OBJECT_ATTRIBUTES					= new MenuGroup("Object Attributes"),
		GROUP_OBJECT_UTILITIES					= new MenuGroup("Object Utilities"),
		GROUP_OBJECTS 							= new MenuGroup("Objects"),
		GROUP_OBJECT_PATHS 						= new MenuGroup("Objects Paths"),
		GROUP_OBJECT_PATHS_POPUP 				= new MenuGroup("Objects Paths Popup"),
		GROUP_LOCK								= new MenuGroup("Lock"),
		GROUP_HIDE								= new MenuGroup("Hide"),

		GROUP_GUIDES 							= new MenuGroup("Guides"),
		GROUP_MASK 								= new MenuGroup("Masks"),
		GROUP_COMPOUNDPATHS						= new MenuGroup("Compound Paths"),
		GROUP_CROPMARKS 						= new MenuGroup("Crop Marks"),
		GROUP_GRAPHS							= new MenuGroup("Graphs"),
		GROUP_BLOCKS 							= new MenuGroup("Blocks"),
		GROUP_WRAP 								= new MenuGroup("Wrap"),
		GROUP_TYPE_TEXTPATH						= new MenuGroup("Text Path Type"),
		GROUP_TYPE_ATTRIBUTES 					= new MenuGroup("Type Attributes"),
		GROUP_TYPE_PALETTES 					= new MenuGroup("Type Palettes"),
		GROUP_TYPE_LAYOUT 						= new MenuGroup("Type Layout"),
		GROUP_TYPE_UTILITIES 					= new MenuGroup("Type Utilities"),
		GROUP_TYPE_PLUGINS1						= new MenuGroup("Type Plugins1"),
		GROUP_TYPE_PLUGINS2						= new MenuGroup("Type Plugins2"),
		GROUP_TYPE_ASIAN_OPTIONS				= new MenuGroup("Type Asian Options"),

		GROUP_TYPE_SIZE_UTILITIES 				= new MenuGroup("Type Size Utilities"),
		GROUP_TYPE_SIZE 						= new MenuGroup("Type Size"),
		GROUP_TYPE_LEADING_UTILITIES			= new MenuGroup("Type Leading Utilities"),
		GROUP_TYPE_LEADING 						= new MenuGroup("Type Leading"),
		GROUP_TYPE_ALIGNMENT					= new MenuGroup("Type Alignment"),

		GROUP_FILTER_UTILITIES					= new MenuGroup("Filter Utilities"),

		GROUP_EFFECTS							= new MenuGroup("Effects"),

		GROUP_HELP 								= new MenuGroup("Help Menu"),

		// The following groups do not show up in the menu bar. They only
		// show up in the keyboard shortcuts dialog.
		GROUP_HIDDEN_OTHER_SELECT				= new MenuGroup("Hidden Other Select"),
		GROUP_HIDDEN_OTHER_TEXT					= new MenuGroup("Hidden Other Text"),
		GROUP_HIDDEN_OTHER_OBJECT				= new MenuGroup("Hidden Other Object"),
		GROUP_HIDDEN_OTHER_PALETTE				= new MenuGroup("Hidden Other Palette"),
		GROUP_HIDDEN_OTHER_MISC					= new MenuGroup("Hidden Other Misc"),

		GROUP_WINDOW_UTILITIES 					= new MenuGroup("Window Utilities"),
		GROUP_TOOL_PALETTES						= new MenuGroup("Tool Palettes"),
		GROUP_WINDOW_LIBARIES					= new MenuGroup("Window Libraries"),

		/////////////////////////////////////////////////
		//
		// The menus below are added by plug-ins, not by
		// the application.  If you intend to use one, your
		// plug-in should add it (adding a menu group twice
		// is ok; the second call simply returns the existing
		// group reference).
		//
		// When adding a group, be sure to use the option
		// indicated in below the group
		//
		/////////////////////////////////////////////////

		GROUP_DOCUMENT_INFO						= new MenuGroup("AIPlugin Document Info"),

		GROUP_OBJECT_RASTER						= new MenuGroup("AIPlugin Object Raster"),

		GROUP_ARRANGE_TRANSFORM_MULTIPLE		= new MenuGroup("Arrange Multiple Transform"),
		GROUP_ARRANGE_TRANSFORM_MULTIPLE_NEAR	= GROUP_ARRANGE_TRANSFORM,

		GROUP_OBJECT_PATHS_POPOUT				= new MenuGroup("More Menus in the Object Path Popout"),
		GROUP_OBJECT_PATHS_POPOUT_NEAR 			= GROUP_OBJECT_PATHS_POPUP,

		GROUP_DOCUMENT_SUPPORT					= new MenuGroup("AIPlugin Document Support"),

		GROUP_ASSET_MANAGEMENT					= new MenuGroup("Adobe Plugin Asset Mgmt"),
		GROUP_WORKGROUP							= GROUP_DOCUMENT_SUPPORT,

		//Scripting Plugin
		GROUP_SCRIPTS 							= new MenuGroup("ScriptsMenuGroup");

	public static final int
		OPTION_NONE								= 0,
		OPTION_SORTED_ALPHABETICALLY			= 1 << 0,
		OPTION_SEPERATOR_ABOVE 					= 1 << 1,
		OPTION_SEPARATOR_BELOW					= 1 << 2;

	protected String name;

	private static IntMap groups = new IntMap();

	private MenuGroup(String name) {
		this.name = name;
	}

	/**
	 *
	 * @param name
	 * @param near
	 * @param options MenuGroup.OPTION_*
	 */
	public MenuGroup(String name, MenuGroup near, int options) {
		this(name);
		// use this.name, instead of name, because it was modified
		// in the constructor above
		handle = nativeCreate(this.name, near.name, 0, options);

		if (handle == 0)
			throw new RuntimeException("Unable to create MenuGroup");

		putGroup(this);
	}

	/**
	 * Creates a submenu group at parent
	 * @param name
	 * @param parent
	 * @param options MenuGroup.OPTION_*
	 */
	public MenuGroup(String name, MenuItem parent, int options) {
		this(name);
		// if parent already has a subGroup, append this one after:
		MenuGroup subGroup = parent.getSubGroup();
		if (subGroup != null) {
			handle = nativeCreate(this.name, subGroup.name, 0, options);
		} else {
			handle = nativeCreate(this.name, null, parent.handle, options);
		}

		if (handle == 0)
			throw new RuntimeException("Unable to create MenuGroup");

		putGroup(this);
	}

	/**
	 * Used in wrapGroupHandle
	 *
	 * @param groupHandle
	 * @param name
	 */
	protected MenuGroup(int handle, String name) {
		this.handle = handle;
		this.name = name;
		putGroup(this);
	}

	/**
	 * Called from the native environment to wrap a MenuGroup:
	 *
	 * @param groupHandle
	 * @param name
	 * @return
	 */
	protected static MenuGroup wrapGroupHandle(int handle, String name) {
		MenuGroup group = getGroup(handle);
		if (group == null) {
			group = new MenuGroup(handle, name);
		}
		return group;
	}

	private static native int nativeCreate(String name, String nearGroup, int parentItemHandle, int options);

	public boolean equals(Object obj) {
		if (obj instanceof MenuGroup) {
			MenuGroup group = (MenuGroup) obj;
			return name.equals(group.name);
		}
		return false;
	}

	public String getName() {
		return name;
	}

	public native void setOption(int options);
	public native int getOptions();

	private static void putGroup(MenuGroup group) {
		groups.put(group.handle, group);
	}

	private static MenuGroup getGroup(int handle) {
		return (MenuGroup) groups.get(handle);
	}
}
