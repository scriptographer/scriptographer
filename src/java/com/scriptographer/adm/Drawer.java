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
 * $Id$
 */

package com.scriptographer.adm;

import java.awt.Color;

/**
 * @author lehni
 */
public class Drawer extends NativeObject {
	// if this drawer draws into an image:
	private Image image;
	private boolean destroy;
	
	// ADMColor
	public static final int
		COLOR_BLACK = 0,
		COLOR_WHITE = 1,
		COLOR_HILITE = 2,
		COLOR_HILITE_TEXT = 3,
		COLOR_LIGHT = 4,
		COLOR_BACKGROUND = 5,
		COLOR_SHADOW = 6,
		COLOR_DISABLED = 7,
		COLOR_BUTTON_UP = 8,
		COLOR_BUTTON_DOWN = 9,
		COLOR_BUTTON_DOWN_SHADOW = 10,
		COLOR_TOOLTIP_BACKGROUND = 11,
		COLOR_TOOLTIP_FOREGROUND = 12,
		COLOR_WINDOW = 13,
		COLOR_FOREGROUND = 14,
		COLOR_TEXT = 15,
		COLOR_RED = 16,
		COLOR_TAB_BACKGROUND = 17,
		COLOR_ACTIVE_TAB = 18,
		COLOR_INACTIVE_TAB = 19;

	// ADMDrawMode
	public static final int
		MODE_NORMAL = 0,
		MODE_XOR = 1;

	// ADMRecolorStyle
	public static final int
		RECOLOR_NO = 0,
		RECOLOR_ACTIVE = 1,
		RECOLOR_INACTIVE = 2,
		RECOLOR_DISABLED = 3;

	protected Drawer(int handle, Image image, boolean destroy) {
		super(handle);
		this.image = image;
		this.destroy = destroy;
	}
	
	protected Drawer() {
		this(0, null, false);
	}
	
	/**
	 * This constructor is only used from the native environment for
	 * creating Drawers for Annotators. These have the destroy flag
	 * set and get destroyed in dispose()
	 * @param handle
	 */
	protected Drawer(int handle) {
		this(handle, null, true);
	}
	
	public void dispose() {
		if (image != null) {
			image.endDrawer();
			image = null;
		}
		if (destroy)
			nativeDestroy(handle);
		handle = 0;
	}
	
	private native void nativeDestroy(int handle);
	
	protected void finalize() {
		dispose();
	}

	/* 
	 * clear entire area of drawer
	 * 
	 */

	public native void clear();

	/* 
	 * bounds accessor
	 * 
	 */
	
	public native Rectangle getBoundsRect();

	/* 
	 * clipping
	 * 
	 */
	
	public native Rectangle getClipRect();
	public native void setClipRect(int x, int y, int width, int height);

	public void setClipRect(Rectangle rect) {
		setClipRect(rect.x, rect.y, rect.width, rect.height);
	}

	public native void intersectClipRect(int x, int y, int width, int height);

	public void intersectClipRect(Rectangle rect) {
		intersectClipRect(rect.x, rect.y, rect.width, rect.height);
	}

	public native void unionClipRect(int x, int y, int width, int height);

	public void unionClipRect(Rectangle rect) {
		unionClipRect(rect.x, rect.y, rect.width, rect.height);
	}

	public native void subtractClipRect(int x, int y, int width, int height);

	public void subtractClipRect(Rectangle rect) {
		subtractClipRect(rect.x, rect.y, rect.width, rect.height);
	}

	public native void setClipPolygon(Point[] points);
	public native void intersectClipPolygon(Point[] points);
	public native void unionClipPolygon(Point[] points);
	public native void subtractClipPolygon(Point[] points);
	
	/* 
	 * port origin
	 * 
	 */
	
	public native Point getOrigin();
	
	public native void setOrigin(int x, int y);
	
	public void setOrigin(Point point) {
		setOrigin(point.x, point.y);
	}
	
	/* 
	 * drawing state accessors
	 * 
	 */
	
	public native Color getColor();
	public native void setColor(Color color);
	public native void setColor(int colorType); // Drawer.COLOR_ segmentValues
	
	public native int getDrawMode(); // Drawer.MODE_ segmentValues
	public native void setDrawMode(int mode);
	
	public native int getFont(); // Dialog.FONT_ segmentValues
	public native void setFont(int font);

	/* 
	 * simple shape drawers
	 * 
	 */

	public native void drawLine(int x1, int y1, int x2, int y2);
	
	public void drawLine(Point p1, Point p2) {
		drawLine(p1.x, p1.y, p2.x, p2.y);
	}

	public native void drawPolygon(Point[] points);
	public native void fillPolygon(Point[] points);
	
	public native void drawRect(int x, int y, int width, int height);

	public void drawRect(Rectangle rect) {
		drawRect(rect.x, rect.y, rect.width, rect.height);
	}

	public native void fillRect(int x, int y, int width, int height);

	public void fillRect(Rectangle rect) {
		fillRect(rect.x, rect.y, rect.width, rect.height);
	}

	public native void clearRect(int x, int y, int width, int height);

	public void clearRect(Rectangle rect) {
		clearRect(rect.x, rect.y, rect.width, rect.height);
	}

	public native void drawSunkenRect(int x, int y, int width, int height);

	public void drawSunkenRect(Rectangle rect) {
		drawSunkenRect(rect.x, rect.y, rect.width, rect.height);
	}

	public native void drawRaisedRect(int x, int y, int width, int height);

	public void drawRaisedRect(Rectangle rect) {
		drawRaisedRect(rect.x, rect.y, rect.width, rect.height);
	}

	public native void invertRect(int x, int y, int width, int height);

	public void invertRect(Rectangle rect) {
		invertRect(rect.x, rect.y, rect.width, rect.height);
	}

	
	public native void drawOval(int x, int y, int width, int height);

	public void drawOval(Rectangle rect) {
		drawOval(rect.x, rect.y, rect.width, rect.height);
	}

	public native void fillOval(int x, int y, int width, int height);

	public void fillOval(Rectangle rect) {
		fillOval(rect.x, rect.y, rect.width, rect.height);
	}


	/* 
	 * image and icon drawing
	 * 
	 */

	/**
	 * Draws the image with a recoloring style
	 * @param image
	 * @param x
	 * @param y
	 * @param style Drawer.RECOLOR_ values
	 */
	public native void drawImage(Image image, int x, int y, int style);

	/**
	 * Draws the image with a recoloring style
	 * @param image
	 * @param point
	 * @param style Drawer.RECOLOR_ values
	 */
	public void drawImage(Image image, Point point, int style) {
		drawImage(image, point.x, point.y, style);
	}

	public native void drawImage(Image image, int x, int y);

	public void drawImage(Image image, Point point) {
		drawImage(image, point.x, point.y);
	}
	
	/**
	 * Draws the image centered in a rectangle, with a recoloring style
	 * 
	 * @param image
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param style style Drawer.RECOLOR_ values
	 */
	public native void drawImage(Image image, int x, int y,
			int width, int height, int style);

	/**
	 * Draws the image centered in a rectangle, with a recoloring style
	 * 
	 * @param image
	 * @param rect
	 * @param style style Drawer.RECOLOR_ values
	 */
	public void drawImage(Image image, Rectangle rt, int style) {
		drawImage(image, rt.x, rt.y, rt.width, rt.height, style);
	}

	public native void drawImage(Image image, int x, int y,
			int width, int height);	

	public void drawImage(Image image, Rectangle rt) {
		drawImage(image, rt.x, rt.y, rt.width, rt.height);
	}

	/* 
	 * text drawing
	 * 
	 */

	public native int getTextWidth(String text);
	
	public native void drawText(String text, int x, int y);

	public void drawText(String text, Point point) {
		drawText(text, point.x, point.y);
	}
	
	public native void drawTextLeft(String text, int x, int y,
			int width, int height);

	public void drawTextLeft(String text, Rectangle rect) {
		drawTextLeft(text, rect.x, rect.y, rect.width, rect.height);
	}
	
	public native void drawTextCentered(String text, int x, int y,
			int width, int height);

	public void drawTextCentered(String text, Rectangle rect) {
		drawTextCentered(text, rect.x, rect.y, rect.width, rect.height);
	}
	
	public native void drawTextRight(String text, int x, int y,
			int width, int height);

	public void drawTextRight(String text, Rectangle rect) {
		drawTextRight(text, rect.x, rect.y, rect.width, rect.height);
	}
	
	public native void drawTextInABox(String text, int x, int y,
			int width, int height);

	public void drawTextInABox(String text, Rectangle rect) {
		drawTextRight(text, rect.x, rect.y, rect.width, rect.height);
	}
	
	/* 
	 * standard arrows
	 * 
	 */

	public native void drawUpArrow(int x, int y, int width, int height);

	public void drawUpArrow(Rectangle rect) {
		drawUpArrow(rect.x, rect.y, rect.width, rect.height);
	}

	public native void drawDownArrow(int x, int y, int width, int height);

	public void drawDownArrow(Rectangle rect) {
		drawDownArrow(rect.x, rect.y, rect.width, rect.height);
	}

	public native void drawLeftArrow(int x, int y, int width, int height);

	public void drawLeftArrow(Rectangle rect) {
		drawLeftArrow(rect.x, rect.y, rect.width, rect.height);
	}

	public native void drawRightArrow(int x, int y, int width, int height);

	public void drawRightArrow(Rectangle rect) {
		drawRightArrow(rect.x, rect.y, rect.width, rect.height);
	}


	/* 
	 * drawer creation/destruction
	 * 
	 */

/*
	ADMDrawerRef ADMAPI (*Create(ADMPortRef inPortRef, const ADMRect* inBoundsRect, ADMFont inFont, ADMBoolean inForceRoman);	
	void ADMAPI (*Destroy(ADMDrawerRef inDrawer);
*/

	/* 
	 * platform port accessors
	 * 
	 */

/*
	ADMPortRef ADMAPI (*GetADMWindowPort(ADMWindowRef inWindowRef);
	void ADMAPI (*ReleaseADMWindowPort(ADMWindowRef inWindowRef, ADMPortRef inPort);
*/
	/* 
	 * font information
	 * 
	 */

	public native FontInfo getFontInfo();
	public native FontInfo getFontInfo(int font);

	/* 
	 * updatePoint area accessor
	 * 
	 */

	public native Rectangle getUpdateRect();

	/* 
	 * text measurement
	 * 
	 */
	
	public native int getTextRectHeight(int width, String text);

}
