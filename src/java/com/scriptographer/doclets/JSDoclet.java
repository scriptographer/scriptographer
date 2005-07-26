package com.scriptographer.doclets;

import com.sun.javadoc.*;

import java.io.*;
import java.util.*;

/**
 * This class provides a Java 2,<code>javadoc</code> Doclet which generates a
 * HTML document out of the java classes that it is used on.
 * 
 * @author Gregg Wonderly - C2 Technologies Inc.
 * @author Søren Caspersen - XO Software.
 * @author Stefan Marx
 */
public class JSDoclet extends Doclet {
	private static boolean inherited = true;
	private static Hashtable map2;
	private static Vector map;
	private static String basePackage = "";
	private static String title;
	private static String destDir;
	private static String author;
	private static ClassFilter clsFilt;
	private static boolean overview = false;
	private static boolean serial = false;
	private static double overviewIndent = 4.0;
	public static boolean hyperref = true;
	private static boolean versionInfo = false;
	private static boolean summaries = true;
	private static RootDoc root;

	private static boolean fieldSummary = true;
	private static boolean constructorSummary = true;
	private static String section1Open = "<h2>";
	private static String section2Open = "<h3>";
	private static String section3Open = "<h4>";
	private static String section1Close = "</h2>";
	private static String section2Close = "</h3>";
	private static String section3Close = "</h4>";
	private static boolean shortInherited = false;

	/**
	 * Called by the framework to format the entire document
	 * 
	 * @param root the root of the starting document
	 */
	public static boolean start(RootDoc root) {
		JSDoclet.root = root;
		map2 = new Hashtable();
		map = new Vector();
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(destDir + "packages.html"));

			writer.println("<head>");

			if (title != null)
				writer.println("<title>" + title + "</title>");

			writer.println("</head>");
			writer.println("<html><body>");

			if (title != null)
				writer.println("<center><h1>" + title + "</h1></center>");
			if (author != null)
				writer.println("<center>" + author + "</center>");

			ClassDoc[] cls = root.classes();

			PackageDoc[] specifiedPackages = root.specifiedPackages();
			for (int i = 0; i < specifiedPackages.length; i++) {
				Package P = new Package(specifiedPackages[i].name(), specifiedPackages[i]);
				map.add(P);
				map2.put(specifiedPackages[i].name(), P);
			}

			if (clsFilt != null)
				System.out.println("...Filter Classes with: " + clsFilt);
			for (int i = 0; i < cls.length; ++i) {
				ClassDoc cd = cls[i];

				if (clsFilt != null && !clsFilt.includeClass(cd)) {
					System.out.println("...Filtering out Class: " + cd.qualifiedName());
					continue;
				}

				Package v;
				PackageDoc pkgDoc = cd.containingPackage();
				String pkg = pkgDoc.name();
				if ((v = (Package) map2.get(pkg)) == null) {
					v = new Package(pkg, pkgDoc);
					map2.put(pkg, v);
					map.add(v);
				}
				v.addElement(cd);
			}

			// Sorting
			Enumeration h = map.elements();
			while (h.hasMoreElements()) {
				final Package pkg = (Package) h.nextElement();
				pkg.sort();
			}

			// Class hierachy
			if (overview) {
				writer.println(section1Open + "Class Hierarchy" + section1Close);

				// Classes
				ClassHierachy classHierachy = new ClassHierachy();
				Enumeration f = map.elements();
				while (f.hasMoreElements()) {
					final Package pkg = (Package) f.nextElement();
					for (int i = 0; i < pkg.classes.size(); i++) {
						classHierachy.add((ClassDoc) pkg.classes.get(i));
					}
				}
				if (classHierachy.root.size() != 0) {
					writer.println(section2Open + "Classes" + section2Close);
					classHierachy.printTree(writer, root, overviewIndent);
				}

				// Interfaces
				InterfaceHierachy interfaceHierachy = new InterfaceHierachy();
				f = map.elements();
				while (f.hasMoreElements()) {
					final Package pkg = (Package) f.nextElement();
					for (int i = 0; i < pkg.interfaces.size(); i++) {
						interfaceHierachy.add((ClassDoc) pkg.interfaces.get(i));
					}
				}
				if (interfaceHierachy.root.size() != 0) {
					writer.println(section2Open + "Interfaces" + section2Close);
					interfaceHierachy.printTree(writer, root, overviewIndent);
				}

				// Exceptions
				ClassHierachy exceptionHierachy = new ClassHierachy();
				f = map.elements();
				while (f.hasMoreElements()) {
					final Package pkg = (Package) f.nextElement();
					for (int i = 0; i < pkg.exceptions.size(); i++) {
						exceptionHierachy.add((ClassDoc) pkg.exceptions.get(i));
					}
				}
				if (exceptionHierachy.root.size() != 0) {
					writer.println(section2Open + "Exceptions" + section2Close);
					exceptionHierachy.printTree(writer, root, overviewIndent);
				}

				// Errors
				ClassHierachy errorHierachy = new ClassHierachy();
				f = map.elements();
				while (f.hasMoreElements()) {
					final Package pkg = (Package) f.nextElement();
					for (int i = 0; i < pkg.errors.size(); i++) {
						errorHierachy.add((ClassDoc) pkg.errors.get(i));
					}
				}
				if (errorHierachy.root.size() != 0) {
					writer.println(section2Open + "Errors" + section2Close);
					errorHierachy.printTree(writer, root, overviewIndent);
				}

				writer.println("<br><br>");

			} // End of Class hierachy

			Enumeration e = map.elements();
			while (e.hasMoreElements()) {
				final Package pkg = (Package) e.nextElement();

				System.out.println("processing Package: " + pkg.pkg);

				writer.println(section1Open + "Package " + pkg.pkg + section1Close);

				writer.print(createAnchor(pkg.pkg));

				writer.println("<b>Package Contents</b><br>");
				writer.println("<ul>");
				if (pkg.interfaces != null && pkg.interfaces.size() != 0) {
					writer.println("<li>");
					tocForClasses(writer, "Interfaces", pkg.interfaces);
				}
				if (pkg.classes != null && pkg.classes.size() != 0) {
					writer.println("<li>");
					tocForClasses(writer, "Classes", pkg.classes);
				}
				if (pkg.exceptions != null && pkg.exceptions.size() != 0) {
					writer.println("<li>");
					tocForClasses(writer, "Exceptions", pkg.exceptions);
				}
				if (pkg.errors != null && pkg.errors.size() != 0) {
					writer.println("<li>");
					tocForClasses(writer, "Errors", pkg.errors);
				}
				writer.println("</ul>");
				writer.println("<br>");

				// Package comments
				if (pkg.pkgDoc.inlineTags().length > 0) {
					printTags(writer, pkg.pkgDoc.inlineTags());

				}

				writer.println("<br><br>");

				layoutClasses(pkg.interfaces);
				layoutClasses(pkg.classes);
				layoutClasses(pkg.exceptions);
				layoutClasses(pkg.errors);
			}

			writer.println("</body>");
			writer.println("</html>");
			writer.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return true;
	}

	/**
	 * Returns how many arguments would be consumed if <code>option</code> is
	 * a recognized option.
	 * 
	 * @param option the option to check
	 */
	public static int optionLength(String option) {
		if (option.equals("-basepackage"))
			return 2;
		else if (option.equals("-d"))
			return 2;
		else if (option.equals("-windowtitle"))
			return 2;
		else if (option.equals("-doctitle"))
			return 2;
		else if (option.equals("-bottom"))
			return 2;
		else if (option.equals("-title"))
			return 2;
		else if (option.equals("-date"))
			return 2;
		else if (option.equals("-author"))
			return 2;
		else if (option.equals("-classfilter"))
			return 2;
		else if (option.equals("-noinherited"))
			return 1;
		else if (option.equals("-serial"))
			return 1;
		else if (option.equals("-nosummaries"))
			return 1;
		else if (option.equals("-noindex"))
			return 1;
		else if (option.equals("-tree"))
			return 1;
		else if (option.equals("-hyperref"))
			return 1;
		else if (option.equals("-version"))
			return 1;
		else if (option.equals("-link"))
			return 3;

		else if (option.equals("-help")) {
			System.err.println("JSDoclet Usage:");
			return 1;
		}
		System.out.println("unknown option " + option);
		int length = Doclet.optionLength(option);
		if (length < 1)
			length = 1;
		return length;
	}

	/**
	 * Checks the passed options and their arguments for validity.
	 * 
	 * @param args the arguments to check
	 * @param err the interface to use for reporting errors
	 */
	static public boolean validOptions(String[][] args, DocErrorReporter err) {
		for (int i = 0; i < args.length; ++i) {
			String[] arg = args[i];
			String option = arg[0];
			if (option.equals("-basepackage")) {
				basePackage = arg[1];
			} else if (option.equals("-d")) {
				destDir = arg[1];
				if (!destDir.endsWith("/"))
					destDir += "/";
			} else if (option.equals("-title")) {
				title = arg[1];
			} else if (option.equals("-author")) {
				author = arg[1];
			} else if (option.equals("-classfilter")) {
				String fcl = arg[1];
				try {
					clsFilt = (ClassFilter) Class.forName(fcl).newInstance();
				} catch (Exception ex) {
					ex.printStackTrace();
					System.exit(2);
				}
			} else if (option.equals("-noinherited")) {
				inherited = false;
			} else if (option.equals("-serial")) {
				serial = true;
			} else if (option.equals("-nosummaries")) {
				summaries = false;
			} else if (option.equals("-tree")) {
				overview = true;
			} else if (option.equals("-hyperref")) {
				hyperref = true;
			} else if (option.equals("-version")) {
				versionInfo = true;
			} else if (option.equals("-treeindent")) {
				overviewIndent = Double.parseDouble(arg[1]);
			} else if (option.equals("-version")) {
				versionInfo = true;
			} else if (option.equals("-nofieldsummary")) {
				fieldSummary = false;
			} else if (option.equals("-noconstructorsummary")) {
				constructorSummary = false;
			} else if (option.equals("-shortinherited")) {
				shortInherited = true;
			}

		}
		return true;
	}
	
	static String createLink(String qualifiedName, String name, String anchor, String title) {
		if (hyperref) {
			if (anchor == null) {
				anchor = name;
			}
			String path = qualifiedName.substring(0, qualifiedName.length() - name.length());
			path = packageRelativIdentifier(basePackage, path).replace('.', '/');
			return "<a href=\"" + path + name + ".html#" + anchor + "\" target=\"classFrame\">" + title + "</a>";
		} else
			return title;
	}
	
	static String createLink(ClassDoc cl) {
		return createLink(cl.qualifiedName(), cl.name(), cl.qualifiedName(), cl.name());
	}
	
	static String createLink(ExecutableMemberDoc mem) {
		return createLink(mem.containingClass().qualifiedName(), mem.containingClass().name(), mem.qualifiedName() + mem.signature(), mem.name() + mem.signature());
	}

	static String createLink(FieldDoc mem) {
		return createLink(mem.containingClass().qualifiedName(), mem.containingClass().name(), mem.qualifiedName(), mem.name());
	}

	static String createAnchor(String name) {
		if (hyperref)
			return "<a name=\"" + name + "\">";
		else
			return "";
	}
	
	static String createAnchor(ClassDoc cl) {
		return createAnchor(cl.qualifiedName());
	}

	static String createAnchor(ExecutableMemberDoc mem) {
		return createAnchor(mem.qualifiedName() + mem.signature());
	}

	static String createAnchor(FieldDoc mem) {
		return createAnchor(mem.qualifiedName());
	}

	/**
	 * Produces a table-of-contents for classes.
	 */
	static void tocForClasses(PrintWriter writer, String title, Vector v) {
		if (v.size() > 0) {
			writer.println("<b> " + title + "</b><ul>");
			for (int i = 0; i < v.size(); ++i) {
				ClassDoc cd = (ClassDoc) v.elementAt(i);
				writer.print("<li>");
				writer.print(createLink(cd));
				writer.println(" ");
				printTags(writer, cd.firstSentenceTags());

			}
			writer.println("</ul>");
		}
	}

	static Hashtable refs = new Hashtable();
	static Hashtable externalrefs = new Hashtable();

	/**
	 * Lays out a list of classes.
	 * 
	 * @param type Title of the section, e.g. "Interfaces", "Exceptions" etc.
	 * @param classes Vector of the classes to be laid out.
	 */
	static void layoutClasses(Vector classes) {

		for (int i = 0; i < classes.size(); ++i) {
			ClassDoc cd = (ClassDoc) classes.elementAt(i);
			try {
				String name = cd.name();
				String path = cd.qualifiedName();
				// cut away name:
				path = path.substring(0, path.length() - name.length());
				path = packageRelativIdentifier(basePackage, path);
				// now split into packages and create subdirs:
				String[] parts = path.split("\\.");
				path = "";
				int levels = 0;
				for (int j = 0; j < parts.length; j++) {
					if (parts[j].equals(name))
						break;

					path += parts[j] + "/";
					levels++;
					File dir = new File(destDir + path);
					if (!dir.exists())
						dir.mkdir();
				}
				PrintWriter writer = new PrintWriter(new FileWriter(destDir + path + name + ".html"));
				writer.println("<html><head>");
				
				String base = "";
				for (int j = 0; j < levels; j++)
					base += "../";
				writer.println("<base href=\"" + base + "\">");
				writer.println("</head><body>");
				writer.print(createAnchor(cd));

				writer.print(section2Open);

				String type = "Class";
				if (cd.isInterface())
					type = "Interface";
				else if (cd.isException())
					type = "Exception";

				writer.println(type + " " + cd.name() + section2Close);

				if (cd.inlineTags().length > 0) {
					printTags(writer, cd.inlineTags());
				}

				SeeTag[] sees = cd.seeTags();
				if (sees.length > 0) {
					writer.println(section3Open + "See also" + section3Close);
					writer.println("<ul>");
					for (int j = 0; j < sees.length; ++j) {
						writer.print("<li>");
						printSeesTag(writer, sees[j], cd.containingPackage());

					}
					writer.println("</ul>");
				}

				writer.println(section3Open + "Declaration" + section3Close);

				writer.print(cd.modifiers() + " ");
				if (cd.isInterface() == false)
					writer.print("class ");
				writer.println(cd.name());
				ClassDoc sc = cd.superclass();
				if (sc != null) {
					writer.println("<br> <b> extends </b> ");
					writer.print(createLink(sc));
				}

				ClassDoc intf[] = cd.interfaces();
				if (intf.length > 0) {

					if (cd.isInterface() == false) {
						writer.println("<br> <b> implements </b> ");
					} else {
						writer.println("<br> <b> extends </b> ");
					}
					for (int j = 0; j < intf.length; ++j) {
						ClassDoc in = intf[j];
						String nm;
						if (in.containingPackage().name().equals(cd.containingPackage().name())) {
							nm = in.name();
						} else
							nm = in.qualifiedName();
						if (j > 0)
							writer.print(", ");
						writer.print(nm);
					}
				}
				ExecutableMemberDoc[] mems;
				FieldDoc[] flds;

				Tag[] verTags = cd.tags("version");
				if (versionInfo && verTags.length > 0) {
					writer.println(section3Open + "Version" + section3Close + verTags[0].text());
				}

				String subclasses = "";
				for (int index = 0; index < root.classes().length; index++) {
					ClassDoc cls = root.classes()[index];
					if (cls.subclassOf(cd) && !cls.equals(cd)) {
						if (!subclasses.equals(""))
							subclasses += ", ";
						subclasses += cls.name();
					}
				}

				if (cd.isInterface()) {
					if (!subclasses.equals("")) {
						writer.println(section3Open + "All known subinterfaces" + section3Close + subclasses);
					}
				} else {
					if (!subclasses.equals("")) {
						writer.println(section3Open + "All known subclasses" + section3Close + subclasses);
					}
				}

				String subintf = "";
				String implclasses = "";
				if (cd.isInterface()) {
					for (int index = 0; index < root.classes().length; index++) {
						ClassDoc cls = root.classes()[index];
						boolean impls = false;
						for (int w = 0; w < cls.interfaces().length; w++) {
							ClassDoc intfDoc = cls.interfaces()[w];
							if (intfDoc.equals(cd))
								impls = true;
						}
						if (impls) {
							if (cls.isInterface()) {
								if (!subintf.equals(""))
									subintf += ", ";

								subintf += createLink(cls);
							} else {
								if (!implclasses.equals(""))
									implclasses += ", ";
								implclasses += createLink(cls);
							}
						}
					}

					if (!implclasses.equals(""))
						writer.println(section3Open + "All classes known to implement interface" + section3Close
							+ implclasses);
				}

				if (summaries) {
					if (fieldSummary) {
						flds = cd.fields();
						if (flds.length > 0) {
							printFieldSummary(writer, flds, "Field summary");
						}
					}

					if (constructorSummary) {
						mems = cd.constructors();
						if (mems.length > 0) {
							printMethodSummary(writer, mems, "Constructor summary");
						}
					}

					mems = cd.methods();
					if (mems.length > 0) {
						printMethodSummary(writer, mems, "Method summary");
					}
				}

				flds = cd.serializableFields();
				if (flds.length > 0 && serial) {
					printFields(writer, cd, flds, "Serializable Fields", false);
				}
				flds = cd.fields();
				if (flds.length > 0) {
					printFields(writer, cd, flds, "Fields", true);
				}
				mems = cd.constructors();
				if (mems.length > 0) {
					writer.println(section3Open + "Constructors" + section3Close);
					printMembers(writer, cd, mems, true);
				}
				mems = cd.methods();
				if (mems.length > 0) {
					writer.println(section3Open + "Methods" + section3Close);
					printMembers(writer, cd, mems, true);
				}

				if (inherited == true) {
					boolean yet = false;
					if (!cd.isInterface()) {

						ClassDoc par = cd.superclass();

						while (par != null && par.qualifiedName().equals("java.lang.Object") == false) {

							MemberDoc[] inheritedmembers = new MemberDoc[par.fields().length + par.methods().length];
							for (int k = 0; k < par.fields().length; k++) {
								inheritedmembers[k] = par.fields()[k];
							}
							for (int k = 0; k < par.methods().length; k++) {
								inheritedmembers[k + par.fields().length] = par.methods()[k];
							}
							// print only if members available (
							// if class not found because classpath not
							// correctly
							// set, they would be missed)
							if (inheritedmembers.length > 0) {
								yet = true;
								writer.print(section3Open + "Members inherited from class <tt>" + par.qualifiedName()
									+ "</tt>" + section3Close);
								if (hyperref) {
									writer.print(createLink(par));
									if (!shortInherited)
										writer.print("<br>");
								}
								printinheritedMembers(writer, par, inheritedmembers, false);
							}
							par = par.superclass();
						}

					} else {

						List superclasses = new Vector();
						while (getSuperClass(cd, superclasses) != null) {
						}

						superclasses = sortSuperclasses(superclasses);

						for (int m = superclasses.size() - 1; m >= 0; m--) {

							ClassDoc par = (ClassDoc) superclasses.get(m);
							MemberDoc[] inheritedmembers = new MemberDoc[par.fields().length + par.methods().length];
							for (int k = 0; k < par.fields().length; k++) {
								inheritedmembers[k] = par.fields()[k];
							}
							for (int k = 0; k < par.methods().length; k++) {
								inheritedmembers[k + par.fields().length] = par.methods()[k];
							}
							// print only if members available (
							// if class not found because classpath not
							// correctly
							// set, they would be missed)
							if (inheritedmembers.length > 0) {
								yet = true;
								writer.print(section3Open + "Members inherited from class <tt>" + par.qualifiedName()
									+ "</tt>" + section3Close);
								if (hyperref) {
									writer.print(createLink(par));
									if (!shortInherited)
										writer.print("<br>");
								}
								printinheritedMembers(writer, par, inheritedmembers, false);
							}
						}

					} // end if interface

					if (shortInherited && yet)
						writer.println("<br><br>");

				}
				writer.println("</body>");
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * searches in all classes of the root doc for a superclass of the given
	 * subsclass, that is not already in the list of superclasses adn adds it to
	 * the list if found
	 */
	static private ClassDoc getSuperClass(ClassDoc subclass, List superclasses) {
		ClassDoc[] cls = root.classes();
		for (int n = 0; n < cls.length; ++n) {
			ClassDoc cd2 = (ClassDoc) cls[n];
			if (subclass.subclassOf(cd2)) {
				if (subclass != cd2 && !superclasses.contains(cd2)) {
					superclasses.add(cd2);
					return cd2;
				}
			}
		}
		return null;
	}

	static private List sortSuperclasses(List superclasses) {
		List result = new Vector();
		int count = superclasses.size();

		for (int k = 0; k < count; k++) {
			for (int i = 0; i < superclasses.size(); i++) {
				ClassDoc cd = (ClassDoc) superclasses.get(i);
				boolean isSubInterface = false;
				for (int j = 0; j < superclasses.size(); j++) {
					ClassDoc cd2 = (ClassDoc) superclasses.get(j);
					if (cd.subclassOf(cd2) && cd != cd2) {
						isSubInterface = true;
						break;
					}

				}
				if (!isSubInterface) {
					result.add(cd);
					superclasses.remove(cd);
					break;
				}
			}

		}
		return result;
	}

	/**
	 * Enumerates the fields passed and formats them using Html statements.
	 * 
	 * @param flds the fields to format
	 */
	static void printFields(PrintWriter writer, ClassDoc cd, FieldDoc[] flds, String title, boolean labels) {
		boolean yet = false;
		for (int i = 0; i < flds.length; ++i) {
			FieldDoc f = flds[i];
			if (!yet) {
				writer.println(section3Open + "" + title + "" + section3Close);
				writer.println("<ul>");
				yet = true;
			}
			writer.println("<li>");
			if (labels) {
				writer.print(createAnchor(f));
			}
			if (!cd.isInterface())
				writer.print(f.modifiers() + " ");
			writer.print(packageRelativIdentifier(f.containingPackage(), f.type().qualifiedTypeName()) + " ");
			writer.print(" <b> " + f.name() + "</b>");

			if (f.inlineTags().length > 0 || f.seeTags().length > 0) {
				writer.println("<ul>");
				if (f.inlineTags().length > 0) {
					writer.println("<li>");
					printTags(writer, f.inlineTags());
				}

				// See tags
				SeeTag[] sees = f.seeTags();
				if (sees.length > 0) {
					writer.println("<li>See also");
					writer.println("  <ul>");
					for (int j = 0; j < sees.length; ++j) {
						writer.print("<li>");
						printSeesTag(writer, sees[j], f.containingPackage());
					}
					writer.println("  </ul>");
				}

				writer.println("</ul>");
			}
		}
		if (yet) {
			writer.println("</ul>");
		}
	}

	/**
	 * Produces a constructor/method summary.
	 * 
	 * @param dmems The fields to be summarized.
	 * @param title The title of the section.
	 */
	static void printMethodSummary(PrintWriter writer, ExecutableMemberDoc[] dmems, String title) {
		if (dmems.length == 0)
			return;
		writer.println(section3Open + "" + title + "" + section3Close);
		writer.println("<ul>");
		List l = Arrays.asList(dmems);
		Collections.sort(l);
		Iterator itr = l.iterator();
		for (int i = 0; itr.hasNext(); ++i) {
			ExecutableMemberDoc mem = (ExecutableMemberDoc) itr.next();
			writer.println("<li>");
			writer.print(createLink(mem));
			writer.print(" ");
			printTags(writer, mem.firstSentenceTags());
		}
		writer.println("</ul>");
	}

	/**
	 * Produces a field summary.
	 * 
	 * @param dmems The fields to be summarized.
	 * @param title The title of the section.
	 */
	static void printFieldSummary(PrintWriter writer, FieldDoc[] dmems, String title) {
		if (dmems.length == 0)
			return;
		writer.println(section3Open + "" + title + "" + section3Close);
		writer.println("<ul>");
		List l = Arrays.asList(dmems);
		Collections.sort(l);
		Iterator itr = l.iterator();
		for (int i = 0; itr.hasNext(); ++i) {
			FieldDoc mem = (FieldDoc) itr.next();
			writer.print("<li>");
			writer.print(createLink(mem));
			writer.print(" ");
			printTags(writer, mem.firstSentenceTags());
		}
		writer.println("</ul>");
	}

	/**
	 * Enumerates the members of a section of the document and formats them
	 * using Tex statements.
	 */
	static void printMembers(PrintWriter writer, ClassDoc cd, ExecutableMemberDoc[] dmems, boolean labels) {
		if (dmems.length == 0)
			return;
		List l = Arrays.asList(dmems);
		Collections.sort(l);
		Iterator itr = l.iterator();
		writer.println("<ul>");
		for (int i = 0; itr.hasNext(); ++i) {
			ExecutableMemberDoc mem = (ExecutableMemberDoc) itr.next();
			printMember(writer, mem);
		}
		writer.println("</ul>");
	}

	/**
	 * Enumerates the members of a section of the document and formats them
	 * using Tex statements.
	 */
	static void printMember(PrintWriter writer, ExecutableMemberDoc mem) {
		printMember(writer, mem, null);
	}

	/**
	 * Enumerates the members of a section of the document and formats them
	 * using Tex statements.
	 */
	static void printMember(PrintWriter writer, ExecutableMemberDoc mem, ExecutableMemberDoc copiedTo) {
		PackageDoc pac = copiedTo == null ? mem.containingPackage() : copiedTo.containingPackage();
		if (mem instanceof MethodDoc) {
			MethodDoc method = (MethodDoc) mem;
			if (method.commentText() == "" && method.seeTags().length == 0 && method.throwsTags().length == 0
				&& method.paramTags().length == 0) {

				// No javadoc available for this method. Recurse through
				// superclasses
				// and implemented interfaces to find javadoc of overridden
				// methods.

				boolean found = false;

				ClassDoc doc = method.overriddenClass();
				if (doc != null) {
					for (int i = 0; !found && i < doc.methods().length; ++i) {
						if (doc.methods()[i].name().equals(mem.name())
							&& doc.methods()[i].signature().equals(mem.signature())) {
							printMember(writer, doc.methods()[i], copiedTo == null ? mem : copiedTo);
							found = true;
						}
					}
				}
				doc = method.containingClass();
				for (int j = 0; !found && j < doc.interfaces().length; j++) {
					ClassDoc inf = doc.interfaces()[j];
					for (int i = 0; !found && i < inf.methods().length; ++i) {
						if (inf.methods()[i].name().equals(mem.name())
							&& inf.methods()[i].signature().equals(mem.signature())) {
							printMember(writer, inf.methods()[i], copiedTo == null ? mem : copiedTo);
							found = true;
						}
					}
				}
				if (found)
					return;
			}
		}

		ParamTag[] params = mem.paramTags();

		// Some index and hyperref stuff
		writer.println("<li>");

		if (copiedTo == null) {
			writer.print(createAnchor(mem));
		} else {
			writer.print(createAnchor(copiedTo));
		}

		writer.print("<b> " + mem.name() + "</b><br> ");

		// Print signature
		writer.print("<tt> ");
		if (!mem.containingClass().isInterface())
			writer.print(mem.modifiers());
		if (mem instanceof MethodDoc) {
			writer.print(" " + packageRelativIdentifier(pac, ((MethodDoc) mem).returnType().toString()));
		}
		writer.print("<b> " + mem.name() + "(</b> ");
		Parameter[] parms = mem.parameters();
		int p = 0;
		String qparmstr = "";
		String parmstr = "";
		for (; p < parms.length; ++p) {
			if (p > 0)
				writer.println(",");
			Type t = parms[p].type();
			writer.print("<tt> " + packageRelativIdentifier(pac, t.qualifiedTypeName()));
			writer.print(t.dimension() + "</tt>");
			if (parms[p].name().equals("") == false)
				writer.print(" <b> " + parms[p].name() + "</b>");
			if (qparmstr.length() != 0)
				qparmstr += ",";
			qparmstr += t.qualifiedTypeName() + t.dimension();
			if (parmstr.length() != 0)
				parmstr += ",";
			parmstr += t.typeName() + t.dimension();
		}
		writer.print(" <b>)</b>");

		// Thrown exceptions
		ClassDoc[] thrownExceptions = mem.thrownExceptions();
		if (thrownExceptions != null && thrownExceptions.length > 0) {
			writer.print(" throws " + thrownExceptions[0].qualifiedName());
			for (int e = 1; e < thrownExceptions.length; e++) {
				writer.print(", " + thrownExceptions[e].qualifiedName());
			}
		}
		writer.println();

		writer.println("</tt>");
		boolean yet = false;

		// Description
		if (mem.inlineTags().length > 0) {
			if (!yet) {
				writer.println("<ul>");
				yet = true;
			}
			writer.println("<li>");
			writer.println("<b> Description </b> ");
			printTags(writer, mem.inlineTags());
		}

		// Parameter tags
		if (params.length > 0) {
			if (!yet) {
				writer.println("<ul>");
				yet = true;
			}
			writer.println("<li>");

			writer.println("<b> Parameters </b>");

			writer.println("  <ul>");

			for (int j = 0; j < params.length; ++j) {
				writer.println("   <li>");

				writer.print("<tt> " + params[j].parameterName() + "</tt>" + " -- ");
				printTags(writer, params[j].inlineTags());
			}
			writer.println("  </ul>");
		}

		// Return tag
		if (mem instanceof MethodDoc) {
			Tag[] ret = mem.tags("return");
			if (ret.length > 0) {
				if (!yet) {
					writer.println("<ul>");

					yet = true;
				}
				writer.println("<li><b> Returns </b> -- ");
				for (int j = 0; j < ret.length; ++j) {
					printTags(writer, ret[j].inlineTags());
					writer.println(" ");
				}

			}
		}

		// Throws or Exceptions tag
		if (mem instanceof ExecutableMemberDoc) {
			ThrowsTag[] excp = ((ExecutableMemberDoc) mem).throwsTags();
			if (excp.length > 0) {
				if (!yet) {
					writer.println("<ul>");

					yet = true;
				}
				writer.println("<li><b> Throws</b>");
				writer.println("  <ul>");

				for (int j = 0; j < excp.length; ++j) {
					String ename = excp[j].exceptionName();
					ClassDoc cdoc = excp[j].exception();
					if (cdoc != null)
						ename = cdoc.qualifiedName();
					writer.print("   <li> " + ename + " -- ");
					printTags(writer, excp[j].inlineTags());

				}
				writer.println("  </ul>");

			}
		}

		// See tags
		SeeTag[] sees = mem.seeTags();
		if (sees.length > 0) {
			if (!yet) {
				writer.println("<ul>");
				yet = true;
			}
			writer.println("<li><b> See also</b>");
			writer.println("  <ul>");
			for (int j = 0; j < sees.length; ++j) {
				writer.print("<li> ");
				printSeesTag(writer, sees[j], pac);
			}
			writer.println("  </ul>");
		}
		if (yet)
			writer.println("</ul>");
		else
			writer.println("<br>");

		// writer.println( "<br>");
	}

	/**
	 * Enumerates the members of a section of the document and formats them
	 * using Tex statements.
	 * 
	 * @param mems the members of this entity
	 * @see #start
	 */
	static void printinheritedMembers(PrintWriter writer, ClassDoc cd, MemberDoc[] dmems, boolean labels) {
		if (dmems.length == 0)
			return;

		List l = Arrays.asList(dmems);
		Collections.sort(l);
		Iterator itr = l.iterator();

		if (shortInherited) {

			for (int i = 0; itr.hasNext(); ++i) {
				MemberDoc mem = (MemberDoc) itr.next();

				// print only member names

				if (i != 0)
					writer.print(", ");
				writer.print(mem.name());
			}
		} else {

			writer.println("<ul>");
			for (int i = 0; itr.hasNext(); ++i) {
				MemberDoc mem = (MemberDoc) itr.next();

				// Print signature

				writer.println("<li> ");
				writer.print("<tt> ");
				writer.print(mem.modifiers());
				if (mem instanceof MethodDoc) {
					writer.print(" " + ((MethodDoc) mem).returnType().typeName());
				}
				writer.print(" <b> " + mem.name() + "</b>");
				if (mem instanceof MethodDoc) {
					writer.print("( ");
					Parameter[] parms = ((MethodDoc) mem).parameters();
					int p = 0;
					String qparmstr = "";
					String parmstr = "";
					for (; p < parms.length; ++p) {
						if (p > 0)
							writer.println(",");
						Type t = parms[p].type();
						writer.print("<tt> " + packageRelativIdentifier(mem.containingPackage(), t.qualifiedTypeName()));
						writer.print(t.dimension() + "</tt>");
						if (parms[p].name().equals("") == false)
							writer.print(" <b> " + parms[p].name() + "</b>");
						if (qparmstr.length() != 0)
							qparmstr += ",";
						qparmstr += t.qualifiedTypeName() + t.dimension();
						if (parmstr.length() != 0)
							parmstr += ",";
						parmstr += t.typeName() + t.dimension();
					}
					writer.print(" )");

					// Thrown exceptions
					if (mem instanceof ExecutableMemberDoc) {
						ClassDoc[] thrownExceptions = ((ExecutableMemberDoc) mem).thrownExceptions();
						if (thrownExceptions != null && thrownExceptions.length > 0) {
							writer.print(" throws "
								+ packageRelativIdentifier(mem.containingPackage(), thrownExceptions[0].qualifiedName()));
							for (int e = 1; e < thrownExceptions.length; e++) {
								writer.print(", " + thrownExceptions[e].qualifiedName());
							}
						}
					}
					writer.println();

					if (labels && qparmstr.startsWith("field") == false) {
						// writer.print("\\label{"+cd.qualifiedName()+"."+mem.name()+
						// (( qparmstr.length() > 0 ) ?
						// ("("+qparmstr+")") :
						// "" ))) );
						// writer.print("\\label{"+cd.name()+"."+mem.name()+
						// (( parmstr.length() > 0 ) ?
						// ("("+parmstr+")"):
						// ""))) );
					}
				}
				writer.println("</tt>");
				boolean yet = false;

				if (yet)
					writer.println("</ul>");

			}
			writer.println("</ul>");
		} // short inherited
	}

	/**
	 * Prints a sequence of tags obtained from e.g. com.sun.javadoc.Doc.tags().
	 */
	static void printTags(PrintWriter writer, Tag[] tags) {
		String htmlstr = new String();

		for (int i = 0; i < tags.length; i++)
			if (tags[i] instanceof SeeTag) {
				SeeTag link = (SeeTag) tags[i];

				String linkstr = "";
				if (link.referencedMember() != null) {
					if (link.referencedMember() instanceof ExecutableMemberDoc) {
						ExecutableMemberDoc m = (ExecutableMemberDoc) link.referencedMember();
						linkstr = m.qualifiedName() + m.signature();
					} else
						linkstr = link.referencedMember().qualifiedName();
				} else if (link.referencedClass() != null)
					linkstr = link.referencedClass().qualifiedName();
				else if (link.referencedPackage() != null)
					linkstr = link.referencedPackage().name();

				if (linkstr.equals("")) {
					htmlstr += link.text();
				} else {
				}
			} else {
				htmlstr += tags[i].text();
			}

		writer.print(htmlstr);
	}

	/**
	 * Returns a package relative identifier.
	 * 
	 * @param doc The package the identifier should be relative to.
	 * @param str The identifier to be made relative.
	 */
	static String packageRelativIdentifier(PackageDoc doc, String str) {
		return packageRelativIdentifier(doc.name(), str);
	}

	static String packageRelativIdentifier(String name, String str) {
		if (str.startsWith(name + ".")) {
			return str.substring(name.length() + 1);
		} else {
			return str;
		}
	}

	/**
	 * Prints a "see also" tag.
	 * 
	 * @param tag The "see also" tag to print.
	 * @param relativeTo The package to which the see tag should be relative to.
	 */
	static void printSeesTag(PrintWriter writer, SeeTag tag, PackageDoc relativeTo) {
		String memName = "";
		String memText = "";
		if (tag.referencedMember() != null) {
			if (tag.referencedMember() instanceof ExecutableMemberDoc) {
				ExecutableMemberDoc m = (ExecutableMemberDoc) tag.referencedMember();
				memName = m.qualifiedName() + m.signature();
				memText = packageRelativIdentifier(relativeTo, m.qualifiedName());
				memText += "(";
				for (int i = 0; i < m.parameters().length; i++) {
					memText += packageRelativIdentifier(relativeTo, m.parameters()[i].typeName());
					if (i < m.parameters().length - 1)
						// memText += ",\\allowbreak ";
						memText += ", ";
				}
				memText += ")";
			} else {
				memName = tag.referencedMember().qualifiedName();
				memText = packageRelativIdentifier(relativeTo, memName);
			}

		} else if (tag.referencedClass() != null) {
			memName = tag.referencedClass().qualifiedName();
			memText = packageRelativIdentifier(relativeTo, memName);
		} else if (tag.referencedPackage() != null) {
			memName = tag.referencedPackage().name();
			memText = memName;
		}

		if (memName.equals("") == false) {
			writer.print("<tt> ");
			writer.print(createLink(tag.referencedClass().qualifiedName(), tag.referencedClass().name(), memName, memText));
			writer.println("</tt>");
		} else {
			writer.print(tag.text());
		}
	}
}