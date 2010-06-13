/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2010 Juerg Lehni, http://www.scratchdisk.com.
 * All rights reserved.
 *
 * Please visit http://scriptographer.org/ for updates and contact.
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
 */

package com.scriptographer.ai;

import java.util.EnumSet;

import com.scratchdisk.util.IntegerEnumUtils;

/**
 * @author lehni
 */
public class DocumentView extends DocumentObject {
	protected DocumentView(int handle, Document document) {
		super(handle, document);
	}

	protected static DocumentView wrapHandle(int handle, Document document) {
		return (DocumentView) wrapHandle(DocumentView.class, handle, document);
	}

	/**
	 * The bounds of the document view, i.e. the bounds of the part of the
	 * artboard which is visible in the window.
	 */
	public native Rectangle getBounds();

	/**
	 * The center point of the document view, i.e. the point on the artboard
	 * that maps to the center of the window.
	 */
	public native Point getCenter();

	public native void setCenter(float x, float y);

	public void setCenter(Point point) {
		setCenter((float) point.x, (float) point.y);
	}

	/**
	 * The zoom factor for the view. This is the scale factor from artwork
	 * coordinates to window coordinates. The zoom is clamped to lie between the
	 * minimum and maximum values supported (between 1/32 and 64). After
	 * adjusting the zoom factor the center point of the document view center is
	 * unchanged.
	 */
	public native float getZoom();

	public native void setZoom(float zoom);

	/**
	 * @jshide
	 * Convert a point from artwork coordinates to view (window) coordinates.
	 */
	public native Point artworkToView(float x, float y);

	/**
	 * Convert a point from artwork coordinates to view (window) coordinates.
	 */
	public Point artworkToView(Point point) {
		return artworkToView((float) point.x, (float) point.y);
	}

	/**
	 * @jshide
	 */
	public native Rectangle artworkToView(float x, float y, float width,
			float height);

	/**
	 * Converts a rectangle from artwork coordinates to view (window)
	 * coordinates.
	 */
	public Rectangle artworkToView(Rectangle rect) {
		return artworkToView((float) rect.x, (float) rect.y,
				(float) rect.width, (float) rect.height);
	}

	/**
	 * @jshide
	 * Convert a point from view coordinates to artwork coordinates. This
	 * version takes pixel coordinates as an input.
	 */
	public native Point viewToArtwork(float x, float y);

	/**
	 * Convert a point from view coordinates to artwork coordinates. This
	 * version takes pixel coordinates as an input.
	 */
	public Point viewToArtwork(Point point) {
		return viewToArtwork((float) point.x, (float) point.y);
	}

	/**
	 * @jshide
	 */
	public native Rectangle viewToArtwork(float x, float y, float width,
			float height);

	/**
	 * Converts a rectangle from view (window) coordinates to rectangle
	 * coordinates.
	 */
	public Rectangle viewToArtwork(Rectangle rect) {
		return viewToArtwork((float) rect.x, (float) rect.y,
				(float) rect.width, (float) rect.height);
	}

	private native void nativeSetScreenMode(int mode);

	private native int nativeGetScreenMode();

	/**
	 * The screen mode of the view. The screen mode is selected via the three
	 * screen mode icons on the bottom of the tool palette.
	 */
	public void setScreenMode(ScreenMode mode) {
		nativeSetScreenMode((mode != null ? mode : ScreenMode.MULTI_WINDOW).value);
	}

	public ScreenMode getScreenMode() {
		return IntegerEnumUtils.get(ScreenMode.class, nativeGetScreenMode());
	}

	/**
	 * The page tiling information that describes how the artwork will be
	 * printed onto one or more pages.
	 */
	// public native PageTiling getPageTiling(); // TODO: implement

	/**
	 * Checks whether there is a visible template layer.
	 * 
	 * @return {@true if there is a visible template laye}
	 */
	public native boolean isTemplateVisible();

	/**
	 * @jshide
	 * Scrolls the document window by a vector in artwork coordinates.
	 */
	public native void scrollBy(float x, float y);

	/**
	 * Scrolls the document window by a vector in artwork coordinates.
	 */
	public void scrollBy(Point point) {
		scrollBy((float) point.x, (float) point.y);
	}

	/**
	 * Returns a rectangle in artwork coordinates that encloses (at least) the
	 * portions of the document that have been changed and so need to be
	 * redrawn. This rectangle is reset to be empty each time the
	 * #kAIDocumentViewInvalidRectChangedNotifier is sent.
	 */
	public native Rectangle getInvalidBounds();

	/**
	 * @deprecated
	 */
	public Rectangle getUpdateRect() {
		return getInvalidBounds();
	}
	public native void invalidate(float x, float y, float width, float height);

	public void invalidate(Rectangle rect) {
		invalidate((float) rect.x, (float) rect.y, (float) rect.width, (float) rect.height);
	}

	public void invalidate() {
		invalidate(getBounds());
	}

	private native int nativeGetStyle();

	/**
	 * The display mode for the current view.
	 */
	public EnumSet<ViewStyle> getStyle() {
		return IntegerEnumUtils.getSet(ViewStyle.class, nativeGetStyle());
	}

	/**
	 * Specifies whether page tiling is shown.
	 */
	public native boolean getShowPageTiling();

	public native void setShowPageTiling(boolean show);

	/**
	 * Specifies whether the grid is visible.
	 */
	public native boolean getShowGrid();

	/**
	 * Specifies whether snapping is enabled.
	 */
	public native boolean getSnapGrid();

	public native void setShowGrid(boolean show);

	public native void setSnapGrid(boolean snap);

	/**
	 * Specifies whether the transparency grid is shown in the view.
	 */
	public native boolean getShowTransparencyGrid();

	public native void setShowTransparencyGrid(boolean show);

	/**
	 * The current mouse position within the view, in document coordinates.
	 */
	public native Point getMousePoint();
}
