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
 * $RCSfile: editorDialog.cpp,v $
 * $Author: lehni $
 * $Revision: 1.1 $
 * $Date: 2005/02/23 22:00:59 $
 */
 
#include "stdHeaders.h"
#include "Plugin.h"
#include "Tool.h"
#include "resourceIds.h"
#include "admHandler.h"
#include "mainDialog.h"
#include "editorDialog.h"

#include <stdio.h>

ASErr ASAPI editorDlgInit(ADMDialogRef dlg) {
	ASErr fxErr = kNoErr;
 
	// Attach the dialog-level callbacks
	sADMDialog->SetDestroyProc(dlg, editorDlgDestroy);
	sADMDialog->SetNotifyProc(dlg, editorDlgNotify);

	// resize handler
	ADMItemRef resizeItemRef = sADMDialog->GetItem(dlg, kADMResizeItemID);
	if (resizeItemRef) sADMItem->SetNotifyProc(resizeItemRef, editorResize);

	// edit field:
	ADMItemRef editorItemRef = sADMDialog->GetItem(dlg, kEditorTextItem);
	sADMItem->SetTrackProc(editorItemRef, editorTrack);
	sADMItem->SetMaxTextLength(editorItemRef, (1 << 31) - 1);
	
	// Setup popup menu on dialog
	ADMItemRef menuItemRef = sADMDialog->GetItem(dlg, kADMMenuItemID);
	if (menuItemRef) {
		ADMListRef menuListRef = sADMItem->GetList(menuItemRef);
		if (menuListRef)
			sADMList->SetMenuID(menuListRef, gPlugin->getPluginRef(), kEditorPulldownMenuID, NULL);
		sADMItem->SetNotifyProc(menuItemRef, editorPopupMenuNotify);
		sADMItem->SetTrackProc(menuItemRef, editorPopupMenuTrack);
	}

	ADMItemRef *buttonItems = gPlugin->editor.buttonItems;

	buttonItems[0] = initPictureButton(dlg, kEditorPlayItem, editorPlayNotify, kPlayPictureID);
	buttonItems[1] = initPictureButton(dlg, kEditorStopItem, editorStopNotify, kStopPictureID);
	buttonItems[2] = initPictureButton(dlg, kEditorSaveItem, editorSaveNotify, kSavePictureID);
	buttonItems[3] = initPictureButton(dlg, kEditorNewItem, editorNewNotify, kScriptPictureID);
	buttonItems[4] = sADMDialog->GetItem(dlg, kEditorLineItem);
	dialogLoadPreference(dlg, gPlugin->editor.name);

	return fxErr;
}


static void ASAPI editorDlgDestroy(ADMDialogRef dlg) {
	dialogSavePreference(dlg, gPlugin->editor.name);
	
	void *data = sADMItem->GetUserData(sADMDialog->GetItem(dlg, kEditorTextItem));
	if (data != NULL) free(data);

	editorSetFileName(NULL);

	gPlugin->editor.dlg = NULL;
}

static void ASAPI editorDlgNotify(ADMDialogRef dlg, ADMNotifierRef notifier) {
	dialogTextDlgNotify(dlg, notifier, kEditorTextItem);
}

static void ASAPI editorResize(ADMItemRef item, ADMNotifierRef notifier) {
	dialogResize(item, notifier, kEditorTextItem, gPlugin->editor.buttonItems, sizeof(gPlugin->editor.buttonItems) / sizeof(ADMItemRef), true);
}

static ASBoolean ASAPI editorPopupMenuTrack(ADMItemRef item, ADMTrackerRef tracker) {
	if (sADMTracker->GetAction(tracker) == kADMButtonDownAction) { 	
		SPPluginRef pluginRef = sADMItem->GetPluginRef(item);
		ADMListRef menuListRef = sADMItem->GetList(item);
		ADMDialogRef dlg = sADMItem->GetDialog(item);
		if (menuListRef) enableButtonPopupItem(dlg, menuListRef, 4, kEditorSaveItem);
	} 	

	return sADMItem->DefaultTrack(item, tracker);
}

static void ASAPI editorPopupMenuNotify( ADMItemRef item, ADMNotifierRef notifier) {
	if ( sADMNotifier->IsNotifierType( notifier, kADMUserChangedNotifier ) ) {
		int id = getPopupID(item);
		switch(id) {
			case 1: editorPlayNotify(item, notifier); break;
			case 2: editorStopNotify(item, notifier); break;
			case 4: editorSaveNotify(item, notifier); break;
			case 5: editorNewNotify(item, notifier); break;
		}
	}
}

void editorShow(bool show) {
	sADMDialog->Show(gPlugin->editor.dlg, show);
}

bool editorVisible() {
	return sADMDialog->IsVisible(gPlugin->editor.dlg);
}

bool editorCheckModified(bool cancel) {
	if (gPlugin->editor.modified) {
		// bring window to front
		editorShow(true);
		ADMAnswer answer;
		char *question = "Do you want to save the changes?";
		if (cancel) answer = sADMBasic->QuestionAlert(question);
		else answer = sADMBasic->YesNoAlert(question);
		if (answer == kADMYesAnswer) editorSave((char *)sADMDialog->GetUserData(gPlugin->editor.dlg), (char *)sADMItem->GetUserData(sADMDialog->GetItem(gPlugin->editor.dlg, kEditorTextItem)));
		else if (answer == kADMCancelAnswer) return false;
	}
	return true;
}

void editorOpen(ADMListEntryRef entry) {
	ADMDialogRef dlg = gPlugin->editor.dlg;

	char *filename;
	if (entry != NULL) filename = (char *)sADMListEntry->GetUserData(entry);
	else filename = NULL;
		
	if (!editorCheckModified(true)) return;

	char *title = NULL, *text = NULL;
	char titleBuf[300];

	if (filename != NULL) {

		editorSetFileName(filename);

		strcpy(titleBuf, &filename[strlen(gPlugin->baseDir)]);
		title = titleBuf;

		// read the file binary
		FILE* fp = fopen(filename, "rb");
		if (fp != NULL && !fseek(fp, 0, SEEK_END)) {
			// now parse the textfile: convert breaks to the right format and \t to INDENT
			int len = ftell(fp);
			fseek(fp, 0, SEEK_SET);
			// allocate memory for the whole binary data and read it:
			char *tmp = (char *)malloc(len + 1);
			tmp[len] = '\0';
			fread(tmp, sizeof(char), len, fp);
			fclose(fp);
			// now convert the breaks if necessary. there's support for pc, mac and unix formated files:

			// count the number of \r, \n and \t
			int breakCount = 0, tabCount = 0;
			int pos = 0, num;
			char ch;
			while((ch = tmp[pos++]) != '\0') {
				if (ch == '\n' || ch == '\r') breakCount++;
				else if (ch == '\t') tabCount++;
			}
			int indentLen = strlen(INDENT);
#ifdef WIN_ENV
			// allocate a new buffer where \r\n instead of \n or \r fits in:
			int newLen = len + breakCount + tabCount * indentLen;
			text = (char *)malloc(newLen + 1);
			// repleace every \n and \r by \r\n, but only if they're not already part of \r\n
			int lastPos = 0, textPos = 0;
			pos = -1;
#elif MAC_ENV
			// allocate a new buffer
			text = (char *)malloc(len + tabCount * indentLen + 1);
			// repleace every \n and \r\n by \r
			int lastPos = 0, textPos = 0;
			pos = -1;
#endif
			while((ch = tmp[++pos]) != '\0') {
#ifdef WIN_ENV
				if ((ch == '\r' && tmp[pos + 1] != '\n') ||
					(ch == '\n' && (pos == 0 || tmp[pos - 1] != '\r'))) {
					num = pos - lastPos;
					strncpy(&text[textPos], &tmp[lastPos], num);
					textPos += num;
					strncpy(&text[textPos], "\r\n", 2);
					textPos += 2;
					lastPos = pos + 1;
				} 
#elif MAC_ENV
				if (ch == '\n') {
					bool pc = (pos > 0 && tmp[pos - 1] == '\r');// pc format ?
					if (pc) pos--;
					num = pos - lastPos;
					if (num > 0) {
						strncpy(&text[textPos], &tmp[lastPos], num);
						textPos += num;
					}
					strncpy(&text[textPos], "\r", 1);
					textPos ++;
					if (pc) pos++; // skip the '\r'
					lastPos = pos + 1;
				}
#endif // \t conversion is the same on mac & pc
				else if (ch == '\t') {
					num = pos - lastPos;
					strncpy(&text[textPos], &tmp[lastPos], num);
					textPos += num;
					strncpy(&text[textPos], INDENT, indentLen);
					textPos += indentLen;
					lastPos = pos + 1;
				}
			}

			num = pos - lastPos;
			strncpy(&text[textPos], &tmp[lastPos], num);
			textPos += num;
			text[textPos] = '\0';
			free(tmp);
		}
	} else {
		title = "untitled.js";

		editorSetFileName(NULL);
	}

	if (title != NULL) {
		editorShow(true);
		sADMItem->SetText(sADMDialog->GetItem(dlg, kEditorFileNameItem), title);
		ADMItemRef editorItemRef = sADMDialog->GetItem(dlg, kEditorTextItem);
		editorSetModified(false);
		if (text == NULL) sADMItem->SetText(editorItemRef, "");
		else {
			sADMItem->SetText(editorItemRef, text);
			dialogSetEditFont(gPlugin->editor.dlg, kEditorTextItem);
			free(text);
		}
	}
}

static void ASAPI editorPlayNotify(ADMItemRef item, ADMNotifierRef notifier) {
	// dispatch the notifier type
	if ( sADMNotifier->IsNotifierType( notifier, kADMUserChangedNotifier ) ) {
		ADMDialogRef dlg = sADMItem->GetDialog(item);
		char *filename = (char *)sADMDialog->GetUserData(dlg);

		ADMItemRef editorRef = sADMDialog->GetItem(dlg, kEditorTextItem);
		char *text = getText(editorRef);
		/* TODO: Play Execute
		jsExecute(text, sADMItem->GetTextLength(editorRef), filename);
		*/
		free(text);
	}
}

static void ASAPI editorStopNotify( ADMItemRef item, ADMNotifierRef notifier) {
	if ( sADMNotifier->IsNotifierType( notifier, kADMUserChangedNotifier ) ) {
//		jsStopTimers();
	}
}

void editorSetFileName(char *filename) {
	void *data = sADMDialog->GetUserData(gPlugin->editor.dlg);
	if (data != NULL) free(data);
	if (filename != NULL) {
		data = malloc(strlen(filename) + 1);
		strcpy((char *)data, filename);
	}
	else data = NULL;
	sADMDialog->SetUserData(gPlugin->editor.dlg, data);
}

void editorSetModified(bool modified) {
	gPlugin->editor.modified = modified;
	sADMItem->Enable(sADMDialog->GetItem(gPlugin->editor.dlg, kEditorSaveItem), modified);
}

void editorSave(char *filename, char *text) {
	ADMDialogRef dlg = gPlugin->editor.dlg;
	ASBoolean refresh = false;
	if (filename == NULL) {
		SPPlatformFileSpecification startingDir, result;
		sAIUser->Path2SPPlatformFileSpecification(gPlugin->baseDir, &startingDir);
		filename = "untitled.js";
		char filenameBuf[kMaxPathLength];
		while(true) {
			ADMPlatformFileTypesSpecification3 inFilter;
			if (!sADMBasic->StandardPutFileDialog("Save As", &inFilter, &startingDir, filename, &result)) {
				filename = NULL;
				break;
			}
			sAIUser->SPPlatformFileSpecification2Path(&result, filenameBuf);
			filename = filenameBuf;
			if (isJavaScript(filename)) break;
			strcat(filename, ".js");
		}
		if (filename != NULL) editorSetFileName(filename);
		refresh = true;
	}
	if (filename != NULL) {
		FILE* fp = fopen(filename, "wb");
		ADMItemRef editorRef = sADMDialog->GetItem(dlg, kEditorTextItem);
		
		char *str = NULL;
		if (text == NULL) str = getText(editorRef);
		else str = text;
		fwrite(str, sizeof(char), strlen(str), fp);
		fclose(fp);
		if (text == NULL) free(str);
		editorSetModified(false);

		char title[300];
		strcpy(title, &filename[strlen(gPlugin->baseDir)]);
		sADMItem->SetText(sADMDialog->GetItem(dlg, kEditorFileNameItem), title);
		if (refresh) mainRefreshFiles();
		Tool *tool = gPlugin->getTool(filename);
		if (tool != NULL) tool->initScript();
	}
}

static void ASAPI editorSaveNotify( ADMItemRef item, ADMNotifierRef notifier) {
	if ( sADMNotifier->IsNotifierType( notifier, kADMUserChangedNotifier ) ) {
		ADMDialogRef dlg = sADMItem->GetDialog(item);
		editorSave((char *)sADMDialog->GetUserData(dlg));
	}
}

static void ASAPI editorNewNotify( ADMItemRef item, ADMNotifierRef notifier) {
	if ( sADMNotifier->IsNotifierType( notifier, kADMUserChangedNotifier ) ) {
		editorOpen(NULL);
	}
}

long lastStart = 0, lastEnd = 0, dir;
void editorUpdateLineNumber(ADMItemRef item) {
	long start, end;
	sADMItem->GetSelectionRange(item, &start, &end);
	char *str = getText(item);
	int i = 0, pos;
	if (end != lastEnd) dir = 1;
	else if (start != lastStart) dir = -1;
	if (dir < 0) pos = start;
	else pos = end;
	int lineCount = 1;
	while(i < pos) {
		if (str[i++] == '\r') lineCount++;
	}
	char text[16];
	sprintf(text, "Line: %i", lineCount);
	ADMDialogRef dlg = sADMItem->GetDialog(item);
	sADMItem->SetText(sADMDialog->GetItem(dlg, kEditorLineItem), text);
	free(str);
	lastStart = start;
	lastEnd = end;
}

static ASBoolean ASAPI editorTrack(ADMItemRef item, ADMTrackerRef tracker) {
	ASBoolean res = dialogTextTrack(item, tracker);
	ADMAction action = sADMTracker->GetAction(tracker);
	ADMModifiers modifiers = sADMTracker->GetModifiers(tracker);
	if (action == kADMKeyStrokeAction) {
		ADMChar key = sADMTracker->GetVirtualKey(tracker);
		// detect normal keys and copy&paste keys:
		if (isNormalKey(key, modifiers) || (modifiers & kMacCommandKeyDownModifier && (key == 'X' || key == 'V')))
			editorSetModified(true);
		// check for keys that change position on the current line:
		if ((key >= kADMEnterKey && key <= kADMBackspaceKey) || 
			(key >= kADMLeftKey && key <= kADMDownKey) ||
			key == kADMDeleteKey || key == kADMReturnKey || key == kADMClearKey) {
			editorUpdateLineNumber(item);
		}
	} else if (action == kADMButtonUpAction || ((action == kADMMouseMovedDownAction || action == kADMMouseMovedUpAction) && (modifiers & kADMButtonDownModifier))) {
		editorUpdateLineNumber(item);
	}
	return res;
}
