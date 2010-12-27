/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Scripting Plugin for Adobe Illustrator
 * http://scriptographer.org/
 *
 * Copyright (c) 2002-2010, Juerg Lehni
 * http://scratchdisk.com/
 *
 * All rights reserved. See LICENSE file for details.
 */

#define kExceptionErr 'EXPT';

class ScriptographerException: public std::exception {
public:
	virtual void convert(JNIEnv *env);
	virtual char *toString(JNIEnv *env);
	void report(JNIEnv *env);
};

class StringException: public ScriptographerException {
protected:
	char *m_message;
	
public:
	StringException(const char *message, ...);
	void convert(JNIEnv *env);
	char *toString(JNIEnv *env);
	
	~StringException() throw() {
		delete m_message;
	}
};

class JObjectException: public StringException {
public:
	JObjectException(JNIEnv *env, const char *message, jobject object);
};

class ASErrException: public ScriptographerException {
private:
	ASErr m_error;
	
public:
	ASErrException(ASErr error);
	void convert(JNIEnv *env);
	char *toString(JNIEnv *env);
};

class JThrowableException: public ScriptographerException {
private:
	jthrowable m_throwable;	
	
public:
	JThrowableException(jthrowable throwable);
	void convert(JNIEnv *env);
	char *toString(JNIEnv *env);
};

class JThrowableClassException: public ScriptographerException {
private:
	jclass m_class;
		
public:
	JThrowableClassException(jclass cls);
	JThrowableClassException(JNIEnv *env, const char *name);
	void convert(JNIEnv *env);
	char *toString(JNIEnv *env);
};
