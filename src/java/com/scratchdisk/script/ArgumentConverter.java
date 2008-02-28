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
 * File created on Feb 12, 2008.
 *
 * $Id$
 */

package com.scratchdisk.script;

import com.scratchdisk.util.ClassUtils;

/**
 * @author lehni
 *
 */
public abstract class ArgumentConverter {

	public static void loadConverters() {
		String[] converters = ClassUtils.getServiceInformation(ArgumentConverter.class);
		if (converters != null) {
			for (int i = 0; i < converters.length; i++) {
				String[] parts = converters[i].split(":");
				try {
					ArgumentConverter converter = (ArgumentConverter) Class.forName(parts[0]).newInstance();
					Class type = Class.forName(parts[1]);
					ArgumentReader.registerConverter(type, converter);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public abstract Object convert(ArgumentReader reader);
}
