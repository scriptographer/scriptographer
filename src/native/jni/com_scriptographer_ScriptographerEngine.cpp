/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2008 Juerg Lehni, http://www.scratchdisk.com.
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
 * $Id$
 */

#include "StdHeaders.h"
#include "ScriptographerPlugin.h"
#include "ScriptographerEngine.h"
#include "com_scriptographer_ScriptographerEngine.h"
#include "com_scriptographer_ui_Key.h"

/*
 * com.scriptographer.ScriptographerEngine
 */

/*
 * java.lang.String nativeReload()
 */
JNIEXPORT jstring JNICALL Java_com_scriptographer_ScriptographerEngine_nativeReload(JNIEnv *env, jclass cls) {
	try {
		return gEngine->reloadEngine();
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * boolean launch(java.lang.String filename)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ScriptographerEngine_launch(JNIEnv *env, jclass cls, jstring filename) {
	if (filename == NULL)
		return false;
	
	char *path = NULL;
	bool result = false;
	
	try {
		path = gEngine->convertString(env, filename);
		if (strlen(path) >= 10 &&
			strncmp(path, "http://", 7) == 0 ||
			strncmp(path, "https://", 8) == 0 ||
			strncmp(path, "mailto://", 9) == 0 ||
			strncmp(path, "ftp://", 6) == 0 ||
			strncmp(path, "file://", 7) == 0) {
/*
#ifdef WIN_ENV
			// sAIURL->OpenURL does not seem to work on Windows, or often fail?
			AIWindowRef handle = NULL;
			sAIAppContext->GetPlatformAppWindow(&handle);
			char test[1024];
			sprintf(test, "url.dll,FileProtocolHandler %s", path);
			HINSTANCE res = ShellExecute(NULL, "open", "rundll32.exe", test, NULL, SW_SHOWNORMAL);
			result = !res;
#else
*/
			result = !sAIURL->OpenURL(path);
/*
#endif
*/
		} else {
			SPPlatformFileSpecification fileSpec;
			if (gPlugin->pathToFileSpec(path, &fileSpec)) {
#if kPluginInterfaceVersion < kAI12
				result = !sAIUser->LaunchApp(&fileSpec, true);
#else
				ai::FilePath filePath(fileSpec);
				result = !sAIUser->LaunchApp(filePath, true);
#endif
			}
		}
	} EXCEPTION_CONVERT(env);
	if (path != NULL)
		delete path;
	return result;
}

/*
 * long getNanoTime()
 */
JNIEXPORT jlong JNICALL Java_com_scriptographer_ScriptographerEngine_getNanoTime(JNIEnv *env, jclass cls) {
	return gEngine->getNanoTime();
}

/*
 * void nativeSetProgressText(java.lang.String text)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ScriptographerEngine_nativeSetProgressText(JNIEnv *env, jclass cls, jstring text) {
	try {
#if kPluginInterfaceVersion < kAI12
		char *str = gEngine->convertString(env, text);
		sAIUser->SetProgressText(str);
		delete str;
#else
		ai::UnicodeString str = gEngine->convertString_UnicodeString(env, text);
		sAIUser->SetProgressText(str);
#endif
	} EXCEPTION_CONVERT(env);
}


/*
 * boolean nativeUpdateProgress(long current, long max, boolean visible)
 */
JNIEXPORT jboolean JNICALL Java_com_scriptographer_ScriptographerEngine_nativeUpdateProgress(JNIEnv *env, jclass cls, jlong current, jlong max, jboolean visible) {
	try {
		if (gEngine->isKeyDown(com_scriptographer_ui_Key_VK_ESCAPE))
			return false;
		if (visible) {
			sAIUser->UpdateProgress(current, max);
			return !sAIUser->Cancel();
		} else {
			return true;
		}
	} EXCEPTION_CONVERT(env);
	return false;
}

/*
 * void nativeCloseProgress()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ScriptographerEngine_nativeCloseProgress(JNIEnv *env, jclass cls) {
	try {
		sAIUser->CloseProgress();
	} EXCEPTION_CONVERT(env);
}

/*
 * void dispatchNextEvent()
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ScriptographerEngine_dispatchNextEvent(JNIEnv *env, jclass cls) {
	try {
		// Manually process event loop events:
#ifdef MAC_ENV
		// http://developer.apple.com/documentation/Carbon/Conceptual/Carbon_Event_Manager/Tasks/chapter_3_section_12.html
		EventRef event;
		if (ReceiveNextEvent(0, NULL, kEventDurationForever, true, &event) == noErr) {
			SendEventToEventTarget (event, GetEventDispatcherTarget());
            ReleaseEvent(event);
		}
#endif
#ifdef WIN_ENV
		// http://msdn2.microsoft.com/en-US/library/aa452701.aspx
		MSG message;
		if (GetMessage(&message, NULL, 0, 0)) {
			TranslateMessage(&message);
			DispatchMessage(&message);
		}
#endif
	} EXCEPTION_CONVERT(env);
}

/*
 * java.lang.String getApplicationVersion()
 */
JNIEXPORT jstring JNICALL Java_com_scriptographer_ScriptographerEngine_getApplicationVersion(JNIEnv *env, jclass cls) {
	try {
#if kPluginInterfaceVersion >= kAI12
		ASInt32 major = sAIRuntime->GetAppMajorVersion();
		ASInt32 minor = sAIRuntime->GetAppMinorVersion();
#else
		// TODO: how to find this out on CS?
		ASInt32 major = 11;
		ASInt32 minor = 0;
#endif
		char version[32];
		sprintf(version, "%i.%i", major, minor);
		return gEngine->convertString(env, version);
	} EXCEPTION_CONVERT(env);
	return NULL;
}

/*
 * int getApplicationRevision()
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ScriptographerEngine_getApplicationRevision(JNIEnv *env, jclass cls) {
	try {
#if kPluginInterfaceVersion >= kAI12
		return sAIRuntime->GetAppRevisionVersion();
#else
		// TODO: how to find this out on CS?
		return 0;
#endif
	} EXCEPTION_CONVERT(env);
	return 0;
}
