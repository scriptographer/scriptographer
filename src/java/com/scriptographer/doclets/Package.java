package com.scriptographer.doclets;

import com.sun.javadoc.*;
import java.util.*;

/**
 *  This class is used to manage the contents of a Java package.
 *  It accepts ClassDoc objects and examines them and groups them
 *  according to whether they are classes, interfaces, exceptions
 *  or errors.  The accumulated Vectors can then be processed to
 *  get to all of the elements of the package that fall into each
 *  category.
 *  If needed the classes, interfaces, exceptions and errors can
 *  be sorted using the <CODE>sort</CODE> method.
 *
 *  @see #sort
 *  @version $Revision: 1.1 $
 *  @author Gregg Wonderly - C2 Technologies Inc.
 */
public class Package {
	protected PackageDoc pkgDoc;
	/** The name of the package this object is for */
	protected String pkg;
	/** The classes this package has in it */
	protected Vector classes;
	/** The interfaces this package has in it */
	protected Vector interfaces;
	/** The exceptions this package has in it */
	protected Vector exceptions;
	/** The errors this package has in it */
	protected Vector errors;

	/**
	 *  Construct a new object corresponding to the passed package
	 *  name.
	 *  @param pkg the package name to use
	 */
	public Package(String pkg, PackageDoc doc) {
		pkgDoc = doc;
		this.pkg = pkg;
		if (pkg.equals(""))
			this.pkg = "<none>";
		classes = new Vector();
		interfaces = new Vector();
		exceptions = new Vector();
		errors = new Vector();
	}

	/**
	 *  Adds a ClassDoc element to this package.
	 *  @param cd the object to add to this package
	 */
	public void addElement(ClassDoc cd) {
		if (cd.isInterface()) {
			interfaces.addElement(cd);
		} else if (cd.isClass()) {
			if (cd.isException()) {
				exceptions.addElement(cd);
			} else if (cd.isError()) {
				errors.addElement(cd);
			} else {
				classes.addElement(cd);
			}
		}
	}

	/**
	 * Sorts the vectors of classes, interfaces exceptions and errors.
	 */
	public void sort() {

		Comparator comp = new Comparator() {
			public int compare(Object o1, Object o2) {
				ClassDoc cls1 = (ClassDoc) o1;
				ClassDoc cls2 = (ClassDoc) o2;
				return cls1.name().compareToIgnoreCase(cls2.name());
			}

			public boolean equals(Object obj) {
				return false;
			}
		};

		Collections.sort(classes, comp);
		Collections.sort(interfaces, comp);
		Collections.sort(exceptions, comp);
		Collections.sort(errors, comp);
	}
}