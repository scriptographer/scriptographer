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
 * $RCSfile: consoleDialog.cpp,v $
 * $Author: lehni $
 * $Revision: 1.2 $
 * $Date: 2005/03/05 21:30:37 $
 */
 
#include "stdHeaders.h"
#include "Plugin.h"
#include "ScriptographerEngine.h"
#include "resourceIds.h"
#include "admHandler.h"
#include "mainDialog.h"
#include "consoleDialog.h"

ASErr ASAPI consoleDlgInit(ADMDialogRef dlg) {
	ASErr fxErr = kNoErr;
 
	// Attach the dialog-level callbacks
	sADMDialog->SetDestroyProc( dlg, consoleDlgDestroy );
	sADMDialog->SetNotifyProc(dlg, consoleDlgNotify);

	// resize handler

	ADMItemRef resizeItemRef = sADMDialog->GetItem(dlg, kADMResizeItemID);
	if (resizeItemRef) sADMItem->SetNotifyProc(resizeItemRef, consoleResize);

	ADMItemRef consoleItemRef = sADMDialog->GetItem(dlg, kConsoleTextItem);
	sADMItem->SetTrackProc(consoleItemRef, consoleTrack);
	sADMItem->SetMaxTextLength(consoleItemRef, (1 << 31) - 1);

	dialogLoadPreference(dlg, gPlugin->console.name);

	// everything's set up now, so let the engine now and we're all set:
	gEngine->initEngine();

	return fxErr;
}

static void ASAPI consoleDlgDestroy(ADMDialogRef dlg) {
	dialogSavePreference(dlg, gPlugin->console.name);
	
	void *data = sADMItem->GetUserData(sADMDialog->GetItem(dlg, kConsoleTextItem));
	if (data != NULL) free(data);

	gPlugin->console.dlg = NULL;
}

static void ASAPI consoleDlgNotify(ADMDialogRef dlg, ADMNotifierRef notifier) {
	dialogTextDlgNotify(dlg, notifier, kConsoleTextItem);
}

static void ASAPI consoleResize(ADMItemRef item, ADMNotifierRef notifier) {
	dialogResize(item, notifier, kConsoleTextItem, NULL, 0, true);
}

void consoleShow(bool show) {
	sADMDialog->Show(gPlugin->console.dlg, show);
}

bool consoleVisible() {
	return sADMDialog->IsVisible(gPlugin->console.dlg);
}

void consoleShowText(char **strings, int numStrings) {
	ADMDialogRef dlg = gPlugin->console.dlg;
	if (dlg == NULL) {
		int len = 0, i;
		for (i = 0; i < numStrings; i++)
			len += strlen(strings[i]) + strlen(NEWLINE);
			
		char *msg = new char[len];
		msg[0] = '\0';
		
		for (i = 0; i < numStrings; i++) {
			strcat(msg, strings[i]);
			strcat(msg, NEWLINE);
		}

		gPlugin->reportError(msg);
		delete msg;
	} else {
		consoleShow(true);

		char *prepend = "// ";
		char *separate = ", ";

		int totalLen = strlen(prepend) + strlen(NEWLINE), i;
		for (i = 0; i < numStrings; i++) totalLen += strlen(strings[i]);
		totalLen += (numStrings - 1) * strlen(separate);

		ADMItemRef consoleRef = sADMDialog->GetItem(dlg, kConsoleTextItem);
		char *str = getText(consoleRef);
		long start = 0, end = 0;
		sADMItem->GetSelectionRange(consoleRef, &start, &end);
		if (start < 0) start = 0;
		if (end < 0) end = 0;
		int len = strlen(str);
		char *text = (char *)malloc(len + totalLen + start - end + 1);

		text[0] = '\0';
		strncat(text, str, start);
		strcat(text, prepend);
		for (i = 0; i < numStrings; i++) {
			if (i > 0) strcat(text, separate);
			strcat(text, strings[i]);
		}
		strcat(text, NEWLINE);
		int pos = strlen(text);
		strcat(text, &str[end]);

		sADMItem->SetText(consoleRef, text);
		sADMItem->SetSelectionRange(consoleRef, pos, pos);
		free(text);
		free(str);
	}
}

void consoleShowText(char *string) {
	consoleShowText(&string, 1);
}

static ASBoolean ASAPI consoleTrack(ADMItemRef item, ADMTrackerRef tracker) {
	ASBoolean notify = dialogTextTrack(item, tracker);
	if (sADMTracker->GetAction(tracker) == kADMKeyStrokeAction &&
		sADMTracker->GetVirtualKey(tracker) == kADMReturnKey) {

		char *str = getText(item);;
		long start, end;
		sADMItem->GetSelectionRange(item, &start, &end);
		char ch;
		while (--end >= 0 && ((ch = str[end]) == '\n' || ch == '\r')){}
		end++;
		start = end;
		while (--start >= 0 && (ch = str[start]) != '\n' && ch != '\r'){}
		start++;

		str[end+1] = '\0';
		char *text = &str[start];
		
		gEngine->evaluateString(str);
				
		free(str);
	}
	return notify;
}
