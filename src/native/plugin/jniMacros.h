/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2010 Juerg Lehni, http://www.scratchdisk.com.
 * All rights reserved.
 *
 * Please visit http://scriptographer.org/ for updates and contact.
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
 */

/*
 *  Some very handy macros that make the life much easier for defining wrapper functions for all the various
 * JNI types:
 */
#define JNI_CHECK_ENV \
	if (env == NULL) m_javaVM->AttachCurrentThread((void **)&env, NULL);
	
#define JNI_ARGS_BEGIN(START) \
	va_list args; \
	va_start(args, START);
	
#define JNI_ARGS_END \
	va_end(args);

/*
 * Calls a given macro for all JNI return types, except void
 */
#define JNI_DEFINE_TYPES(DEFINE_TYPE) \
	DEFINE_TYPE(Object, jobject) \
	DEFINE_TYPE(Boolean, jboolean) \
	DEFINE_TYPE(Byte, jbyte) \
	DEFINE_TYPE(Char, jchar) \
	DEFINE_TYPE(Short, jshort) \
	DEFINE_TYPE(Int, jint) \
	DEFINE_TYPE(Long, jlong) \
	DEFINE_TYPE(Float, jfloat) \
	DEFINE_TYPE(Double, jdouble)

/*
 * Declares and defines wrapper functions for calling the jni Get<type>Field functions:
 */

#define JNI_DECLARE_GETFIELD(NAME, TYPE) \
	TYPE get##NAME##Field(JNIEnv *env, jobject obj, jfieldID fid);

#define JNI_DEFINE_GETFIELD(NAME, TYPE) \
	TYPE ScriptographerEngine::get##NAME##Field(JNIEnv *env, jobject obj, jfieldID fid) { \
		JNI_CHECK_ENV \
		TYPE res = env->Get##NAME##Field(obj, fid); \
		EXCEPTION_CHECK(env); \
		return res; \
	}

#define JNI_DECLARE_GETFIELD_FUNCTIONS \
	JNI_DEFINE_TYPES(JNI_DECLARE_GETFIELD)

#define JNI_DEFINE_GETFIELD_FUNCTIONS \
	JNI_DEFINE_TYPES(JNI_DEFINE_GETFIELD)


/*
 * Declares and defines wrapper functions for calling the jni Set<type>Field functions:
 */

#define JNI_DECLARE_SETFIELD(NAME, TYPE) \
	void set##NAME##Field(JNIEnv *env, jobject obj, jfieldID fid, TYPE val);

#define JNI_DEFINE_SETFIELD(NAME, TYPE) \
	void ScriptographerEngine::set##NAME##Field(JNIEnv *env, jobject obj, jfieldID fid, TYPE val) { \
		JNI_CHECK_ENV \
		env->Set##NAME##Field(obj, fid, val); \
		EXCEPTION_CHECK(env); \
	}

#define JNI_DECLARE_SETFIELD_FUNCTIONS \
	JNI_DEFINE_TYPES(JNI_DECLARE_SETFIELD)

#define JNI_DEFINE_SETFIELD_FUNCTIONS \
	JNI_DEFINE_TYPES(JNI_DEFINE_SETFIELD)


/*
 * Declares and defines wrapper functions for calling the jni GetStatic<type>Field functions:
 */

#define JNI_DECLARE_GETSTATICFIELD(NAME, TYPE) \
	TYPE getStatic##NAME##Field(JNIEnv *env, jclass cls, const char *name, const char *signature);

#define JNI_DEFINE_GETSTATICFIELD(NAME, TYPE) \
	TYPE ScriptographerEngine::getStatic##NAME##Field(JNIEnv *env, jclass cls, const char *name, const char *signature) { \
		JNI_CHECK_ENV \
		TYPE res = env->GetStatic##NAME##Field(cls, env->GetStaticFieldID(cls, name, signature)); \
		EXCEPTION_CHECK(env); \
		return res; \
	}

#define JNI_DECLARE_GETSTATICFIELD_FUNCTIONS \
	JNI_DEFINE_TYPES(JNI_DECLARE_GETSTATICFIELD)

#define JNI_DEFINE_GETSTATICFIELD_FUNCTIONS \
	JNI_DEFINE_TYPES(JNI_DEFINE_GETSTATICFIELD)

/*
 * Macros, used to automate the definition of Call<type>Method wrapper functions:
 */
 
#define JNI_DEFINE_CALLMETHOD_ARGS(FUNC, TYPE, HANDLE_EXCEPTION) \
	JNI_CHECK_ENV \
	JNI_ARGS_BEGIN(mid) \
	FUNC; \
	JNI_ARGS_END \
	HANDLE_EXCEPTION(env)

#define JNI_DEFINE_CALLMETHOD_ARGS_RETURN(FUNC, TYPE, HANDLE_EXCEPTION) \
	JNI_CHECK_ENV \
	JNI_ARGS_BEGIN(mid) \
	TYPE res = FUNC; \
	JNI_ARGS_END \
	HANDLE_EXCEPTION(env) \
	return res;

/*
 * Declares and defines wrapper functions for calling the jni Call<type>Method functions.
 * There are both static and non static versions, and each has a Report version, which catches exceptions
 * and reports them.
 */
 
#define JNI_DECLARE_CALLMETHODS(NAME, TYPE) \
 	TYPE call##NAME##Method(JNIEnv *env, jobject obj, jmethodID mid, ...); \
	TYPE callStatic##NAME##Method(JNIEnv *env, jclass cls, jmethodID mid, ...); \
	TYPE call##NAME##MethodReport(JNIEnv *env, jobject obj, jmethodID mid, ...); \
	TYPE callStatic##NAME##MethodReport(JNIEnv *env, jclass cls, jmethodID mid, ...);

#define JNI_DEFINE_CALLMETHODS(NAME, TYPE, CALLER) \
	TYPE ScriptographerEngine::call##NAME##Method(JNIEnv *env, jobject obj, jmethodID mid, ...) { \
		CALLER(env->Call##NAME##MethodV(obj, mid, args), TYPE, EXCEPTION_CHECK); \
	} \
\
	TYPE ScriptographerEngine::callStatic##NAME##Method(JNIEnv *env, jclass cls, jmethodID mid, ...) { \
		CALLER(env->CallStatic##NAME##MethodV(cls, mid, args), TYPE, EXCEPTION_CHECK); \
	} \
\
	TYPE ScriptographerEngine::call##NAME##MethodReport(JNIEnv *env, jobject obj, jmethodID mid, ...) { \
		CALLER(env->Call##NAME##MethodV(obj, mid, args), TYPE, EXCEPTION_REPORT); \
	} \
\
	TYPE ScriptographerEngine::callStatic##NAME##MethodReport(JNIEnv *env, jclass cls, jmethodID mid, ...) { \
		CALLER(env->CallStatic##NAME##MethodV(cls, mid, args), TYPE, EXCEPTION_REPORT); \
	}

#define JNI_DEFINE_CALLMETHODS_RETURN(NAME, TYPE) \
	JNI_DEFINE_CALLMETHODS(NAME, TYPE, JNI_DEFINE_CALLMETHOD_ARGS_RETURN)

#define JNI_DECLARE_CALLMETHOD_FUNCTIONS \
	JNI_DECLARE_CALLMETHODS(Void, void) \
	JNI_DEFINE_TYPES(JNI_DECLARE_CALLMETHODS)

#define JNI_DEFINE_CALLMETHOD_FUNCTIONS \
	JNI_DEFINE_CALLMETHODS(Void, void, JNI_DEFINE_CALLMETHOD_ARGS) \
	JNI_DEFINE_TYPES(JNI_DEFINE_CALLMETHODS_RETURN)
/*
 *
 * Exception handling helper macros
 *
 */

/*
 * EXCEPTION_CHECK can be called whenever a JNI function that potentially caused a Java exception
 * was called. The Java exception is then cleared and lives on as a C++ exception that is thrown.
 * In EXCEPTION_CONVERT(env), this exception is then turned back into a Java exception again.
 * This stragety also allows the C++ code to throw newly built jthrowables.
 */
#define EXCEPTION_CHECK(env); \
	{ \
		jthrowable throwable = env->ExceptionOccurred(); \
		if (throwable != NULL) { \
			env->ExceptionClear(); \
			throw new JThrowableException(throwable); \
		} \
	}
/*
 * EXCEPTION_CONVERT(env) needs to be called at the end of JNI native functions, to be sure that any
 * exception thrown by native code is turned proberly into a Java exception again.
 * EXCEPTION_CONVERT(env) also catches char * exceptions which are turned into an IllustratorException
 * afterwards, and thrown jclasses which are assumed to be exception classes of which then 
 * an object is created an thrown.
 * Any more exception types can be easily added and handled everwhere.
 * any code that follows after EXCEPTION_CONVERT(env) is executed just like finally() {} in Java, because
 * no more C++ exceptions are thrown from here.
 */

#define EXCEPTION_CONVERT(env) \
	catch (ScriptographerException *e) { \
		e->convert(env); \
		delete e; \
	} catch (ATE::Exception e) { \
		ASErrException(e.error).convert(env);\
	} catch (...) { \
		env->ThrowNew(gEngine->cls_ScriptographerException, "Unknown error"); \
	}

#define EXCEPTION_REPORT(env) \
	if (env->ExceptionCheck()) { \
		if (gEngine != NULL) gEngine->reportError(env); \
		else env->ExceptionDescribe(); \
	}
	
#define EXCEPTION_CATCH_REPORT(env) \
	catch (ScriptographerException *e) { \
		e->report(env); \
		gPlugin->log("Exception: %s", e->toString(env)); \
		delete e; \
	} catch (...) { \
		ScriptographerException e; \
		e.report(env); \
	}
