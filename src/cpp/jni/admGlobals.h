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
 * $RCSfile: admGlobals.h,v $
 * $Author: lehni $
 * $Revision: 1.3 $
 * $Date: 2005/03/10 22:48:43 $
 */

ASErr ASAPI callbackDialogInit(ADMDialogRef dialog);
void ASAPI callbackDialogDestroy(ADMDialogRef dialog);
void ASAPI callbackDialogResize(ADMItemRef item, ADMNotifierRef notifier); 
void ASAPI callbackDialogNotify(ADMDialogRef dialog, ADMNotifierRef notifier);
ASBoolean ASAPI callbackDialogTrack(ADMDialogRef dialog, ADMTrackerRef tracker);
void ASAPI callbackDialogDraw(ADMDialogRef dialog, ADMDrawerRef drawer);

ASErr ASAPI callbackHierarchyListInit(ADMHierarchyListRef lst);

ASErr ASAPI callbackItemInit(ADMItemRef item);
void ASAPI callbackItemDestroy(ADMItemRef item);
void ASAPI callbackItemNotify(ADMItemRef item, ADMNotifierRef notifier);
ASBoolean ASAPI callbackItemTrack(ADMItemRef item, ADMTrackerRef tracker);
void ASAPI callbackItemDraw(ADMItemRef item, ADMDrawerRef drawer);

ASErr ASAPI callbackListInit(ADMListRef lst);
void ASAPI callbackListDestroy(ADMListRef lst);

ASErr ASAPI callbackHierarchyListInit(ADMHierarchyListRef list);
void ASAPI callbackHierarchyListDestroy(ADMHierarchyListRef list);

ASErr ASAPI callbackHierarchyListInit(ADMHierarchyListRef lst);
void ASAPI callbackHierarchyListDestroy(ADMHierarchyListRef lst);

void ASAPI callbackListEntryDestroy(ADMEntryRef entry);
void ASAPI callbackListEntryNotify(ADMEntryRef entry, ADMNotifierRef notifier);
ASBoolean ASAPI callbackListEntryTrack(ADMEntryRef entry, ADMTrackerRef tracker);
void ASAPI callbackListEntryDraw(ADMEntryRef entry, ADMDrawerRef drawer);

void ASAPI callbackHierarchyListEntryDestroy(ADMListEntryRef entry);
void ASAPI callbackHierarchyListEntryNotify(ADMListEntryRef entry, ADMNotifierRef notifier);
ASBoolean ASAPI callbackHierarchyListEntryTrack(ADMListEntryRef entry, ADMTrackerRef tracker);
void ASAPI callbackHierarchyListEntryDraw(ADMListEntryRef entry, ADMDrawerRef drawer);

#define DEFINE_ADM_POINT(PT, X, Y) \
	ADMPoint PT; \
	PT.h = X; \
	PT.v  =Y;


#define DEFINE_ADM_RECT(RT, X, Y, WIDTH, HEIGHT) \
	ADMRect RT; \
	RT.left = X; \
	RT.top  = Y; \
	RT.right =  X + WIDTH; \
	RT.bottom = Y + HEIGHT;
