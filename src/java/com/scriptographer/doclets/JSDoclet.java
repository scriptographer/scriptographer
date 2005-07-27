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
	private static boolean debug = false;
	private static boolean inherited = true;
	private static String basePackage = "";
	private static String doctitle;
	private static String bottom;
	private static String destDir;
	private static String author;
	public static boolean hyperref = true;
	private static boolean versionInfo = false;
	private static boolean summaries = true;
	private static RootDoc root;

	private static boolean fieldSummary = true;
	private static boolean constructorSummary = true;
	private static String section1Open = "<h1>";
	private static String section2Open = "<h2>";
	private static String section1Close = "</h1>";
	private static String section2Close = "</h2>";
	private static boolean shortInherited = false;
	private static Hashtable classInfos;
	private static Hashtable memberGroups;

	/**
	 * A group of members that are all "compatible" in a JS way, e.g. have the same
	 * amount of parameter with different types each (e.g. setters)
	 * or various amount of parameters with default parameter versions, e.g.
	 * all com.scriptogrpaher.ai.Pathfinder functions
	 */
	static class MemberGroup {
		Vector members = new Vector();
		Hashtable map = new Hashtable();
		ClassInfo classInfo;
		String name;
		static final int MODE_NONE = 0;
		static final int MODE_PARAMCOUNT = 1;
		static final int MODE_PARAMS = 2;
		int mode = MODE_NONE;
		
		boolean isField = false;
		boolean isMethod = false;
		String parameters = null;
		MemberDoc mainMember = null;
		
		MemberGroup(ClassInfo classInfo, String name) {
			this.classInfo = classInfo;
			this.name = name;
		}
		
		boolean isArray(String typeName) {
			return typeName.indexOf('[') != -1 && typeName.indexOf(']') != -1;
		}
		
		boolean isSuperclass(ClassDoc cd, String superclass) {
			while (cd != null) {
				if (cd.qualifiedName().equals(superclass))
					return true;
				cd = cd.superclass();
			}
			return false;
		}
		
		boolean isCollection(ClassDoc cd) {
			return isSuperclass(cd, "java.util.Collection");
		}
		
		boolean isPoint(ClassDoc cd) {
			return isSuperclass(cd, "java.awt.geom.Point2D");
		}
		
		boolean isPointCompatible(ClassDoc cd) {
			return isSuperclass(cd, "java.awt.Dimension") || isSuperclass(cd, "java.awt.geom.Point2D");
		}
		
		boolean isCompatible(Parameter param1 ,Parameter param2) {
		//  if (param1.name().equals(param2.name())) {
				String typeName1 = param1.typeName();
				String typeName2 = param2.typeName();
				ClassDoc cd1 = param1.type().asClassDoc();
				ClassDoc cd2 = param2.type().asClassDoc();
				if (typeName1.equals(typeName2))
					return true;
				else if (cd1 != null && cd2 != null && (cd1.subclassOf(cd2) || cd2.subclassOf(cd1))) {
						return true;
				} else if (isArray(typeName1) && (isArray(typeName2) || isCollection(cd2)) ||
						  isArray(typeName2) && (isArray(typeName1) || isCollection(cd1))) {
					return true;
				} else if (isPoint(cd1) && isPointCompatible(cd2) || isPoint(cd2) && isPointCompatible(cd1)) {
					return true;
				}
		//  }
			return false;
		}

		boolean isCompatible(MemberDoc member1, MemberDoc member2) {
			if (debug) System.out.println(mode + " " + member1 + " " + member2);
			if (member1 instanceof ExecutableMemberDoc && member2 instanceof ExecutableMemberDoc) {
				ExecutableMemberDoc method1 = (ExecutableMemberDoc) member1;
				ExecutableMemberDoc method2 = (ExecutableMemberDoc) member2;
				// rule 1: static or not
				if (method1.isStatic() != method2.isStatic()) {
					if (debug) System.out.println("R 1");
					return false;
				}
				// rule 2: same return type
				if (method1 instanceof MethodDoc && member2 instanceof MethodDoc &&
					!((MethodDoc) method1).returnType().qualifiedTypeName().equals(((MethodDoc) method2).returnType().qualifiedTypeName())) {
					if (debug) System.out.println("R 2");
					return false;
				}
				Parameter[] params1 = method1.parameters();
				Parameter[] params2 = method2.parameters();
				if ((mode == MODE_NONE || mode == MODE_PARAMCOUNT) && params1.length == params2.length) {
					// rule 3: in case of same amount of params,  the types should be compatible
					for (int i = 0; i < params1.length; i++) {
						Parameter param1 = params1[i];
						Parameter param2 = params2[i];
						if (!isCompatible(param1, param2)) {
							if (debug) System.out.println("R 3 " + params1[i].name() + " " + params2[i].name());
							return false;
						}
					}
					mode = MODE_PARAMCOUNT;
					return true;
				} else if (mode == MODE_NONE || mode == MODE_PARAMS) {
					// rule 4: if not the same amount of params, the types need to be the same:
					int count = Math.min(params1.length, params2.length);
					for (int i = 0; i < count; i++) {
						if (!isCompatible(params1[i], params2[i])) {
							if (debug) System.out.println("R 4");
							return false;
						}
					}
					mode = MODE_PARAMS;
					return true;
				}
				return true;
			} else { // fields cannot be grouped
				return false;
			}
		}

		boolean add(MemberDoc member) {
			boolean swallow = true;
			if (member instanceof ExecutableMemberDoc) {
				isMethod = true;
				// do not add base versions for overridden functions 
				String signature = ((ExecutableMemberDoc) member).signature();
				if (map.get(signature) != null)
					swallow = false;
				map.put(signature, member);
			} else {
				isField = true;
			}
			if (swallow) {
				// see wther the new member fits the existing ones:
				for (Iterator it = members.iterator(); it.hasNext();) {
					if (!isCompatible((MemberDoc) it.next(), member))
						return false;
				}
				members.add(member);
			}
			// even add it to the global table when it's not added here, so hrefs to overriden functions work
			memberGroups.put(member.qualifiedName(), this);
			return true;
		}
		
		void init() {
			if (mode == MODE_PARAMS) {
				// now sort the members by param count:
				Comparator comp = new Comparator() {
					public int compare(Object o1, Object o2) {
						ExecutableMemberDoc mem1 = (ExecutableMemberDoc) o1;
						ExecutableMemberDoc mem2 = (ExecutableMemberDoc) o2;
						int c1 = mem1.parameters().length;
						int c2 = mem2.parameters().length;
						if (c1 < c2) return -1;
						else if (c1 > c2) return 1;
						else return 0;
					}
	
					public boolean equals(Object obj) {
						return false;
					}
				};
				Collections.sort(members, comp);
				mainMember = (MemberDoc) members.lastElement();
			} else if (mode == MODE_PARAMCOUNT) {
				// find the suiting member: take the one with the most documentation
				int maxTags = 0;
				for (Iterator it = members.iterator(); it.hasNext();) {
					MemberDoc mem = (MemberDoc) it.next();
					int numTags = mem.inlineTags().length;
					if (numTags > maxTags) {
						mainMember = mem;
						maxTags = numTags;
					}
				}
			}
		}

		String name() {
			return name;
		}
		
		MemberDoc getFirstMember() {
			return ((MemberDoc) members.get(0));
		}

		Tag[] firstSentenceTags() {
			return getFirstMember().firstSentenceTags();
		}

		boolean isStatic() {
			return getFirstMember().isStatic();
		}

		ClassDoc containingClass() {
			return classInfo.classDoc;
		}

		Tag[] inlineTags() {
			return getFirstMember().inlineTags();
		}

		SeeTag[] seeTags() {
			return getFirstMember().seeTags();
		}

		PackageDoc containingPackage() {
			return classInfo.classDoc.containingPackage();
		}

		String signature() {
			if (isMethod) {
				return ((ExecutableMemberDoc) getFirstMember()).signature();
			} else {
				return "";
			}
		}

		String getParameterText() {
			if (parameters == null) {
				parameters = "";
				if (isMethod) {
					parameters += "(";
					if (mode == MODE_PARAMS) {
						int prevCount = 0;
						int closeCount = 0;
						for (Iterator it = members.iterator(); it.hasNext();) {
							ExecutableMemberDoc mem = ((ExecutableMemberDoc) it.next());
							Parameter[] params = mem.parameters();
							int count = params.length;
							if (count > prevCount) {
								if (prevCount > 0)
									parameters += "[";
								
								for (int i = prevCount; i < count; i++) {
									if (i > 0)
										parameters += ", ";
									parameters += params[i].name();
								}
								closeCount++;
								prevCount = count;
							}
						}
						for (int i = 1; i < closeCount; i++)
							parameters += "]";
					} else {
						ExecutableMemberDoc member = (ExecutableMemberDoc) getFirstMember();
						Parameter[] params = member.parameters();
						for (int i = 0; i < params.length; i++) {
							if (i > 0)
								parameters += ", ";
							parameters += params[i].name();
						}
					}
					parameters += ")";
				}
			}
			return parameters;
		}

		public String getEmptyParameterText() {
			if (isMethod) return "()";
			else return "";
		}
		
		ParamTag[] paramTags() {
			MemberDoc member = getFirstMember();
			if (member instanceof ExecutableMemberDoc)
				return ((ExecutableMemberDoc) member).paramTags();
			else return null;
		}

		String modifiers() {
			return getFirstMember().modifiers();
		}

		Parameter[] parameters() {
			MemberDoc member = getFirstMember();
			if (member instanceof ExecutableMemberDoc)
				return ((ExecutableMemberDoc) member).parameters();
			else return null;
		}

		ClassDoc[] thrownExceptions() {
			MemberDoc member = getFirstMember();
			if (member instanceof ExecutableMemberDoc)
				return ((ExecutableMemberDoc) member).thrownExceptions();
			else return null;
		}

		Tag[] tags(String string) {
			return getFirstMember().tags();
		}
	}
	
	/**
	 * A list of member group that are unified under the same name 
	 */
	static class MemberGroupList {
		ClassInfo classInfo;
		String name;
		Vector lists = new Vector();
		
		public MemberGroupList(ClassInfo classInfo, String name) {
			this.classInfo = classInfo;
			this.name = name;
		}

		static String[] methodNameFilter = {
			"setWrapper",
			"getWrapper",
			"iterator"
		};
		
		void add(MemberDoc member) {
			if (member instanceof MethodDoc) {
				String name = member.name();
				// filter out stuff:
				for (int i = 0; i < methodNameFilter.length; i++)
					if (methodNameFilter[i].equals(name))
						return;
			}
			
			boolean add = true;
			for (Iterator it = lists.iterator(); it.hasNext();) {
				MemberGroup group = (MemberGroup) it.next();
				if (group.add(member)) {
					add = false;
					break;
				}
			}
			// couldn't add to an existing MemberInfo, create a new one:
			if (add) {
//				System.out.println("Group " + member.qualifiedName());
				MemberGroup list = new MemberGroup(classInfo, name);
				list.add(member);
				lists.add(list);
			}
		}

		public void init() {
			for (Iterator it = lists.iterator(); it.hasNext();) {
				((MemberGroup) it.next()).init();
			}
		}

		public void addLists(Vector list) {
			for (Iterator it = lists.iterator(); it.hasNext();) {
				list.add(it.next());
			}
		}
	}
	
	/**
	 * A list of member-group lists, accessible by member name:
	 */
	static class MemberGroupLists {
		Hashtable groups = new Hashtable();
		Vector flatList = null;
		ClassInfo classInfo;
		
		MemberGroupLists(ClassInfo classInfo) {
			this.classInfo = classInfo;
		}
		void add(MemberDoc member) {
			String name = member.name();
			MemberGroupList group = (MemberGroupList) groups.get(name); 
			if (group == null)
				group = new MemberGroupList(classInfo, name); 
			group.add(member);
			groups.put(name, group);
		}
		
		void init() {
			for (Enumeration e = groups.elements(); e.hasMoreElements();) {
				((MemberGroupList) e.nextElement()).init();
			}
		}

		public void addAll(MemberDoc[] members) {
			for (int i = 0; i < members.length; i++) {
				add(members[i]);
			}
		}
		
		public MemberGroup[] getFlattened() {
			if (flatList == null) {
				// now sort the lists alphabetically
				Comparator comp = new Comparator() {
					public int compare(Object o1, Object o2) {
						MemberGroupList cls1 = (MemberGroupList) o1;
						MemberGroupList cls2 = (MemberGroupList) o2;
						return cls1.name.compareToIgnoreCase(cls2.name);
					}

					public boolean equals(Object obj) {
						return false;
					}
				};
				Vector sorted = new Vector(groups.values());
				Collections.sort(sorted, comp);
				// flatten the list of groups:
				flatList = new Vector();
				for (Iterator it = sorted.iterator(); it.hasNext();) {
					MemberGroupList group = (MemberGroupList) it.next();
					group.addLists(flatList);
				}
			}
			MemberGroup[] array = new MemberGroup[flatList.size()];
			flatList.toArray(array);
			return array;
		}
	}
	
	static class ClassInfo {
		ClassDoc classDoc;
		MemberGroupLists fields;
		MemberGroupLists methods;
		MemberGroupLists constructors;

		ClassInfo(ClassDoc cd) {
			this.classDoc = cd;
		}
	
		void add(ClassDoc cd, boolean addConstructors) {
			fields.addAll(cd.fields(true));
			methods.addAll(cd.methods(true));
			if (addConstructors)
				constructors.addAll(cd.constructors(true));
		}
		
		void init() {
			fields = new MemberGroupLists(this);
			methods = new MemberGroupLists(this);
			constructors = new MemberGroupLists(this);
			add(classDoc, true);
			ClassDoc superclass = classDoc.superclass();
			// add the members of direct invisible superclasses to this class for JS documentation:
			while (superclass != null && !JSDoclet.isVisibleClass(superclass) && !superclass.qualifiedName().equals("java.lang.Object")) {
				add(superclass, false);
				superclass = superclass.superclass();
			}
			fields.init();
			methods.init();
			constructors.init();
		}
		
		MemberGroup[] methods() {
			return methods.getFlattened();
		}
		
		MemberGroup[] fields() {
			return fields.getFlattened();
		}

		public MemberGroup[] constructors() {
			return constructors.getFlattened();
		}
	}
	
	static boolean isVisibleClass(ClassDoc cd) {
		return classInfos.get(cd.qualifiedName()) != null;
	}
	
	static boolean isVisibleMember(MemberDoc mem) {
		return memberGroups.get(mem.qualifiedName()) != null;
	}

	static ClassInfo getClassInfo(ClassDoc cd) {
		return (ClassInfo) classInfos.get(cd.qualifiedName());
	}

	static MemberGroup getMemberGroup(MemberDoc mem) {
		return (MemberGroup) memberGroups.get(mem.qualifiedName());
	}
	
	/**
	 * Called by the framework to format the entire document
	 * 
	 * @param root the root of the starting document
	 */
	public static boolean start(RootDoc root) {
		JSDoclet.root = root;
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(destDir + "packages.html"));

			writer.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
			writer.println("<html>");
			writer.println("<head>");
			if (doctitle != null)
				writer.println("<title>" + doctitle + "</title>");
			writer.println("<link rel=\"stylesheet\" href=\"style.css\" type=\"text/css\">");
			writer.println("</head>");
			writer.println("<html>");
			writer.println("<body>");
			if (doctitle != null)
				writer.println(section1Open + doctitle + section1Close);
			if (author != null)
				writer.println(author);

			// Setting up fields and methods for all visible classes:
			classInfos = new Hashtable();
			memberGroups = new Hashtable();
			ClassDoc[] classes = root.classes();
			for (int i = 0; i < classes.length; i++) {
				ClassDoc cd = classes[i];
				classInfos.put(cd.qualifiedName(), new ClassInfo(cd));
			}
			
			for (Enumeration elements = classInfos.elements(); elements.hasMoreElements();) {
				((ClassInfo) elements.nextElement()).init();
			}
			
			PackageDoc[] packages = root.specifiedPackages();
			for (int i = 0; i < packages.length; i++) {
				PackageDoc pkg = packages[i];
				writer.println(section1Open + "Package " + packageRelativIdentifier(basePackage, pkg.name()) + section1Close);
				writer.print(createAnchor(pkg.name()));

				processClasses(writer, "Interfaces", pkg.interfaces());
				processClasses(writer, "Classes", pkg.allClasses(true));
				processClasses(writer, "Exceptions", pkg.exceptions());
				processClasses(writer, "Errors", pkg.errors());

				// Package comments
				printTags(writer, null, pkg.inlineTags());
			}

			writer.println("</body>");
			writer.println("</html>");
			writer.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return true;
	}

	static class ClassNode {
		ClassDoc cd;
		LinkedHashMap nodes = new LinkedHashMap();
		
		ClassNode(ClassDoc cd) {
			this.cd = cd;
		}
		
		void add(ClassNode node) {
			nodes.put(node, node);
		}
		
		void remove(ClassNode node) {
			nodes.remove(node);
		}
		
		void printHierarchy(PrintWriter writer) {
			if (nodes.size() > 0) {
				writer.println("<ul>");
				for (Iterator it = nodes.keySet().iterator(); it.hasNext();) {
					ClassNode node = (ClassNode) it.next();
					writer.println("<li>" + createLink(node.cd) + "</li>");
					layoutClass(node.cd);
					node.printHierarchy(writer);
				}
				writer.println("</ul>");
			}
		}
	}
	
	/**
	 * Produces a table-of-contents for classes and calls layoutClass on each class.
	 */
	static void processClasses(PrintWriter writer, String title, ClassDoc[] classes) {
		// use LinkedHashMaps to keep alphabetical order
		Hashtable nodes = new Hashtable();
		ClassNode root = new ClassNode(null);
		for (int i = classes.length - 1;  i >= 0; i--) {
			ClassDoc cd = classes[i];
			ClassNode node = new ClassNode(cd);
			nodes.put(cd, node);
			root.add(node);
		}
		
		for (int i = classes.length - 1;  i >= 0; i--) {
			ClassDoc cd = classes[i];
			ClassNode node = (ClassNode) nodes.get(cd);
			ClassNode superclass = (ClassNode) nodes.get(cd.superclass());
			if (superclass != null) {
				root.remove(node);
				superclass.add(node);
			}
		}
		root.printHierarchy(writer);
	}

	/**
	 * Lays out a list of classes.
	 * 
	 * @param type Title of the section, e.g. "Interfaces", "Exceptions" etc.
	 * @param classes Vector of the classes to be laid out.
	 */
	static void layoutClass(ClassDoc cd) {
		try {
			// determine folder + filename for class file:
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
			writer.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
			writer.println("<html>");
			writer.println("<head>");
			writer.println("<title>" + name + "</title>");
			
			String base = "";
			for (int j = 0; j < levels; j++)
				base += "../";
			writer.println("<base href=\"" + base + "\">");
			writer.println("<link rel=\"stylesheet\" href=\"style.css\" type=\"text/css\">");
			writer.println("</head>");
			writer.println("<body>");
			writer.print(createAnchor(cd));

			writer.print(section1Open);

			String type = "Prototype";
			if (cd.isInterface())
				type = "Interface";
			else if (cd.isException())
				type = "Exception";
			
			if (cd.isAbstract())
				type = "Abstract " + type;

			writer.println(type + " " + cd.name() + section1Close);

			ClassDoc sc = cd.superclass();
			if (sc != null && isVisibleClass(sc)) {
				writer.println("<p><b> extends </b> " + createLink(sc) + "<p/>");
			}

			printTags(writer, cd, cd.inlineTags());

			printSeeTags(writer, cd, cd.seeTags(), section2Open + "See also" + section2Close, null);

			Tag[] verTags = cd.tags("version");
			if (versionInfo && verTags.length > 0) {
				writer.println(section2Open + "Version" + section2Close + verTags[0].text());
			}

			String subclasses = "";
			for (int index = 0; index < root.classes().length; index++) {
				ClassDoc cls = root.classes()[index];
				if (cls.superclass() == cd && !cls.equals(cd)) {
					if (!subclasses.equals(""))
						subclasses += ", ";
					subclasses += createLink(cls);
				}
			}
			if (!subclasses.equals("")) {
				writer.println(section2Open + "Direct known subprototypes" + section2Close + subclasses);
			}

			if (cd.isInterface()) {
				String subintf = "";
				String implclasses = "";
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
					writer.println(section2Open + "All classes known to implement interface" + section2Close
						+ implclasses);
			}

			ClassInfo info = getClassInfo(cd);
			MemberGroup[] fields = info.fields(); // cd.fields(true);
			MemberGroup[] constructors = info.constructors(); // cd.constructors(true);
			MemberGroup[] methods = info.methods(); // cd.methods(true);

			if (summaries) {
				if (fieldSummary && fields.length > 0)
					printSummary(writer, cd, fields, "Field summary");

				if (constructorSummary && constructors.length > 0)
					printSummary(writer, cd, constructors, "Constructor summary");

				if (methods.length > 0)
					printSummary(writer, cd, methods, "Method summary");
			}

			if (fields.length > 0)
				printFields(writer, cd, fields, "Fields");

			if (constructors.length > 0)
				printMembers(writer, cd, constructors, "Constructors");

			if (methods.length > 0)
				printMembers(writer, cd, methods, "Methods");

			if (inherited) {
				boolean yet = false;
				ClassDoc superclass = cd.superclass();

				while (superclass != null && !superclass.qualifiedName().equals("java.lang.Object")) {
					if (isVisibleClass(superclass)) {
						info = getClassInfo(superclass);
						fields = info.fields(); // par.fields(true);
						methods = info.methods(); // par.methods(true);
						MemberGroup[] inheritedMembers = new MemberGroup[fields.length + methods.length];
						for (int k = 0; k < fields.length; k++) {
							inheritedMembers[k] = fields[k];
						}
						for (int k = 0; k < methods.length; k++) {
							inheritedMembers[k + fields.length] = methods[k];
						}
						// print only if members available
						// (if class not found because classpath not
						// correctly set, they would be missed)
						if (inheritedMembers.length > 0) {
							if (!yet) {
								writer.print(section2Open + "Inherited Members" + section2Close);
							}
							yet = true;
							writer.print(createLink(superclass));
							if (!shortInherited)
								writer.print("<br/>");
							printInheritedMembers(writer, superclass, inheritedMembers, false);
							writer.print("<br/>");
						}
					}
					superclass = superclass.superclass();
				}
				if (shortInherited && yet)
					writer.println("<br/><br/>");

			}
			if (bottom != null)
				writer.println("<p>" + bottom + "</p>");
			writer.println("</body>");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Enumerates the fields passed and formats them using Html statements.
	 * 
	 * @param flds the fields to format
	 */
	static void printFields(PrintWriter writer, ClassDoc cd, MemberGroup[] flds, String title) {
		boolean yet = false;
		for (int i = 0; i < flds.length; ++i) {
			MemberGroup f = flds[i];
			if (!yet) {
				writer.println(section2Open + "" + title + "" + section2Close);
				writer.println("<ul>");
				yet = true;
			}
			writer.println("<li>");
			writer.print(createAnchor(f, cd));

			writer.print("<b>");
			// Static = PROTOTYPE.NAME
			if (f.isStatic())
				writer.print(f.containingClass().name() + ".");
			writer.print(f.name() + "</b>");

			Tag[] inlineTags = f.inlineTags();
			SeeTag[] seeTags = f.seeTags();
			if (inlineTags.length > 0 || seeTags.length > 0) {
				writer.println("<ul>");
				printTags(writer, cd, inlineTags, "<li>", "</li>", false);
				printSeeTags(writer, cd, seeTags, "<li>See also", "</li>");
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
	static void printSummary(PrintWriter writer, ClassDoc cd, MemberGroup[] dmems, String title) {
		if (dmems.length > 0) {
			writer.println(section2Open + "" + title + "" + section2Close);
			writer.println("<ul>");
			for (int i = 0; i < dmems.length; i++) {
				MemberGroup mem = dmems[i];
				writer.println("<li>");
				writer.print(createLink(mem, cd));
				printTags(writer, cd, mem.firstSentenceTags(), "<ul><li>", "</li></ul>", true);
				writer.println("</li>");
			}
			writer.println("</ul>");
		}
	}

	/**
	 * Enumerates the members of a section of the document and formats them
	 * using Tex statements.
	 */
	static void printMembers(PrintWriter writer, ClassDoc cd, MemberGroup[] dmems, String title) {
		if (dmems.length > 0) {
			writer.println(section2Open + "" + title + "" + section2Close);
			writer.println("<ul>");
			for (int i = 0; i < dmems.length; i++) {
				MemberGroup mem = dmems[i];
				printMember(writer, cd, mem);
				writer.println("<br/>");
			}
			writer.println("</ul>");
		}
	}

	/**
	 * Enumerates the members of a section of the document and formats them
	 * using Tex statements.
	 */
	static void printMember(PrintWriter writer, ClassDoc cd, MemberGroup mem) {
		printMember(writer, cd, mem, null);
	}

	/**
	 * Enumerates the members of a section of the document and formats them
	 * using Tex statements.
	 */
	static void printMember(PrintWriter writer, ClassDoc cd, MemberGroup mem, MemberGroup copiedTo) {
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
				ClassInfo info = getClassInfo(doc);
				if (info != null) {
					MemberGroup[] methods = info.methods();
					for (int i = 0; !found && i < methods.length; ++i) {
						if (methods[i].name().equals(mem.name())
							&& methods[i].signature().equals(mem.signature())) {
							printMember(writer, cd, methods[i], copiedTo == null ? mem : copiedTo);
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
			writer.print(createAnchor(mem, cd));
		} else {
			writer.print(createAnchor(copiedTo, cd));
		}

		writer.print("<b>" + mem.name + "</b>" + mem.getParameterText());

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
			printTags(writer, cd, mem.inlineTags());
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

				writer.print("<tt> " + params[j].parameterName() + "</tt>" + " - ");
				printTags(writer, cd, params[j].inlineTags());
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
				writer.println("<li><b>Returns</b> ");
				for (int j = 0; j < ret.length; ++j) {
					printTags(writer, cd, ret[j].inlineTags());
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
				writer.println("<li><b>Throws</b>");
				writer.println("  <ul>");

				for (int j = 0; j < excp.length; ++j) {
					String ename = excp[j].exceptionName();
					ClassDoc cdoc = excp[j].exception();
					if (cdoc != null)
						ename = cdoc.qualifiedName();
					writer.print("   <li> " + ename + " - ");
					printTags(writer, cd, excp[j].inlineTags());

				}
				writer.println("  </ul>");

			}
		}

		// See tags
		SeeTag[] seeTags = mem.seeTags();
		if (seeTags.length > 0) {
			if (!yet) {
				writer.println("<ul>");
				yet = true;
			}
			printSeeTags(writer, cd, seeTags, "<li><b>See also</b>", "</li>");
		}
		if (yet)
			writer.println("</ul>");
		else
			writer.println("<br/>");
	}

	/**
	 * Enumerates the members of a section of the document and formats them
	 * using Tex statements.
	 * 
	 * @param mems the members of this entity
	 * @see #start
	 */
	static void printInheritedMembers(PrintWriter writer, ClassDoc cd, MemberGroup[] dmems, boolean labels) {
		if (dmems.length == 0)
			return;

		if (shortInherited) {
			for (int i = 0; i < dmems.length; i++) {
				MemberGroup mem = dmems[i];
				// print only member names
				if (i != 0)
					writer.print(", ");
				writer.print(mem.name());
			}
		} else {

			writer.println("<ul>");
			for (int i = 0; i < dmems.length; i++) {
				MemberGroup mem = dmems[i];
				writer.println("<li>" + createLink(mem, cd) + "</li>");
			}
			writer.println("</ul>");
		}
	}

	/**
	 * Prints a sequence of tags obtained from e.g. com.sun.javadoc.Doc.tags().
	 */
	static void printTags(PrintWriter writer, ClassDoc cd, Tag[] tags, String prefix, String sufix, boolean filterFirstSentence) {
		if (tags.length > 0) {
			if (prefix != null)
				writer.println(prefix);
			boolean more = true;
			for (int i = 0; i < tags.length && more; i++) {
				if (tags[i] instanceof SeeTag) {
					writer.print(createLink(((SeeTag) tags[i]).referencedMember(), cd));
				} else {
					String text = tags[i].text();
					if (filterFirstSentence) {
						// cut away ":" and everything that follows:
						int pos = text.indexOf(':');
						if (pos >= 0) {
							text = text.substring(0, pos) + ".";
							more = false;
						}
					}
					writer.print(text);
				}
			}
			if (sufix != null)
				writer.println(sufix);
		}
	}

	static void printTags(PrintWriter writer, ClassDoc cd, Tag[] tags) {
		printTags(writer, cd, tags, null, null, false);
	}

	static void printSeeTags(PrintWriter writer, ClassDoc cd, SeeTag[] seeTags, String prefix, String sufix) {
		if (seeTags.length > 0) {
			if (prefix != null)
				writer.println(prefix);
			writer.println("<ul>");
			boolean first = true;
			for (int i = 0; i < seeTags.length; ++i) {
				MemberDoc mem = seeTags[i].referencedMember();
				if (mem != null) {
					if (first) first = false;
					else writer.print(", ");
					writer.print(createLink(mem, cd));
				}
			}
			writer.println("</ul>");
			if (sufix != null)
				writer.println(sufix);
		}
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
		else if (option.equals("-date"))
			return 2;
		else if (option.equals("-author"))
			return 2;
		else if (option.equals("-noinherited"))
			return 1;
		else if (option.equals("-nosummaries"))
			return 1;
		else if (option.equals("-noindex"))
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
			} else if (option.equals("-doctitle")) {
				doctitle = arg[1];
			} else if (option.equals("-bottom")) {
				bottom = arg[1];
			} else if (option.equals("-author")) {
				author = arg[1];
			} else if (option.equals("-noinherited")) {
				inherited = false;
			} else if (option.equals("-nosummaries")) {
				summaries = false;
			} else if (option.equals("-hyperref")) {
				hyperref = true;
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
			String str = "<a href=\"";
			if (qualifiedName != null) {
				String path = qualifiedName.substring(0, qualifiedName.length() - name.length());
				path = packageRelativIdentifier(basePackage, path).replace('.', '/');
				str += path + name + ".html";
			}
			return str + "#" + anchor + "\" target=\"classFrame\">" + title + "</a>";
		} else
			return title;
	}
	
	static String createLink(ClassDoc cl) {
		String str = "";
		if (cl.isAbstract())
			str += "<i>";
		str += createLink(cl.qualifiedName(), cl.name(), cl.qualifiedName(), cl.name());
		if (cl.isAbstract())
			str += "</i>";
		return str;
	}

	static ClassDoc getMemberClass(MemberGroup group, ClassDoc currentClass) {
		// in case the class is invisible, the current class needs to be used instead
		ClassDoc containingClass = group.containingClass();
		if (isVisibleClass(containingClass) || currentClass.superclass() != containingClass)
			return containingClass;
		else
			return currentClass;
	}
	
	static String createLink(MemberDoc mem, ClassDoc currentClass) {
		MemberGroup group = getMemberGroup(mem);
		if (group != null)
			return createLink(group, currentClass);
		else
			return "";
	}

	static String createLink(MemberGroup mem, ClassDoc currentClass) {
		ClassDoc cd = getMemberClass(mem, currentClass);
		// dont use mem.qualifiedName(). use cd.qualifiedName() + "." + mem.name()
		// instead in order to catch the case where functions are moved from invisible
		// classes to visible ones (e.g. AffineTransform -> Matrix)
		return createLink(cd.qualifiedName(), cd.name(), cd.qualifiedName() + "." + mem.name() + mem.signature(), mem.name() + mem.getEmptyParameterText());
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

	static String createAnchor(MemberGroup mem, ClassDoc currentClass) {
		ClassDoc cd = getMemberClass(mem, currentClass);
		return createAnchor(cd.qualifiedName() + "." + mem.name() + mem.signature());
	}
}