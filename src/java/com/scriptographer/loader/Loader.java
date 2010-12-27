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
 * File created on 05.01.2005.
 */

package com.scriptographer.loader;

import java.net.URLClassLoader;
import java.net.URL;
import java.net.MalformedURLException;
import java.lang.reflect.Method;
import java.io.*;

/**
 * @author lehni
 */
public class Loader {
	static URLClassLoader loader = null;
	static String pluginPath;

	public static void init(String path) throws MalformedURLException {
		pluginPath = path;
		File javaDir = new File(new File(pluginPath, "Core"), "Java");
		// Filter out all the files in lib that are not jar or zip files and
		// create a URL array of it:
		File libDir = new File(javaDir, "lib");
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".jar") || name.endsWith(".zip");
			}
		};
		File[] libs = libDir.listFiles(filter);
		// Add two more, for the classes and rhino directories:
		URL[] urls = new URL[libs.length + 2];
		// And classes and rhino first, libraries after, to get the
		// priorities right:
		urls[0] = new File(javaDir, "classes").toURI().toURL();
		// Rhino is usually loaded from lib, but during development
		// it can also be live compiled into the rhino folder, which
		// makes debugging easier:
		urls[1] = new File(javaDir, "rhino").toURI().toURL();
		
		// Now add the urls from above
		for (int i = 0; i < libs.length; i++)
			urls[i + 2] = libs[i].toURI().toURL();

		loader = new URLClassLoader(urls);
		// Set the new class loader as context class loader
		Thread.currentThread().setContextClassLoader(loader);
	}

	/**
	 * Reloads the Scriptographer Engine from a new class loader
	 * 
	 * @return a string, representing any errors that happened during reload, or
	 *         null if all went well
	 */
	public static String reload() {
		StringWriter stringWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(stringWriter);
		try {
			// First call destroy in the currently loaded ScriptographerEngine class:
			Class<?> cls = loader.loadClass("com.scriptographer.ScriptographerEngine");
			Method destroy = cls.getDeclaredMethod("destroy", new Class[] {});
			destroy.invoke(null, new Object[] {});
		} catch(Exception e) {
			e.printStackTrace(writer);
		}
		try {
			// Now (re)load all:
			init(pluginPath);
		} catch (Exception e) {
			e.printStackTrace(writer);
		}
		String errors = stringWriter.toString();
		return errors.length() == 0 ? null : errors;
	}

	/**
	 * To be called from the native environment. This replaces the static
	 * JNI_LoadClass, that always depends on the initial loader. This one
	 * allows reloading classes from a reloaded classpath for reflection.
	 * 
	 * @param name in the slash-format (e.g. "java/lang/Object")
	 * @return the loaded class
	 * @throws ClassNotFoundException
	 */
	protected static Class loadClass(String name) throws ClassNotFoundException {
		return loader.loadClass(name.replace('/', '.'));
	}
}
