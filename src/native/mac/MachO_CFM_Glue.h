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

#include <MacTypes.h>

typedef struct {
	ProcPtr procPtr;
	UInt32 toc;
} TVector;

void *createCFMGlue(void* machoProc, TVector *vector = NULL);
void disposeCFMGlue(void* cfmProc);

void *createMachOGlue(void* cfmProc, Ptr machoProc = NULL);
void disposeMachOGlue(void *machoProc);
