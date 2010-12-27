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

#ifdef DEBUG
#define _DEBUG 1
#define LOGFILE
#endif

#define MAC_ENV 1

#ifndef macintosh
#define macintosh 1
#endif

#define Platform_Carbon 1

#include "stdHeaders.h"
