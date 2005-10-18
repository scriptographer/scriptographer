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
 * File created on 15.02.2005.
 *
 * $RCSfile: CommitManager.java,v $
 * $Author: lehni $
 * $Revision: 1.3 $
 * $Date: 2005/10/18 15:31:15 $
 */

package com.scriptographer;

import java.util.HashMap;
import java.util.Iterator;

public class CommitManager {
	private CommitManager() {
		// Don't let anyone instantiate this class.
	}

	private static HashMap commitables = new HashMap();

	public static void commit() {
		if (commitables.size() > 0) {
			for (Iterator iterator = commitables.keySet().iterator(); iterator.hasNext();) {
				((Commitable) iterator.next()).commit();
			}
			commitables.clear();
		}
	}

	public static void markDirty(Commitable commitable) {
		commitables.put(commitable, commitable);
	}
}
