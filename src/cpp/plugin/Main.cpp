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
 * $Revision: 1.2 $
 * $Date: 2005/03/10 22:48:43 $
 */
 
#include "stdHeaders.h"

#include "Plugin.h"

static bool unloaded = true;

DLLExport SPAPI SPErr PluginMain(char *caller, char *selector, void *message) {
	SPErr error = kNoErr;

	SPMessageData *msgData = static_cast<SPMessageData *>(message);
		
	Plugin *plugin = static_cast<Plugin *>(msgData->globals);
	
	bool shouldDelete = false;

	try {
		sSPBasic = msgData->basic;

		if (plugin != NULL && unloaded)
			throw((SPErr)kBadParameterErr);

		if (plugin == NULL)	{
			plugin = new Plugin(msgData->self);
			
			if (plugin != NULL)	{
				msgData->globals = plugin;
				unloaded = false;
			} else {
				error = kOutOfMemoryErr;
				throw((SPErr)error);
			}
		}

		error = plugin->handleMessage(caller, selector, message);
		if (error) throw((SPErr)error);

		if (plugin->isUnloadMsg(caller, selector))
			shouldDelete = true;
	} catch (SPErr inError) {
		error = inError;

		if (plugin != NULL && (plugin->isUnloadMsg(caller, selector) || 
		     				   plugin->isReloadMsg(caller, selector)))
			shouldDelete = true;
	} catch (...) {
		error = kBadParameterErr;

		if (plugin != NULL && (plugin->isUnloadMsg(caller, selector) || 
		     				   plugin->isReloadMsg(caller, selector)))
			shouldDelete = true;
	}

	shouldDelete = false;

	if (shouldDelete) {
		delete plugin;
		msgData->globals = plugin = NULL;
		unloaded = true;
	}

	return error;
}
