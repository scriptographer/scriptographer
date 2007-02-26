package com.scriptographer.doclets;

import com.sun.javadoc.AnnotationTypeDoc;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.ParameterizedType;
import com.sun.javadoc.Type;
import com.sun.javadoc.TypeVariable;
import com.sun.javadoc.WildcardType;

/**
 * ParamType fixes a bug in the Type returned by Parameter.type(), where toString does not return []
 * for arrays and there does not seem to be another way to find out if it's an array or not...
 */
public class ParamType implements Type {
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

	public AnnotationTypeDoc asAnnotationTypeDoc() {
		return type.asAnnotationTypeDoc();
	}

	public ParameterizedType asParameterizedType() {
		return type.asParameterizedType();
	}

	public TypeVariable asTypeVariable() {
		return type.asTypeVariable();
	}

	public WildcardType asWildcardType() {
		return type.asWildcardType();
	}

	public boolean isPrimitive() {
		return type.isPrimitive();
	}

	public String simpleTypeName() {
		return type.simpleTypeName();
	}
}