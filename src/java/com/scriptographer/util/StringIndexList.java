/*
 * Scriptographer
 * StringIndexList.java
 * 
 * Created by  Lehni on 12.02.2005.
 * Copyright (c) 2004 http://www.scratchdisk.com. All rights reserved.
 * 
 */
package com.scriptographer.util;

/*
 * Adds getting objects by name to Lists (an extension needed for some list objects like LayerList)
 */

public interface StringIndexList {
	public Object get(String index);
}
