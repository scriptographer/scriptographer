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
 * File created on 07.04.2005.
 *
 * $Id$
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

	public void setCenter(Point point) {
		setCenter((float) point.x, (float) point.y);
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

	public Point artworkToView(Point point) {
		return artworkToView((float) point.x, (float) point.y);
	}

	public native Rectangle artworkToView(float x, float y, float width,
			float height);

	public Rectangle artworkToView(Rectangle rect) {
		return artworkToView((float) rect.x, (float) rect.y, (float) rect.width, (float) rect.height);
	}

	/**
	 * Convert a point from view coordinates to artwork coordinates. This
	 * version takes pixel coordinates as an input.
	 */
	public native Point viewToArtwork(float x, float y);

	public Point viewToArtwork(Point point) {
		return viewToArtwork((float) point.x, (float) point.y);
	}

	public native Rectangle viewToArtwork(float x, float y, float width,
			float height);

	public Rectangle viewToArtwork(Rectangle rect) {
		return viewToArtwork((float) rect.x, (float) rect.y, (float) rect.width, (float) rect.height);
	}

	private native void nativeSetScreenMode(int mode);
	private native int nativeGetScreenMode();

	/**
	 * @jsbean The screen mode of the specified view. The screen mode
	 * @jsbean is selected via the three screen mode icons on the bottom of the tool
	 * @jsbean palette.
	 */
	public void setScreenMode(ScreenMode mode) {
		nativeSetScreenMode((mode != null ? mode : ScreenMode.MULTI_WINDOW).value);
	}

	public ScreenMode getScreenMode() {
		return IntegerEnumUtils.get(ScreenMode.class,
				nativeGetScreenMode());
	}

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

	public void scrollBy(Point point) {
		scrollBy((float) point.x, (float) point.y);
	}

	/**
	 * Returns a rectangle in artwork coordinates that encloses (at least) the
	 * portions of the document that have been changed and so need to be
	 * redrawn. This rectangle is reset to be empty each time the
	 * #kAIDocumentViewInvalidRectChangedNotifier is sent.
	 */
	public native Rectangle getUpdateRect();

	public native void invalidate(float x, float y, float width, float height);

	public void invalidate(Rectangle rect) {
		invalidate((float) rect.x, (float) rect.y, (float) rect.width, (float) rect.height);
	}

	private native int nativeGetStyle();

	/**
	 * Get the display mode for the current view. 
	 */
	public EnumSet<ViewStyle> getStyle() {
		return IntegerEnumUtils.getSet(ViewStyle.class, nativeGetStyle());
	}

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
	 * Return the current mouse position within this view, in document
	 * coordinates.
	 */
	public native Point getMousePoint();
}
