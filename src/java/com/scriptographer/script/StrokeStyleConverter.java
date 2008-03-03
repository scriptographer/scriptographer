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
 * File created on Mar 2, 2008.
 *
 * $Id$
 */

package com.scriptographer.script;

import com.scratchdisk.script.ArgumentConverter;
import com.scratchdisk.script.ArgumentReader;
import com.scriptographer.ai.Color;
import com.scriptographer.ai.StrokeStyle;

/**
 * @author lehni
 *
 */
public class StrokeStyleConverter extends ArgumentConverter {

	public Object convert(ArgumentReader reader, Object from) {
		if (from instanceof Color) {
			return new StrokeStyle((Color) from, null, null, null, null, null, null, null);
		} else {
			return new StrokeStyle(reader);
		}
	}
}
