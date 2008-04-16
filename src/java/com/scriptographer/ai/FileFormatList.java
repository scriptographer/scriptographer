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
 * File created on  16.02.2005.
 *
 * $Id: DocumentList.java 537 2008-04-14 14:41:26Z lehni $
 */

package com.scriptographer.ai;

import java.util.ArrayList;
import java.util.HashMap;

import com.scratchdisk.list.AbstractReadOnlyList;
import com.scratchdisk.list.StringIndexReadOnlyList;

/**
 * @author lehni
 */
public class FileFormatList extends AbstractReadOnlyList<FileFormat> implements StringIndexReadOnlyList<FileFormat> {

	private ArrayList<FileFormat> formats = null;
	private HashMap<String, FileFormat> lookup = null;
	
	private FileFormatList() {
		formats = FileFormat.getFileFormats();
		lookup = new HashMap<String, FileFormat>();
		for (FileFormat format : formats)
			for (String extension : format.getExtensions())
				lookup.put(extension.toLowerCase(), format);
	}

	public int size() {
		return formats.size();
	}
	
	public FileFormat get(int index) {
		return formats.get(index);
	}

	private static FileFormatList formatList;

	public static FileFormatList getInstance() {
		if (formatList == null)
			formatList = new FileFormatList();

		return formatList;
	}

	public FileFormat get(String extenion) {
		return lookup.get(extenion.toLowerCase());
	}
}
