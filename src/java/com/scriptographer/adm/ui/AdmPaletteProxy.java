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
 * File created on Jun 2, 2010.
 */

package com.scriptographer.adm.ui;

import java.util.LinkedHashMap;

import com.scriptographer.ScriptographerEngine;
import com.scriptographer.adm.Dialog;
import com.scriptographer.adm.DialogFont;
import com.scriptographer.adm.DialogOption;
import com.scriptographer.adm.FloatingDialog;
import com.scriptographer.adm.ImagePane;
import com.scriptographer.adm.Size;
import com.scriptographer.adm.layout.TableLayout;
import com.scriptographer.ui.Component;
import com.scriptographer.ui.Palette;
import com.scriptographer.ui.PaletteProxy;

/**
 * @author lehni
 *
 */
public class AdmPaletteProxy extends PaletteProxy {

	private FloatingDialog dialog;

	public AdmPaletteProxy(Palette palette, Component[] components) {
		super(palette);
		
		dialog = new FloatingDialog(new DialogOption[] {
				DialogOption.TABBED,
				DialogOption.SHOW_CYCLE,
				DialogOption.REMEMBER_PLACING
		}) {
			protected void onInitialize() {
				// Since palettes remember placing, we need to explicitly set
				// them visible when they are created.
				setVisible(true);
				super.onInitialize();
			}
		};

		double version = ScriptographerEngine.getIllustratorVersion();
		boolean upperCase = false;
		int extraWidth;
		if (version >= 15) { // CS4 and above
			upperCase = true;
			extraWidth = 67;
		} else if (version >= 14) { // CS4 and above
			upperCase = true;
			extraWidth = 64;
		} else if (version >= 13) { // CS3
			extraWidth = 82;
		} else {
			// TODO: Test / Implement
			extraWidth = 32;
		}
		// Calculate title size. Temporarily set bold font
		dialog.setFont(DialogFont.PALETTE_BOLD);
		String title = palette.getTitle();
		int width = Math.round(dialog.getTextSize(upperCase 
				? title.toUpperCase() : title).width);
		dialog.setFont(DialogFont.PALETTE);
		// UI Requires 64px more to show title fully in palette windows.
		dialog.setMinimumSize(width + extraWidth, -1);
		dialog.setTitle(title);
		createLayout(dialog, components, false, 0, 3);

		if (palette.hasLabels())
			dialog.setMargin(2, 2, 2, 4);
		else
			dialog.setMargin(2, -1, 2, -1);
	}

	public void update(boolean sizeChanged) {
		if (sizeChanged) {
			// Make sure size changes are taken into account and palette is
			// resized accordingly.
			Size size = dialog.getPreferredSize();
			if (!dialog.getSize().equals(size)) {
				// setSize internally causes doLayout to be called, no need
				// to call here too.
				dialog.setSize(dialog.getPreferredSize());
			} else {
				// Just call doLayout to realign things, as the total dialog
				// size has not changed.
				dialog.doLayout();
			}
		}
		dialog.update();
	}

	protected static TableLayout createLayout(Dialog dialog,
			Component[] components, boolean hasLogo, int extraRows, int gap) {
		// First collect all content in a LinkedHashMap, then create the layout
		// at the end, and add the items to it. This allows flexibility
		// regarding amount of rows, as needed by the ruler element that uses
		// two rows when it has a title.

		LinkedHashMap<String, com.scriptographer.adm.Component> content =
				new LinkedHashMap<String, com.scriptographer.adm.Component>();

		int column = hasLogo ? 1 : 0, row = 0;
		for (int i = 0; i < components.length; i++) {
			Component item = components[i];
			if (item != null) {
				AdmComponentProxy proxy = new AdmComponentProxy(item);
				row = proxy.addToContent(dialog, content, column, row);
			}
		}

		if (hasLogo) {
			ImagePane logo = new ImagePane(dialog);
			logo.setImage(AdmUiFactory.getImage("logo.png"));
			logo.setMargin(-4, 4, -4, -4);
			// Logo uses all rows of components + filler row
			content.put("0, 0, 0, " + row + ", left, top",
					logo);
			row++;
		}
	
		double[] rows = new double[row + extraRows];
		for (int i = 0; i < rows.length; i++)
			rows[i] = TableLayout.PREFERRED;

		// Define the filler row, 2nd last
		if (hasLogo)
			rows[rows.length - extraRows - 1] = TableLayout.FILL;
		else if (rows.length > 0)
			rows[rows.length - 1] = TableLayout.FILL;

		double[][] sizes = {
			hasLogo
				? new double[] { TableLayout.PREFERRED, TableLayout.PREFERRED,
					TableLayout.FILL }
				: new double[] { TableLayout.PREFERRED, TableLayout.FILL },
			rows
		};
		TableLayout layout = new TableLayout(sizes, 0, gap);
		dialog.setLayout(layout);
		dialog.setContent(content);

		return layout;
	}
}
