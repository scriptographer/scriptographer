package com.scriptographer.doclets;

import com.sun.javadoc.*;

/**
 * This class filters out classes beginning with "Test" when
 * applied to the doclet.
 * @version $Revision: 1.1 $
 */
public class TestFilter implements ClassFilter {

	/**
	 * Returns false if class name starts with "Test".
	 */
	public boolean includeClass(ClassDoc cd) {
		return !cd.name().startsWith("Test");
	}
}
