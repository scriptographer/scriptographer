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
 * File created on 08.12.2004.
 *
 * $RCSfile: Raster.java,v $
 * $Author: lehni $
 * $Revision: 1.3 $
 * $Date: 2005/03/30 08:21:33 $
 */

package com.scriptographer.ai;

import java.awt.Dimension;
import java.awt.Transparency;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.awt.color.ColorSpace;

import com.scriptographer.util.Handle;

public class Raster extends Art {

	// native pointer to an attached data struct:
	private int rasterData = 0;

	public Raster(Handle handle) {
		super(handle);
	}

	/**
	 * Creates a raster object
	 * 
	 * @param document
	 * @param width
	 * @param height
	 * @param type Color.TYPE_*
	 */
	public Raster(Document document, int type, int width, int height) {
		super(document, TYPE_RASTER);
		nativeConvert(type, width, height);
	}
	
	public Raster(Document document, int type) {
		this(document, type, -1, -1);
	}
	
	public Raster(Document document, java.awt.Image image) {
		this(document, getCompatibleType(image), image.getWidth(null), image.getHeight(null));
		drawImage(image, 0, 0);
	}
	
	public Raster(Document document, com.scriptographer.adm.Image image) {
		// TODO: handle this case directly, without converting back and from
		// a java BufferedImage, through native code!
		this(document, image.getImage());
	}

	public Raster(Document document) {
		this(document, -1, -1, -1);
	}

	public Raster(int type, int width, int height) {
		this(null, type, width, height);
	}

	public Raster(int type) {
		this(null, type, -1, -1);
	}

	public Raster(java.awt.Image image) {
		this(null, image);
	}

	public Raster(com.scriptographer.adm.Image image) {
		this(null, image);
	}

	public Raster() {
		this(null, -1, -1, -1);
	}

	private native int nativeConvert(int type, int width, int height);

	public native Dimension getSize();
	
	public void setSize(int width, int height) {
		// changing the size creates a new art handle internally
		handle = nativeConvert(-1, width, height);
	}

	public void setSize(Dimension size) {
		setSize(size.width, size.height);
	}

	public void setSize(Point2D size) {
		setSize((int) size.getX(), (int) size.getY());
	}
	
	public int getWidth() {
		return getSize().width;
	}
	
	public int getHeight() {
		return getSize().height;
	}
	
	public native int getType();
	
	public void setType(int type) {
		// changing the type creates a new art handle internally
		handle = nativeConvert(type, -1, -1);
	}

	public native Color getPixel(int x, int y);
	
	public native void setPixel(int x, int y, Color color);
	
	public Color getPixel(Point2D pt) {
		return getPixel((int) pt.getX(), (int) pt.getY());
	}
	
	public void setPixel(Point2D pt, Color color) {
		setPixel((int) pt.getX(), (int) pt.getY(), color);
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
					alpha ? new int[] { 8, 8, 8, 8, 8 } : new int [] { 8, 8, 8, 8 },
					alpha, false,
					alpha ? Transparency.TRANSLUCENT : Transparency.OPAQUE,
					DataBuffer.TYPE_BYTE
				);
			} break;
			case Color.TYPE_GRAY:
			case Color.TYPE_AGRAY: {
				boolean alpha = type == Color.TYPE_AGRAY;
				cm = new ComponentColorModel(Grayscale.getColorSpace(),
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
	
	public static int getCompatibleType(java.awt.Image image) {
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
			Dimension size = getSize();
			if (width == -1)
				width = size.width;
			if (height == -1)
				height = size.height;
		}
		BufferedImage img = createCompatibleImage(width, height);
		WritableRaster raster = img.getRaster();
		byte[] data = data = ((DataBufferByte) raster.getDataBuffer()).getData();
		nativeGetPixels(data, raster.getNumBands(), x, y, width, height);
		return img;
	}
	
	public BufferedImage getImage() {
		return getSubImage(0, 0, -1, -1);
	}
	
	public void drawImage(java.awt.Image image, int x, int y) {
		BufferedImage buf;
		// if image is already a compatible BufferedImage, use it. Otherwise create
		// a new one:
		if (image instanceof BufferedImage && 
			getColorModel().isCompatibleSampleModel(((BufferedImage) image).getSampleModel())) {
			buf = (BufferedImage) image;
		} else {
			buf = createCompatibleImage(image.getWidth(null), image.getHeight(null));
			buf.createGraphics().drawImage(image, x, y, null);
		}
		WritableRaster raster = buf.getRaster();
		byte[] data = data = ((DataBufferByte) raster.getDataBuffer()).getData();
		nativeSetPixels(data, raster.getNumBands(), x, y, buf.getWidth(), buf.getHeight());
	}
	
	public void setImage(java.awt.Image image) {
		setSize(image.getWidth(null), image.getHeight(null));
		drawImage(image, 0, 0);
	}

	private native void nativeSetPixels(byte[] data, int numComponents, int x, int y, int width, int height);
	
	private native void nativeGetPixels(byte[] data, int numComponents, int x, int y, int width, int height);

	native protected void finalize();
}
