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
 * File created on 10.06.2006.
 * 
 * $Id$
 */

package com.scriptographer.sg;

import java.net.URI;

public class File extends java.io.File {

	public File(String pathname) {
		super(pathname);
	}

	public File(String parent, String child) {
		super(parent, child);
	}

    public File(java.io.File parent, String child) {
    	super(parent, child);
    }

    public File(URI uri) {
    	super(uri);
    }

    public boolean remove() {
    	return delete();
    }

    /**
	 * Returns the length of the file denoted by this abstract pathname. The
	 * return value is unspecified if this pathname denotes a directory.
	 * 
	 * @return The length, in bytes, of the file denoted by this abstract
	 *         pathname, or <code>0L</code> if the file does not exist
	 * 
	 * @throws SecurityException If a security manager exists and its
	 *         <code>{@link java.lang.SecurityManager#checkRead(java.lang.String)}</code>
	 *         method denies read access to the file
	 */
    public long getLength() {
    	return length();
    }
}