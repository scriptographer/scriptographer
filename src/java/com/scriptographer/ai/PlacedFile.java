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
 * File created on Oct 16, 2006.
 * 
 * $Id$
 */

package com.scriptographer.ai;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import com.scratchdisk.util.NetUtils;

/**
 * @author lehni
 */
public class PlacedFile extends Item {

	protected PlacedFile(int handle) {
		super(handle);
	}

	private static native int nativeCreate(File file);

	/**
	 * Creates a PlacedFile item from a URL.
	 * This blocks until the file is loaded or an error occured.
	 * 
	 * Sample code:
	 * <code>
	 * var url = new java.net.URL('http://www.server.com/image.jpg');
	 * var raster = new PlacedFile(url);
	 * </code>
	 * 
	 * @param url the URL of the file to place
	 * @throws IOException
	 */
	public PlacedFile(URL url) throws IOException {
		this(NetUtils.loadFile(url, "sg_"));
	}


	/**
	 * Creates a PlacedFile from a local file.
	 * 
	 * Sample code:
	 * <code>
	 * var file = new java.io.File('/folder/image.jpg');
	 * var raster = new PlacedFile(file);
	 * </code>
	 * 
	 * @param file the file to be placed
	 */
	public PlacedFile(File file) {
		super(nativeCreate(file));
	}

	/**
	 * The original size of the placed file, ignoring any transformation
	 * matrices applied to it in the document.
	 */
	public native com.scriptographer.ui.Size getSize();

	/**
	 * The original width of the placed file.
	 */
	public int getWidth() {
		return getSize().width;
	}

	/**
	 * The original height of the placed file.
	 */
	public int getHeight() {
		return getSize().height;
	}
	
	public native Matrix getMatrix();
	
	public native void setMatrix(Matrix matrix);
	
	/**
	 * The file associated with the placed file item.
	 */
	public native File getFile();
	
	/**
	 * Checks whether the placed file item is an eps.
	 * @return {@true if the placed file item is an eps}
	 */
	public native boolean isEps();
	
	/**
	 * Traces the placed file.
	 * @return the newly created tracing item
	 */
	public Tracing trace() {
		return new Tracing(this);
	}
	
	/**
	 * Embeds the placed file within the document.
	 * @param askParams specifies whether to show dialog for user input if needed {@default false}
	 * @return the embedded item
	 */
	public native Item embed(boolean askParams);
	
	public Item embed() {
		return embed(false);
	}
	
	// TODO:
	/*
	Specify the placement options for the object. These options are used when the object
	is scaled or replaced by a new object.
	AIAPI AIErr (*SetPlaceOptions) ( AIArtHandle placed, enum PlaceMethod method, enum PlaceAlignment alignment, ASBoolean clip );
	Get the placement options for the object. These options are used when the object
	is scaled or replaced by a new object.
	AIAPI AIErr (*GetPlaceOptions) ( AIArtHandle placed, enum PlaceMethod *method, enum PlaceAlignment *alignment, ASBoolean *clip );
	AIAPI AIErr (*GetPlacedDimensions) ( AIArtHandle placed, ASRealPoint *size, ASRealRect *viewBounds, AIRealMatrix *viewMatrix,
			ASRealRect *imageBounds, AIRealMatrix *imageMatrix );
	 */
}
