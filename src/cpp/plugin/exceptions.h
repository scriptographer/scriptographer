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
 * $RCSfile: exceptions.h,v $
 * $Author: lehni $
 * $Revision: 1.2 $
 * $Date: 2005/03/05 21:32:09 $
 */
 
#define kExceptionErr 'EXPT';

class Exception : public std::exception {
public:
	virtual void convert(JNIEnv *env);

	virtual void report(JNIEnv *env);
};

class StringException : public Exception {
private:
	char *fMessage;
	
public:

	StringException(char *message, ...) {
		fMessage = new char[1024];
		va_list args;
		va_start(args, message);
		vsprintf(fMessage, message, args);
		va_end(args);
	}

	void convert(JNIEnv *env);
	void report(JNIEnv *env);
	
	~StringException() {
		delete fMessage;
	}
};

class ASErrException : public Exception {
private:
	ASErr fError;
	
public:
	ASErrException(ASErr error) {
		fError = error;
	}

	void convert(JNIEnv *env);
	void report(JNIEnv *env);
};

class JThrowableException : public Exception {
private:
	jthrowable fThrowable;	
	
public:
	JThrowableException(jthrowable throwable) {
		fThrowable = throwable;
	}

	void convert(JNIEnv *env);
	void report(JNIEnv *env);
};

class JThrowableClassException : public Exception {
private:
	jclass fClass;
		
public:
	JThrowableClassException(jclass cls) {
		fClass = cls;
	}

	JThrowableClassException(JNIEnv *env, const char *name) {
		fClass = env->FindClass(name);
	}

	void convert(JNIEnv *env);
	void report(JNIEnv *env);
};