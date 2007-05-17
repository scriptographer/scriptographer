/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2007 Juerg Lehni, http://www.scratchdisk.com.
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
 * File created on 08.12.2004.
 *
 * $Id$
 */

package com.scriptographer.ai;

import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.awt.color.ColorSpace;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import com.scratchdisk.util.NetUtils;
import com.scriptographer.adm.Size;

/**
 * @author lehni
 */
public class Raster extends Art {

	// native pointer to an attached data struct:
	private int data = 0;

	protected Raster(int handle) {
		super(handle);
	}

	private native int nativeConvert(short type, int width, int height);

	/**
	 * Creates a raster object
	 * 
	 * @param width
	 * @param height
	 * @param type Color.TYPE_*
	 */
	public Raster(short type, int width, int height) {
		super(TYPE_RASTER);
		nativeConvert(type, width, height);
	}

	/**
	 * An empty Raster image of the given color type
	 * @param type Color.TYPE_*
	 */
	public Raster(short type) {
		this(type, -1, -1);
	}

	/**
	 * Creeates a raster item from an AWT image.
	 * @param image the AWT image to be converted to a raster item.
	 */
	public Raster(Image image) {
		this(getCompatibleType(image), image.getWidth(null),
				image.getHeight(null));
		drawImage(image, 0, 0);
	}

	/**
	 * Creeates a raster item from an ADM image.
	 * @param image the ADM image to be converted to a raster item.
	 */
	public Raster(com.scriptographer.adm.Image image) {
		// TODO: handle this case directly, without converting back and from
		// a java BufferedImage, through native code?
		this(image.getImage());
	}

	/**
	 * Creates an empty raster item.
	 */
	public Raster() {
		this((short) -1, -1, -1);
	}

	/**
	 * Creates a raster item from a local image file.
	 * @param file the image file to be loaded.
	 */
	public Raster(File file) {
		this(file, false);
	}

	/**
	 * Creates a raster image from an URL.
	 * This blocks until the image is loaded or an error occured.
	 * @param url the URL of the image to load.
	 * @throws IOException
	 */
	public Raster(URL url) throws IOException {
		// this(NetUtils.loadImage(url));
		// Immediatelly delete the downloaded file afterwards
		this(NetUtils.loadFile(url, "sg_"), true);
	}

	native private static int nativeCreate(File file);

	private Raster(File file, boolean deleteFile) {
		super(nativeCreate(file));
		if (deleteFile)
			file.delete();
	}

	public native Matrix getMatrix();

	public native void setMatrix(Matrix matrix);

	public native Size getSize();

	public void setSize(int width, int height) {
		// changing the size creates a new art handle internally
		handle = nativeConvert((short) -1, width, height);
	}

	public void setSize(Size size) {
		setSize(size.width, size.height);
	}

	public void setSize(Point size) {
		setSize((int) size.x, (int) size.y);
	}

	public int getWidth() {
		return getSize().width;
	}

	public int getHeight() {
		return getSize().height;
	}

	public native short getType();

	public void setType(short type) {
		// changing the type creates a new art handle internally
		handle = nativeConvert(type, -1, -1);
	}

	public native Color getPixel(int x, int y);

	public native void setPixel(int x, int y, Color color);

	public Color getPixel(Point point) {
		return getPixel((int) point.x, (int) point.y);
	}

	public void setPixel(Point point, Color color) {
		setPixel((int) point.x, (int) point.y, color);
	}

	public ColorModel getColorModel() {
		int type = getType();
		ColorModel cm = null;
		switch (type) {
			case Color.TYPE_RGB:
			case Color.TYPE_ARGB: {
				boolean alpha = type == Color.TYPE_ARGB;
				cm = new ComponentColorModel(RGBColor.getColorSpace(),
					alpha ? new int[] { 8, 8, 8, 8 } : new int [] { 8, 8, 8 },
					alpha, false,
					alpha ? Transparency.TRANSLUCENT : Transparency.OPAQUE,
					DataBuffer.TYPE_BYTE
				);
			}
			break;
			case Color.TYPE_CMYK:
			case Color.TYPE_ACMYK: {
				boolean alpha = type == Color.TYPE_ACMYK;
				cm = new ComponentColorModel(CMYKColor.getColorSpace(),
					alpha ? new int[] { 8, 8, 8, 8, 8 } :
						new int [] { 8, 8, 8, 8 },
					alpha, false,
					alpha ? Transparency.TRANSLUCENT : Transparency.OPAQUE,
					DataBuffer.TYPE_BYTE
				);
			} break;
			case Color.TYPE_GRAY:
			case Color.TYPE_AGRAY: {
				boolean alpha = type == Color.TYPE_AGRAY;
				cm = new ComponentColorModel(GrayColor.getColorSpace(),
					alpha ? new int[] { 8, 8 } : new int [] { 8 },
					alpha, false,
					alpha ? Transparency.TRANSLUCENT : Transparency.OPAQUE,
					DataBuffer.TYPE_BYTE
				);
				
			} break;
			case Color.TYPE_BITMAP:
			case Color.TYPE_ABITMAP: {
				boolean alpha = type == Color.TYPE_ABITMAP;
				// create an IndexColorModel with two colors, black and white:
				// black is the transparent color in case of an alpha image
				cm = new IndexColorModel(2,
					2,
					new byte[] { 0, (byte) 255 },
					new byte[] { 0, (byte) 255 },
					new byte[] { 0, (byte) 255 },
					alpha ? 0 : -1
				);
			} break;
		}
		return cm;
	}
	
	public static short getCompatibleType(Image image) {
		if (image instanceof BufferedImage) {
			ColorModel cm = ((BufferedImage) image).getColorModel();
			int type = cm.getColorSpace().getType();
			boolean alpha = cm.hasAlpha();
			if (type == ColorSpace.TYPE_RGB) {
				return alpha ? Color.TYPE_ARGB : Color.TYPE_RGB;
			} else if (type == ColorSpace.TYPE_CMYK) {
				return alpha ? Color.TYPE_ACMYK : Color.TYPE_CMYK;
			} if (type == ColorSpace.TYPE_GRAY) {
				return alpha ? Color.TYPE_AGRAY : Color.TYPE_GRAY;
			}
		}
		return -1;
	}

	public BufferedImage createCompatibleImage(int width, int height) {
		ColorModel cm = getColorModel();
		WritableRaster raster = cm.createCompatibleWritableRaster(width, height);
		return new BufferedImage(cm, raster, false, null);
	}
	
	public BufferedImage getSubImage(int x, int y, int width, int height) {
		if (width == -1 || height == -1) {
			Size size = getSize();
			if (width == -1)
				width = size.width;
			if (height == -1)
				height = size.height;
		}
		BufferedImage img = createCompatibleImage(width, height);
		WritableRaster raster = img.getRaster();
		byte[] data = ((DataBufferByte) raster.getDataBuffer()).getData();
		nativeGetPixels(data, raster.getNumBands(), x, y, width, height);
		return img;
	}
	
	public BufferedImage getImage() {
		return getSubImage(0, 0, -1, -1);
	}
	
	public void drawImage(Image image, int x, int y) {
		BufferedImage buf;
		// if image is already a compatible BufferedImage, use it. Otherwise create
		// a new one:
		if (image instanceof BufferedImage && 
			getColorModel().isCompatibleSampleModel(
					((BufferedImage) image).getSampleModel())) {
			buf = (BufferedImage) image;
		} else {
			buf = createCompatibleImage(image.getWidth(null),
					image.getHeight(null));
			buf.createGraphics().drawImage(image, x, y, null);
		}
		WritableRaster raster = buf.getRaster();
		byte[] data = ((DataBufferByte) raster.getDataBuffer()).getData();
		nativeSetPixels(data, raster.getNumBands(), x, y, buf.getWidth(),
				buf.getHeight());
	}
	
	public void setImage(Image image) {
		setSize(image.getWidth(null), image.getHeight(null));
		drawImage(image, 0, 0);
	}
	
	public Tracing trace() {
		return new Tracing(this);
	}

	private native void nativeSetPixels(byte[] data, int numComponents, int x,
			int y, int width, int height);

	private native void nativeGetPixels(byte[] data, int numComponents, int x,
			int y, int width, int height);

	native protected void finalize();
}
