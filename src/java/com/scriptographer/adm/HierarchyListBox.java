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
 * File created on 03.01.2005.
 *
 * $RCSfile: HierarchyListBox.java,v $
 * $Author: lehni $
 * $Revision: 1.2 $
 * $Date: 2005/03/10 22:48:43 $
 */

package com.scriptographer.adm;

import java.awt.geom.Rectangle2D;

public class HierarchyListBox extends ListBox {
	public final static int
			// hathaway : 8/22/02 : Added to support creation of hierarchical palette popups for Pangea
			// Popup menu creation options
		OPTION_HIERARCHY_POPUP = (1 << 0);

	public HierarchyListBox(Dialog dialog, Rectangle2D bounds, int style, int options) {
		super(dialog, Item.TYPE_HIERARCHY_LISTBOX, bounds, style, options);
	}

	public HierarchyListBox(Dialog dialog, Rectangle2D bounds, int style) {
		this(dialog, bounds, style, 0);
	}

	public HierarchyListBox(Dialog dialog, Rectangle2D bounds) {
		this(dialog, bounds, 0, 0);
	}

	public List getList() {
		if (list == null)
			list = new HierarchyList(this);
		return list;
	}
}
