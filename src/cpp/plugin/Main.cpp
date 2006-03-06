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
 * $RCSfile: Main.cpp,v $
 * $Author: lehni $
 * $Revision: 1.3 $
 * $Date: 2006/03/06 15:32:47 $
 */
 
#include "stdHeaders.h"

#include "ScriptographerPlugin.h"

static bool loaded = false;

DLLExport SPAPI SPErr PluginMain(char *caller, char *selector, void *message) {
	SPErr error = kNoErr;

	SPMessageData *msgData = static_cast<SPMessageData *>(message);
	ScriptographerPlugin *plugin = static_cast<ScriptographerPlugin *>(msgData->globals);
	sSPBasic = msgData->basic;

	bool remove = false;

	if (plugin != NULL && !loaded) {
		plugin->log("Plugin object is created, but the loaded flag is not set.");
		error = kBadParameterErr;
	} else {
		if (plugin == NULL) {
			plugin = new ScriptographerPlugin(msgData->self);
			if (plugin != NULL)	{
				msgData->globals = plugin;
				loaded = true;
			} else {
				error = kOutOfMemoryErr;
			}
		}
		if (plugin != NULL)
			error = plugin->handleMessage(caller, selector, message);
	}
	
	if (error == kUnloadErr) {
		remove = true;
		error = kNoErr;
	}

	if (remove) {
		delete plugin;
		msgData->globals = NULL;
		loaded = false;
	}

	return error;
}
