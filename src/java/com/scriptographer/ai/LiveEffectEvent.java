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
 * File created on Feb 27, 2010.
 */

package com.scriptographer.ai;

/**
 * @author lehni
 */
public class LiveEffectEvent {

	private int itemHandle;
	private int parametersHandle;

	private Item item;
	private LiveEffectParameters parameters;

	protected LiveEffectEvent(int itemHandle, int dictionaryHandle) {
		this.itemHandle = itemHandle;
		this.parametersHandle = dictionaryHandle;
		item = null;
		parameters = null;
	}

	protected LiveEffectEvent(Item item, LiveEffectParameters parameters) {
		this.item = item;
		this.parameters = parameters;
		itemHandle = 0;
		parametersHandle = 0;
	}

	public Item getItem() {
		if (item == null && itemHandle != 0) {
			// This is the same comment as in LiveEffect_onCalculate on the
			// native side:
			// Do not check wrappers as the art items in live effects are
			// duplicated and therefore seem to contain the m_artHandleKey,
			// causing wrapped to be set to true when Item#wrapHandle is called.
			// And sometimes their handles are reused, causing reuse of wrong
			// wrappers.
			// We could call Item_clearArtHandles but that's slow. Passing false
			// for checkWrapped should do it.
			item = Item.wrapHandle(itemHandle, 0, true, false);
		}
		return item;
	}

	public LiveEffectParameters getParameters() {
		if (parameters == null && parametersHandle != 0) {
			parameters =
					LiveEffectParameters.wrapHandle(parametersHandle, null);
		}
		return parameters;
	}
}
