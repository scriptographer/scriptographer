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
 * File created on 08.12.2004.
 *
 * $Id$
 */

package com.scriptographer.ai;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.Transparency;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
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

import com.scratchdisk.util.IntegerEnumUtils;
import com.scratchdisk.util.NetUtils;
import com.scriptographer.adm.Size;

/**
 * @author lehni
 */
public class Raster extends Item {

	// native pointer to an attached data struct:
	@SuppressWarnings("unused")
	private int data = 0;

	protected Raster(int handle) {
		super(handle);
	}

	private native int nativeConvert(int type, int width, int height);

	/**
	 * Creates a raster object
	 * 
	 * @param width
	 * @param height
	 * @param type
	 */
	public Raster(ColorType type, int width, int height) {
		super(TYPE_RASTER);
		nativeConvert(type != null ? type.value : -1, width, height);
	}

	/**
	 * An empty Raster image of the given color type
	 * @param type
	 */
	public Raster(ColorType type) {
		this(type, -1, -1);
	}

	/**
	 * Creates a raster item from an AWT image.
	 * @param image the AWT image to be converted to a raster item.
	 */
	public Raster(Image image) {
		this(getCompatibleType(image), image.getWidth(null),
				image.getHeight(null));
		drawImage(image, 0, 0);
	}

	/**
	 * Creates a raster item from an ADM image.
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
		this(null, -1, -1);
	}

	/**
	 * Creates a raster item from a local image file.
	 * 
	 * Sample code:
	 * <pre>
	 * var file = new java.io.File("/folder/image.jpg");
	 * var raster = new Raster(file);</pre>
	 * 
	 * @param file the image file to be loaded.
	 */
	public Raster(File file) {
		this(file, false);
	}

	/**
	 * Creates a raster image from an URL.
	 * This blocks until the image is loaded or an error occured.
	 * 
	 * Sample code:
	 * <pre>
	 * var url = new java.net.URL("http://www.server.com/image.jpg");
	 * var raster = new Raster(url);</pre>
     *
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

	/**
	 * @jsbean The width of the raster
	 * @return
	 */
	public int getWidth() {
		return getSize().width;
	}

	/**
	 * @jsbean The height of the raster
	 * @return
	 */
	public int getHeight() {
		return getSize().height;
	}

	private native int nativeGetType();

	public ColorType getType() {
		return (ColorType) IntegerEnumUtils.get(ColorType.class,
				nativeGetType());
	}

	public void setType(ColorType type) {
		// changing the type creates a new art handle internally
		handle = nativeConvert(type.value, -1, -1);
	}

	/**
	 * Gets the color of a pixel in the raster.
	 * @param x
	 * @param y
	 * @return
	 */
	public native Color getPixel(int x, int y);

	/**
	 * Sets the color of a pixel in the raster.
	 * Sample code:
	 * <pre>
	 * // Creates an RGB raster of 1px*1px
	 * var raster = new Raster(Color.TYPE_RGB,1,1);
	 * 
	 * // Changes the color of the first pixel to red
	 * var redColor = new RGBColor(1,0,0);
	 * raster.setPixel(0,0,redColor)</pre>
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public native void setPixel(int x, int y, Color color);

	/**
	 * Gets the color of a pixel in the raster.
	 * @param x
	 * @param y
	 * @return
	 */
	public Color getPixel(Point point) {
		return getPixel((int) point.x, (int) point.y);
	}

	/**
	 * Sets the color of a pixel in the raster.
	 * 
	 * Sample code:
	 * <pre>
	 * // Creates an RGB raster of 1px*1px
	 * var raster = new Raster(Color.TYPE_RGB,1,1);
	 * 
	 * // Changes the color of the first pixel to red
	 * var redColor = new RGBColor(1,0,0);
	 * var point = new Point(0,0);
	 * raster.setPixel(point,redColor)</pre>
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public void setPixel(Point point, Color color) {
		setPixel((int) point.x, (int) point.y, color);
	}

	/**
	 * @jsbean REturns the Java2D color model of the raster
	 * @return
	 */
	public ColorModel getColorModel() {
		ColorType type = getType();
		ColorModel cm = null;
		switch (type) {
		case RGB:
		case ARGB:
			cm = new ComponentColorModel(RGBColor.getColorSpace(),
				type.alpha ? new int[] { 8, 8, 8, 8 } : new int [] { 8, 8, 8 },
				type.alpha, false,
				type.alpha ? Transparency.TRANSLUCENT : Transparency.OPAQUE,
				DataBuffer.TYPE_BYTE
			);
		break;
		case CMYK:
		case ACMYK:
			cm = new ComponentColorModel(CMYKColor.getColorSpace(),
				type.alpha ? new int[] { 8, 8, 8, 8, 8 } :
					new int [] { 8, 8, 8, 8 },
				type.alpha, false,
				type.alpha ? Transparency.TRANSLUCENT : Transparency.OPAQUE,
				DataBuffer.TYPE_BYTE
			);
		break;
		case GRAY:
		case AGRAY:
			cm = new ComponentColorModel(GrayColor.getColorSpace(),
				type.alpha ? new int[] { 8, 8 } : new int [] { 8 },
				type.alpha, false,
				type.alpha ? Transparency.TRANSLUCENT : Transparency.OPAQUE,
				DataBuffer.TYPE_BYTE
			);
		break;
		case BITMAP:
		case ABITMAP:
			// create an IndexColorModel with two colors, black and white:
			// black is the transparent color in case of an alpha image
			cm = new IndexColorModel(2,
				2,
				new byte[] { 0, (byte) 255 },
				new byte[] { 0, (byte) 255 },
				new byte[] { 0, (byte) 255 },
				type.alpha ? 0 : -1
			);
		break;
		}
		return cm;
	}
	
	public static ColorType getCompatibleType(Image image) {
		if (image instanceof BufferedImage) {
			ColorModel cm = ((BufferedImage) image).getColorModel();
			int type = cm.getColorSpace().getType();
			boolean alpha = cm.hasAlpha();
			if (type == ColorSpace.TYPE_RGB) {
				return alpha ? ColorType.ARGB : ColorType.RGB;
			} else if (type == ColorSpace.TYPE_CMYK) {
				return alpha ? ColorType.ACMYK : ColorType.CMYK;
			} if (type == ColorSpace.TYPE_GRAY) {
				return alpha ? ColorType.AGRAY : ColorType.GRAY;
			}
		}
		return null;
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
		Graphics2D g2d = img.createGraphics();
		g2d.setColor(java.awt.Color.WHITE);
		g2d.fillRect(0, 0, width, height);
		g2d.dispose();
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

	private Matrix inverse;
	private int inverseVersion = -1;

	private Matrix getInverseMatrix() {
		// Cache the inverse matrix as it might be used often for rastering images
		if (inverseVersion != version) {
			inverse = this.getMatrix().invert();
			inverseVersion = version;
		}
		return inverse;
	}
	
	/**
	 * Calculate the average color of the image within the given shape. This can
	 * be used for creating raster image effects.
	 * 
	 * @param shape
	 * @return the average color contained in the area covered by the specified
	 *         shape.
	 */
	public Color getAverageColor(Shape shape) {
		GeneralPath path;
		int width, height, startX, startY;
		if (shape != null) {
			Matrix inverse = getInverseMatrix();
			if (inverse == null)
				return null;
			// Create a transformed path. This is faster than
			// path.clone() / path.transform(at);
			PathIterator pi = shape.getPathIterator(inverse.toAffineTransform());
			path = new GeneralPath();
			path.setWindingRule(pi.getWindingRule());
			path.append(pi, false);
			Rectangle2D bounds = path.getBounds2D();
			// Fetch the sub image to iterate over and calculate average colors from
			width = (int) Math.ceil(bounds.getWidth());
			height = (int) Math.ceil(bounds.getHeight());
			startX = (int) Math.floor(bounds.getMinX());
			startY = (int) Math.floor(bounds.getMinY());
		} else {
			width = getWidth();
			height = getHeight();
			startX = 0;
			startY = -height;
			path = null;
		}
		BufferedImage img = getSubImage(startX, -startY - height, width, height);
		WritableRaster raster = img.getRaster();
		byte[] data = (byte[]) raster.getDataElements(0, 0, null);
		float components[] = new float[data.length];
		for (int i = 0; i < data.length; i++)
			components[i] = data[i] & 0xff;
		long total = 1;
		for (int y = 0; y < height; y++) {
			for (int x = (y == 0) ? 1 : 0; x < width; x++) {
				if (path == null || path.contains(x + startX, y + startY)) {
					data = (byte[]) raster.getDataElements(x, height - y, data);
					for (int i = 0; i < data.length; i++)
						components[i] += data[i] & 0xff;
					total++;
				}
			}
		}
		total *= 255;
		for (int i = 0; i < components.length; i++)
			components[i] = components[i] / total;
		// Return colors
		if (components.length == 4) return new CMYKColor(components);
		else if (components.length == 3) return new RGBColor(components);
		else return new GrayColor(components);
	}

	public Color getAverageColor() {
		return getAverageColor((Shape) null);
	}

	public Color getAverageColor(Path path) {
		return getAverageColor(path.toShape());
	}

	public Color getAverageColor(Rectangle rect) {
		return getAverageColor(rect.toRectangle2D());
	}

	private native void nativeSetPixels(byte[] data, int numComponents, int x,
			int y, int width, int height);

	private native void nativeGetPixels(byte[] data, int numComponents, int x,
			int y, int width, int height);

	native protected void finalize();
}
