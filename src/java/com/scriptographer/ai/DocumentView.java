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
 * File created on 07.04.2005.
 *
 * $RCSfile: DocumentView.java,v $
 * $Author: lehni $
 * $Revision: 1.1 $
 * $Date: 2007/01/03 15:10:16 $
 */

package com.scriptographer.ai;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class DocumentView extends AIWrapper {
	public static final int
	/** Only when there is no visibile document */
	MODE_NOSCREEN = 0,
	/** The normal display mode. Multiple windows are visible. */
	MODE_MULTIWINDOW = 1,
	/** A single view takes up the whole screen but the menu is visible. */
	MODE_FULLSCREEN_MENU = 2,
	/** A single view takes up the whole screen, the menu is not visible. */
	MODE_FULLSCREEN = 3;

	public static final int
	/** Outline mode. */
	STYLE_ARTWORK = 0x0001,
	/** Preview mode. */
	STYLE_PREVIEW = 0x0002,
	/** Pixel preview mode. */
	STYLE_RASTER = 0x0040,
	/** Unimplemented. Transparency attributes and masks are ignored. */
	STYLE_OPAQUE = 0x0040,
	/** OPP preview mode. */
	STYLE_INK = 0x0100;

	protected DocumentView(int handle) {
		super(handle);
	}

	protected static DocumentView wrapHandle(int handle) {
		return (DocumentView) wrapHandle(DocumentView.class, handle, null, true);
	}

	/**
	 * Returns the bounds of the document view in artwork coordinates. That is,
	 * the bounds of the artboard that are visible in the window.
	 */
	public native Rectangle getBounds();

	/**
	 * Returns the center of the document view in artwork coordinates. That is,
	 * the point of the artboard that maps to the center of the window.
	 */
	public native Point getCenter();

	/**
	 * Sets the point of the artboard that maps to the center of the window.
	 */
	public native void setCenter(float x, float y);

	public void setCenter(Point2D point) {
		setCenter((float) point.getX(), (float) point.getY());
	}

	/**
	 * Gets the zoom factor for the view. This is the scale factor from artwork
	 * coordinates to window coordinates.
	 */
	public native float getZoom();

	/**
	 * Sets the scale factor from artwork coordinates to window coordinates. The
	 * scale factor is silently clamped to lie between the minimum and maximum
	 * values supported (currently between 1/32 and 64). After adjusting the
	 * zoom factor the document view center is unchanged.
	 */

	public native void setZoom(float zoom);

	/**
	 * Convert a point from artwork coordinates to view (window) coordinates.
	 */
	public native Point artworkToView(float x, float y);

	public Point artworkToView(Point2D point) {
		return artworkToView((float) point.getX(), (float) point.getY());
	}

	public native Rectangle artworkToView(float x, float y, float width,
			float height);

	public Rectangle artworkToView(Rectangle2D rect) {
		return artworkToView((float) rect.getX(), (float) rect.getY(),
			(float) rect.getWidth(), (float) rect.getHeight());
	}

	/**
	 * Convert a point from view coordinates to artwork coordinates. This
	 * version takes pixel coordinates as an input.
	 */
	public native Point viewToArtwork(float x, float y);

	public Point viewToArtwork(Point2D point) {
		return viewToArtwork((float) point.getX(), (float) point.getY());
	}

	public native Rectangle viewToArtwork(float x, float y, float width,
			float height);

	public Rectangle viewToArtwork(Rectangle2D rect) {
		return viewToArtwork((float) rect.getX(), (float) rect.getY(),
			(float) rect.getWidth(), (float) rect.getHeight());
	}

	/**
	 * This function sets the screen mode of the specified view. The screen mode
	 * is selected via the three screen mode icons on the bottom of the tool
	 * palette.
	 * 
	 * @param mode
	 *            View.MODE_*
	 */
	public native void setScreenMode(int mode);

	/**
	 * This function gets the screen mode of the specified view. The screen mode
	 * is selected via the three screen mode icons on the bottom of the tool
	 * palette.
	 * 
	 * @return View.MODE_*
	 */
	public native int getScreenMode();

	/**
	 * Get the page tiling information that describes how the artwork will be
	 * printed onto one or more pages.
	 */
	// public native PageTiling getPageTiling(); // TODO: implement
	/**
	 * True if there is a visible template layer.
	 */
	public native boolean isTemplateVisible();

	/**
	 * Scrolls the document window by a vector in artwork coordinates.
	 */
	public native void scrollBy(float x, float y);

	public void scrollBy(Point2D point) {
		scrollBy((float) point.getX(), (float) point.getY());
	}

	/**
	 * Returns a rectangle in artwork coordinates that encloses (at least) the
	 * portions of the document that have been changed and so need to be
	 * redrawn. This rectangle is reset to be empty each time the
	 * #kAIDocumentViewInvalidRectChangedNotifier is sent.
	 */

	public native Rectangle getUpdateRect();

	public native void invalidate(float x, float y, float width, float height);

	public void invalidate(Rectangle2D rect) {
		invalidate((float) rect.getX(), (float) rect.getY(),
			(float) rect.getWidth(), (float) rect.getHeight());
	}

	/**
	 * Get the display mode for the current view. This is a set of flags whose
	 * values may be View.STYLE_*
	 */
	public native int getStyle();

	/**
	 * Is page tiling being shown? This API operates on the current view though
	 * each view maintains its own setting.
	 */
	public native boolean getShowPageTiling();

	/**
	 * Set whether page tiling is being shown. This API operates on the current
	 * view though each view maintains its own setting.
	 */
	public native void setShowPageTiling(boolean show);

	/**
	 * Get options for the grid: whether it is visible and whether snapping is
	 * enabled in the view.
	 */
	public native boolean getShowGrid();

	public native boolean getSnapGrid();

	/**
	 * Get options for the grid: whether it is visible and whether snapping is
	 * enabled in the view.
	 */
	public native void setShowGrid(boolean show);

	public native void setSnapGrid(boolean snap);

	/**
	 * Is the transparency grid shown in the view.
	 */
	public native boolean getShowTransparencyGrid();

	/**
	 * Set whether the transparency grid is shown in the view.
	 */
	public native void setShowTransparencyGrid(boolean show);

	/**
	 * Get the document displayed in the view.
	 */
	public Document getDocument() {
		return document;
	}
	
	/**
	 * Return the current mouse position within this view, in document
	 * coordinates.
	 */
	public native Point getMousePoint();
}
