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
 * $RCSfile: mainDialog.h,v $
 * $Author: lehni $
 * $Revision: 1.1 $
 * $Date: 2005/02/23 22:00:59 $
 */

ASErr ASAPI mainDlgInit(ADMDialogRef dlg);
static void ASAPI mainDlgDestroy(ADMDialogRef dlg);
static void ASAPI mainDlgNotify(ADMDialogRef dialog, ADMNotifierRef notifier);
static void ASAPI mainResize( ADMItemRef item, ADMNotifierRef notifier);
static void ASAPI mainPopupMenuNotify( ADMItemRef item, ADMNotifierRef notifier);
static ASBoolean ASAPI mainPopupMenuTrack( ADMItemRef item, ADMTrackerRef tracker);
static ASBoolean ASAPI mainScriptListTrack( ADMListEntryRef entry, ADMTrackerRef tracker);
static void ASAPI mainScriptListNotify( ADMListEntryRef entry, ADMNotifierRef notifier);
static void ASAPI mainPlayNotify( ADMItemRef item, ADMNotifierRef notifier);
static void ASAPI mainStopNotify( ADMItemRef item, ADMNotifierRef notifier);
static void ASAPI mainRefreshNotify( ADMItemRef item, ADMNotifierRef notifier);
static void ASAPI mainEditorNotify( ADMItemRef item, ADMNotifierRef notifier);
static void ASAPI mainConsoleNotify( ADMItemRef item, ADMNotifierRef notifier);
static void ASAPI mainBaseDirNotify( ADMItemRef item, ADMNotifierRef notifier);
static void ASAPI mainTool1Notify( ADMItemRef item, ADMNotifierRef notifier);
static void ASAPI mainTool2Notify( ADMItemRef item, ADMNotifierRef notifier);

void mainShow(bool show);
bool mainVisible();
void mainRemoveFiles(ADMHierarchyListRef listRef);
void mainRefreshFiles(ADMDialogRef dlg = NULL);
void mainChooseBaseDir(ADMDialogRef dlg = NULL);
