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
