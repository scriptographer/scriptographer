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
 * File created on Apr 14, 2008.
 */

package com.scriptographer.ai;

import com.scratchdisk.util.IntegerEnum;

/**
 * AIHitRequest
 * 
 * @author lehni
 */
public enum HitRequest implements IntegerEnum {
	/**
	 * Any object hits anywhere.
	 */
	ALL(0),
	/**
	 * Only hits on curve anchor points.
	 */
	ANCHORS(1),
	/**
	 * @deprecated
	 */
	POINTS(1),
	/**
	 * Only first or last bezier point hits on path.
	 */
	END_ANCHORS(2),
	/**
	 * @deprecated
	 */
	END_POINTS(2),
	/**
	 * Only guide object hits.
	 */
	GUIDES(3),
	/**
	 * Only hits on points on paths or any guides.
	 */
	PATHS(4),
	/**
	 * Only hits on text objects.
	 */
	TEXT_ITEMS(5),
	/**
	 * @deprecated
	 */
	TEXTS(5),
	/**
	 * Only hits already-selected objects.
	 */
	SELECTION(6),
	/**
	 * Only hits paintable objects (non-guide paths & text)
	 */
	ALL_EXCEPT_GUIDES(7),
	/**
	 * @deprecated
	 */
	PAINTABLES(7),
	/**
	 * Same as all but doesn't test against object fills
	 */
	ALL_EXCEPT_FILLS(8),
	/**
	 * Same as all but doesn't test against direction line end points (the in and
	 * out handles of a bezier)
	 */
	ALL_EXCEPT_HANDLES(10),
	/**
	 * Same as paintables but doesn't test against object fills
	 */
	ALL_EXCEPT_GUIDES_AND_FILLS(9),
	/**
	 * Same as paintables but no locked objects
	 */
	ALL_EXCEPT_GUIDES_AND_LOCKED(11);

	protected int value;

	private HitRequest(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
