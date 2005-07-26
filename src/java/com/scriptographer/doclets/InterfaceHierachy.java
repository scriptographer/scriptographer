package com.scriptographer.doclets;

import java.io.PrintWriter;
import java.util.*;
import com.sun.javadoc.*;

/**
 * Manages and prints a interface hierachy. Use <CODE>add</CODE> to add another interface
 * to the hierachy. Use <CODE>printTree</CODE> to print the corrosponding
 * interface hierarchy.
 * @version $Revision: 1.1 $
 * @author Søren Caspersen - XO Software
 * @author Stefan Marx
 */
public class InterfaceHierachy extends java.lang.Object {

	public SortedMap root = new TreeMap();

	/**
	 * Creates new InterfaceHierachy
	 */
	public InterfaceHierachy() {
	}

	/**
	 * Adds another interface to the hierachy
	 */
	protected SortedMap add(ClassDoc cls) {
		SortedMap temp;
		if (cls.interfaces().length > 0)
			temp = add(cls.interfaces()[0]);
		else
			temp = root;

		SortedMap result = (SortedMap) temp.get(cls.qualifiedName());
		if (result == null) {
			result = new TreeMap();
			temp.put(cls.qualifiedName(), result);
		}
		return result;
	}

	/**
	 * Prints the html code corresponding to the tree.
	 * The tree is printed using <CODE>HtmlDoclet.os</CODE>.
	 */
	public void printTree(PrintWriter writer, RootDoc rootDoc, double overviewindent) {
		printBranch(writer, rootDoc, root, 0, overviewindent);
	}

	/**
	 * Prints a branch of the tree. The branch is printed using
	 * <CODE>HtmlDoclet.os</CODE>.
	 */
	protected void printBranch(PrintWriter writer, RootDoc rootDoc, SortedMap map, double indent, double overviewindent) {
		Set set = map.keySet();
		Iterator it = set.iterator();
		while (it.hasNext()) {
			String qualifName = (String) it.next();
			for (int i = 0; i < indent; i++)
				writer.print("&nbsp;");
			if (JSDoclet.hyperref)
				writer.print("<A HREF=\"#" + qualifName + "\">" + qualifName + "</A>");
			else
				writer.print(qualifName);
			//HtmlDoclet.printRef(cls.containingPackage(), cls.name(), "");
			writer.println("<br>");
			printBranch(writer, rootDoc, (SortedMap) map.get(qualifName), indent + overviewindent, overviewindent);
		}

	}
}
