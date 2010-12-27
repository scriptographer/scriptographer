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
 * File created on May 11, 2007.
 */

package com.scratchdisk.util;

import java.awt.Image;
import java.awt.MediaTracker;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @author lehni
 *
 */
public class NetUtils {

	private NetUtils() {
	}

	/**
	 * Downloads a URL resource into a temporary file on the disk.
	 * @param url
	 * @throws IOException
	 */
	public static File loadFile(URL url, String prefix) throws IOException {
		String name = url.getPath();
		int pos = name.lastIndexOf('.');
		if (pos != -1) {
			String ext = name.substring(pos);
			InputStream in = url.openStream();
			// Create temp file:
			File file = File.createTempFile(prefix, ext);
			// Delete temp file when program exits.
			file.deleteOnExit();
			// Write to temp file
			FileOutputStream out = new FileOutputStream(file);
			int bytesRead;
			byte[] buf = new byte[4 * 1024]; // 4K buffer
			while ((bytesRead = in.read(buf)) != -1) {
				out.write(buf, 0, bytesRead);
			}
			in.close();
			out.close();
			return file;
		}
		return null;
	}

	public static File loadFile(URL url) throws IOException {
		return loadFile(url, "");
	}

	/**
	 * Load an image from a given URL. This blocks until the image is
	 * loaded or an error occured.
	 * @param url the URL of the image to load.
	 * @return the loaded image
	 * @throws InterruptedException
	 */
	public static Image loadImage(URL url) throws InterruptedException {
		MediaTracker tracker = new MediaTracker(new java.awt.Container());
		Image img = java.awt.Toolkit.getDefaultToolkit().createImage(url);
		tracker.addImage(img, 0);
		tracker.waitForAll();
		return img;
	}
}
