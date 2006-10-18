/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2006 Juerg Lehni, http://www.scratchdisk.com.
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
 * $Revision: 1.11 $
 * $Date: 2006/10/18 14:08:29 $
 */

package com.scriptographer.adm;

import java.awt.Container;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.ImageProducer;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import javax.imageio.ImageIO;

import com.scriptographer.ai.Raster;

public class Image extends ADMObject {
	// an image can wrap its representation as an icon as well...
	private int iconHandle = 0;
	
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
		TYPE_ARGB = 1,
		TYPE_SCREEN = 2,
		TYPE_ASCREEN = 3;
	
	public Image(int width, int height, int type) {
		this.width = width;
		this.height = height;
		this.type = type;
		handle = nativeCreate(width, height, type);
	}
	
	public Object clone() {
		Image copy = new Image(width, height, type);
		copy.nativeSetPixels(handle, byteWidth * height);
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
					type = TYPE_ARGB;
					break;

				// indirect types, copying is needed:
				case BufferedImage.TYPE_4BYTE_ABGR:
				case BufferedImage.TYPE_4BYTE_ABGR_PRE:
				case BufferedImage.TYPE_BYTE_INDEXED: {
					BufferedImage tmp = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
					tmp.createGraphics().drawImage(buf, null, 0, 0);
					image = tmp;
					type = TYPE_ARGB;
				}
				break;
				default: {
					BufferedImage tmp = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
					tmp.createGraphics().drawImage(buf, null, 0, 0);
					buf = tmp;
					type = TYPE_ARGB;
				}
			}
		} else {
			width = image.getWidth(null);
			height = image.getHeight(null);
			buf = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			buf.getGraphics().drawImage(image, 0, 0, null);
			type = TYPE_ARGB;
		}
		handle = nativeCreate(width, height, type);
		DataBufferInt buffer = (DataBufferInt) buf.getRaster().getDataBuffer();
		int data[] = buffer.getData();
		nativeSetPixels(data, width, height, byteWidth);
	}
	
	public Image(Raster raster) {
		// TODO: handle this case directly, without converting back and from
		// a java BufferedImage, through native code in Raster (the oposite
		// of the Raster(Image admImage);
		this(raster.getImage());
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

	public Image(ImageProducer producer) throws IOException {
		this(checkImage(waitForImage(Toolkit.getDefaultToolkit().createImage(producer)), producer));
	}

	public Image(URL url) throws IOException {
//		this(checkImage(ImageIO.read(url), url));
		this(checkImage(waitForImage(Toolkit.getDefaultToolkit().createImage(url)), url));
	}

	public Image(String str) throws IOException {
		this(getURL(str));
	}
	
	public Image(InputStream in) throws IOException {
//		this(checkImage(ImageIO.read(in), in));
		this(checkImage(waitForImage(Toolkit.getDefaultToolkit().createImage(getBytes(in))), in));
	}

	private static java.awt.Image checkImage(java.awt.Image image, Object srcObj) throws IOException {
		if (image == null)
			throw new IOException("The specified image could not be read: " + srcObj);
		return image;
	}

	public static java.awt.Image waitForImage(java.awt.Image image) {
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
	
	private static byte[] getBytes(InputStream in) throws IOException {
		if (in == null)
			throw new IOException("InputStream is null");
		byte bytes[] = new byte[in.available()];
		in.read(bytes);
		return bytes;
	}

	private static URL getURL(String str) throws IOException {
		// the string could either be an url or a local filename, let's try both:
		URL url = null;
		try {
	        url = new URL(str);
	    } catch (MalformedURLException e) {
	        // try the local file now:
	    	// url = new URL("file://" + str);
	    	url = new File(str).toURL();
		}
		return url;
	}

	public int getCompatibleType() {
		switch(type) {
		case TYPE_ARGB:
		case TYPE_ASCREEN:
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
	
	public int getIconHandle() {
		if (iconHandle == 0)
			iconHandle = nativeCreateIcon();
		return iconHandle;
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
	 * and don't use the native setImage methods at all... some more work with the
	 * Tracker would be needed in order to emulate rollover behavior. But it may be worth
	 * geting around the memory consumption of this dirty hack here...
	 */
	public int createIconHandle() {
		Image img = ((Image) clone());
		int handle = img.nativeCreateIcon();
		// clear the handle so that nothing happens in finalize or destroy!
		img.handle = 0;
		img.iconHandle = 0;
		return handle;
	}
	
	public Drawer getDrawer() {
		if (drawer == null)
			drawer = new Drawer(nativeBeginDrawer(), this, false);
		return drawer;
	}
	
	public void dispose() {
		if (handle != 0) {
			if (drawer != null) {
				drawer.dispose();
				drawer = null;
			}
			nativeDestroy(handle, iconHandle);
			handle = 0;
			iconHandle = 0;
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
	private native void nativeDestroy(int handle, int iconHandle);

	private native void nativeSetPixels(int[] data, int width, int height, int byteWidth);
	private native void nativeGetPixels(int[] data, int width, int height, int byteWidth);
	private native void nativeSetPixels(int handle, int numBytes);
	
	private native int nativeCreateIcon();

	private native int nativeBeginDrawer();
	private native void nativeEndDrawer();
}
