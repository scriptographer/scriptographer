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
 * File created on Oct 17, 2006.
 */

package com.scriptographer.ai;

/**
 * A PlacedSymbol represents a symbol which has been placed in an Illustrator
 * document.
 * 
 * @author lehni
 */
public class PlacedSymbol extends Item {

	protected PlacedSymbol(int handle, int docHandle, boolean created) {
		super(handle, docHandle, created);
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
	 * var matrix = new Matrix().translate(100, 100).rotate(45);
	 * var placedSymbol = new PlacedSymbol(symbol, matrix);
	 * </code>
	 * 
	 * @param symbol the symbol to place
	 * @param matrix
	 */
	public PlacedSymbol(Symbol symbol, Matrix matrix) {
		super(nativeCreate(symbol.handle, matrix), symbol.document, true, false);
	}

	public PlacedSymbol(Symbol symbol) {
		this(symbol, new Matrix());
	}

	/**
	 * Creates a new PlacedSymbol item from the provided item by converting it
	 * to a Symbol behind and placing it in the same spot as the original item.
	 * The original item is then removed.
	 * 
	 * @param item the item to create a symbol out of
	 */
	public PlacedSymbol(Item item) {
		this(new Symbol(item), item.getPosition());
		item.remove();
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

	public native Item embed();
}
