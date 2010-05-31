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

#include "StdHeaders.h"
#include "ScriptographerEngine.h"
#include "ScriptographerPlugin.h"
#include "AppContext.h"
#include "com_scriptographer_ui_Timer.h"

/*
 * com.scriptographer.ui.Timer
 */

#ifdef WIN_ENV
void CALLBACK Dialog_onTimer(HWND hwnd, UINT uMsg, UINT_PTR timerId, DWORD dwTime) {
#endif // WIN_ENV

#ifdef MAC_ENV

pascal void Dialog_onTimerFuntion(EventLoopTimerRef timerId, void* userData) {
#endif // MAC_ENV
	AppContext context;
	// Establish an application context for undoing.
	sAIUndo->SetKind(kAIAppendUndoContext);
	// Call run on runnable
	JNIEnv *env = gEngine->getEnv();
	try {
		gEngine->callStaticVoidMethod(env, gEngine->cls_ui_Timer,
				gEngine->mid_ui_Timer_onExecute, (jint) timerId);
		// Since the onExecute call might have changed the currently visible
		// document, always try to redraw it here. RedrawDocument() seems cheap
		// and only consumes time if something actually needs refreshing, so
		// don't bother tracking changes.
		// sAIDocument->RedrawDocument();
	} EXCEPTION_CATCH_REPORT(env);
}

#ifdef MAC_ENV
EventLoopTimerUPP Dialog_onTimer = NewEventLoopTimerUPP(Dialog_onTimerFuntion);
#endif // MAC_ENV

/*
 * int nativeCreate(int period)
 */
JNIEXPORT jint JNICALL Java_com_scriptographer_ui_Timer_nativeCreate(
		JNIEnv *env, jobject obj, jint period) {
	try {
#ifdef WIN_ENV
		return (jint) SetTimer(NULL, NULL, period, Dialog_onTimer);
#endif // WIN_ENV
#ifdef MAC_ENV
		EventLoopTimerRef timer;
		InstallEventLoopTimer(GetMainEventLoop(), 0,
                kEventDurationMillisecond * period, Dialog_onTimer, NULL, &timer);
		return (jint) timer;
#endif // MAC_ENV
	} EXCEPTION_CONVERT(env);
	return 0;
}

/*
 * void nativeAbort(int handle)
 */
JNIEXPORT void JNICALL Java_com_scriptographer_ui_Timer_nativeAbort(
		JNIEnv *env, jobject obj, jint handle) {
	try {
#ifdef WIN_ENV
		KillTimer(NULL, (UINT_PTR) handle);
#endif // WIN_ENV
#ifdef MAC_ENV
		RemoveEventLoopTimer((EventLoopTimerRef) handle);
#endif // MAC_ENV
	} EXCEPTION_CONVERT(env);
}
