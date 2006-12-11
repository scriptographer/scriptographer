package com.scriptographer.adm;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class Drawer extends ADMObject {
	// if this drawer draws into an image:
	private Image image;
	private boolean destroy;
	
	// ADMColor
	public final static int
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
	public final static int
		MODE_NORMAL = 0,
		MODE_XOR = 1;

	// ADMRecolorStyle
	public final static int
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

	public void setClipRect(Rectangle2D rect) {
		setClipRect((int) rect.getX(), (int) rect.getY(), (int) rect.getWidth(), (int) rect.getHeight());
	}

	public native void intersectClipRect(int x, int y, int width, int height);

	public void intersectClipRect(Rectangle2D rect) {
		intersectClipRect((int) rect.getX(), (int) rect.getY(), (int) rect.getWidth(), (int) rect.getHeight());
	}

	
	public native void unionClipRect(int x, int y, int width, int height);

	public void unionClipRect(Rectangle2D rect) {
		unionClipRect((int) rect.getX(), (int) rect.getY(), (int) rect.getWidth(), (int) rect.getHeight());
	}

	public native void subtractClipRect(int x, int y, int width, int height);

	public void subtractClipRect(Rectangle2D rect) {
		subtractClipRect((int) rect.getX(), (int) rect.getY(), (int) rect.getWidth(), (int) rect.getHeight());
	}

	public native void setClipPolygon(Point2D[] points);
	public native void intersectClipPolygon(Point2D[] points);
	public native void unionClipPolygon(Point2D[] points);
	public native void subtractClipPolygon(Point2D[] points);
	
	/* 
	 * port origin
	 * 
	 */
	
	public native Point getOrigin();
	
	public native void setOrigin(int x, int y);
	
	public void setOrigin(Point2D point) {
		setOrigin((int) point.getX(), (int) point.getY());
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
	
	public void drawLine(Point2D p1, Point2D p2) {
		drawLine((int) p1.getX(), (int) p1.getY(), (int) p2.getX(), (int) p2.getY());
	}

	public native void drawPolygon(Point2D[] points);
	public native void fillPolygon(Point2D[] points);
	
	public native void drawRect(int x, int y, int width, int height);

	public void drawRect(Rectangle2D rect) {
		drawRect((int) rect.getX(), (int) rect.getY(), (int) rect.getWidth(), (int) rect.getHeight());
	}

	public native void fillRect(int x, int y, int width, int height);

	public void fillRect(Rectangle2D rect) {
		fillRect((int) rect.getX(), (int) rect.getY(), (int) rect.getWidth(), (int) rect.getHeight());
	}

	public native void clearRect(int x, int y, int width, int height);

	public void clearRect(Rectangle2D rect) {
		clearRect((int) rect.getX(), (int) rect.getY(), (int) rect.getWidth(), (int) rect.getHeight());
	}

	public native void drawSunkenRect(int x, int y, int width, int height);

	public void drawSunkenRect(Rectangle2D rect) {
		drawSunkenRect((int) rect.getX(), (int) rect.getY(), (int) rect.getWidth(), (int) rect.getHeight());
	}

	public native void drawRaisedRect(int x, int y, int width, int height);

	public void drawRaisedRect(Rectangle2D rect) {
		drawRaisedRect((int) rect.getX(), (int) rect.getY(), (int) rect.getWidth(), (int) rect.getHeight());
	}

	public native void invertRect(int x, int y, int width, int height);

	public void invertRect(Rectangle2D rect) {
		invertRect((int) rect.getX(), (int) rect.getY(), (int) rect.getWidth(), (int) rect.getHeight());
	}

	
	public native void drawOval(int x, int y, int width, int height);

	public void drawOval(Rectangle2D rect) {
		drawOval((int) rect.getX(), (int) rect.getY(), (int) rect.getWidth(), (int) rect.getHeight());
	}

	public native void fillOval(int x, int y, int width, int height);

	public void fillOval(Rectangle2D rect) {
		fillOval((int) rect.getX(), (int) rect.getY(), (int) rect.getWidth(), (int) rect.getHeight());
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
	public void drawImage(Image image, Point2D point, int style) {
		drawImage(image, (int) point.getX(), (int) point.getY(), style);
	}

	public native void drawImage(Image image, int x, int y);

	public void drawImage(Image image, Point2D point) {
		drawImage(image, (int) point.getX(), (int) point.getY());
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
	public native void drawImage(Image image, int x, int y, int width, int height, int style);

	/**
	 * Draws the image centered in a rectangle, with a recoloring style
	 * 
	 * @param image
	 * @param rect
	 * @param style style Drawer.RECOLOR_ values
	 */
	public void drawImage(Image image, Rectangle2D rect, int style) {
		drawImage(image, (int) rect.getX(), (int) rect.getY(), (int) rect.getWidth(), (int) rect.getHeight(), style);
	}

	public native void drawImage(Image image, int x, int y, int width, int height);	

	public void drawImage(Image image, Rectangle2D rect) {
		drawImage(image, (int) rect.getX(), (int) rect.getY(), (int) rect.getWidth(), (int) rect.getHeight());
	}

	/* 
	 * text drawing
	 * 
	 */

	public native int getTextWidth(String text);
	
	public native void drawText(String text, int x, int y);

	public void drawText(String text, Point2D point) {
		drawText(text, (int) point.getX(), (int) point.getY());
	}
	
	public native void drawTextLeft(String text, int x, int y, int width, int height);

	public void drawTextLeft(String text, Rectangle2D rect) {
		drawTextLeft(text, (int) rect.getX(), (int) rect.getY(), (int) rect.getWidth(), (int) rect.getHeight());
	}
	
	public native void drawTextCentered(String text, int x, int y, int width, int height);

	public void drawTextCentered(String text, Rectangle2D rect) {
		drawTextCentered(text, (int) rect.getX(), (int) rect.getY(), (int) rect.getWidth(), (int) rect.getHeight());
	}
	
	public native void drawTextRight(String text, int x, int y, int width, int height);

	public void drawTextRight(String text, Rectangle2D rect) {
		drawTextRight(text, (int) rect.getX(), (int) rect.getY(), (int) rect.getWidth(), (int) rect.getHeight());
	}
	
	public native void drawTextInABox(String text, int x, int y, int width, int height);

	public void drawTextInABox(String text, Rectangle2D rect) {
		drawTextRight(text, (int) rect.getX(), (int) rect.getY(), (int) rect.getWidth(), (int) rect.getHeight());
	}
	
	/* 
	 * standard arrows
	 * 
	 */

	public native void drawUpArrow(int x, int y, int width, int height);

	public void drawUpArrow(Rectangle2D rect) {
		drawUpArrow((int) rect.getX(), (int) rect.getY(), (int) rect.getWidth(), (int) rect.getHeight());
	}

	public native void drawDownArrow(int x, int y, int width, int height);

	public void drawDownArrow(Rectangle2D rect) {
		drawDownArrow((int) rect.getX(), (int) rect.getY(), (int) rect.getWidth(), (int) rect.getHeight());
	}

	public native void drawLeftArrow(int x, int y, int width, int height);

	public void drawLeftArrow(Rectangle2D rect) {
		drawLeftArrow((int) rect.getX(), (int) rect.getY(), (int) rect.getWidth(), (int) rect.getHeight());
	}

	public native void drawRightArrow(int x, int y, int width, int height);

	public void drawRightArrow(Rectangle2D rect) {
		drawRightArrow((int) rect.getX(), (int) rect.getY(), (int) rect.getWidth(), (int) rect.getHeight());
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
