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
 * $RCSfile: consoleDialog.h,v $
 * $Author: lehni $
 * $Revision: 1.1 $
 * $Date: 2005/02/23 22:00:59 $
 */

ASErr ASAPI consoleDlgInit(ADMDialogRef dlg);
static void ASAPI consoleDlgDestroy(ADMDialogRef dlg);
static void ASAPI consoleDlgNotify(ADMDialogRef dialog, ADMNotifierRef notifier);
static void ASAPI consoleResize( ADMItemRef item, ADMNotifierRef notifier);
static ASBoolean ASAPI consoleTrack(ADMItemRef item, ADMTrackerRef tracker);

void consoleShow(bool show);
bool consoleVisible();
void consoleShowText(char **strings, int numStrings);
void consoleShowText(char *string);
