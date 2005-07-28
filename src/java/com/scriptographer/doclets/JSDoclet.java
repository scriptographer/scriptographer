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
	private static boolean debug = true;
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
	private static Hashtable memberInfos;

	static class MemberInfo {
		ClassInfo classInfo;
		MemberDoc member = null;

		void init() {
		}
		
		MemberInfo(ClassInfo classInfo) {
			this.classInfo = classInfo;
		}
		
		MemberInfo(ClassInfo classInfo, MemberDoc member) {
			this.classInfo = classInfo;
			this.member = member;
		}

		String name() {
			return member.name();
		}

		Tag[] firstSentenceTags() {
			return member.firstSentenceTags();
		}

		boolean isStatic() {
			return member.isStatic();
		}

		ClassDoc containingClass() {
			return member.containingClass();
		}

		Tag[] inlineTags() {
			return member.inlineTags();
		}

		SeeTag[] seeTags() {
			return member.seeTags();
		}

		PackageDoc containingPackage() {
			return member.containingPackage();
		}

		String signature() {
			return "";
		}

		String getParameterText() {
			return "";
		}

		public String getEmptyParameterText() {
			return "";
		}
		
		ParamTag[] paramTags() {
			return null;
		}

		String modifiers() {
			return member.modifiers();
		}

		Parameter[] parameters() {
			return null;
		}

		ClassDoc[] thrownExceptions() {
			return null;
		}

		Tag[] tags(String tagname) {
			return member.tags(tagname);
		}

		public Type returnType() {
			return null;
		}
	}
	
	/**
	 * A group of members that are all "compatible" in a JS way, e.g. have the same
	 * amount of parameter with different types each (e.g. setters)
	 * or various amount of parameters with default parameter versions, e.g.
	 * all com.scriptogrpaher.ai.Pathfinder functions
	 */
	static class MethodInfo extends MemberInfo {
		Vector members = new Vector();
		Hashtable map = new Hashtable();
		String name;
		
		boolean isGrouped = false;
		String parameters = null;
		
		MethodInfo(ClassInfo classInfo, String name) {
			super(classInfo);
			this.name = name;
		}
				
		boolean isSuperclass(ClassDoc cd, String superclass) {
			while (cd != null) {
				if (cd.qualifiedName().equals(superclass))
					return true;
				cd = cd.superclass();
			}
			return false;
		}

		boolean isArray(Parameter param) {
			String typeName = param.typeName();
			return typeName.indexOf('[') != -1 && typeName.indexOf(']') != -1 || isSuperclass(param.type().asClassDoc(), "java.util.Collection");
		}
		
		boolean isPoint(Parameter param) {
			ClassDoc cd = param.type().asClassDoc();
			return isSuperclass(cd, "java.awt.geom.Point2D") || isSuperclass(cd, "java.awt.Dimension");
		}
		
		boolean isNumber(Parameter param) {
			String type = param.typeName();
			return isSuperclass(param.type().asClassDoc(), "java.lang.Number") ||
				type.equals("int") || type.equals("double") || type.equals("float");
		}
		
		boolean isMap(Parameter param) {
			ClassDoc cd = param.type().asClassDoc();
			return isSuperclass(cd, "java.util.Map") || isSuperclass(cd, "org.mozilla.javascript.NativeArray");
		}
		
		boolean isCompatible(Parameter param1 ,Parameter param2) {
			String typeName1 = param1.typeName();
			String typeName2 = param2.typeName();
			ClassDoc cd1 = param1.type().asClassDoc();
			ClassDoc cd2 = param2.type().asClassDoc();
			return typeName1.equals(typeName2) ||
				(cd1 != null && cd2 != null && (cd1.subclassOf(cd2) || cd2.subclassOf(cd1))) ||
				(isNumber(param1) && isNumber(param2)) ||
				(isArray(param1) && isArray(param2)) ||
				(isPoint(param1) && isPoint(param2));
		}

		boolean isCompatible(MemberDoc member1, MemberDoc member2) {
			if (debug) System.out.println(member1 + " " + member2);
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

				// rule 3: if not the same amount of params, the types need to be the same:
				int count = Math.min(params1.length, params2.length);
				for (int i = 0; i < count; i++) {
					if (!isCompatible(params1[i], params2[i])) {
						if (debug) System.out.println("R 3");
						return false;
					}
				}
				isGrouped = true;
				return true;
			} else { // fields cannot be grouped
				return false;
			}
		}

		boolean add(MemberDoc member) {
			boolean swallow = true;
			// do not add base versions for overridden functions 
			String signature = ((ExecutableMemberDoc) member).signature();
			if (map.get(signature) != null)
				swallow = false;
			map.put(signature, member);
			if (swallow) {
				// see wther the new member fits the existing ones:
				for (Iterator it = members.iterator(); it.hasNext();) {
					if (!isCompatible((MemberDoc) it.next(), member))
						return false;
				}
				members.add(member);
			}
			// even add it to the global table when it's not added here, so hrefs to overriden functions work
			memberInfos.put(member.qualifiedName(), this);
			return true;
		}
		
		void init() {
			if (isGrouped) {
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
				member = (MemberDoc) members.lastElement();
			} /*else if (mode == MODE_PARAMCOUNT) {
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
			}*/ else {
				member = (MemberDoc) members.firstElement();
			}
		}

		String name() {
			return name;
		}

		Tag[] firstSentenceTags() {
			return member.firstSentenceTags();
		}

		boolean isStatic() {
			return member.isStatic();
		}

		ClassDoc containingClass() {
			return classInfo.classDoc;
		}

		Tag[] inlineTags() {
			return member.inlineTags();
		}

		SeeTag[] seeTags() {
			return member.seeTags();
		}

		PackageDoc containingPackage() {
			return classInfo.classDoc.containingPackage();
		}

		String signature() {
			return ((ExecutableMemberDoc) member).signature();
		}

		String getParameterText() {
			if (parameters == null) {
				parameters = "(";
				if (isGrouped) {
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
					ExecutableMemberDoc mem = (ExecutableMemberDoc) member;
					Parameter[] params = mem.parameters();
					for (int i = 0; i < params.length; i++) {
						if (i > 0)
							parameters += ", ";
						parameters += params[i].name();
					}
				}
				parameters += ")";
			}
			return parameters;
		}

		public String getEmptyParameterText() {
			return "()";
		}
		
		ParamTag[] paramTags() {
			return ((ExecutableMemberDoc) member).paramTags();
		}

		String modifiers() {
			return member.modifiers();
		}

		Parameter[] parameters() {
			return ((ExecutableMemberDoc) member).parameters();
		}

		ClassDoc[] thrownExceptions() {
			return ((ExecutableMemberDoc) member).thrownExceptions();
		}

		Tag[] tags(String string) {
			return member.tags();
		}

		public Type returnType() {
			return ((MethodDoc) member).returnType();
		}
	}
	
	/**
	 * A virtual field that unifies getter and setter functions, just like Rhino does
	 */
	static class BeanProperty extends MemberInfo {
		String name;
		MethodInfo getter;
		MethodInfo setter;
		Tag[] textTags;
		SeeTag[] seeTags;

		BeanProperty(ClassInfo classInfo, String name, MethodInfo getter, MethodInfo setter) {
			super(classInfo);
			this.name = name;
			this.getter = getter;
			this.setter = setter;
			String str = "A ";
			if (setter == null)
				str += "read-only ";
			str += "BeanProperty, defined by <tt>" + this.getter.name + "</tt>";
			if (setter != null)
				str += " and " + setter.name;
			textTags = new Tag[] {
				new BeanTag(str)
			};
			if (setter != null) {
				seeTags = new SeeTag[] {
					new BeanSeeTag(getter),
					new BeanSeeTag(setter)
				};
			} else {
				seeTags = new SeeTag[] {
					new BeanSeeTag(getter)
				};
			}
		}
		
		class BeanTag implements Tag {
			String text;
			
			BeanTag(String text) {
				this.text = text;
			}
			
			public String name() {
				return null;
			}

			public Doc holder() {
				return null;
			}

			public String kind() {
				return null;
			}

			public String text() {
				return text;
			}

			public Tag[] inlineTags() {
				return null;
			}

			public Tag[] firstSentenceTags() {
				return null;
			}

			public SourcePosition position() {
				return null;
			}
			
		}

		class BeanSeeTag extends BeanTag implements SeeTag {
			MemberInfo member;
			
			BeanSeeTag(MemberInfo member) {
				super("");
				this.member = member;
			}

			public String label() {
				return null;
			}

			public PackageDoc referencedPackage() {
				return member.containingPackage();
			}

			public String referencedClassName() {
				return member.containingClass().name();
			}

			public ClassDoc referencedClass() {
				return member.containingClass();
			}

			public String referencedMemberName() {
				return member.name();
			}

			public MemberDoc referencedMember() {
				return member.member;
			}
		}
		
		String name() {
			return name;
		}

		Tag[] firstSentenceTags() {
			return textTags;
		}

		boolean isStatic() {
			return getter.isStatic();
		}

		ClassDoc containingClass() {
			return getter.containingClass();
		}

		Tag[] inlineTags() {
			return textTags;
		}

		SeeTag[] seeTags() {
			return seeTags;
		}

		PackageDoc containingPackage() {
			return getter.containingPackage();
		}

		String modifiers() {
			return "";
		}

		Tag[] tags(String tagname) {
			return new Tag[]{};
		}
	}
	
	/**
	 * A list of members or groups of members that are unified under the same name 
	 */
	static class MemberList {
		ClassInfo classInfo;
		String name;
		Vector lists = new Vector();
		
		MemberList(ClassInfo classInfo, String name) {
			this.classInfo = classInfo;
			this.name = name;
		}

		MemberList(ClassInfo classInfo, FieldDoc member) {
			this.classInfo = classInfo;
			this.name = member.name();
			lists.add(new MemberInfo(classInfo, member));
		}

		MemberList(ClassInfo classInfo, MemberInfo member) {
			this.classInfo = classInfo;
			this.name = member.name();
			lists.add(member);
		}

		static String[] methodNameFilter = {
			"setWrapper",
			"getWrapper",
			"iterator",
			"hashCode"
		};
		
		void add(ExecutableMemberDoc member) {
			if (member instanceof MethodDoc) {
				String name = member.name();
				// filter out stuff:
				for (int i = 0; i < methodNameFilter.length; i++)
					if (methodNameFilter[i].equals(name))
						return;
			}
			
			boolean add = true;
			for (Iterator it = lists.iterator(); it.hasNext();) {
				MethodInfo group = (MethodInfo) it.next();
				if (group.add(member)) {
					add = false;
					break;
				}
			}
			// couldn't add to an existing MemberInfo, create a new one:
			if (add) {
				MethodInfo list = new MethodInfo(classInfo, member.name());
				list.add(member);
				lists.add(list);
			}
		}

		public void init() {
			for (Iterator it = lists.iterator(); it.hasNext();) {
				((MemberInfo) it.next()).init();
			}
		}

		public void addLists(Vector list) {
			for (Iterator it = lists.iterator(); it.hasNext();) {
				list.add(it.next());
			}
		}

		/**
		 * @return
		 */
		public MethodInfo extractGetMethod() {
		    // Inspect the list of all MemberBox for the only one having no
	        // parameters
			for (Iterator it = lists.iterator(); it.hasNext();) {
				MethodInfo method = (MethodInfo) it.next();
	            // Does getter method have an empty parameter list with a return
	            // value (eg. a getSomething() or isSomething())?
	            if (method.parameters().length == 0/* && (!isStatic || method.isStatic())*/ ) {
	                if (!method.returnType().equals("void")) {
	                    return method;
	                }
	                break;
	            }
	        }
			return null;
		}

		public MethodInfo extractSetMethod(Type type) {
			//
			// Note: it may be preferable to allow
			// NativeJavaMethod.findFunction()
			//       to find the appropriate setter; unfortunately, it requires an
			//       instance of the target arg to determine that.
			//

			// Make two passes: one to find a method with direct type
			// assignment,
			// and one to find a widening conversion.
			for (int pass = 1; pass <= 2; ++pass) {
				for (Iterator it = lists.iterator(); it.hasNext();) {
					MethodInfo method = (MethodInfo) it.next();
					//	                if (!isStatic || method.isStatic()) {
					if (method.returnType().typeName().equals("void")) {
						Parameter[] params = method.parameters();
						if (params.length == 1) {
							if (pass == 1) {
								if (params[0].typeName().equals(type.typeName())) {
									return method;
								}
							} else {
								 // TODO: if (params[0].isAssignableFrom(type)) {
								 // return method; }
								return method;
							}
						}
					}
				}
			}
			return null;
		}
	}
	
	/**
	 * A list of member lists, accessible by member name:
	 */
	static class MemberLists {
		Hashtable groups = new Hashtable();
		Hashtable lookup = null;
		Vector flatList = null;
		ClassInfo classInfo;
		
		MemberLists(ClassInfo classInfo) {
			this.classInfo = classInfo;
		}
		
		void add(ExecutableMemberDoc member) {
			String name = member.name();
			String key = name;
			if (member instanceof MethodDoc) {
				// for members, use the return type for grouping as well!
				key = ((MethodDoc) member).returnType().typeName() + " " + name;
			}
			MemberList group = (MemberList) groups.get(key); 
			if (group == null) {
				group = new MemberList(classInfo, name); 
				groups.put(key, group);
				if (lookup == null)
					lookup = new Hashtable();
				lookup.put(name, group);
			}

			group.add(member);
		}
		
		void add(FieldDoc member) {
			// fields won't be grouped, but for simplicty, greate groups of one element for each field,
			// so it can be treated the same as functions:
			String name = member.name();
			MemberList group = new MemberList(classInfo, member); 
			groups.put(name, group);
		}
		
		void add(MemberInfo member) {
			groups.put(member.name(), new MemberList(classInfo, member));
		}

		public void addAll(ExecutableMemberDoc[] members) {
			for (int i = 0; i < members.length; i++)
				add(members[i]);
		}

		public void addAll(FieldDoc[] members) {
			for (int i = 0; i < members.length; i++)
				add(members[i]);
		}

		void init() {
			for (Enumeration e = groups.elements(); e.hasMoreElements();) {
				((MemberList) e.nextElement()).init();
			}
		}

		public MemberInfo[] getFlattened() {
			if (flatList == null) {
				// now sort the lists alphabetically
				Comparator comp = new Comparator() {
					public int compare(Object o1, Object o2) {
						MemberList cls1 = (MemberList) o1;
						MemberList cls2 = (MemberList) o2;
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
					MemberList group = (MemberList) it.next();
					group.addLists(flatList);
				}
			}
			MemberInfo[] array = new MemberInfo[flatList.size()];
			flatList.toArray(array);
			return array;
		}

		/**
		 * @param fields
		 */
		public void scanBeanProperties(MemberLists fields) {
			for (Enumeration e = groups.elements(); e.hasMoreElements();) {
				MemberList member = (MemberList) e.nextElement();
				String name = member.name;
	            // Is this a getter?
	            boolean memberIsGetMethod = name.startsWith("get");
	            boolean memberIsIsMethod = name.startsWith("is");
	            if (memberIsGetMethod || memberIsIsMethod) {
	                // Double check name component.
	                String nameComponent = name.substring(memberIsGetMethod ? 3 : 2);
	                if (nameComponent.length() == 0)
	                    continue;
	
	                // Make the bean property name.
	                String beanPropertyName = nameComponent;
	                char ch0 = nameComponent.charAt(0);
	                if (Character.isUpperCase(ch0)) {
	                    if (nameComponent.length() == 1) {
	                        beanPropertyName = nameComponent.toLowerCase();
	                    } else {
	                        char ch1 = nameComponent.charAt(1);
	                        if (!Character.isUpperCase(ch1)) {
	                            beanPropertyName = Character.toLowerCase(ch0) + nameComponent.substring(1);
	                        }
	                    }
	                }
	
	                // If we already have a member by this name, don't do this
	                // property.
	                if (fields.contains(beanPropertyName))
	                    continue;
	
	                MethodInfo getter = member.extractGetMethod();
	                MethodInfo setter = null;
	                if (getter != null) {
	                    // We have a getter. Now, do we have a setter?
	                    MemberList setters = (MemberList) lookup.get("set" + nameComponent);
	                    if (setters != null) {
	                        // Is this value a method?
                            setter = setters.extractSetMethod(getter.returnType());
	                    }
	                }
                    // Make the property.
                    fields.add(new BeanProperty(classInfo, beanPropertyName, getter, setter));
	            }
            }
		}

		boolean contains(String name) {
			return groups.containsKey(name);
		}
	}
	
	static class ClassInfo {
		ClassDoc classDoc;
		MemberLists fields;
		MemberLists methods;
		MemberLists constructors;

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
			fields = new MemberLists(this);
			methods = new MemberLists(this);
			constructors = new MemberLists(this);
			add(classDoc, true);
			ClassDoc superclass = classDoc.superclass();
			// add the members of direct invisible superclasses to this class for JS documentation:
			while (superclass != null && !JSDoclet.isVisibleClass(superclass) && !superclass.qualifiedName().equals("java.lang.Object")) {
				add(superclass, false);
				superclass = superclass.superclass();
			}
			// now scan for beanProperties:
			methods.init();
			constructors.init();
			
			methods.scanBeanProperties(fields);
		}
		
		MemberInfo[] methods() {
			return methods.getFlattened();
		}
		
		MemberInfo[] fields() {
			return fields.getFlattened();
		}

		public MemberInfo[] constructors() {
			return constructors.getFlattened();
		}
	}
	
	static boolean isVisibleClass(ClassDoc cd) {
		return classInfos.get(cd.qualifiedName()) != null;
	}
	
	static boolean isVisibleMember(MemberDoc mem) {
		return memberInfos.get(mem.qualifiedName()) != null;
	}

	static ClassInfo getClassInfo(ClassDoc cd) {
		return (ClassInfo) classInfos.get(cd.qualifiedName());
	}

	static MemberInfo getMemberGroup(MemberDoc mem) {
		return (MethodInfo) memberInfos.get(mem.qualifiedName());
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
			memberInfos = new Hashtable();
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

			printSeeTags(writer, cd, cd.seeTags(), section2Open + "See also:" + section2Close, null);

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
			MemberInfo[] fields = info.fields(); // cd.fields(true);
			MemberInfo[] constructors = info.constructors(); // cd.constructors(true);
			MemberInfo[] methods = info.methods(); // cd.methods(true);

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
						MemberInfo[] inheritedMembers = new MemberInfo[fields.length + methods.length];
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
	static void printFields(PrintWriter writer, ClassDoc cd, MemberInfo[] flds, String title) {
		boolean yet = false;
		for (int i = 0; i < flds.length; ++i) {
			MemberInfo f = flds[i];
			if (!yet) {
				writer.println(section2Open + "" + title + "" + section2Close);
				writer.println("<ul>");
				yet = true;
			}
			writer.println("<li>");
			writer.print(createAnchor(f, cd));

			writer.print("<tt><b>");
			// Static = PROTOTYPE.NAME
			if (f.isStatic())
				writer.print(f.containingClass().name() + ".");
			writer.print(f.name() + "</b></tt>");

			Tag[] inlineTags = f.inlineTags();
			SeeTag[] seeTags = f.seeTags();
			if (inlineTags.length > 0 || seeTags.length > 0) {
				writer.println("<ul>");
				printTags(writer, cd, inlineTags, "<li>", "</li>", false);
				printSeeTags(writer, cd, seeTags, "<li>See also:", "</li>");
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
	static void printSummary(PrintWriter writer, ClassDoc cd, MemberInfo[] dmems, String title) {
		if (dmems.length > 0) {
			writer.println(section2Open + "" + title + "" + section2Close);
			writer.println("<ul>");
			String prevName = null;
			for (int i = 0; i < dmems.length; i++) {
				MemberInfo mem = dmems[i];
				String name = mem.name();
				if (!name.equals(prevName)) {
					writer.println("<li>");
					writer.print(createLink(mem, cd));
					printTags(writer, cd, mem.firstSentenceTags(), "<ul><li>", "</li></ul>", true);
					writer.println("</li>");
				}
				prevName = name;
			}
			writer.println("</ul>");
		}
	}

	/**
	 * Enumerates the members of a section of the document and formats them
	 * using Tex statements.
	 */
	static void printMembers(PrintWriter writer, ClassDoc cd, MemberInfo[] dmems, String title) {
		if (dmems.length > 0) {
			writer.println(section2Open + "" + title + "" + section2Close);
			writer.println("<ul>");
			for (int i = 0; i < dmems.length; i++) {
				MemberInfo mem = dmems[i];
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
	static void printMember(PrintWriter writer, ClassDoc cd, MemberInfo mem) {
		printMember(writer, cd, mem, null);
	}

	/**
	 * Enumerates the members of a section of the document and formats them
	 * using Tex statements.
	 */
	static void printMember(PrintWriter writer, ClassDoc cd, MemberInfo mem, MemberInfo copiedTo) {
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
					MemberInfo[] methods = info.methods();
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

		writer.print("<tt><b>" + mem.name() + "</b>" + mem.getParameterText() + "</tt>");

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
			printSeeTags(writer, cd, seeTags, "<li><b>See also:</b>", "</li>");
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
	static void printInheritedMembers(PrintWriter writer, ClassDoc cd, MemberInfo[] dmems, boolean labels) {
		if (dmems.length == 0)
			return;

		if (shortInherited) {
			for (int i = 0; i < dmems.length; i++) {
				MemberInfo mem = dmems[i];
				// print only member names
				if (i != 0)
					writer.print(", ");
				writer.print(mem.name());
			}
		} else {

			writer.println("<ul>");
			for (int i = 0; i < dmems.length; i++) {
				MemberInfo mem = dmems[i];
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
			// writer.println("<ul>");
			boolean first = true;
			for (int i = 0; i < seeTags.length; ++i) {
				MemberDoc mem = seeTags[i].referencedMember();
				if (mem != null) {
					if (first) first = false;
					else writer.print(", ");
					writer.print(createLink(mem, cd));
				}
			}
			// writer.println("</ul>");
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

	static ClassDoc getMemberClass(MemberInfo group, ClassDoc currentClass) {
		// in case the class is invisible, the current class needs to be used instead
		ClassDoc containingClass = group.containingClass();
		if (isVisibleClass(containingClass) || currentClass.superclass() != containingClass)
			return containingClass;
		else
			return currentClass;
	}
	
	static String createLink(MemberDoc mem, ClassDoc currentClass) {
		MemberInfo group = getMemberGroup(mem);
		if (group != null)
			return createLink(group, currentClass);
		else
			return "";
	}

	static String createLink(MemberInfo mem, ClassDoc currentClass) {
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

	static String createAnchor(MemberInfo mem, ClassDoc currentClass) {
		ClassDoc cd = getMemberClass(mem, currentClass);
		return createAnchor(cd.qualifiedName() + "." + mem.name() + mem.signature());
	}
}