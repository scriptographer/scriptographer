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
 * File created on Apr 11, 2008.
 *
 * $Id$
 */

package com.scriptographer.ai;

import com.scriptographer.NamedOption;

/**
 * AIArtUserAttr
 * Used in Document.getMatchingArt.
 * @author lehni
 *
 */
public class ItemAttribute extends NamedOption {
	public static final ItemAttribute
		SELECTED						= new ItemAttribute("selected",					0x00000001),
		LOCKED							= new ItemAttribute("locked",						0x00000002),
		HIDDEN							= new ItemAttribute("hidden",						0x00000004),
		FULLY_SELECTED					= new ItemAttribute("fullySelected",				0x00000008),
		// Valid only for groups and plugin groups. Indicates whether the
		// contents of the object are expanded in the layers palette.
		EXPANDED						= new ItemAttribute("expanded",					0x00000010),
		TARGETED						= new ItemAttribute("targeted",					0x00000020),
		// Indicates that the object defines a clip mask. This can only be set on
		// paths), compound paths), and text frame objects. This property can only be
		// set on an object if the object is already contained within a clip group.
		IS_CLIPMASK						= new ItemAttribute("clipmask",					0x00001000),
		// Indicates that text is to wrap around the object. This property cannot be
		// set on an object that is part of compound group), it will return
		// kBadParameterErr. private final int ATTR_IsTextWrap has to be set to the
		// ancestor compound group in this case.
		IS_TEXTWRAP						= new ItemAttribute("textwrap",					0x00010000),
		// Meaningful only to GetMatchingArt passing to SetArtUserAttr will cause an error. Only one
		// of kArtSelectedTopLevelGroups), kArtSelectedLeaves or kArtSelectedTopLevelWithPaint can
		// be passed into GetMatchingArt), and they cannot be combined with anything else. When
		// passed to GetMatchingArt causes only fully selected top level objects to be returned
		// and not their children.
		SELECTED_TOPLEVEL_GROUPS		= new ItemAttribute("selectedToplevelGroups",		0x00000040),
		// Meaningful only to GetMatchingArt passing to SetArtUserAttr will cause an error. When passed
		// to GetMatchingArt causes only leaf selected objects to be returned and not their containers.
		// See also kArtSelectedTopLevelGroups
		SELECTED_LAYERS 				= new ItemAttribute("selectedLayers",				0x00000080),
		// Meaningful only to GetMatchingArt passing to SetArtUserAttr will cause an error. When passed
		// to GetMatchingArt causes only top level selected objects that have a stroke or fill to be
		// returned. See also kArtSelectedTopLevelGroups
		SELECTED_TOPLEVEL_WITH_PAINT 	= new ItemAttribute("selectedToplevelWithPaint",	0x00000100), // Top level groups that have a stroke or fill), or leaves
		// Valid only for GetArtUserAttr and GetMatchingArt passing to
		// SetArtUserAttr will cause an error. true if the item has a simple
		// style.
		HAS_SIMPLE_STYLE				= new ItemAttribute("simpleStyle",					0x00000200),
		// Valid only for GetArtUserAttr and GetMatchingArt passing to
		// SetArtUserAttr will cause an error. true if the item has an active
		// style.
		HAS_ACTIVE_STYLE				= new ItemAttribute("activeStyle",					0x00000400),
		// Valid only for GetArtUserAttr and GetMatchingArt passing to
		// SetArtUserAttr will cause an error. true if the item is a part of a
		// compound path.
		CHILD_OF_COMPOUND				= new ItemAttribute("childOfCompoundPath",			0x00000800),
		// On GetArtUserAttr), reports whether the object has a style that is
		// pending re-execution. On SetArtUserAttr), marks the style dirty
		// without making any other changes to the item or to the style.
		STYLE_IS_DIRTY					= new ItemAttribute("dirtyStyle",					0x00040000);

	private ItemAttribute(String name, int value) {
		super(name, value);
	}

	protected static ItemAttribute get(Object key) {
		return (ItemAttribute) get(ItemAttribute.class, key);
	}
}
