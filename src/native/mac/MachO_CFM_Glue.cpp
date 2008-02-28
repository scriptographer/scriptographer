/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2008 Juerg Lehni, http://www.scratchdisk.com.
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
 * $Id$
 */

#include "MachO_CFM_Glue.h"

/*
	FILE:		MachOCFMGlue.c
	
	PURPOSE:	Code that does the magic needed to call MachO functions
				from CFM and CFM functions from Carbon.

	COPYRIGHT:	Copyright (c) 2002 by M. Uli Kusterer, all rights reserved.
				Thanks to George Warner, Chris Silverberg and Ricky Sharp for
				clues on how to do this and implementation snippets.
*/

#include <stdlib.h>
#include <MacMemory.h>

void *createCFMGlue(void* machoProc, TVector *vector) {
	if (vector == NULL)
		vector = new TVector;
    
    if (vector != NULL) {
        vector->procPtr = (ProcPtr) machoProc;
        vector->toc = 0;  // ignored
    }
	
	return vector;
}

void disposeCFMGlue(void* cfmProc) {
    if (cfmProc)
		delete (TVector *) cfmProc;
}

void *createMachOGlue(void* cfmProc, Ptr machoProc) {
	static const UInt32 glueTemplate[6] = { 0x3D800000, 0x618C0000, 0x800C0000, 0x804C0004, 0x7C0903A6, 0x4E800420 };
	if (machoProc == NULL)
		machoProc = NewPtr(sizeof(glueTemplate));
	memcpy(machoProc, &glueTemplate, sizeof(glueTemplate));
	((UInt32 *) machoProc)[0] |= ((UInt32) cfmProc >> 16);
	((UInt32 *) machoProc)[1] |= ((UInt32) cfmProc & 0xFFFF);
	MakeDataExecutable(machoProc, sizeof(glueTemplate));
	return machoProc;
}

void disposeMachOGlue(void *machoProc) {
    if (machoProc)
		DisposePtr((Ptr) machoProc);
}
