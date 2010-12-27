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
 * File created on May 3, 2010.
 */

package com.scriptographer.script;

import com.scratchdisk.script.ArgumentConverter;
import com.scratchdisk.script.ArgumentReader;
import com.scriptographer.ai.FontFamily;
import com.scriptographer.ai.FontList;
import com.scriptographer.ai.FontWeight;

/**
 * @author lehni
 *
 */
public class FontWeightConverter extends ArgumentConverter<FontWeight> {

	public FontWeight convert(ArgumentReader reader, Object from) {
		if (from instanceof FontFamily) {
			return ((FontFamily) from).getFirst();
		} else if (from instanceof String) {
			return FontList.getInstance().getWeight((String) from);
		}
		// TODO: Map?
		return null;
	}

}
