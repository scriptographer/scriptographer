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
 * File created on  16.02.2005.
 */


package com.scriptographer.ai;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.scratchdisk.list.AbstractReadOnlyList;
import com.scratchdisk.list.ReadOnlyStringIndexList;

/**
 * @author lehni
 * 
 * @jshide
 */
public class FileFormatList extends AbstractReadOnlyList<FileFormat> implements ReadOnlyStringIndexList<FileFormat> {

	private ArrayList<FileFormat> formats = null;
	private LinkedHashMap<String, FileFormat> lookup = null;
	
	private FileFormatList() {
		formats = FileFormat.getFileFormats();
		lookup = new LinkedHashMap<String, FileFormat>();
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

	public String[] getExtensions() {
		return lookup.keySet().toArray(new String[lookup.size()]);
	}

	public Class<?> getComponentType() {
		return FileFormat.class;
	}
}
