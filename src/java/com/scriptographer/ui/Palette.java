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
 * File created on Feb 23, 2010.
 *
 * $Id$
 */

package com.scriptographer.ui;

import java.util.HashMap;
import java.util.Map;

import com.scratchdisk.script.Callable;
import com.scriptographer.ScriptographerEngine;
import com.scriptographer.ui.layout.TableLayout;

/**
 * @author lehni
 * 
 */
public class Palette extends FloatingDialog {
	private PaletteItem[] items = null;
	Map<String, Object> values;

	public Palette(String title, Map<String, Map> items,
			Map<String, Object> values) {
		super(new DialogOption[] {
				DialogOption.TABBED,
				DialogOption.SHOW_CYCLE
//				DialogOption.RESIZING
		});

		double version = ScriptographerEngine.getApplicationVersion();
		boolean upperCase = false;
		int extraWidth = 32;
		double factor = 1;
		if (version >= 14) { // CS4
			upperCase = true;
			extraWidth = 64;
		} else if (version >= 13) { // CS3
			factor = 1;
			extraWidth = 82;
		} else {
			// TODO: Test / Implement
		}
		// Calculate title size. Temporarily set bold font
		setFont(DialogFont.PALETTE_BOLD);
		int width = (int) Math.round(getTextSize(upperCase 
				? title.toUpperCase() : title).width * factor);
		setFont(DialogFont.PALETTE);
		// UI Requires 64px more to show title fully in palette windows.
		setMinimumSize(width + extraWidth, -1);
		this.items = PaletteItem.getItems(items, values);
		setTitle(title);
		createLayout(this, this.items, false, 0);
		setMargin(2, 2, 0, 4);
		if (values == null)
			values = new HashMap<String, Object>();
		this.values = values;
	}

	public Map<String, Object> getValues() {
		return values;
	}

	private Callable onChange = null;

	public Callable getOnChange() {
		return onChange;
	}

	public void setOnChange(Callable onChange) {
		this.onChange = onChange;
	}

	protected void onChange(PaletteItem item, String name, Object value) {
		values.put(name, value);
		if (onChange != null)
			ScriptographerEngine.invoke(onChange, this, name, value);
	}

	protected static TableLayout createLayout(Dialog dialog,
			PaletteItem[] items, boolean hasLogo, int extraRows) {
		// Add one more row as a filler in case there's less rows than the
		// height of the logo.
		if (hasLogo)
			extraRows++;
		double[] rows = new double[items.length + extraRows];
		for (int i = 0; i < rows.length; i++)
			rows[i] = TableLayout.PREFERRED;
		// Define the filler row, 2nd last
		if (hasLogo)
			rows[rows.length - extraRows] = TableLayout.FILL;
		else
			rows[rows.length - 1] = TableLayout.FILL;
		double[][] sizes = {
			hasLogo
				? new double[] { TableLayout.PREFERRED, TableLayout.FILL,
					TableLayout.PREFERRED }
				: new double[] { TableLayout.FILL, TableLayout.PREFERRED },
			rows
		};
		TableLayout layout = new TableLayout(sizes);
		dialog.setLayout(layout);

		if (hasLogo) {
			ImagePane logo = new ImagePane(dialog);
			logo.setImage(Dialog.getImage("logo.png"));
			logo.setMargin(-4, 4, -4, -4);
			// Logo uses all rows of items + filler row
			dialog.addToContent(logo, "0, 0, 0, " + (rows.length - extraRows)
					+ ", left, top");
		}

		int columnIndex = hasLogo ? 1 : 0;
		for (int i = 0; i < items.length; i++) {
			PaletteItem promptItem = items[i];
			if (promptItem != null) {
				String desc = promptItem.getLabel();
				if (desc != null) {
					TextPane descItem = new TextPane(dialog);
					descItem.setText(desc + ":");
					descItem.setMargin(0, 4, 0, 0);
					dialog.addToContent(descItem, columnIndex + ", " + i
							+ ", left, center");
				}
				Item valueItem = promptItem.createItem(dialog,
						new Border(1, 0, 1, 0));
				dialog.addToContent(valueItem, (columnIndex + 1) + ", " + i
						+ ", left, center");
			}
		}
		return layout;
	}
}
