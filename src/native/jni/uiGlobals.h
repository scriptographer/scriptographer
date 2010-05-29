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

ASErr ASAPI Dialog_onInit(ADMDialogRef dialog);
ADMBoolean ADMAPI Dialog_onInitialize(ADMDialogRef dialog, ADMTimerRef timerID);
void ASAPI Dialog_onDestroy(ADMDialogRef dialog);
void ASAPI Dialog_onSizeChanged(ADMItemRef item, ADMNotifierRef notifier); 
void ASAPI Dialog_onNotify(ADMDialogRef dialog, ADMNotifierRef notifier);
ASBoolean ASAPI Dialog_onTrack(ADMDialogRef dialog, ADMTrackerRef tracker);
void ASAPI Dialog_onDraw(ADMDialogRef dialog, ADMDrawerRef drawer);

ASErr ASAPI Item_onInit(ADMItemRef item);
void ASAPI Item_onDestroy(ADMItemRef item);
void ASAPI Item_onNotify(ADMItemRef item, ADMNotifierRef notifier);
ASBoolean ASAPI Item_onTrack(ADMItemRef item, ADMTrackerRef tracker);
void ASAPI Item_onDraw(ADMItemRef item, ADMDrawerRef drawer);

ASErr ASAPI List_onInit(ADMListRef lst);
void ASAPI List_onDestroy(ADMListRef lst);

ASErr ASAPI HierarchyList_onInit(ADMHierarchyListRef list);
void ASAPI HierarchyList_onDestroy(ADMHierarchyListRef list);

void ASAPI ListEntry_onDestroy(ADMEntryRef entry);
void ASAPI ListEntry_onNotify(ADMEntryRef entry, ADMNotifierRef notifier);
ASBoolean ASAPI ListEntry_onTrack(ADMEntryRef entry, ADMTrackerRef tracker);
void ASAPI ListEntry_onDraw(ADMEntryRef entry, ADMDrawerRef drawer);

void ASAPI HierarchyListEntry_onDestroy(ADMListEntryRef entry);
void ASAPI HierarchyListEntry_onNotify(ADMListEntryRef entry, ADMNotifierRef notifier);
ASBoolean ASAPI HierarchyListEntry_onTrack(ADMListEntryRef entry, ADMTrackerRef tracker);
void ASAPI HierarchyListEntry_onDraw(ADMListEntryRef entry, ADMDrawerRef drawer);

// Pseudo notifiers: 
#define kADMInitializeWindowNotifier "ADM Initialize Window Notifier"
#define kADMDestroyNotifier "ADM Destroy Notifier"

#define DEFINE_ADM_POINT(PT, X, Y) \
	ADMPoint PT; \
	PT.h = (short) X; \
	PT.v = (short) Y;


#define DEFINE_ADM_RECT(RT, X, Y, WIDTH, HEIGHT) \
	ADMRect RT; \
	RT.left = (short) X; \
	RT.top  = (short) Y; \
	RT.right =  (short) (X + WIDTH); \
	RT.bottom = (short) (Y + HEIGHT);
