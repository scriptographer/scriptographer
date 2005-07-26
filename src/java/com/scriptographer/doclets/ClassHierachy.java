package com.scriptographer.doclets;

import java.io.PrintWriter;
import java.util.*;
import com.sun.javadoc.*;

/**
 * Manages and prints a class hierachy. Use <CODE>add</CODE> to add another class
 * to the hierachy. Use <CODE>printTree</CODE> to print the corrosponding
 * class hierarchy.
 * @version $Revision: 1.1 $
 * @author Søren Caspersen - XO Software
 * @author Stefan Marx
 */
public class ClassHierachy extends java.lang.Object {

	public SortedMap root = new TreeMap();

	/**
	 * Creates new ClassHierachy
	 */
	public ClassHierachy() {
	}

	/**
	 * Adds another class to the hierachy
	 */
	protected SortedMap add(ClassDoc cls) {
		System.out.println("ADD: " + cls);
		SortedMap temp;
		if (cls.superclass() != null) {
			temp = add(cls.superclass());
		} else {
			temp = root;
		}
		System.out.println("QN: " + cls.qualifiedName());
		System.out.println();
		SortedMap result = (SortedMap) temp.get(cls.qualifiedName());
		if (result == null) {
			result = new TreeMap();
			temp.put(cls.qualifiedName(), result);
		}
		return result;
	}

	/**
	 * Prints the html code corresponding to the tree.
	 */
	public void printTree(PrintWriter writer, RootDoc rootDoc, double overviewindent) {
		printBranch(writer, rootDoc, root, 0, overviewindent);
	}

	/**
	 * Prints a branch of the tree.
	 */
	protected void printBranch(PrintWriter writer, RootDoc rootDoc, SortedMap map, double indent, double overviewindent) {
		Set set = map.keySet();
		Iterator it = set.iterator();
		while (it.hasNext()) {
			String qualifName = (String) it.next();
			for (int i = 0; i < indent; i++)
				writer.print("&nbsp;");
			if (qualifName.equals("java.lang.Object"))
				writer.print(qualifName);
			else if (JSDoclet.hyperref)
				writer.print("<A HREF=\"#" + qualifName + "\">" + qualifName + "</A>");
			else
				writer.print(qualifName);

			writer.println("<br>");
			printBranch(writer, rootDoc, (SortedMap) map.get(qualifName), indent + overviewindent, overviewindent);
		}
	}

}
