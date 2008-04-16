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
 * File created on Apr 17, 2008.
 *
 * $Id$
 */

package com.scriptographer.adm;

import java.util.HashMap;

/**
 * @author lehni
 *
 */
public class FlowLayout extends java.awt.FlowLayout {

	private static HashMap<String, Integer> alignment = new HashMap<String, Integer>();
	static {
		alignment.put("left", FlowLayout.LEFT);
		alignment.put("center", FlowLayout.CENTER);
		alignment.put("right", FlowLayout.RIGHT);
		alignment.put("leading", FlowLayout.LEADING);
		alignment.put("trailing", FlowLayout.TRAILING);
	}

    /**
	 * Constructs a new <code>FlowLayout</code> with a centered alignment and
	 * a default 0-unit horizontal and vertical gap.
	 */
	public FlowLayout() {
		super(CENTER, 0, 0);
	}

	/**
	 * Constructs a new <code>FlowLayout</code> with the specified alignment
	 * and a default 0-unit horizontal and vertical gap. The value of the
	 * alignment argument must be one of <code>"left"</code>,
	 * <code>"right"</code>, <code>"center"</code>, <code>"leading"</code>
	 * or <code>"trailing"</code>.
	 * 
	 * @param align the alignment value
	 */
	public FlowLayout(String align) {
		this(align, 0, 0);
	}

	/**
	 * Creates a new flow layout manager with the indicated alignment and the
	 * indicated horizontal and vertical gaps.
	 * <p>
	 * The value of the alignment argument must be one of <code>"left"</code>,
	 * <code>"right"</code>, <code>"center"</code>, <code>"leading"</code>
	 * or <code>"trailing"</code>.
	 * 
	 * @param align the alignment value
	 * @param hgap the horizontal gap between components and between the
	 *        components and the borders of the <code>Container</code>
	 * @param vgap the vertical gap between components and between the
	 *        components and the borders of the <code>Container</code>
	 */
	public FlowLayout(String align, int hgap, int vgap) {
		super(getAlignment(align), hgap, vgap);
	}

	public void setAlignement(String align) {
		setAlignment(getAlignment(align));
	}

	public static Integer getAlignment(String align) {
		return alignment.get(align);
	}
}
