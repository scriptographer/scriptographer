/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Scripting Plugin for Adobe Illustrator
 * http://scriptographer.org/
 *
 * Copyright (c) 2002-2010, Juerg Lehni
 * http://scratchdisk.com/
 *
 * All rights reserved. See LICENSE file for details.
 * 
 * File created on Apr 11, 2008.
 */

package com.scriptographer.ai;

import com.scratchdisk.util.IntegerEnum;

/**
 * AIArtUserAttr
 * Used in Document.getMatchingArt.
 * @author lehni
 *
 */
public enum ItemAttribute implements IntegerEnum {
	SELECTED(0x00000001),
	LOCKED(0x00000002),
	HIDDEN(0x00000004),
	FULLY_SELECTED(0x00000008),
	// Valid only for groups and plugin groups. Indicates whether the
	// contents of the object are expanded in the layers palette.
	EXPANDED(0x00000010),
	TARGETED(0x00000020),
	// Indicates that the object defines a clip mask. This can only be set on
	// paths), compound paths), and text frame objects. This property can only be
	// set on an object if the object is already contained within a clip group.
	CLIP_MASK(0x00001000),
	// Indicates that text is to wrap around the object. This property cannot be
	// set on an object that is part of compound group), it will return
	// kBadParameterErr. private final int ATTR_IsTextWrap has to be set to the
	// ancestor compound group in this case.
	TEXTWRAP(0x00010000),
	// Meaningful only to GetMatchingArt passing to SetArtUserAttr will cause an error. Only one
	// of kArtSelectedTopLevelGroups), kArtSelectedLeaves or kArtSelectedTopLevelWithPaint can
	// be passed into GetMatchingArt), and they cannot be combined with anything else. When
	// passed to GetMatchingArt causes only fully selected top level objects to be returned
	// and not their children.
	SELECTED_TOPLEVEL_GROUPS(0x00000040),
	// Meaningful only to GetMatchingArt passing to SetArtUserAttr will cause an error. When passed
	// to GetMatchingArt causes only leaf selected objects to be returned and not their containers.
	// See also kArtSelectedTopLevelGroups
	SELECTED_LAYERS(0x00000080),
	// Meaningful only to GetMatchingArt passing to SetArtUserAttr will cause an error. When passed
	// to GetMatchingArt causes only top level selected objects that have a stroke or fill to be
	// returned. See also kArtSelectedTopLevelGroups
	SELECTED_TOPLEVEL_WITH_PAINT(0x00000100), // Top level groups that have a stroke or fill), or leaves
	// Valid only for GetArtUserAttr and GetMatchingArt passing to
	// SetArtUserAttr will cause an error. true if the item has a simple
	// style.
	SIMPLE_STYLE(0x00000200),
	// Valid only for GetArtUserAttr and GetMatchingArt passing to
	// SetArtUserAttr will cause an error. true if the item has an active
	// style.
	ACTIVE_STYLE(0x00000400),
	// Valid only for GetArtUserAttr and GetMatchingArt passing to
	// SetArtUserAttr will cause an error. true if the item is a part of a
	// compound path.
	CHILD_OF_COMPOUND_PATH(0x00000800),
	// On GetArtUserAttr), reports whether the object has a style that is
	// pending re-execution. On SetArtUserAttr), marks the style dirty
	// without making any other changes to the item or to the style.
	DIRTY_STYLE(0x00040000);

	protected int value;

	private ItemAttribute(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
