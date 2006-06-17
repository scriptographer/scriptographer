package com.scriptographer.doclets;

import com.sun.javadoc.*;

import java.io.*;
import java.util.*;

/**
 * This class provides a Java 2,<code>javadoc</code> Doclet which generates
 * the HTML documents for the Scriptographer JS Help files and the
 * templates for the interactive version on the website.
 *
 * According to the rules with which Scriptogrpaher turns Java classes into JS Prototypes
 * This doclet performs similar conversions on the javadoc structure....
 */
public class JSDoclet extends Doclet {
	static boolean debug = false;
	static boolean inherited = true;
	static String basePackage = "";
	static String doctitle;
	static String bottom;
	static String destDir;
	static String author;
	static boolean hyperref = true;
	static boolean versionInfo = false;
	static boolean summaries = false;
	static boolean templates = false;
	static RootDoc root;

	static boolean fieldSummary = true;
	static boolean constructorSummary = true;
	static String section1Open = "<h1>";
	static String section2Open = "<h2>";
	static String section1Close = "</h1>";
	static String section2Close = "</h2>";
	static boolean shortInherited = false;
	static Hashtable classInfos;
	static Hashtable memberInfos;
	static String[] filterClasses = null;
	static String[] packageSequence = null;
	static String[] methodFilter = null;
	static HashMap classOrder = null;
	static String base;

	static boolean isSuperclass(ClassDoc cd, String superclass) {
		while (cd != null) {
			if (cd.qualifiedName().equals(superclass))
				return true;
			cd = cd.superclass();
		}
		return false;
	}

	static boolean hasInterface(ClassDoc cd, String face) {
		if (cd != null) {
			if (cd.qualifiedName().equals(face))
				return true;
			ClassDoc[] faces = cd.interfaces();
			for (int i = 0; i < faces.length; i++) {
				// if an interface extends another one, its superclass is not set here, but the
				// super interface is simply in the interfaces() list. strange...
				if (hasInterface(faces[i], face))
					return true;
			}
			cd = cd.superclass();
			if (cd != null)
				return hasInterface(cd, face);
		}
		return false;
	}

	static boolean isNumber(Type type) {
		String typeName = type.toString();
		return isSuperclass(type.asClassDoc(), "java.lang.Number") ||
			typeName.equals("int") ||
			typeName.equals("double") ||
			typeName.equals("float");
	}
	
	static boolean isBoolean(Type type) {
		String typeName = type.toString();
		return isSuperclass(type.asClassDoc(), "java.lang.Boolean") ||
			typeName.equals("boolean");
	}

	static boolean isArray(Type type) {
		String typeName = type.toString();
		ClassDoc cd = type.asClassDoc();
		return typeName.indexOf('[') != -1 && typeName.indexOf(']') != -1 ||
			hasInterface(cd, "java.util.Collection") ||
			isSuperclass(cd, "org.mozilla.javascript.NativeArray");
	}
	
	static boolean isMap(Type type) {
		ClassDoc cd = type.asClassDoc();
		return hasInterface(cd, "java.util.Map") ||
			isSuperclass(cd, "org.mozilla.javascript.NativeObject");
	}
	
	static boolean isPoint(Type type) {
		ClassDoc cd = type.asClassDoc();
		return isSuperclass(cd, "java.awt.geom.Point2D") ||
			isSuperclass(cd, "java.awt.Dimension");
	}
	
	static boolean isRectangle(Type type) {
		return isSuperclass(type.asClassDoc(), "java.awt.geom.Rectangle2D");
	}
	
	static boolean isMatrix(Type type) {
		return isSuperclass(type.asClassDoc(), "java.awt.geom.AffineTransform");
	}
	
	static boolean isCompatible(Type type1 ,Type type2) {
		String typeName1 = type1.toString();
		String typeName2 = type2.toString();
		ClassDoc cd1 = type1.asClassDoc();
		ClassDoc cd2 = type2.asClassDoc();
		return typeName1.equals(typeName2) ||
			(cd1 != null && cd2 != null && (cd1.subclassOf(cd2) || cd2.subclassOf(cd1))) ||
			(isNumber(type1) && isNumber(type2)) ||
			(isArray(type1) && isArray(type2)) ||
			(isMap(type1) && isMap(type2)) ||
			(isPoint(type1) && isPoint(type2)) ||
			(isRectangle(type1) && isRectangle(type2)) ||
			(isMatrix(type1) && isMatrix(type2));
	}

	static boolean isCompatible(MemberDoc member1, MemberDoc member2) {
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
				if (!isCompatible(new ParamType(params1[i]), new ParamType(params2[i]))) {
					if (debug) System.out.println("R 3");
					return false;
				}
			}
			return true;
		} else { // fields cannot be grouped
			return false;
		}
	}

	/**
	 * ParamType fixes a bug in the Type returned by Parameter.type(), where toString does not return []
	 * for arrays and there does not seem to be another way to ¶find out if it's an array or not...
	 */
	static class ParamType implements Type {
		Type type;
		String str;
		
		ParamType(Parameter param) {
			this.type = param.type();
			this.str = param.typeName();
		}

		public String typeName() {
			return type.typeName();
		}

		public String qualifiedTypeName() {
			return type.qualifiedTypeName();
		}

		public String dimension() {
			return type.dimension();
		}

		public ClassDoc asClassDoc() {
			return type.asClassDoc();
		}
		
		public String toString() {
			return str;
		}
	}
	
	static class JSTag implements Tag {
		String text;
		
		JSTag(String text) {
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

	static class JSSeeTag extends JSTag implements SeeTag {
		MemberInfo member;
		
		JSSeeTag(MemberInfo member) {
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

	static class MemberInfo {
		ClassInfo classInfo;
		MemberDoc member = null;

		MemberInfo(ClassInfo classInfo) {
			this.classInfo = classInfo;
		}
		
		MemberInfo(ClassInfo classInfo, MemberDoc member) {
			this.classInfo = classInfo;
			this.member = member;
		}

		void init() {
		}

		public void printReturnType(PrintWriter writer) {
			Type retType = this.returnType();
			if (retType != null && !retType.typeName().equals("void")) {
				writer.println("<div class=\"member-paragraph\"><b>Returns:</b> ");
				writer.print("<div>");
				writer.print(JSDoclet.createLink(retType));
				Tag[] ret = this.tags("return");
				boolean first = true;
				if (ret.length > 0) {
					for (int j = 0; j < ret.length; ++j) {
						Tag[] inlineTags = ret[j].inlineTags();
						if (inlineTags.length > 0) {
							if (first) {
								writer.print(" - ");
								first = false;
							}
							printTags(writer, classInfo.classDoc, inlineTags);
						}
					}
				}
				writer.println("</div>");
				writer.println("</div>");
			}
		}

		void printMember(PrintWriter writer, PrintWriter indexWriter, ClassDoc cd, String id, String title, String text, Tag[] tags) {
			if (templates) {
				writer.print("<% this.memberLink id=\"" + id + "\" %>");
				indexWriter.print(", \"" + id + "\": { title: \"" + name() + "\", text: \"" + encodeJs(getTags(cd, tags)) + "\" }");
			} else {
				writer.print("<div id=\"" + id + "-link\" class=\"member-link\">");
			}
			writer.print(createAnchor(id, true));
			writer.print(title);
			writer.print("</a>");
			writer.println("</div>");

			if (templates) {
				writer.print("<% this.memberDescription id=\"" + id + "\" %>");
			} else {
				writer.print("<div id=\"" + id + "-description\" class=\"member-description\">");
			}
			writer.println("<div class=\"member-header\">");
			writer.println("<div class=\"member-title\"><a href=\"#\" onClick=\"return toggleMember('" + id + "', false);\"" + title + "</a></div>");
			writer.println("<div class=\"member-close\"><input type=\"button\" value=\"Close\" onClick=\"toggleMember('" + id + "', false);\"></div>");
			writer.println("<div class=\"clear\"></div>");
			writer.println("</div>");

			if (text != null && text.length() > 0) {
				writer.println("<div class=\"member-text\">");
				writer.println(text);
				writer.println("</div>");
			}

			writer.println("</div>");

			if (templates) {
				writer.print("<% this.memberPosts id=\"" + id + "\" %>");
			}
		}

		void printMember(PrintWriter writer, PrintWriter indexWriter, ClassDoc cd) {
			StringBuffer title = new StringBuffer("<tt><b>");
			// Static = PROTOTYPE.NAME
			if (isStatic())
				title.append(cd.name()).append(".");
			title.append(name());
			title.append("</b></tt>");

			StringWriter text = new StringWriter();
			PrintWriter strWriter = new PrintWriter(text);

			// Description
			printTags(strWriter, cd, inlineTags(), "<div class=\"member-paragraph\">", "</div>");
			// Return tag
			this.printReturnType(strWriter);
			// See tags
			printSeeTags(strWriter, cd, seeTags(), "<div class=\"member-paragraph\"><b>See also:</b>", "</div>");

			printMember(writer, indexWriter, cd, getId(), title.toString(), text.toString(), inlineTags());
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

		String getNameSuffix() {
			return "";
		}

		Parameter[] parameters() {
			return null;
		}

		public Type returnType() {
			return member instanceof FieldDoc ? ((FieldDoc) member).type() : null;
		}

		Tag[] tags(String tagname) {
			return member.tags(tagname);
		}

		public void printSummary(PrintWriter writer, ClassDoc cd) {
			writer.println("<li class=\"summary\">");
			writer.print(createLink(cd));
//			printTags(writer, cd, firstSentenceTags(), "<ul><li>", "</li></ul>", true);
			writer.println("</li>");
		}

		String getId() {
			return name() + signature();
		}

		ClassDoc getClass(ClassDoc currentClass) {
			// in case the class is invisible, the current class needs to be used instead
			ClassDoc containingClass = containingClass();
			if (isVisibleClass(containingClass) || currentClass.superclass() != containingClass)
				return containingClass;
			else
				return currentClass;
		}

		String createLink(ClassDoc currentClass) {
			ClassDoc cd = getClass(currentClass);
			// dont use mem.qualifiedName(). use cd.qualifiedName() + "." + mem.name()
			// instead in order to catch the case where functions are moved from invisible
			// classes to visible ones (e.g. AffineTransform -> Matrix)
			return JSDoclet.createLink(cd.qualifiedName(), cd.name(), getId(), name() + getNameSuffix());
		}

		public boolean isSimilar(MemberInfo info) {
			return isStatic() == info.isStatic() && name().equals(info.name());
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
		String parameters;
		
		boolean isGrouped = false;
		
		MethodInfo(ClassInfo classInfo, String name) {
			super(classInfo);
			this.name = name;
		}

		/**
		 * used only in printMember for overriding tags
		 * @param doc
		 */
		MethodInfo(ClassInfo info, MethodDoc doc) {
			this(info, doc.name());
			add(doc);
			member = doc;
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
				isGrouped = true;
				members.add(member);
			}
			return true;
		}
		
		void init() {
			if (isGrouped) {
				// see if all elements have the same amount of parameters
				boolean sameParamCount = true;
				int firstCount = -1;
				for (Iterator it = members.iterator(); it.hasNext();) {
					ExecutableMemberDoc mem = ((ExecutableMemberDoc) it.next());
					int count = mem.parameters().length;
					if (firstCount == -1)
						firstCount = count;
					else if (count != firstCount) {
						sameParamCount = false;
						break;
					}
				}
				if (sameParamCount) {
					// find the suiting member: take the one with the most documentation
					int maxTags = -1;
					for (Iterator it = members.iterator(); it.hasNext();) {
						MemberDoc mem = (MemberDoc) it.next();
						int numTags = mem.inlineTags().length;
						if (numTags > maxTags) {
							member = mem;
							maxTags = numTags;
						}
					}
				} else {
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
				}
			} else {
				member = (MemberDoc) members.firstElement();
			}
		}

		String getNameSuffix() {
			return getParameters();
		}
		
		MethodInfo getOverriddenMethodToUse() {
			if (member instanceof MethodDoc) {
				MethodDoc method = (MethodDoc) member;
				if (method.commentText().equals("") &&
					method.seeTags().length == 0 &&
					method.throwsTags().length == 0 &&
					method.paramTags().length == 0) {
					// No javadoc available for this method. Recurse through
					// superclasses
					// and implemented interfaces to find javadoc of overridden
					// methods.
					MethodDoc overridden = method.overriddenMethod();
					if (overridden != null) {
						MethodInfo info = (MethodInfo) getMemberInfo(overridden);
						// prevent endless loops:
						// if this method is not wrapped, quickly wrap it just to call printMember
						// prevent endless loops that happen when overriden functions from inivisble classes
						// where moved to the derived class and getMethodInfo lookup points there instead of
						// the overridden version:
						if (info != null && info.member.containingClass() != method.overriddenClass())
							info = null;
						if (info == null)
							info = new MethodInfo(classInfo, overridden);
						return info;
					}
				}
			}
			return null;
		}

		public void printSummary(PrintWriter writer, ClassDoc cd) {
			MethodInfo overridden = getOverriddenMethodToUse();
			if (overridden != null)
				overridden.printSummary(writer, cd);
			else
				super.printSummary(writer, cd);
		}

		void printMember(PrintWriter writer, PrintWriter indexWriter, ClassDoc cd) {
			printMember(writer, indexWriter, cd, null);
		}

		void printMember(PrintWriter writer, PrintWriter indexWriter, ClassDoc cd, MemberInfo copiedTo) {
			MethodInfo overridden = getOverriddenMethodToUse();
			if (overridden != null) {
				overridden.printMember(writer, indexWriter, cd, copiedTo == null ? this : copiedTo);
			} else {
				ExecutableMemberDoc executable = (ExecutableMemberDoc) member;
				MemberInfo mbr = copiedTo == null ? this : copiedTo;

				StringBuffer title = new StringBuffer("<tt><b>");
				// Static = PROTOTYPE.NAME
				if (isStatic())
					title.append(cd.name()).append(".");

				title.append(name()).append("</b>").append(getParameters()).append("</tt>");

				// Thrown exceptions
				/*
				ClassDoc[] thrownExceptions = executable.thrownExceptions();
				if (thrownExceptions != null && thrownExceptions.length > 0) {
					writer.print(" throws <tt>");
					for (int e = 0; e < thrownExceptions.length; e++) {
						if (e > 0)
							writer.print(", ");
						writer.print(thrownExceptions[e].qualifiedName());
					}
					writer.print("</tt>");
				}
				writer.println();
				*/

				StringWriter text = new StringWriter();
				PrintWriter strWriter = new PrintWriter(text);

				// Description
				Tag[] descriptionTags = mbr.inlineTags();
				printTags(strWriter, classInfo.classDoc, descriptionTags, "<div class=\"member-paragraph\">", "</div>");

				// Parameter tags
				printParameterTags(strWriter, cd);
	
				// Return tag
				mbr.printReturnType(strWriter);

				// Throws or Exceptions tag
				ThrowsTag[] excp = executable.throwsTags();
				if (excp.length > 0) {
					strWriter.println("<div class=\"member-paragraph\"><b>Throws:</b>");
					for (int j = 0; j < excp.length; ++j) {
						String exception = excp[j].exceptionName();
						ClassDoc cdoc = excp[j].exception();
						if (cdoc != null)
							exception = createLink(cdoc); // cdoc.qualifiedName();
						strWriter.print("<div>" + exception + " - ");
						printTags(strWriter, cd, excp[j].inlineTags());
						strWriter.print("</div>");
					}
					strWriter.println("</div>");
				}

				// See tags
				printSeeTags(strWriter, cd, mbr.seeTags(), "<div class=\"member-paragraph\"><b>See also:</b>", "</div>");

				printMember(writer, indexWriter, cd, mbr.getId(), title.toString(), text.toString(), descriptionTags);
			}
		}
		
		String getParameters() {
			if (parameters == null) {
				StringBuffer buf = new StringBuffer();
				buf.append("(");
				if (isGrouped) {
					int prevCount = 0;
					int closeCount = 0;
					for (Iterator it = members.iterator(); it.hasNext();) {
						ExecutableMemberDoc mem = ((ExecutableMemberDoc) it.next());
						Parameter[] params = mem.parameters();
						int count = params.length;
						if (count > prevCount) {
							if (prevCount > 0)
								buf.append("[");

							for (int i = prevCount; i < count; i++) {
								if (i > 0)
									buf.append(", ");
								buf.append(params[i].name());
							}
							closeCount++;
							prevCount = count;
						}
					}
					for (int i = 1; i < closeCount; i++)
						buf.append("]");
				} else {
					ExecutableMemberDoc mem = (ExecutableMemberDoc) member;
					Parameter[] params = mem.parameters();
					for (int i = 0; i < params.length; i++) {
						if (i > 0)
							buf.append(", ");
						buf.append(params[i].name());
					}
				}
				buf.append(")");
				parameters = buf.toString();
			}
			return parameters;
		}
		
		boolean printParameterTags(PrintWriter writer, ClassDoc cd) {
			Parameter[] params = ((ExecutableMemberDoc) member).parameters();
			if (params.length > 0) {
				ParamTag[] origTags = ((ExecutableMemberDoc) member).paramTags();
				Hashtable lookup = new Hashtable();
				for (int i = 0; i < origTags.length; i++) {
					lookup.put(origTags[i].parameterName(), origTags[i]);
				}
				writer.println("<div class=\"member-paragraph\"><b>Parameters:</b>");
				for (int i = 0; i < params.length; i++) {
					Parameter param = params[i];
					String name = param.name();
					ParamTag origTag = (ParamTag) lookup.get(name);
					writer.print("<div><tt>" + name + ":</tt>" + JSDoclet.createLink(new ParamType(param)));
					if (origTag != null) {
						Tag[] inlineTags = origTag.inlineTags();
						printTags(writer, cd, inlineTags, " - ", null);
					}
					writer.println("</div>");
				}
				writer.println("</div>");
				return true;
			}
			return false;
		}
		
		String name() {
			return name;
		}

		boolean isStatic() {
			return member.isStatic();
		}

		ClassDoc containingClass() {
			return classInfo.classDoc;
		}

		PackageDoc containingPackage() {
			return classInfo.classDoc.containingPackage();
		}

		Tag[] firstSentenceTags() {
			return member.firstSentenceTags();
		}

		Tag[] inlineTags() {
			return member.inlineTags();
		}

		SeeTag[] seeTags() {
			return member.seeTags();
		}

		Tag[] tags(String tag) {
			return member.tags(tag);
		}

		String signature() {
			return ((ExecutableMemberDoc) member).signature();
		}

		Parameter[] parameters() {
			return ((ExecutableMemberDoc) member).parameters();
		}

		public Type returnType() {
			return member instanceof MethodDoc ? ((MethodDoc) member).returnType() : null;
		}

		public boolean isSimilar(MemberInfo obj) {
			if (obj instanceof MethodInfo) {
				MethodInfo info = (MethodInfo) obj;
				return isStatic() == info.isStatic() &&
					name().equals(info.name()) &&
					getParameters().equals(info.getParameters());
			} else {
				return false;
			}
		}
	}

	/**
	 * A virtual field that unifies getter and setter functions, just like Rhino does
	 */
	static class BeanProperty extends MemberInfo {
		String name;
		MethodInfo getter;
		MethodInfo setter;
		Tag[] inlineTags;
		SeeTag[] seeTags;

		BeanProperty(ClassInfo classInfo, String name, MethodInfo getter, MethodInfo setter) {
			super(classInfo);
			this.name = name;
			this.getter = getter;
			this.setter = setter;
			String str = "";
			if (setter == null)
				str += "Read-only ";
			if (templates)
				str += "<% this.beanProperty %>";
			else
				str += "Bean Property";
			str += ", defined by " + getter.createLink(classInfo.classDoc);
			if (setter != null)
				str += " and " + setter.createLink(classInfo.classDoc);
			Tag[] tags = getter.inlineTags();
			inlineTags = new Tag[tags.length + 1];
			for (int i = 0; i < tags.length; i++)
				inlineTags[i] = tags[i];
			if (tags.length > 0)
				str = "<br />" + str;
			inlineTags[tags.length] = new JSTag(str);
			seeTags = new SeeTag[] {
			};
			/*
			if (setter != null) {
				seeTags = new SeeTag[] {
					new JSSeeTag(getter),
					new JSSeeTag(setter)
				};
			} else {
				seeTags = new SeeTag[] {
					new JSSeeTag(getter)
				};
			}
			*/
		}
		
		String name() {
			return name;
		}

		Tag[] firstSentenceTags() {
			return inlineTags;
		}

		boolean isStatic() {
			return getter.isStatic();
		}

		ClassDoc containingClass() {
			return getter.containingClass();
		}

		Tag[] inlineTags() {
			return inlineTags;
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

		public Type returnType() {
			return getter.returnType();
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
			add(new MemberInfo(classInfo, member), member.qualifiedName());
		}

		MemberList(ClassInfo classInfo, BeanProperty member) {
			this.classInfo = classInfo;
			this.name = member.name();
			add(member, null);
		}

		void add(MemberInfo member, String lookupName) {
			lists.add(member);
			if (lookupName != null)
				memberInfos.put(lookupName, member);
		}
		
		void add(ExecutableMemberDoc member) {
			String name = member.name();
			if (methodFilter != null && member instanceof MethodDoc) {
				// filter out stuff:
				for (int i = 0; i < methodFilter.length; i++)
					if (methodFilter[i].equals(name))
						return;
			}
			
			MethodInfo method = null;
			for (Iterator it = lists.iterator(); it.hasNext();) {
				MethodInfo info = (MethodInfo) it.next();
				if (info.add(member)) {
					method = info;
					break;
				}
			}
			// couldn't add to an existing MemberInfo, create a new one:
			if (method == null) {
				method = new MethodInfo(classInfo, name);
				if (method.add(member))
					add(method, null);
			}
			memberInfos.put(member.qualifiedName(), method);
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

		public MethodInfo extractGetMethod() {
		    // Inspect the list of all MemberBox for the only one having no
	        // parameters
			for (Iterator it = lists.iterator(); it.hasNext();) {
				MethodInfo method = (MethodInfo) it.next();
	            // Does getter method have an empty parameter list with a return
	            // value (eg. a getSomething() or isSomething())?

				// as a convention, only add non static bean properties to the documentation.
				// static properties are all supposed to be uppercae and constants
				if (method.parameters().length == 0 && !method.isStatic()) {
	                if (!method.returnType().typeName().equals("void")) {
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
					// as a convention, only add non static bean properties to the documentation.
					// static properties are all supposed to be uppercae and constants
					if (!method.isStatic()) {
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

		void add(BeanProperty member) {
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
					String adjustCase(String name) {
						// swap the first char in case so the sorting shows lowercase members first
						char ch = name.charAt(0);
						if (Character.isLowerCase(ch))
							ch = Character.toUpperCase(ch);
						else
							ch = Character.toLowerCase(ch);
						return ch + name.substring(1);
					}
					
					public int compare(Object o1, Object o2) {
						MemberList cls1 = (MemberList) o1;
						MemberList cls2 = (MemberList) o2;
						return adjustCase(cls1.name).compareTo(adjustCase(cls2.name));
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
	                    // Make the property.
	                    fields.add(new BeanProperty(classInfo, beanPropertyName, getter, setter));
	                }
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

			if (!classDoc.name().equals("global"))
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

		String name() {
			String name = classDoc.name();
			if (name.equals("global")) return "Global Scope";
			else return name;
		}

		public boolean hasSimilar(MemberInfo member, boolean method) {
			MemberInfo[] members = method ? methods() : fields();
			for (int i = 0; i < members.length; i++) {
				if (member.isSimilar(members[i]))
					return true;
			}
			return false;
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

	static MemberInfo getMemberInfo(MemberDoc mem) {
		return (MemberInfo) memberInfos.get(mem.qualifiedName());
	}
	
	/**
	 * Called by the framework to format the entire document
	 * 
	 * @param root the root of the starting document
	 */
	public static boolean start(RootDoc root) {
		JSDoclet.root = root;
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(destDir + (templates ? "packages.js" : "packages/packages.html")));
			if (!templates) {
				writer.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
				writer.println("<html>");
				writer.println("<head>");
				if (doctitle != null)
					writer.println("<title>" + doctitle + "</title>");
				writer.println("<base target=\"classFrame\">");
				writer.println("<link rel=\"stylesheet\" href=\"../resources/style.css\" type=\"text/css\">");
				writer.println("<script src=\"../resources/scripts.js\" type=\"text/javascript\"></script>");
				writer.println("</head>");
				writer.println("<html>");
				writer.println("<body class=\"documentation\">");
				writer.println("<div class=\"documentation-packages\">");
				if (doctitle != null)
					writer.println(section1Open + doctitle + section1Close);
				if (author != null)
					writer.println(author);
				writer.println("<ul class=\"documentation-list\">");
			} else {
//				writer.println("var basePackage = \"" + basePackage + "\";");
			}

			// Setting up fields and methods for all visible classes:
			classInfos = new Hashtable();
			memberInfos = new Hashtable();
			ClassDoc[] classes = root.classes();
			for (int i = 0; i < classes.length; i++) {
				ClassDoc cd = classes[i];
				boolean add = true;
				String name = cd.qualifiedName();
				if (filterClasses != null) {
					for (int j = 0; j < filterClasses.length; j++) {
						String filter = filterClasses[j];
						if (filter.equals(name) || (filter.endsWith("*") && name.startsWith(filter.substring(0, filter.length() - 1)))) {
							add = false;
							break;
						}
					}
				}
				if (add) {
					classInfos.put(name, new ClassInfo(cd));
				}
			}
			
			for (Enumeration elements = classInfos.elements(); elements.hasMoreElements();) {
				((ClassInfo) elements.nextElement()).init();
			}
			
			Hashtable packages = new Hashtable();
			PackageDoc[] pkgs = root.specifiedPackages();
			boolean createSequence = false;
			if (packageSequence == null) {
				packageSequence = new String[pkgs.length];
				createSequence = true;
			}
			for (int i = 0; i < pkgs.length; i++) {
				PackageDoc pkg = pkgs[i];
				String name = pkg.name();
				packages.put(name, pkg);
				if (createSequence)
					packageSequence[i] = name;
			}
			for (int i = 0; i < packageSequence.length; i++) {
				PackageDoc pkg = (PackageDoc) packages.get(packageSequence[i]);
				if (pkg != null) {
					String name = pkg.name();
					base = "";
					String rel = getRelativeIdentifier(name);
					if (!templates) {
						String pkgName = rel.toUpperCase();
						writer.println("<li><a href=\"#\" onClick=\"return togglePackage('" + pkgName + "', false);\"><img name=\"arrow-" +  pkgName + "\" src=\"../resources/arrow-close.gif\" width=\"8\" height=\"8\" border=\"0\"></a><img src=\"../resources/spacer.gif\" width=\"6\" height=\"1\"><b>" + stripCodeTags(createLink(name, rel, null, pkgName)) + "</b>");
						writer.println("<ul id=\"package-" + pkgName + "\" class=\"hidden\">");
					} else {
						String pkgName = rel.toUpperCase();
						writer.print("createPackage(\"" + pkgName + "\", ");
					}

					processClasses(writer, pkg.interfaces());
					processClasses(writer, pkg.allClasses(true));
					processClasses(writer, pkg.exceptions());
					processClasses(writer, pkg.errors());

					String text = getTags(null, pkg.inlineTags());

					String first = getTags(null, pkg.firstSentenceTags());
					// remove the first sentence from the main text
					if (first.length() > 0 && text.startsWith(first)) {
						text = text.substring(first.length());
						first = first.substring(0, first.length() - 1); // cut away dot
					}

					if (templates) {
						writer.print("\"");
						writer.print(encodeJs(text.trim()));
						writer.println("\");");
					} else {
						writer.println("</li></ul>");
						// write package file:
						try {
							PrintWriter pkgWriter = beginDocument(rel, "index");
							pkgWriter.println(section1Open + first + section1Close);
							pkgWriter.println(text);
							endDocument(pkgWriter);
						} catch(IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
			if (!templates) {
				writer.println("</ul>");
				writer.println("</div>");
				writer.println("</body>");
				writer.println("</html>");
			}
			writer.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return true;
	}

	static class ClassNode {
		ClassInfo classInfo;
		TreeMap nodes;

		ClassNode(ClassInfo classInfo) {
			this.classInfo = classInfo;
			Comparator comp = new Comparator() {
				public int compare(Object o1, Object o2) {
					Integer obj1 = (Integer) classOrder.get(((ClassNode) o1).name());
					Integer obj2 = (Integer) classOrder.get(((ClassNode) o2).name());
					int order1 = obj1 == null ? Integer.MAX_VALUE : obj1.intValue();
					int order2 = obj2 == null ? Integer.MAX_VALUE : obj2.intValue();
					if (order1 < order2) return -1;
					else if (order1 > order2) return 1;
					else return 0;
				}
			};
			this.nodes = new TreeMap(comp);
		}

		void add(ClassNode node) {
			nodes.put(node, node);
		}

		void remove(ClassNode node) {
			nodes.remove(node);
		}

		String name() {
			return classInfo.name();
		}

		void printHierarchy(PrintWriter writer, String prepend) {
			if (nodes.size() > 0) {
				writer.println(prepend + (templates ? "[" : "<ul>"));
				for (Iterator it = nodes.keySet().iterator(); it.hasNext();) {
					ClassNode node = (ClassNode) it.next();
					ClassDoc cd = node.classInfo.classDoc;
					if (templates)
						writer.println(prepend + "\t{ name: \"" + node.name() + "\", isAbstract: " + cd.isAbstract() + ", index: { ");
					layoutClass(writer, node.classInfo);
					if (templates) {
						writer.println(" }},");
					} else {
						base = "";
						writer.println(prepend + "\t<li>" + stripCodeTags(createLink(cd, node.name())) + "</li>");
					}
					node.printHierarchy(writer, prepend + "\t");
				}
				writer.println(prepend + (templates ? "]," : "</ul>"));
			}
		}
	}
	
	/**
	 * Produces a table-of-contents for classes and calls layoutClass on each class.
	 */
	static void processClasses(PrintWriter writer, ClassDoc[] classes) {
		// use LinkedHashMaps to keep alphabetical order
		Hashtable nodes = new Hashtable();
		ClassNode root = new ClassNode(null);

		// loop twice, as in the second loop, superclasses are picked from nodes which is filled in the firs loop
		for (int i = classes.length - 1;  i >= 0; i--) {
			ClassDoc cd = classes[i];
			ClassInfo info = getClassInfo(cd);
			if (info != null) {
				ClassNode node = new ClassNode(info);
				nodes.put(cd, node);
				root.add(node);
			}
		}
		
		for (int i = classes.length - 1;  i >= 0; i--) {
			ClassDoc cd = classes[i];
			ClassNode node = (ClassNode) nodes.get(cd);
			if (node != null) {
				ClassNode superclass = (ClassNode) nodes.get(cd.superclass());
				if (superclass != null) {
					root.remove(node);
					superclass.add(node);
				}
			}
		}
		root.printHierarchy(writer, "");
	}

	static PrintWriter beginDocument(String path, String name) throws IOException {
		// now split into packages and create subdirs:
		if (!templates)
			path = "packages." + path;
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
		PrintWriter writer = new PrintWriter(new FileWriter(destDir + path + name + (templates ? ".jstl" : ".html")));

		if (!templates) {
			base = "";
			for (int j = 1; j < levels; j++)
				base += "../";

			writer.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
			writer.println("<html>");
			writer.println("<head>");
			writer.println("<title>" + name + "</title>");
			writer.println("<base target=\"classFrame\">");
			writer.println("<link rel=\"stylesheet\" href=\"../" + base + "resources/style.css\" type=\"text/css\">");
			writer.println("<script src=\"../" + base + "resources/scripts.js\" type=\"text/javascript\"></script>");
			writer.println("</head>");
			writer.println("<body class=\"documentation\">");
		}

		return writer;
	}

	static void endDocument(PrintWriter writer) {
		if (!templates) {
			if (bottom != null)
				writer.println("<p class=\"footer\">" + bottom + "</p>");
			writer.println("</body>");
			writer.println("</html>");
		}
		writer.close();
	}

	static String getListType(ClassDoc cd) {
		if (hasInterface(cd, "com.scriptographer.util.SimpleList")) return "Normal List";
		else if (hasInterface(cd, "com.scriptographer.util.StringIndexList")) return "String-index List";
		else if (hasInterface(cd, "com.scriptographer.util.ReadOnlyList")) return "Read-only List";
		else return null;
	}

	/**
	 * Lays out a list of classes.
	 */
	static void layoutClass(PrintWriter indexWriter, ClassInfo info) {
		try {
			ClassDoc cd = info.classDoc;
			// determine folder + filename for class file:
			String name = info.name();
			String className = cd.name();
			// name and className might differ, e.g. for global -> Global Scope!
			String path = cd.qualifiedName();
			// cut away name:
			path = path.substring(0, path.length() - className.length());
			path = getRelativeIdentifier(path);
			PrintWriter writer = beginDocument(path, className);

			String subClasses = null;
			for (int index = 0; index < root.classes().length; index++) {
				ClassDoc cls = root.classes()[index];
				if (isVisibleClass(cls) && cls.superclass() == cd && !cls.equals(cd)) {
					if (subClasses == null)
						subClasses = "";
					else
						subClasses += ", ";
					subClasses += createLink(cls);
				}
			}

			String type = "Prototype";
			if (cd.isInterface())
				type = "Interface";
			else if (cd.isException())
				type = "Exception";

			if (cd.isAbstract())
				type = "Abstract " + type;

			String superType = null;
			ClassDoc sc = cd.superclass();
			if (sc != null && isVisibleClass(sc)) {
				superType = createLink(sc);
			}

			if (!templates)
				writer.println(section1Open + name + section1Close);

			String listType = getListType(cd);

			if (cd.isAbstract() || superType != null || listType != null || subClasses != null) {
				writer.print("<p>" + type + " " + createLink(cd));
				if (superType != null)
					writer.print(" extends " + superType);
				if (listType != null) {
					if (templates)
						listType = "<% this.listType type=\"" + listType + "\" %>";
					if (superType != null)
						writer.print(",");
					writer.print(" acts as " + listType);
				}
				if (subClasses != null)
					writer.print("<br />Inherited by " + subClasses);
				writer.println("</p>");
			}

			printTags(writer, cd, cd.inlineTags(), "<p>", "</p>");
			if (templates)
				indexWriter.print("\"prototype\": { title: \"" + cd.name() + "\", text: \"" + encodeJs(getTags(cd, cd.inlineTags())) + "\" }");

			printSeeTags(writer, cd, cd.seeTags(), "<p><b>See also:</b> ", "</p>");

			Tag[] verTags = cd.tags("version");
			if (versionInfo && verTags.length > 0) {
				writer.println(section2Open + "Version" + section2Close + verTags[0].text());
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

			if (constructors.length > 0)
				printMembers(writer, indexWriter, cd, constructors, "Constructors", false);

			if (fields.length > 0) {
				printMembers(writer, indexWriter, cd, fields, "Properties", false);
				printMembers(writer, indexWriter, cd, fields, "Static Properties", true);
			}

			if (methods.length > 0) {
				printMembers(writer, indexWriter, cd, methods, "Functions", false);
				printMembers(writer, indexWriter, cd, methods, "Static Functions", true);
			}

			if (inherited) {
				boolean yet = false;
				ClassDoc superclass = cd.superclass();

				while (superclass != null && !superclass.qualifiedName().equals("java.lang.Object")) {
					if (isVisibleClass(superclass)) {
						ClassInfo superInfo = getClassInfo(superclass);
						fields = superInfo.fields();
						methods = superInfo.methods();
						MemberInfo[] inheritedMembers = new MemberInfo[fields.length + methods.length];
						// first non-static, then static:
						int i = 0;
						for (int j = 0; j < fields.length; j++) {
							if (!fields[j].isStatic() && !info.hasSimilar(fields[j], false))
								inheritedMembers[i++] = fields[j];
						}
						for (int j = 0; j < fields.length; j++) {
							if (fields[j].isStatic() && !info.hasSimilar(fields[j], false))
								inheritedMembers[i++] = fields[j];
						}
						for (int j = 0; j < methods.length; j++) {
							if (!methods[j].isStatic() && !info.hasSimilar(methods[j], true))
								inheritedMembers[i++] = methods[j];
						}
						for (int j = 0; j < methods.length; j++) {
							if (methods[j].isStatic() && !info.hasSimilar(methods[j], true))
								inheritedMembers[i++] = methods[j];
						}
						// print only if members available
						// (if class not found because classpath not
						// correctly set, they would be missed)
						if (inheritedMembers.length > 0) {
							if (!yet)
								writer.print(section2Open + "Inheritance" + section2Close);
							else
								writer.println("<br/>");
							yet = true;
							writer.println("<ul class=\"documentation-inherited\">");
							writer.println("<li>");
							writer.print(createLink(superclass));
							writer.println("</li>");
							writer.println("<li>");
							printInheritedMembers(writer, superclass, inheritedMembers);
							writer.println("</li>");
							writer.println("</ul>");
						}
					}
					superclass = superclass.superclass();
				}
				if (shortInherited && yet)
					writer.println("<br/><br/>");
			}
			endDocument(writer);
		} catch (IOException e) {
			e.printStackTrace();
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
				if (!name.equals(prevName))
					mem.printSummary(writer, cd);
				prevName = name;
			}
			writer.println("</ul>");
		}
	}

	/**
	 * Enumerates the members of a section of the document and formats them
	 * using Tex statements.
	 */
	static void printMembers(PrintWriter writer, PrintWriter indexWriter, ClassDoc cd, MemberInfo[] members, String title, boolean showStatic) {
		if (members.length > 0) {
			StringWriter strBuffer = new StringWriter();
			PrintWriter strWriter = new PrintWriter(strBuffer);
			for (int i = 0; i < members.length; i++) {
				MemberInfo mbr = members[i];
				if (mbr.isStatic() == showStatic)
					mbr.printMember(strWriter, indexWriter, cd);
			}
			String str = strBuffer.toString();
			if (str.length() > 0) {
				writer.println(section2Open + "" + title + "" + section2Close);
				writer.println("<div class=\"documentation-paragraph\">");
				writer.print(str);
				writer.println("</div>");
			}
		}
	}

	/**
	 * Enumerates the members of a section of the document and formats them
	 * using Tex statements.
	 */
	static void printInheritedMembers(PrintWriter writer, ClassDoc cd, MemberInfo[] dmems) {
		if (dmems.length > 0 && dmems[0] != null) {
			writer.println("<ul>");
			for (int i = 0; i < dmems.length; i++) {
				if (dmems[i] != null) {
					writer.println("<li>");
					writer.print(dmems[i].createLink(cd));
					writer.println("</li>");
				}
			}
			writer.println("</ul>");
		}
	}

	/**
	 * Prints a sequence of tags obtained from e.g. com.sun.javadoc.Doc.tags().
	 */
	static void printTags(PrintWriter writer, ClassDoc cd, Tag[] tags, String prefix, String suffix) {
		if (tags.length > 0) {
			if (prefix != null)
				writer.print(prefix);
			boolean more = true;
			for (int i = 0; i < tags.length && more; i++) {
				if (tags[i] instanceof SeeTag) {
					MemberDoc mem = ((SeeTag) tags[i]).referencedMember();
					if (mem != null)
						writer.print(createLink(mem, cd));
				} else {
					String text = tags[i].text();
					/*
					if (filterFirstSentence) {
						// cut away ":" and everything that follows:
						int pos = text.indexOf(':');
						if (pos >= 0) {
							text = text.substring(0, pos) + ".";
							more = false;
						}
					}
					*/
					writer.print(text);
				}
			}
			if (suffix != null)
				writer.println(suffix);
		}
	}

	static void printTags(PrintWriter writer, ClassDoc cd, Tag[] tags) {
		printTags(writer, cd, tags, null, null);
	}

	static String getTags(ClassDoc cd, Tag[] tags, String prefix, String suffix) {
		StringWriter str = new StringWriter();
		PrintWriter writer = new PrintWriter(str);
		printTags(writer, cd, tags, prefix, suffix);
		return str.toString();
	}

	static String getTags(ClassDoc cd, Tag[] tags) {
		return getTags(cd, tags, null, null);
	}

	static void printSeeTags(PrintWriter writer, ClassDoc cd, SeeTag[] seeTags, String prefix, String suffix) {
		if (seeTags.length > 0) {
			if (prefix != null)
				writer.println(prefix);
			boolean first = true;
			for (int i = 0; i < seeTags.length; ++i) {
				MemberDoc mem = seeTags[i].referencedMember();
				if (mem != null) {
					if (first) first = false;
					else writer.print(", ");
					writer.print(createLink(mem, cd));
				}
			}
			if (suffix != null)
				writer.println(suffix);
		}
	}

	static String getRelativeIdentifier(String str) {
		String rel;
		if (str.startsWith(basePackage + ".")) {
			rel = str.substring(basePackage.length() + 1);
		} else {
			rel = str;
		}
		return rel;
	}
	
	static String createLink(String qualifiedName, String name, String anchor, String title) {
		String str = "<tt>";
		if (hyperref) {
			if (anchor != null && anchor.length() == 0)
				anchor = null;
			str += "<a href=\"";
			if (qualifiedName != null) {
				String path = getRelativeIdentifier(qualifiedName).replace('.', '/');
				// link to the index file for packages
				if (Character.isLowerCase(name.charAt(0)) && !name.equals("global"))
					path += "/index";
				if (templates)
					path = "/Documentation/" + path + "/";
				else
					path = base + path + ".html";
				str += path;
			}
			if (anchor != null) {
				if (templates) str += anchor + "/";
				str += "#" + anchor;
				str += "\" onClick=\"return toggleMember('" + anchor + "', true);";
			}
			str += "\">" + title + "</a>";
		} else {
			str += title;
		}
		str += "</tt>";
		return str;
	}
	

	static String createLink(ClassDoc cl) {
		return createLink(cl,  cl.name());
	}

	/**
	 * for renaming the link to the classdoc
	 */
	static String createLink(ClassDoc cl, String name) {
		String str = "";
		if (isVisibleClass(cl)) {
			if (cl.isAbstract())
				str += "<i>";
			str += createLink(cl.qualifiedName(), cl.name(), "", name);
			if (cl.isAbstract())
				str += "</i>";
		} else {
			str = cl.name();
		}
		return str;
	}

	static String createLink(MemberDoc mem, ClassDoc currentClass) {
		MemberInfo info = getMemberInfo(mem);
		if (info != null)
			return info.createLink(currentClass);
		else
			return "";
	}

	static ClassDoc getClass(String name) {
		ClassInfo cls = (ClassInfo) classInfos.get(name);
		if (cls != null)
			return cls.classDoc;
		else
			return null;
	}
	
	static String createClassLink(String name, String qualifiedName) {
		ClassDoc cls = getClass(qualifiedName);
		if (cls != null)
			return createLink(cls);
		else
			return "<tt>" + name + "</tt>";
	}

	static String createLink(Type type) {
		if (isNumber(type))
			return "<tt>Number</tt>";
		else if (isBoolean(type))
			return "<tt>Boolean</tt>";
		else if (isArray(type))
			return "<tt>Array</tt>";
		else if (isMap(type))
			return "<tt>Object</tt>";
		else if (isPoint(type))
			return createClassLink("Point", "com.scriptographer.ai.Point");
		else if (isRectangle(type))
			return createClassLink("Rectangle", "com.scriptographer.ai.Rectangle");
		else if (isMatrix(type))
			return createClassLink("Matrix", "com.scriptographer.ai.Matrix");
		else {
			ClassDoc cls = type.asClassDoc();
			if (cls != null) {
				if (isVisibleClass(cls))
					return createLink(cls);
				else
					return "<tt>" + cls.name() + "</tt>";
			} else {
				return "<tt>" + type.toString() + "</tt>";
			}
		}
	}

	static String createAnchor(String name, boolean memberSelector) {
		if (hyperref) {
			String anchor = "<a name=\"" + name + "\"";
			if (memberSelector) {
				anchor += " href=\"#\" onClick=\"return toggleMember('" + name + "', false);\">";
			} else {
				anchor += "></a>";
			}
			return anchor;
		} else
			return "";
	}

	static String createAnchor(MemberInfo mem, boolean memberSelector) {
		return createAnchor(mem.getId(), memberSelector);
	}

	static String encodeJs(String str) {
		return str.replaceAll("(\\\"|'|\\\n|\\\r|\\\\)", "\\\\$1");
	}

	static String stripCodeTags(String str) {
		return str.replaceAll("<tt>|</tt>", "");
	}

	static String stripTags(String str) {
		return str.replaceAll("<.*?>|</.*?>", " ").replaceAll("\\s+", " ");
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
		else if (option.equals("-filterclasses"))
			return 2;
		else if (option.equals("-packagesequence"))
			return 2;
		else if (option.equals("-methodfilter"))
			return 2;
		else if (option.equals("-classorder"))
			return 2;
		else if (option.equals("-templates"))
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
			} else if (option.equals("-filterclasses")) {
				filterClasses = arg[1].split("\\s");
			} else if (option.equals("-packagesequence")) {
				packageSequence = arg[1].split("\\s");
			} else if (option.equals("-methodfilter")) {
				methodFilter = arg[1].split("\\s");
			} else if (option.equals("-classorder")) {
				classOrder = new HashMap();
				try {
					BufferedReader in = new BufferedReader(new FileReader(arg[1]));
					String line;
					int count = 0;
					while ((line = in.readLine()) != null) {
						line = line.trim();
						if (line.length() > 0)
							classOrder.put(line.trim(), new Integer(count++));
					}
				} catch (IOException e) {
					err.printError(e.toString());
				}
			} else if (option.equals("-templates")) {
				templates = arg[1].equals("on");
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
}