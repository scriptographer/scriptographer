package com.scriptographer.adm;

import java.awt.Color;

import com.scriptographer.ai.*;

public class Drawer {
	private int drawerRef = 0;
	// if this drawer draws into an image:
	private Image image;
	
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
	
	protected Drawer() {
	}

	protected Drawer(int drawerRef, Image image) {
		this.drawerRef = drawerRef;
		this.image = image;
	}
	
	protected Drawer(int drawerRef) {
		this(drawerRef, null);
	}
	
	public void dispose() {
		if (image != null) {
			image.endDrawer();
			image = null;
		}
		drawerRef = 0;
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
	public native void setClipRect(Rectangle rect);
	public native void intersectClipRect(Rectangle rect);
	
	public native void unionClipRect(Rectangle rect);
	public native void subtractClipRect(Rectangle rect);

	public native void setClipPolygon(Point[] points);
	public native void intersectClipPolygon(Point[] points);
	public native void unionClipPolygon(Point[] points);
	public native void subtractClipPolygon(Point[] points);
	
	/* 
	 * port origin
	 * 
	 */
	
	public native Point getOrigin();
	public native void setOrigin(Point origin);
	
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

	public native void drawPolygon(Point[] points);
	public native void fillPolygon(Point[] points);
	
	public native void drawRect(Rectangle rect);
	public native void fillRect(Rectangle rect);
	public native void clearRect(Rectangle rect);
	public native void drawSunkenRect(Rectangle rect);
	public native void drawRaisedRect(Rectangle rect);
	public native void invertRect(Rectangle rect);
	
	public native void drawOval(Rectangle rect);
	public native void fillOval(Rectangle rect);

	/* 
	 * image and icon drawing
	 * 
	 */

	public native void drawImage(Image image, Point topLeft);
	public native void drawImageCentered(Image image, Rectangle rect);	
	public native void drawRecoloredImage(Image image, Point topLeft, int style); // style form Drawer.RECOLOR_ segmentValues
	public native void drawRecoloredImageCentered(Image image, Rectangle rect, int style); // style form Drawer.RECOLOR_ segmentValues

	/* 
	 * text drawing
	 * 
	 */
	
	public native int getTextWidth(String text);
	public native void drawText(String text, Point point);
	public native void drawTextLeft(String text, Rectangle rect);
	public native void drawTextCentered(String text, Rectangle rect);
	public native void drawTextRight(String text, Rectangle rect);
	public native void drawTextInABox(String text, Rectangle rect);
	
	/* 
	 * standard arrows
	 * 
	 */

	public native void drawUpArrow(Rectangle rect);
	public native void drawDownArrow(Rectangle rect);
	public native void drawLeftArrow(Rectangle rect);
	public native void drawRightArrow(Rectangle rect);

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

	public static class FontInfo {
		public int height;
		public int ascent;
		public int descent;
		public int leading;
		public int maxWidth;
		
		public FontInfo(int height, int ascent, int descent, int leading, int maxWidth) {
			this.height = height;
			this.ascent = ascent;
			this.descent = descent;
			this.leading = leading;
			this.maxWidth = maxWidth;
		}
	};

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
