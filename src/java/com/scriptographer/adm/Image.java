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
 * File created on 29.12.2004.
 *
 * $RCSfile: Image.java,v $
 * $Author: lehni $
 * $Revision: 1.2 $
 * $Date: 2005/03/07 13:35:06 $
 */

package com.scriptographer.adm;

import java.awt.image.*;
import java.awt.*;
import java.io.*;
import java.net.*;

import javax.imageio.ImageIO;

public class Image {
	private int imageRef = 0;
	// an image can wrap its representation as an icon as well...
	private int iconRef = 0;
	
	private int width;
	private int height;
	private int type;
	// these variables are set from nativeCreate
	private int byteWidth;
	private int bitsPerPixel;
	
	private Drawer drawer;

	// image types
	public final static int
		TYPE_RGB = 0,
		TYPE_RGB_ALPHA = 1,
		TYPE_OFFSCREEN = 2,
		TYPE_OFFSCREEN_ALPHA = 3;
	
	public Image(int width, int height, int type) {
		this.width = width;
		this.height = height;
		this.type = type;
		imageRef = nativeCreate(width, height, type);
	}
	
	public Object clone() {
		Image copy = new Image(width, height, type);
		copy.nativeSetPixels(imageRef, byteWidth * height);
		return copy;
	}

	public static Image getImage(Object obj) throws IOException {
		if (obj instanceof Image)
			return (Image) obj;
		else if (obj instanceof String)
			return new Image((String) obj);
		else if (obj instanceof File)
			return new Image((File) obj);
		else if (obj instanceof URL)
			return new Image((URL) obj);
		else
			return null;
	}
	
	public Image(java.awt.Image image) {
		BufferedImage buf;
		if (image instanceof BufferedImage) {
			buf = (BufferedImage) image;
			width = buf.getWidth();
			height = buf.getHeight();
			int imgType = buf.getType();
			switch(imgType) {
				// direct types:
				case BufferedImage.TYPE_INT_RGB:
					type = TYPE_RGB;
					break;
				case BufferedImage.TYPE_INT_ARGB:
				case BufferedImage.TYPE_INT_ARGB_PRE:
					type = TYPE_RGB_ALPHA;
					break;

				// indirect types, copying is needed:
				case BufferedImage.TYPE_4BYTE_ABGR:
				case BufferedImage.TYPE_4BYTE_ABGR_PRE:
				case BufferedImage.TYPE_BYTE_INDEXED: {
					BufferedImage tmp = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
					tmp.createGraphics().drawImage(buf, null, 0, 0);
					image = tmp;
					type = TYPE_RGB_ALPHA;
				}
				break;
				default: {
					BufferedImage tmp = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
					tmp.createGraphics().drawImage(buf, null, 0, 0);
					image = tmp;
					type = TYPE_RGB;
				}
			}
		} else {
			width = image.getWidth(null);
			height = image.getHeight(null);
			buf = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			buf.createGraphics().drawImage(image, 0, 0, null);
			type = TYPE_RGB_ALPHA;
		}
		imageRef = nativeCreate(width, height, type);
		DataBufferInt buffer = (DataBufferInt) buf.getRaster().getDataBuffer();
		int data[] = buffer.getData();
		nativeSetPixels(data, width, height, byteWidth);
	}

	// TODO: ImageIO (or sun graphics) on OS X have a bug with the headless mode.
	// some images cannot be used and throw a HeadlessException when used, others
	// work just fine. The workaround is to rely on the toolkit funtion.
	// The resolution is to use helma's image object and generator mechanism here
	// too:
	public Image(File file) throws IOException {
//		this(checkImage(ImageIO.read(file), file));
		this(checkImage(waitForImage(Toolkit.getDefaultToolkit().createImage(file.getPath())), file));
	}

	public Image(URL url) throws IOException {
//		this(checkImage(ImageIO.read(url), url));
		this(checkImage(waitForImage(Toolkit.getDefaultToolkit().createImage(url)), url));
	}

	public Image(String str) throws IOException {
		this(getURL(str));
	}

	private static java.awt.Image checkImage(java.awt.Image image, Object srcObj) {
		if (image == null)
			throw new RuntimeException("The specified image could not be read: " + srcObj);
		return image;
	}

	private static java.awt.Image waitForImage(java.awt.Image image) {
		MediaTracker mediaTracker = new MediaTracker(new Container());
		mediaTracker.addImage(image, 0);
		try {
			mediaTracker.waitForID(0);
			if (image.getWidth(null) == -1 || image.getHeight(null) == -1)
				image = null;
		} catch (InterruptedException e) {
			image = null;
		}
		return image;
	}

	private static URL getURL(String str) throws IOException {
		// the string could either be an url or a local filename, let's try both:
		URL url = null;
		try {
	        url = new URL(str);
	    } catch (MalformedURLException e) {
	        // try the local file now:
			url = new URL("file://" + str);
		}
		return url;
	}

	public int getCompatibleType() {
		switch(type) {
		case TYPE_RGB_ALPHA:
		case TYPE_OFFSCREEN_ALPHA:
			return BufferedImage.TYPE_INT_ARGB;
		default:
			return BufferedImage.TYPE_INT_RGB;
		}
	}

	/**
	 * fetches the pixels from the image and creates a BufferedImage from it
	 */
	public BufferedImage getImage() {
		BufferedImage img = new BufferedImage(width, height, getCompatibleType());
		DataBufferInt buffer = (DataBufferInt)img.getRaster().getDataBuffer();
		int data[] = buffer.getData();
		nativeGetPixels(data, width, height, byteWidth);
		return img;
	}
	
	public void setImage(BufferedImage image) {
		int imgType = getCompatibleType();
		if (image.getType() != imgType || image.getWidth() != width || image.getHeight() != height) {
			BufferedImage tmp = new BufferedImage(width, height, imgType);
			tmp.createGraphics().drawImage(image, 0, 0, null);
			image = tmp;
		}
		DataBufferInt buffer = (DataBufferInt)image.getRaster().getDataBuffer();
		int data[] = buffer.getData();
		nativeSetPixels(data, width, height, byteWidth);
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getByteWidth() {
		return byteWidth;
	}
	
	public int getBitsPerPixel() {
		return bitsPerPixel;
	}
	
	public int getIconRef() {
		if (iconRef == 0)
			iconRef = nativeCreateIcon();
		return iconRef;
	}
	
	/*
	 * This is pretty stupid: ADM seems to take care of destruction of attached pictures
	 * itself (for bezierList entries and dialog items). Therefore one image cannot be attached
	 * to more than on item or entry, otherwise it would be deleted several times.
	 * so in order to create an image to attach it somewhere, a new instance of the 
	 * image needs to be created everytime, and an icon needs to be created as well.
	 * We don't have to take care of the disposal, as the item or entry seems to do so.
	 * 
	 * TODO: workaround: handle images in the DrawProc, adjust entryTextRects accordingly
	 * and don't use the native setPicture methods at all... some more work with the
	 * Tracker would be needed in order to emulate rollover behavior. But it may be worth
	 * geting around the memory consumption of this dirty hack here...
	 */
	public int createIconRef() {
		Image img = ((Image) clone());
		int ref = img.nativeCreateIcon();
		// clear the imageRef so that nothing happens in finalize or destroy!
		img.imageRef = 0;
		img.iconRef = 0;
		return ref;
	}
	
	public Drawer getDrawer() {
		if (drawer == null)
			drawer = new Drawer(nativeBeginDrawer(), this);
		return drawer;
	}
	
	public void dispose() {
		if (imageRef != 0) {
			if (drawer != null) {
				drawer.dispose();
				drawer = null;
			}
			nativeDestroy(imageRef, iconRef);
			imageRef = 0;
			iconRef = 0;
		}
	}
	
	public void finalize() {
		dispose();
	}
	
	/**
	 * called by Drawer.dispose
	 */
	protected void endDrawer() {
		if (drawer != null) {
			nativeEndDrawer();
			drawer = null;
		}
	}

	private native int nativeCreate(int width, int height, int type);
	private native void nativeDestroy(int imageRef, int iconRef);

	private native void nativeSetPixels(int[] data, int width, int height, int byteWidth);
	private native void nativeGetPixels(int[] data, int width, int height, int byteWidth);
	private native void nativeSetPixels(int imageRef, int numBytes);
	
	private native int nativeCreateIcon();

	private native int nativeBeginDrawer();
	private native void nativeEndDrawer();
}
