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
 * $RCSfile: admHandler.cpp,v $
 * $Author: lehni $
 * $Revision: 1.2 $
 * $Date: 2005/03/05 21:29:44 $
 */
 
#include "stdHeaders.h"
#include "ScriptographerEngine.h"
#include "resourceIds.h"
#include "admHandler.h"
#include "mainDialog.h"
#include "editorDialog.h"
#include "consoleDialog.h"

#include <ctype.h>

// This file contains createADMDialog() which initializes the ADM dialog
// It also the initialization and destroy procedures for the dialog
// Private functions include all the notifier procs.

/*******************************************************************************
 **
 ** ADM Dialog
 **
 **/

void dialogLoadPreference(ADMDialogRef dlg, char *name) {
	// Do this only if you have a resizable dialog of type kADMTabbedResizingFloatingDialogStyle or kADMResizingFloatingDialogStyle
	// Set the increments and min/max sizes to limit grow behavior.
	sADMDialog->SetMinWidth( dlg, layerMinWidth );
	sADMDialog->SetMinHeight( dlg, layerMinHeight );
	sADMDialog->SetMaxWidth( dlg, layerMaxWidth );
	sADMDialog->SetVerticalIncrement( dlg, layerLineHeight );

	/*******************************************************************************
 	 *	Restore dialog position
	 *
 	 *	A note about Dialog position code:
 	 *  positionCode is a code to restore a 
 	 *  dialog's position within a docked/tabbed group.  The group is 
 	 *  referred to by name, this being the name of the ADM Dialog that is
 	 *  the first tab in the top dock of the group. 
 	 *
 	 *  You don't need to know what positionCode means, but if you are curious:
 	 *
 	 *  byte		  		meaning
 	 *  ----		 		------------------------------
 	 *	1 (0x000000ff)	dock position. 0 is no dock, 1 is first docked (i.e. top dock), 2 etc.
 	 *	2 (0x0000ff00)	tab position, 0 is no tab group, 1 is the 1st tab, 2 etc.
	 *	3 (0x00010000)	bit 16, boolean, 1 for front tab.
 	 *	3 (0x00020000)	bit 17, boolean, 0 is zoom up, 1 is zoom down.
 	 *	3 (0x00040000)	bit 18, boolean, 0 is in hidden dock, 1 is in visible dock.
 	 *	4 (0x00000000)	reserved. currently unused
	 *
	 **/
	 
	// Get the last known Docking group and Docking code out of the Prefs file
	//sASLib->strcpy( groupName, kLayersPaletteDockGroup ); // is this a default value?

	char groupName[64];
	sAIPreference->GetStringPreference(name, "kADM_DPDockGroupStr", groupName );
	long positionCode = 0x00001c00; // Default: no dock, no tab group, front tab, zoom down, visible
	sAIPreference->GetIntegerPreference(name, "kADM_DPDockCodeStr", &positionCode );

	// Pick a default location in case it has never come up before on this machine
	ADMRect rect, dimensions, boundsRect;
	sADMDialog->GetBoundsRect( dlg, &boundsRect );
	sADMBasic->GetPaletteLayoutBounds( &dimensions );

	ADMPoint location, size;
	location.h = dimensions.right - (boundsRect.right - boundsRect.left);
	location.v = dimensions.bottom - (boundsRect.bottom - boundsRect.top);


	// Get the last known location out of the Prefs file
	sAIPreference->GetPointPreference(name, "kADM_DPLocationStr", &location );

	size.h = 208;	// minimum width (which governs the inner client rect) + 2
	//size.v = layerMinHeight;
	size.v = 258;
#ifdef WIN_ENV	// different rules about whether the borders and tabs are in the dlg rect
	size.v += 6;
	location.v -= 6;
	size.h += 4;
#endif
	// Get the last known size out of the Prefs file
	sAIPreference->GetPointPreference(name, "kADM_DPSizeStr", &size );
	rect.left = location.h;
	rect.right = location.h + size.h;
	rect.top = location.v;
	rect.bottom = location.v + size.v;

	// restore the size and location of the dialog
	sADMDialog->SetBoundsRect(dlg, &rect);
	// restore the position code of the dialog
	sADMDialogGroup->SetDialogGroupInfo(dlg, groupName, positionCode);
}


void dialogSavePreference(ADMDialogRef dlg, char *name) {
	// saving the palette position, tab/dock preference.
	char *groupName;
	long positionCode;
	sADMDialogGroup->GetDialogGroupInfo(
		dlg,
#ifdef kPluginInterfaceVersion10001
		(const char **)
#endif
		&groupName,
		&positionCode);

	ADMRect boundsRect;
	sADMDialog->GetBoundsRect( dlg, &boundsRect );

	ADMPoint location, size;
	location.h = boundsRect.left;
	location.v = boundsRect.top;
	size.h = boundsRect.right - boundsRect.left;
	size.v = boundsRect.bottom - boundsRect.top; 

	sAIPreference->PutIntegerPreference(name , "kADM_DPDockCodeStr", positionCode);
	sAIPreference->PutStringPreference(name, "kADM_DPDockGroupStr", groupName);
	sAIPreference->PutPointPreference(name, "kADM_DPLocationStr", &location);
	sAIPreference->PutPointPreference(name, "kADM_DPSizeStr", &size );
}

/*******************************************************************************
 **	Notification Procedures
 **/

void dialogSetEditFont(ADMDialogRef dlg, int editID) {
	ADMItemRef itemRef = sADMDialog->GetItem(dlg, editID);
	/*
#ifdef WIN_ENV
	SendMessage((HWND)sADMItem->GetWindowRef(itemRef), WM_SETFONT, (WPARAM)gPlugin->fFont, false);
#else
	*/
/*
	WindowRef ref = (WindowRef)sADMDialog->GetWindowRef(dlg);
	FSSpec spec;
	FSMakeFSSpec(0, 0, "\phierarchy.txt", &spec );
	DumpControlHierarchy(ref, &spec ); 

	Rect bounds;
	GetWindowBounds (ref, kWindowStructureRgn , &bounds);
	ControlRef ctrl, ctrl1;
	GetRootControl(ref, &ctrl); 
	UInt16 outNumChildren;
	CountSubControls (ctrl, &outNumChildren);
	GetIndexedSubControl (ctrl, 1, &ctrl1);
	CountSubControls (ctrl1, &outNumChildren);
	
	ControlFontStyleRec fontRec;

    fontRec.flags = kControlUseFontMask | kControlUseFaceMask | kControlAddToMetaFontMask | kControlUseJustMask;
    fontRec.style = bold;
    fontRec.just = teCenter;
    fontRec.font = kControlFontBigSystemFont;
    SetControlFontStyle(ctrl1, &fontRec);
    SetControlFontStyle(ctrl, &fontRec);

	ControlID id, id1;
	GetControlID (ctrl, &id);
	GetControlID (ctrl1, &id1);

  //	GetControlBounds(ctrl1, &bounds);
//	WindowGroupRef group = GetWindowGroup (ref);
#endif
*/
}

void dialogTextDlgNotify(ADMDialogRef dlg, ADMNotifierRef notifier, int editID) {
	sADMDialog->DefaultNotify(dlg, notifier);
	ADMItemRef itemRef = sADMDialog->GetItem(dlg, editID);
	dialogSetEditFont(dlg, editID);
	if (sADMNotifier->IsNotifierType(notifier, kADMWindowShowNotifier)) {
		char *data = (char *)sADMItem->GetUserData(itemRef);
		if (data != NULL) sADMItem->SetText(itemRef, data);
	} else if (sADMNotifier->IsNotifierType(notifier, kADMWindowHideNotifier)) {
		char *data = (char *)sADMItem->GetUserData(itemRef);
		if (data != NULL) free(data);
		data = getText(itemRef);
		sADMItem->SetUserData(itemRef, data);
	}
}

// resize 

void dialogResize(ADMItemRef item, ADMNotifierRef notifier, int topItemID, ADMItemRef *buttonItems, int buttomItemCount, bool isEditor) {
	sADMItem->DefaultNotify(item, notifier);

	if (sADMNotifier->IsNotifierType(notifier, kADMBoundsChangedNotifier)) {
		ADMDialogRef dlg = sADMItem->GetDialog(item);
		ADMItemRef topItemRef = sADMDialog->GetItem(dlg, topItemID);

		ADMRect dlgRect, topRect, rect;
		sADMItem->GetBoundsRect(topItemRef, &topRect);
		sADMDialog->GetBoundsRect( dlg, &dlgRect);
		sADMDialog->ScreenToLocalRect( dlg, &dlgRect);
#ifdef WIN_ENV
		topRect.bottom = dlgRect.bottom - 18;
		topRect.right = dlgRect.right - 3;
#elif MAC_ENV
		topRect.bottom = dlgRect.bottom - 15;
		topRect.right = dlgRect.right;
		if (isEditor) {
			topRect.bottom++;
			topRect.right++;
		}
#endif
		if (topItemRef) {
			sADMItem->SetBoundsRect(topItemRef, &topRect);

			// se wether the topItem is a hierarchyList:
			ADMHierarchyListRef listRef = sADMItem->GetHierarchyList(topItemRef);
			if (listRef) {
				sADMHierarchyList->GetEntryTextRect(listRef, &dlgRect);
				rect = dlgRect;
				rect.right = topRect.right;
				sADMHierarchyList->SetEntryTextRectRecursive(listRef, &rect);
				sADMHierarchyList->SetNonLeafEntryTextRectRecursive(listRef, &rect);
			}
			
			for (int i = 0; i < buttomItemCount; i++) {
				sADMItem->GetBoundsRect(buttonItems[i], &rect);
				int height = rect.bottom - rect.top;
				if (height > 16) height = 16;

				rect.bottom = topRect.bottom + 16 - 1;
#ifdef MAC_ENV
				if (isEditor) rect.bottom--;
#endif
				rect.top = rect.bottom - height;
				sADMItem->SetBoundsRect(buttonItems[i], &rect);
			}
			// stretch the title (only used in editorDialog) 
			ADMItemRef filenameRef = sADMDialog->GetItem(dlg, kEditorFileNameItem);
			if (filenameRef) 
			{
				sADMItem->GetBoundsRect(filenameRef, &rect);
				rect.right = topRect.right;
				sADMItem->SetBoundsRect(filenameRef, &rect);
			}
		}
	}
}

ASBoolean ASAPI dialogTextTrack(ADMItemRef item, ADMTrackerRef tracker) {
	ASBoolean doNotify = false;
	// this is a workaround for the problem on pc, where selections can't be overridden
	// by simply tiping some text. workaround: delete the selection, set new empty selection
	// then execute DefaultTrack. this is only needed when the selection is not already empty
	// and when no special key (as left, right, home, del, ...) is pressed:
	ADMAction action = sADMTracker->GetAction(tracker);
	ADMModifiers modifiers = sADMTracker->GetModifiers(tracker);
	if (action == kADMKeyStrokeAction) {
		long start, end;
		sADMItem->GetSelectionRange(item, &start, &end);

		ADMChar key = sADMTracker->GetVirtualKey(tracker);

		if (key == kADMTabKey) {
			// block indent support for both pc (\r\n) and mac (\r) in one algorithm... :)

			bool shift = modifiers & kADMShiftKeyDownModifier;

			long len;
			char *str = getText(item, &len), *text = NULL;
			int indentLen = strlen(INDENT);
			if (start == end) {
				if (!shift) {
					text = (char *)malloc(len + indentLen);
					strncpy(text, str, start);
					strncpy(&text[start], INDENT, indentLen);
					end += indentLen;
					strncpy(&text[end], &str[start], len - start);
					start = end;
				}
			} else {
				char ch;
				int breakLen = strlen(NEWLINE); // 2 on pc, 1 on mac
				// adjust the start and end marks for block indent:
				// start is moved to the beginning of the current line, without the
				// linebreak of the previous line
				// end is moved to the end of the current line, without the
				// linebreak of the next line
				while (start > 0 && str[start] != '\r') start--;
				if (str[start] == '\r') start += breakLen;
				if (end > 0 && ((ch = str[end - 1]) == '\r' || ch == '\n')) end -= breakLen;
				while (end < len && (ch = str[end]) != '\r') end++;

				// count the number of linebreaks to now how much space must be allocated 
				// in rightshift mode: (!shift)
				int num = 0, pos = start;
				while(pos < end) {
					if (str[pos++] == '\r') num++;
				}
				int textPos, lastPos, newLen, newEnd = end;
				if (shift) // chars are removed, so len is enough:
					newLen = len;
				else // allocate extra space for the inserted indents
					newLen = len + indentLen * (num + 1);

				text = (char *)malloc(newLen);
				pos = textPos = lastPos = start;
				// copy the beginning of str that is not modified:
				strncpy(text, str, start);
				while(pos < end) {
					if (shift) {
						// if it's shifted to the left, try to remove indentLen times a
						// space char. if there are less, remove those anyway...:
						for (num = 0; num < indentLen; num++) {
							if (str[num + pos] != ' ') break;
						}
						pos += num;
						newEnd -= num;
						lastPos += num;
					}
					// search the end of the current linebreak:
					while(pos < end && str[pos++] != '\r'){}
					// whe're now on the new line. insert the indent:
					if (!shift) {
						strncpy(&text[textPos], INDENT, indentLen);
						textPos += indentLen;
						newEnd += indentLen;
					}
					// search the end of the current line, without linebreak:
					while(pos <= end && ((ch = str[pos++]) == '\r' || ch == '\n')){}
					pos--;
					// and copy the whole line
					num = pos - lastPos;
					strncpy(&text[textPos], &str[lastPos], num);
					textPos += num;
					lastPos = pos;
				}
				// copy the end of str that is not modified:
				num = len - lastPos;
				if (num > 0) strncpy(&text[textPos], &str[lastPos], num);
				end = newEnd;
			}
			// now set the new text:
			free(str);
			if (text != NULL) {
				setText(item, text);
				sADMItem->SetSelectionRange(item, start, end);
				free(text);
			}
		} else if (start == end || !isNormalKey(key, modifiers) ||
			key == kADMBackspaceKey || key == kADMClearKey || key == kADMDeleteKey) {

			doNotify = sADMItem->DefaultTrack(item, tracker);
		} else {
#ifdef WIN_ENV
			// allow the overwriting of the selection: (seems to be disabled???)
			char *str = getText(item);
			strcpy(&str[start], &str[end]);

			setText(item, str);
			sADMItem->SetSelectionRange(item, start, start);

			free(str);
#endif
			doNotify = sADMItem->DefaultTrack(item, tracker);
		}
	} else if (action == kADMButtonUpAction && (modifiers & kADMDoubleClickModifier)) {
		long start, end, len;
		sADMItem->GetSelectionRange(item, &start, &end);
		char ch, *str = getText(item, &len);

		long initStart = start;
		while (start >= 0 && isdigit((ch = str[start])) || isalpha(ch)) start--;
		if (start < initStart) start++;
		while (end < len && isdigit((ch = str[end])) || isalpha(ch)) end++;
		free(str);
		sADMItem->SetSelectionRange(item, start, end);
	} else doNotify = sADMItem->DefaultTrack(item, tracker);

	return doNotify;
}

/*******************************************************************************
 ** Utility functions
 **/

ADMItemRef initPictureButton(ADMDialogRef dlg, int id, ADMItemNotifyProc notifyProc, int pictureId, ASBoolean enabled) {
	ADMItemRef itemRef = sADMDialog->GetItem(dlg, id);
	sADMItem->SetItemStyle(itemRef ,kADMBlackRectPictureButtonStyle);
	sADMItem->SetNotifyProc(itemRef, notifyProc);
	sADMItem->SetPictureID(itemRef, pictureId, NULL);
	sADMItem->Enable(itemRef, enabled);
	return itemRef;
}

char *getText(ADMItemRef itemRef, long *length) {
	long len = sADMItem->GetTextLength(itemRef) + 1;
	char *str = (char *)malloc(len);
	sADMItem->GetText(itemRef, str, len);
	if (length != NULL) *length = len;
	return str;
}

void setText(ADMItemRef itemRef, char *text) {
#ifdef WIN_ENV
	// correct the scrolling position, part 1
	HWND wnd = (HWND)sADMItem->GetWindowRef(itemRef);
	int pos = GetScrollPos(wnd, SB_VERT);
	SetWindowText(wnd, text);
	// correct the scrolling position, part 2
	SendMessage(wnd, WM_VSCROLL, MAKEWPARAM(SB_THUMBPOSITION, pos), NULL);
#else
	sADMItem->SetText(itemRef, text);
#endif
}

int getPopupID(ADMItemRef item) {
	ADMListRef listRef = sADMItem->GetList(item);	
	ADMEntryRef activeEntry = sADMList->GetActiveEntry(listRef);
	return sADMEntry->GetID(activeEntry);
}

void enableButtonPopupItem(ADMDialogRef dlg, ADMListRef menuListRef, int itemIndex, int itemID) {
	sADMEntry->Enable(sADMList->GetEntry(menuListRef, itemIndex), sADMItem->IsEnabled(sADMDialog->GetItem(dlg, itemID)));
}

bool isNormalKey(ADMChar key, ADMModifiers modifiers) {
	return !((key >= kADMHomeKey && key <= kADMPageDownKey) ||
		(key >= kADMF1Key && key <= kADMDownKey && key != kADMClearKey) ||
		key == kADMInsertKey ||
		modifiers & kMacCommandKeyDownModifier ||
		modifiers & kMacOptionKeyDownModifier ||
		modifiers & kMacControlKeyDownModifier);
}

bool isJavaScript(char *filename) {
	int len = strlen(filename);
	return (len > 3 && strncmp(&filename[len - 3], ".js", 3) == 0);
}
