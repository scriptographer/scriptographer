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
 * File created on Apr 17, 2008.
 */

package com.scriptographer.adm.layout;

import java.util.HashMap;

/**
 * @author lehni
 */
public class HorizontalLayout extends java.awt.FlowLayout {

	private static HashMap<String, Integer> alignMap = new HashMap<String, Integer>();

	static {
		alignMap.put("left", HorizontalLayout.LEFT);
		alignMap.put("center", HorizontalLayout.CENTER);
		alignMap.put("right", HorizontalLayout.RIGHT);
		alignMap.put("leading", HorizontalLayout.LEADING);
		alignMap.put("trailing", HorizontalLayout.TRAILING);
	}

	/**
	 * Constructs a new {@code FlowLayout} with a centered alignment and
	 * a default 0-unit horizontal and vertical gap.
	 */
	public HorizontalLayout() {
		super(CENTER, 0, 0);
	}

	/**
	 * Constructs a new {@code FlowLayout} with the specified alignment
	 * and a default 0-unit horizontal and vertical gap. The value of the
	 * alignment argument must be one of {@code "left"},
	 * {@code "right"}, {@code "center"}, {@code "leading"}
	 * or {@code "trailing"}.
	 * 
	 * @param align the alignment value
	 */
	public HorizontalLayout(String align) {
		this(align, 0, 0);
	}

	/**
	 * Creates a new flow layout manager with the indicated alignment and the
	 * indicated horizontal and vertical gaps.
	 * <p>
	 * The value of the alignment argument must be one of {@code "left"},
	 * {@code "right"}, {@code "center"}, {@code "leading"}
	 * or {@code "trailing"}.
	 * 
	 * @param align the alignment value
	 * @param hgap the horizontal gap between components and between the
	 *        components and the borders of the {@code Container}
	 * @param vgap the vertical gap between components and between the
	 *        components and the borders of the {@code Container}
	 */
	public HorizontalLayout(String align, int hgap, int vgap) {
		super(getAlignment(align), hgap, vgap);
	}

	public HorizontalLayout(int align, int hgap, int vgap) {
		super(align, hgap, vgap);
	}

	public void setAlignement(String align) {
		setAlignment(getAlignment(align));
	}

	public static Integer getAlignment(String align) {
		return alignMap.get(align);
	}
}
