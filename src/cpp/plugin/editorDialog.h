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
 * $RCSfile: editorDialog.h,v $
 * $Author: lehni $
 * $Revision: 1.1 $
 * $Date: 2005/02/23 22:00:59 $
 */

ASErr ASAPI editorDlgInit(ADMDialogRef dlg);
static void ASAPI editorDlgDestroy(ADMDialogRef dlg);
static void ASAPI editorDlgNotify(ADMDialogRef dialog, ADMNotifierRef notifier);
static void ASAPI editorResize( ADMItemRef item, ADMNotifierRef notifier);
static void ASAPI editorPopupMenuNotify( ADMItemRef item, ADMNotifierRef notifier);
static ASBoolean ASAPI editorPopupMenuTrack( ADMItemRef item, ADMTrackerRef tracker);
static void ASAPI editorPlayNotify( ADMItemRef item, ADMNotifierRef notifier);
static void ASAPI editorStopNotify( ADMItemRef item, ADMNotifierRef notifier);
static void ASAPI editorSaveNotify( ADMItemRef item, ADMNotifierRef notifier);
static void ASAPI editorNewNotify( ADMItemRef item, ADMNotifierRef notifier);
static ASBoolean ASAPI editorTrack(ADMItemRef item, ADMTrackerRef tracker);

void editorShow(bool show);
bool editorVisible();
void editorSetFileName(char *filename);
void editorSetModified(bool modified);
void editorOpen(ADMListEntryRef entry);
void editorSave(char *filename, char *text = NULL);
bool editorCheckModified(bool cancel);
