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
 * File created on Oct 18, 2006.
 * 
 * $Id$
 */

package com.scriptographer.ai;

import java.lang.reflect.Constructor;
import java.util.HashMap;

import com.scratchdisk.util.SoftIntMap;

/**
 * @author lehni
 */
abstract class NativeWrapper extends NativeObject {

	protected Document document = null;

	protected NativeWrapper(int handle, boolean activeDocument) {
		this.handle = handle;
		if (activeDocument)
			this.document = Document.getActiveDocument();
	}

	protected NativeWrapper(int handle) {
		this(handle, false);
	}
	
	protected static NativeWrapper wrapHandle(Class cls, int handle,
			Document document, boolean useDocument) {
		if (handle == 0)
			return null;
		WrapperFactory factory = (WrapperFactory) factories.get(cls);
		if (factory == null) {
			factory = new WrapperFactory(cls);
			factories.put(cls, factory);
		}
		return factory.wrapHandle(handle, document, useDocument);
	}

	protected boolean nativeRemove() {
		return false;
	}

	/**
	 * protected scafold function for removing. subclasses that want to use it
	 * need to override nativeRemove and make remove public
	 * 
	 * @return
	 */
	protected boolean remove() {
		boolean ret = false;
		if (handle != 0) {
			ret = nativeRemove();
			if (ret) {
				WrapperFactory factory = (WrapperFactory) factories
						.get(getClass());
				if (factory != null)
					factory.wrappers.remove(handle);
				handle = 0;
			}
		}
		return ret;
	}

	// cash the factories for the various wrapper classes
	// that use this base class
	private static HashMap factories = new HashMap();

	private static class WrapperFactory {
		// use a SoftIntMap to keep track of already wrapped objects:
		SoftIntMap wrappers = new SoftIntMap();

		Constructor ctor;

		WrapperFactory(Class cls) {
			// get the constructor for this class
			try {
				ctor = cls.getDeclaredConstructor(new Class[] { Integer.TYPE });
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		NativeWrapper wrapHandle(int handle, Document document, boolean useDocument) {
			if (handle == 0)
				return null;
			NativeWrapper wrapper = (NativeWrapper) wrappers.get(handle);
			if (wrapper == null) {
				try {
					// now create a new instance, passing the handle as a
					// parameter
					wrapper = (NativeWrapper) ctor
							.newInstance(new Object[] { new Integer(handle) });
					// store reference to the object's document
					if (useDocument)
						wrapper.document = document != null ? document
								: Document.getActiveDocument();
					wrappers.put(handle, wrapper);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			return wrapper;
		}
	}
}
