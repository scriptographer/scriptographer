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
 * $RCSfile: admHandler.h,v $
 * $Author: lehni $
 * $Revision: 1.1 $
 * $Date: 2005/02/23 22:00:59 $
 */

// private functions
AIErr createADMDialog( AINotifierMessage *message );

void dialogLoadPreference(ADMDialogRef dlg, char *name);
void dialogSavePreference(ADMDialogRef dlg, char *name);
void dialogResize(ADMItemRef item, ADMNotifierRef notifier, int topItemID, ADMItemRef *buttonItems, int buttomItemCount, bool isEditor = false);
void dialogTextDlgNotify(ADMDialogRef dlg, ADMNotifierRef notifier, int editID);
ASBoolean ASAPI dialogTextTrack(ADMItemRef item, ADMTrackerRef tracker);
void dialogSetEditFont(ADMDialogRef dlg, int editID);

ADMItemRef initPictureButton(ADMDialogRef dlg, int id, ADMItemNotifyProc notifyProc, int pictureId, ASBoolean enabled = true);
char *getText(ADMItemRef itemRef, long *length = NULL);
void setText(ADMItemRef itemRef, char *text);
int getPopupID( ADMItemRef item );
void enableButtonPopupItem(ADMDialogRef dlg, ADMListRef menuListRef, int itemIndex, int itemID);
bool isNormalKey(ADMChar key, ADMModifiers modifiers);
bool isJavaScript(char *filename);

#define layerMinWidth  	250
#define layerMaxWidth	1600

#define layerMinHeight	64
#define layerLineHeight 16

#define INDENT "    "

#ifdef WIN_ENV

#define PATH_SEPARATOR_CHR '\\'
#define PATH_SEPARATOR_STR "\\"
#define NEWLINE "\r\n"

#elif MAC_ENV

#define PATH_SEPARATOR_CHR ':'
#define PATH_SEPARATOR_STR ":"
#define NEWLINE "\r"

#endif


