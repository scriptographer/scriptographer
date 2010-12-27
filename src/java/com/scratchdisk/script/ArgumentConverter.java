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
 * File created on Feb 12, 2008.
 */

package com.scratchdisk.script;

import com.scratchdisk.util.ClassUtils;

/**
 * @author lehni
 *
 */
public abstract class ArgumentConverter<T> {

	public static void loadConverters() {
		String[] converters = ClassUtils.getServiceInformation(ArgumentConverter.class);
		if (converters != null) {
			for (int i = 0; i < converters.length; i++) {
				String[] parts = converters[i].split(":");
				try {
					ArgumentConverter converter = (ArgumentConverter) Class.forName(parts[0]).newInstance();
					Class type = Class.forName(parts[1]);
					ArgumentReader.registerConverter(type, converter);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public abstract T convert(ArgumentReader reader, Object from);
}
