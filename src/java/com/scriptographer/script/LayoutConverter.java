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
 * File created on Feb 12, 2008.
 */

package com.scriptographer.script;

import java.awt.BorderLayout;
import java.awt.LayoutManager;

import com.scratchdisk.script.ArgumentConverter;
import com.scratchdisk.script.ArgumentReader;
import com.scriptographer.adm.layout.HorizontalLayout;
import com.scriptographer.adm.layout.TableLayout;

/**
 * @author lehni
 *
 */
public class LayoutConverter extends ArgumentConverter<LayoutManager> {

	public LayoutManager convert(ArgumentReader reader, Object from) {
		if (reader.isArray()) {
			String str = reader.readString();
			if (str != null) {
				// See if it's an available alignment for FlowLayout
				if (HorizontalLayout.getAlignment(str) != null) {
					// FlowLayout
					return new HorizontalLayout(
							str,
							reader.readInteger(0),
							reader.readInteger(0));
				} else {
					// TableLayout
					return new TableLayout(str,
							reader.readString(""),
							reader.readInteger(0),
							reader.readInteger(0));
				}
			} else {
				reader.revert();
				// Try if there's an array now:
				Object[] array = reader.readObject(Object[].class);
				if (array != null) {
					// TableLayout
					return new TableLayout(array,
							reader.readObject(Object[].class),
							reader.readInteger(0),
							reader.readInteger(0));
				} else {
					reader.revert();
					// BorderLayout
					return new BorderLayout(
							reader.readInteger(0),
							reader.readInteger(0));
				}
			}
		} else if (reader.isMap()) {
			if (reader.has("columns")) {
				// TableLayout
				String str = reader.readString("columns");
				if (str != null) {
					return new TableLayout(	str,
							reader.readString("rows", ""),
							reader.readInteger("", 0),
							reader.readInteger(0));
				} else {
					Object[] array = reader.readObject("columns",
							Object[].class);
					if (array != null) {
						return new TableLayout(array,
								reader.readObject("rows", Object[].class),
								reader.readInteger("hgap", 0),
								reader.readInteger("vgap", 0));
					} else {
						throw new RuntimeException(
								"Unsupported format for TableLayout");
					}
				}
			} else if (reader.has("alignment")) {
				// FlowLayout
				String alignment = reader.readString("alignment");
				if (HorizontalLayout.getAlignment(alignment) != null) {
					return new HorizontalLayout(
							alignment,
							reader.readInteger("hgap", 0),
							reader.readInteger("vgap", 0));
				} else {
					throw new RuntimeException(
							"Unsupported alignment for FlowLayout: "
							+ alignment);
				}
			} else {
				return new BorderLayout(
						reader.readInteger("hgap", 0),
						reader.readInteger("vgap", 0));
			}
		}
		return null;
	}
}
