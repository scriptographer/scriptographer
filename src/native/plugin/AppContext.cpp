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

#include "stdHeaders.h"
#include "ScriptographerPlugin.h"
#include "AppContext.h"

AppContext::AppContext() {
	m_appContext = NULL;
	if (sAIAppContext)
		sAIAppContext->PushAppContext(gPlugin->getPluginRef(), &m_appContext);
}

AppContext::~AppContext() {
	if (sAIAppContext)
		sAIAppContext->PopAppContext(m_appContext);
}
