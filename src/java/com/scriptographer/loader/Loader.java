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
 * File created on 05.01.2005.
 *
 * $RCSfile: Loader.java,v $
 * $Author: lehni $
 * $Revision: 1.1 $
 * $Date: 2005/02/23 22:01:02 $
 */

package com.scriptographer.loader;

import java.net.URLClassLoader;
import java.net.URL;
import java.net.MalformedURLException;
import java.lang.reflect.Method;
import java.io.*;

public class Loader {
	static URLClassLoader loader = null;
	static String homeDir;

	public static void init(String dir) throws MalformedURLException {
		homeDir = dir;
		// filter out all the files in lib that are not jar or zip files and create a
		// URL array of it:
		File libDir = new File(homeDir + "/lib/");
		File[] libs = libDir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".jar") || name.endsWith(".zip");
			}
		});
		// add one more for the classes diretory:
		URL[] urls = new URL[libs.length + 1];
		// now add the urls from above
		for (int i = 0; i < libs.length; i++) {
			urls[i] = libs[i].toURL();
		}
		// and the classes
		urls[libs.length] = new URL("file://" + homeDir + "/classes/");

		loader = new URLClassLoader(urls);
	}

	/**
	 *
	 * @return a string, representing any errors that happened, or null if all went well
	 */
	public static String reload() {
		StringWriter stringWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(stringWriter);
		try {
			// first call destory in the currently loaded ScriptographerEngine class:
			Class cls = loader.loadClass("com.scriptographer.ScriptographerEngine");
			Method destroy = cls.getDeclaredMethod("destroy", new Class[] {});
			destroy.invoke(null, new Object[] {});
		} catch(Exception e) {
			e.printStackTrace(writer);
		}
		try {
			// now (re)load all:
			init(homeDir);
		} catch (Exception e) {
			e.printStackTrace(writer);
		}
		String errors = stringWriter.toString();
		return errors.length() == 0 ? null : errors;
	}

	/**
	 * To be called from the native environment. This replaces the static JNI_LoadClass, that allways
	 * depends on the initial loader. This one allows reloading classes from a realoaded classpath
	 * for reflection.
	 *
	 * @param name in the slash-format (e.g. "java/lang/Object")
	 * @return the loaded class
	 * @throws ClassNotFoundException
	 */
	private static Class loadClass(String name) throws ClassNotFoundException {
		return loader.loadClass(name.replace('/', '.'));
	}
}
