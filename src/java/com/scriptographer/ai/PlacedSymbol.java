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
 * File created on Oct 17, 2006.
 * 
 * $Id$
 */

package com.scriptographer.ai;

/**
 * A PlacedSymbol represents a symbol which has been placed in an Illustrator
 * document.
 * 
 * @author lehni
 */
public class PlacedSymbol extends Item {

	protected PlacedSymbol(int handle) {
		super(handle);
	}

	private static native int nativeCreate(int symbolHandle, Matrix matrix);
	
	/**
	 * Creates a new PlacedSymbol Item.
	 * 
	 * Sample code:
	 * <code>
	 * var symbol = document.symbols['Rocket'];
	 * var placedSymbol = new PlacedSymbol(symbol, new Point(100, 100));
	 * </code>
	 * 
	 * @param symbol the symbol to place
	 * @param pt the center point of the placed symbol
	 */
	public PlacedSymbol(Symbol symbol, Point pt) {
		this(symbol, new Matrix().translate(pt));
	}

	/**
	 * Creates a new PlacedSymbol Item.
	 * 
	 * Sample code:
	 * <code>
	 * var symbol = document.symbols['Rocket'];
	 * var rotation = (45).toRadians();
	 * var matrix = new Matrix().translate(100, 100).rotate(rotation);
	 * var placedSymbol = new PlacedSymbol(symbol, matrix);
	 * </code>
	 * 
	 * @param symbol the symbol to place
	 * @param matrix
	 */
	public PlacedSymbol(Symbol symbol, Matrix matrix) {
		super(nativeCreate(symbol.handle, matrix));
	}
	
	public PlacedSymbol(Symbol symbol) {
		this(symbol, new Matrix());
	}
	
	private native int nativeGetSymbol();
	
	/**
	 * The symbol contained within the placed symbol.
	 */
	public Symbol getSymbol() {
		return Symbol.wrapHandle(nativeGetSymbol(), document);
	}
	
	public native void setSymbol(Symbol symbol);
	
	public native Matrix getMatrix();
	
	public native void setMatrix(Matrix matrix);
}
