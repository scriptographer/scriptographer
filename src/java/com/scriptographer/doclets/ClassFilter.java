package com.scriptographer.doclets;

import com.sun.javadoc.*;

/**
 *  This interface can be implemented and a class name provided
 *  to the doclet to filter which classes are and are not included
 *  in the output document.
 *
 *  @version $Revision: 1.1 $
 *  @author Gregg Wonderly - C2 Technologies Inc.
 */
public interface ClassFilter {
    /**
     *  Filters the ClassDoc passed.  If true is returned,
     *  the passed class will be included into the output.
     *  If false is returned, this document will not be
     *  included.
     */
    public boolean includeClass( ClassDoc cd );
}