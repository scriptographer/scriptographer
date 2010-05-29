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
