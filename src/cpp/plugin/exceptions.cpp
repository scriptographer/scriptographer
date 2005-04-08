/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2005 Juerg Lehni, http://www.scratchdisk.com.
 * All rights reserved.
 *
 * Please visit http://scriptographer.com/ for updates and contact.
 *
 * -- GPL LICENSE NOTICE --
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * -- GPL LICENSE NOTICE --
 *
 * $RCSfile: exceptions.cpp,v $
 * $Author: lehni $
 * $Revision: 1.5 $
 * $Date: 2005/04/08 21:56:40 $
 */
 
#include "stdHeaders.h"
#include "Plugin.h"
#include "ScriptographerEngine.h"

void Exception::convert(JNIEnv *env) {
}

char *Exception::toString(JNIEnv *env) {
	return strdup("Unknown Error");
}

void Exception::report(JNIEnv *env) {
	char *str = toString(env);
	if (gEngine != NULL && gEngine->isInitialized()) {
		gEngine->println(env, str);
	} else {
#ifdef MAC_ENV
		int len = strlen(str);
		// convert line breaks on mac:
		for (int i = 0; i < len; i++) {
			if (str[i] == '\r') str[i] = '\n';
		}
#endif
		if (gPlugin != NULL)
			gPlugin->reportError(str);
	}
	delete str;
}

StringException::StringException(char *message, ...) {
	fMessage = new char[1024];
	va_list args;
	va_start(args, message);
	vsprintf(fMessage, message, args);
	va_end(args);
}

void StringException::convert(JNIEnv *env) {
	gEngine->throwException(env, fMessage);
}

char *StringException::toString(JNIEnv *env) {
	return strdup(fMessage);
}

ASErrException::ASErrException(ASErr error) {
	fError = error;
}

void ASErrException::convert(JNIEnv *env) {
	char *str = toString(env);
	gEngine->throwException(env, str);
	delete str;
}

char *ASErrException::toString(JNIEnv *env) {
	char *format = "ASErrException %i\n";
	char *str = new char[strlen(format) + 16];
	sprintf(str, format, fError);
	return str;
}

JThrowableException::JThrowableException(jthrowable throwable) {
	fThrowable = throwable;
}

void JThrowableException::convert(JNIEnv *env) {
	env->Throw(fThrowable);
	env->DeleteLocalRef(fThrowable);
}

char *JThrowableException::toString(JNIEnv *env) {
	// we don't depend on any underlaying structures like gEngine here, so all the classes need
	// to be loaded first:
	jclass cls_StringWriter = env->FindClass("java/io/StringWriter");
	jmethodID ctr_StringWriter = env->GetMethodID(cls_StringWriter, "<init>", "()V");
	jmethodID mid_toString = env->GetMethodID(cls_StringWriter, "toString", "()Ljava/lang/String;");

	jclass cls_PrintWriter = env->FindClass("java/io/PrintWriter");
	jmethodID ctr_PrintWriter = env->GetMethodID(cls_PrintWriter, "<init>", "(Ljava/io/Writer;)V");
	jmethodID mid_println = env->GetMethodID(cls_PrintWriter, "println", "(Ljava/lang/String;)V");

	jclass cls_Throwable = env->FindClass("java/lang/Throwable");
	jmethodID mid_getMessage = env->GetMethodID(cls_Throwable, "getMessage", "()Ljava/lang/String;");
	jmethodID mid_printStackTrace = env->GetMethodID(cls_Throwable, "printStackTrace", "(Ljava/io/PrintWriter;)V");

	jclass cls_String = env->FindClass("java/lang/String");
	jmethodID mid_getBytes = env->GetMethodID(cls_String, "getBytes", "()[B");
	if (env->ExceptionCheck()) {
		env->ExceptionDescribe();
	} else {
		// create the string writer...
		jobject writer = env->NewObject(cls_StringWriter, ctr_StringWriter);
		// ... and wrap it in a PrintWriter.
		jobject printer = env->NewObject(cls_PrintWriter, ctr_PrintWriter, writer);
		// now print the message...
		jobject message = env->CallObjectMethod(fThrowable, mid_getMessage);
		env->CallVoidMethod(printer, mid_println, message);
		// ... and stacktrace to it.
		env->CallVoidMethod(fThrowable, mid_printStackTrace, printer);
		// now fetch the string:
		jstring jstr = (jstring)env->CallObjectMethod(writer, mid_toString);
		// create a c-string from it:
		jbyteArray bytes = (jbyteArray)env->CallObjectMethod(jstr, mid_getBytes);
		jint len = env->GetArrayLength(bytes);
		char *str = new char[len + 1];
		if (str == NULL) {
			env->DeleteLocalRef(bytes);
		} else {
			env->GetByteArrayRegion(bytes, 0, len, (jbyte *)str);
			str[len] = 0; // NULL-terminate
			env->DeleteLocalRef(bytes);
			return str;
		}
	}
	return strdup("Unable to generate the Throwable's Stacktrace");
}

JThrowableClassException::JThrowableClassException(jclass cls) {
	fClass = cls;
}

JThrowableClassException::JThrowableClassException(JNIEnv *env, const char *name) {
	fClass = env->FindClass(name);
}


void JThrowableClassException::convert(JNIEnv *env) {
	env->ThrowNew(fClass, NULL);
}

char *JThrowableClassException::toString(JNIEnv *env) {
	char *format = "JThrowableClassException %i\n";
	char *str = new char[strlen(format) + 16];
	sprintf(str, format, fClass);
	return str;
}