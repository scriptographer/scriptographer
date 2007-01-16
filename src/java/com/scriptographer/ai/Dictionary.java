/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2007 Juerg Lehni, http://www.scratchdisk.com.
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
 * File created on 08.04.2005.
 *
 * $Id$
 */

package com.scriptographer.ai;

import java.util.HashMap;

import org.mozilla.javascript.Scriptable;

import com.scriptographer.CommitManager;
import com.scriptographer.Commitable;
import com.scriptographer.js.Wrappable;

/**
 * @author lehni
 */
public class Dictionary extends HashMap implements Commitable, Wrappable {

	protected DictionaryObject object;
	protected boolean dirty = false;
	protected int version = -1;

	protected Dictionary(DictionaryObject art) {
		this.object = art;
		fetch();
	}
	
	protected Dictionary(Document document) {
		
	}

	protected void update() {
		// only update if it didn't change in the meantime:
		if (!dirty && object != null && version != object.getVersion())
			fetch();
	}

	protected void markDirty() {
		// only mark it as dirty if it's attached to a path already:
		if (!dirty && object != null) {
			CommitManager.markDirty(this.object, this);
			dirty = true;
		}
	}
	
	protected void fetch() {
		object.nativeGetDictionary(this);
		version = object.getVersion();
	}
	
	public void commit() {
		if (dirty && object != null) {
			object.nativeSetDictionary(this);
			dirty = false;
		}
	}
	
	public void clear() {
		super.clear();
		markDirty();
	}

	public Object put(Object key, Object value) {
		Object obj = super.put(key, value);
		markDirty();
		return obj;
	}

	public Object remove(Object key) {
		Object obj = super.remove(key);
		if (obj != null)
			markDirty();
		return obj;
	}

	protected Scriptable wrapper;
	
	public void setWrapper(Scriptable wrapper) {
		this.wrapper = wrapper;
	}
	
	public Scriptable getWrapper() {
		return wrapper;
	}
}
