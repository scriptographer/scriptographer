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
 * File created on Aug 26, 2007.
 */

package com.scratchdisk.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.IdentityHashMap;

/**
 * @author lehni
 *
 */
public class ClassUtils {
	private ClassUtils() {
	}

	public static String[] getServiceInformation(Class cls) {
		InputStream in = cls.getResourceAsStream(
				"/META-INF/services/" + cls.getName());
		if (in != null) {
			ArrayList<String> lines = new ArrayList<String>();
			try {
				BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
				for (String line = buffer.readLine(); line != null; line = buffer.readLine()) {
					if (!line.startsWith("#"))
						lines.add(line);
				}
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return lines.toArray(new String[lines.size()]);
		}
		return null;
	}

	public static Constructor getConstructor(Class<?> cls, Class[] args, IdentityHashMap<Class, Constructor> cache) {
		Constructor ctor = cache != null ? (Constructor) cache.get(cls) : null;
		if (ctor == null) {
			try {
				ctor = cls.getConstructor(args);
				if (cache != null)
					cache.put(cls, ctor);
			} catch (Exception e) {
			}
		}
		return ctor;
	}

	public static Constructor getConstructor(Class cls, Class[] args) {
		return getConstructor(cls, args, null);
	}
}
