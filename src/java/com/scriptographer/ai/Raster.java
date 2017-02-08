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
 * File created on 08.12.2004.
 */

package com.scriptographer.ai;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
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
import java.io.File;
import java.io.IOException;
import java.net.URL;

import com.scratchdisk.util.IntegerEnumUtils;
import com.scratchdisk.util.NetUtils;

/**
 * The Raster item represents an image in an Illustrator document.
 * 
 * @author lehni
 */
public class Raster extends Item {

	// native pointer to an attached data struct:
	private int data = 0;

	protected Raster(int handle, int docHandle, boolean created) {
		super(handle, docHandle, created);
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
	 * @param image the AWT image to be converted to a raster item
	 */
	public Raster(Image image) {
		this(getCompatibleType(image), image.getWidth(null),
				image.getHeight(null));
		drawImage(image, 0, 0);
	}

	/**
	 * Creates a raster item from an UI image.
	 * @param image the UI image to be converted to a raster item
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
	 * <code>
	 * var file = new java.io.File('/folder/image.jpg');
	 * var raster = new Raster(file);</code>
	 * 
	 * @param file the image file to be loaded
	 */
	public Raster(File file) {
		this(file, false);
	}

	/**
	 * Creates a raster image from a URL.
	 * This blocks until the image is loaded or an error occured.
	 * 
	 * Sample code:
	 * <code>
	 * var url = new java.net.URL('http://www.server.com/image.jpg');
	 * var raster = new Raster(url);</code>
	 *
	 * @param url the URL of the image to load
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

	/**
	 * The size of the raster in pixels.
	 */
	public native Size getSize();

	/**
	 * @jshide
	 */
	public void setSize(int width, int height) {
		// changing the size creates a new art handle internally
		handle = nativeConvert((short) -1, width, height);
	}

	public void setSize(com.scriptographer.ui.Size size) {
		setSize(size.width, size.height);
	}

	/**
	 * The width of the raster in pixels.
	 */
	public int getWidth() {
		return (int) getSize().width;
	}

	/**
	 * The height of the raster in pixels.
	 */
	public int getHeight() {
		return (int) getSize().height;
	}

	/**
	 * Pixels per inch of the raster at it's current size.
	 */
	public Size getPpi() {
		Matrix matrix = getMatrix();
		Point orig = new Point(0, 0).transform(matrix);
		Point u = new Point(1, 0).transform(matrix).subtract(orig);
		Point v = new Point(0, 1).transform(matrix).subtract(orig);
		return new Size(
			72.0 / u.getLength(),
			72.0 / v.getLength()
		);
	}

	private native int nativeGetType();

	/**
	 * The color type of the raster.
	 */
	public ColorType getType() {
		return IntegerEnumUtils.get(ColorType.class, nativeGetType());
	}

	public void setType(ColorType type) {
		// changing the type creates a new art handle internally
		handle = nativeConvert(type.value, -1, -1);
	}

	/**
	 * The Java2D color model of the raster.
	 * @jshide
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
	
	/**
	 * @jshide
	 */
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

	/**
	 * @jshide
	 */
	public BufferedImage createCompatibleImage(int width, int height) {
		ColorModel cm = getColorModel();
		WritableRaster raster = cm.createCompatibleWritableRaster(width, height);
		return new BufferedImage(cm, raster, false, null);
	}
	
	public BufferedImage getSubImage(int x, int y, int width, int height) {
		if (width == -1 || height == -1) {
			Size size = getSize();
			if (width == -1)
				width = (int) size.width;
			if (height == -1)
				height = (int) size.height;
		}
		BufferedImage img = createCompatibleImage(width, height);
		Graphics2D g2d = img.createGraphics();
		g2d.setColor(java.awt.Color.WHITE);
//		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0));
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
	
	/**
	 * Traces the raster.
	 */
	public Tracing trace() {
		return new Tracing(this);
	}

	private Matrix inverse;
	private int inverseVersion = -1;

	private Matrix getInverseMatrix() {
		// Cache the inverse matrix as it might be used often for rastering images
		if (needsUpdate(inverseVersion)) {
			inverse = this.getMatrix().invert();
			inverseVersion = version;
		}
		return inverse;
	}
	
	/**
	 * @jshide
	 */
	public Color getAverageColor(Shape shape) {
//		Rectangle2D rect = shape.getBounds2D();
		GeneralPath path;
		int width = getWidth();
		int height = getHeight();
		int startX = 0;
		int startY = 0;
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
			// Fetch the sub image to iterate over and calculate average colors
			// from
			// Crop to the maximum size.
			Rectangle2D.intersect(bounds,
					new Rectangle2D.Double(startX, startY, width, height),
					bounds);
			width = (int) Math.ceil(bounds.getWidth());
			height = (int) Math.ceil(bounds.getHeight());
			// Are we completely outside the raster? If so, return null
			if (width <= 0 || height <= 0)
				return null;
			startX = (int) Math.floor(bounds.getX());
			startY = (int) Math.floor(bounds.getY());
		} else {
			path = null;
		}
		BufferedImage img = getSubImage(startX, startY, width, height);

	//	Raster check = new Raster(img);
	//	check.setPosition(rect.getCenterX(), rect.getCenterY());

		WritableRaster raster = img.getRaster();
		byte[] data = (byte[]) raster.getDataElements(0, 0, null);
		float components[] = new float[data.length];
		for (int i = 0; i < data.length; i++)
			components[i] = data[i] & 0xff;
		long total = 1;
		for (int y = 0; y < height; y++) {
			for (int x = (y == 0) ? 1 : 0; x < width; x++) {
				if (path == null || path.contains(x + startX, y + startY)) {
					data = (byte[]) raster.getDataElements(x, height - 1 - y, data);
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

	/**
	 * The average color of the raster.
	 */
	public Color getAverageColor() {
		return getAverageColor((Shape) null);
	}

	/**
	 * {@grouptitle Average Color}
	 * Calculates the average color of the image within the given path. This can
	 * be used for creating raster image effects.
	 * 
	 * @param path
	 * @return the average color contained in the area covered by the specified
	 *         path.
	 */
	public Color getAverageColor(PathItem path) {
		return getAverageColor(path.toShape());
	}

	/**
	 * Calculates the average color of the image within the given point in the
	 * document. This can be used for creating raster image effects.
	 * 
	 * @param point
	 * @return the average color contained in the area described by the specified
	 *         point.
	 */
	public Color getAverageColor(Point point) {
		return getAverageColor(new Rectangle(point.subtract(0.5, 0.5), new Size(1, 1)));
	}

	/**
	 * Calculates the average color of the image within the given rectangle.
	 * This can be used for creating raster image effects.
	 * 
	 * @param rect
	 * @return the average color contained in the area described by the specified
	 *         rectangle.
	 */
	public Color getAverageColor(Rectangle rect) {
		return getAverageColor(rect.toRectangle2D());
	}

	/**
	 * {@grouptitle Pixels}
	 * 
	 * Gets the color of a pixel in the raster.
	 * @param x
	 * @param y
	 */
	public native Color getPixel(int x, int y);

	/**
	 * Sets the color of a pixel in the raster.
	 * Sample code:
	 * <code>
	 * // Creates an RGB raster of 1px*1px
	 * var raster = new Raster(Color.TYPE_RGB,1,1);
	 * 
	 * // Changes the color of the first pixel to red
	 * var redColor = new RGBColor(1,0,0);
	 * raster.setPixel(0,0,redColor)</code>
	 * 
	 * @param x
	 * @param y
	 */
	public native void setPixel(int x, int y, Color color);

	/**
	 * Gets the color of a pixel in the raster.
	 * @param x
	 * @param y
	 */
	public Color getPixel(Point point) {
		return getPixel((int) point.x, (int) point.y);
	}

	/**
	 * Sets the color of a pixel in the raster.
	 * 
	 * Sample code:
	 * <code>
	 * // Creates an RGB raster of 1px*1px
	 * var raster = new Raster(Color.TYPE_RGB,1,1);
	 * 
	 * // Changes the color of the first pixel to red
	 * var redColor = new RGBColor(1,0,0);
	 * var point = new Point(0,0);
	 * raster.setPixel(point,redColor)</code>
	 * 
	 * @param x
	 * @param y
	 */
	public void setPixel(Point point, Color color) {
		setPixel((int) point.x, (int) point.y, color);
	}
	
	private native void nativeSetPixels(byte[] data, int numComponents, int x,
			int y, int width, int height);

	private native void nativeGetPixels(byte[] data, int numComponents, int x,
			int y, int width, int height);

	native protected void finalize();
}
