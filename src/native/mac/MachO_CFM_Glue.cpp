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
