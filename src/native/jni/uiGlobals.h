/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Scripting Plugin for Adobe Illustrator
 * http://scriptographer.org/
 *
 * Copyright (c) 2002-2010, Juerg Lehni
 * http://scratchdisk.com/
 *
 * All rights reserved. See LICENSE file for details.
 */
#ifndef ADM_FREE
ASErr ASAPI Dialog_onInit(ADMDialogRef dialog);
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

ASErr ASAPI ListItem_onInit(ADMListRef lst);
void ASAPI ListItem_onDestroy(ADMListRef lst);

ASErr ASAPI HierarchyListBox_onInit(ADMHierarchyListRef list);
void ASAPI HierarchyListBox_onDestroy(ADMHierarchyListRef list);

void ASAPI ListEntry_onDestroy(ADMEntryRef entry);
void ASAPI ListEntry_onNotify(ADMEntryRef entry, ADMNotifierRef notifier);
ASBoolean ASAPI ListEntry_onTrack(ADMEntryRef entry, ADMTrackerRef tracker);
void ASAPI ListEntry_onDraw(ADMEntryRef entry, ADMDrawerRef drawer);

void ASAPI HierarchyListEntry_onDestroy(ADMListEntryRef entry);
void ASAPI HierarchyListEntry_onNotify(ADMListEntryRef entry, ADMNotifierRef notifier);
ASBoolean ASAPI HierarchyListEntry_onTrack(ADMListEntryRef entry, ADMTrackerRef tracker);
void ASAPI HierarchyListEntry_onDraw(ADMListEntryRef entry, ADMDrawerRef drawer);

// Pseudo notifiers: 
#define kADMInitializeNotifier "ADM Initialize Notifier"
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

#else !ADM

// Pseudo notifiers: 
#define kWidgetInitializeNotifier "Widget Initialize Notifier"
#define kWidgetDestroyNotifier "Widget Destroy Notifier"


#endif //#ifndef ADM_FREE