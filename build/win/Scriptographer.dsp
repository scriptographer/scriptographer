# Microsoft Developer Studio Project File - Name="Scriptographer" - Package Owner=<4>
# Microsoft Developer Studio Generated Build File, Format Version 6.00
# ** DO NOT EDIT **

# TARGTYPE "Win32 (x86) Dynamic-Link Library" 0x0102

CFG=Scriptographer - Win32 Illustrator CS Debug
!MESSAGE This is not a valid makefile. To build this project using NMAKE,
!MESSAGE use the Export Makefile command and run
!MESSAGE 
!MESSAGE NMAKE /f "Scriptographer.mak".
!MESSAGE 
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "Scriptographer.mak" CFG="Scriptographer - Win32 Illustrator CS Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "Scriptographer - Win32 Illustrator 10 Release" (based on "Win32 (x86) Dynamic-Link Library")
!MESSAGE "Scriptographer - Win32 Illustrator 10 Debug" (based on "Win32 (x86) Dynamic-Link Library")
!MESSAGE "Scriptographer - Win32 Illustrator CS Release" (based on "Win32 (x86) Dynamic-Link Library")
!MESSAGE "Scriptographer - Win32 Illustrator CS Debug" (based on "Win32 (x86) Dynamic-Link Library")
!MESSAGE 

# Begin Project
# PROP AllowPerConfigDependencies 0
# PROP Scc_ProjName "Scriptographer"
# PROP Scc_LocalPath ".."
CPP=cl.exe
MTL=midl.exe
RSC=rc.exe

!IF  "$(CFG)" == "Scriptographer - Win32 Illustrator 10 Release"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 1
# PROP BASE Output_Dir "Illustrator_10_Release"
# PROP BASE Intermediate_Dir "Illustrator_10_Release"
# PROP BASE Ignore_Export_Lib 0
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 1
# PROP Output_Dir "Scriptographer_10_Release"
# PROP Intermediate_Dir "Scriptographer_10_Release"
# PROP Ignore_Export_Lib 0
# PROP Target_Dir ""
# ADD BASE CPP /nologo /MTd /Gi /GX /O1 /I "..\Common\js\\" /I "..\Common" /I "..\..\Illustrator 9.0 SDK\IllustratorAPI" /I "..\..\Illustrator 9.0 SDK\IllustratorAPI\ADM" /I "..\..\Illustrator 9.0 SDK\IllustratorAPI\Illustrator" /I "..\..\Illustrator 9.0 SDK\IllustratorAPI\General" /I "..\..\Illustrator 9.0 SDK\IllustratorAPI\PICA_SP" /I "..\..\Illustrator 9.0 SDK\IllustratorAPI\Undocumented" /I "..\..\Illustrator 9.0 SDK\IllustratorAPI\Legacy\v7" /D "WIN32" /D "_WINDOWS" /D "WIN_ENV" /D "XP_PC" /D "JSFILE" /D "EXPORT_JS_API" /D "_IEEE_LIBM" /D "JS_HAS_REGEXPS" /FR /YX"Common.h" /FD /c
# ADD CPP /nologo /MTd /Gi /GX /O1 /I "..\..\src\cpp\win" /I "..\..\src\cpp\jni" /I "..\..\src\cpp\plugin" /I "..\..\..\Illustrator 10 SDK\IllustratorAPI" /I "..\..\..\Illustrator 10 SDK\IllustratorAPI\ADM" /I "..\..\..\Illustrator 10 SDK\IllustratorAPI\Illustrator" /I "..\..\..\Illustrator 10 SDK\IllustratorAPI\General" /I "..\..\..\Illustrator 10 SDK\IllustratorAPI\PICA_SP" /I "..\..\..\Illustrator 10 SDK\IllustratorAPI\Illustrator\Legacy" /D "WIN32" /D "_WINDOWS" /D "WIN_ENV" /FR /YX"stdHeaders.h" /FD /c
# ADD BASE MTL /nologo /mktyplib203 /win32
# ADD MTL /nologo /mktyplib203 /win32
# ADD BASE RSC /l 0x409 /i "..\includes" /i ".\Win"
# ADD RSC /l 0x409
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib /nologo /subsystem:windows /dll /machine:I386 /out:"C:\Programme\Adobe\Illustrator 9.0\Plug-ins\Extensions\Scriptographer.aip"
# SUBTRACT BASE LINK32 /pdb:none /incremental:no /debug
# ADD LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib winmm.lib /nologo /subsystem:windows /dll /machine:I386 /out:"C:\Programme\Adobe\Illustrator 9.0\Plug-ins\Extensions\Scriptographer.aip"
# SUBTRACT LINK32 /pdb:none /incremental:no /debug

!ELSEIF  "$(CFG)" == "Scriptographer - Win32 Illustrator 10 Debug"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 1
# PROP BASE Output_Dir "Illustrator_10_Debug"
# PROP BASE Intermediate_Dir "Illustrator_10_Debug"
# PROP BASE Ignore_Export_Lib 0
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 1
# PROP Output_Dir "Scriptographer_10_Debug"
# PROP Intermediate_Dir "Scriptographer_10_Debug"
# PROP Ignore_Export_Lib 0
# PROP Target_Dir ""
# ADD BASE CPP /nologo /MTd /Gm /Gi /GX /Zi /Od /I "..\Common\js\\" /I "..\Common" /I "..\..\Illustrator 9.0 SDK\IllustratorAPI" /I "..\..\Illustrator 9.0 SDK\IllustratorAPI\ADM" /I "..\..\Illustrator 9.0 SDK\IllustratorAPI\Illustrator" /I "..\..\Illustrator 9.0 SDK\IllustratorAPI\General" /I "..\..\Illustrator 9.0 SDK\IllustratorAPI\PICA_SP" /I "..\..\Illustrator 9.0 SDK\IllustratorAPI\Undocumented" /I "..\..\Illustrator 9.0 SDK\IllustratorAPI\Legacy\v7" /D "_DEBUG" /D "WIN32" /D "_WINDOWS" /D "WIN_ENV" /D "XP_PC" /D "JSFILE" /D "EXPORT_JS_API" /D "_IEEE_LIBM" /D "JS_HAS_REGEXPS" /FR /YX"Common.h" /FD /c
# ADD CPP /nologo /MTd /Gm /Gi /GX /Zi /Od /I "..\..\src\cpp\win" /I "..\..\src\cpp\jni" /I "..\..\src\cpp\plugin" /I "..\..\..\Illustrator 10 SDK\IllustratorAPI" /I "..\..\..\Illustrator 10 SDK\IllustratorAPI\ADM" /I "..\..\..\Illustrator 10 SDK\IllustratorAPI\Illustrator" /I "..\..\..\Illustrator 10 SDK\IllustratorAPI\General" /I "..\..\..\Illustrator 10 SDK\IllustratorAPI\PICA_SP" /I "..\..\..\Illustrator 10 SDK\IllustratorAPI\Illustrator\Legacy" /D "_DEBUG" /D "WIN32" /D "_WINDOWS" /D "WIN_ENV" /FR /YX"stdHeaders.h" /FD /c
# ADD BASE MTL /nologo /D "_DEBUG" /mktyplib203 /win32
# ADD MTL /nologo /D "_DEBUG" /mktyplib203 /win32
# ADD BASE RSC /l 0x409 /i "..\includes" /i ".\Win" /d "_DEBUG"
# ADD RSC /l 0x409 /d "_DEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib /nologo /subsystem:windows /dll /debug /machine:I386 /out:"C:\Programme\Adobe\Illustrator 9.0\Plug-ins\Extensions\Scriptographer.aip"
# SUBTRACT BASE LINK32 /pdb:none /incremental:no
# ADD LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib winmm.lib /nologo /subsystem:windows /dll /debug /machine:I386 /out:"C:\Programme\Adobe\Illustrator 10\Plug-ins\Extensions\Scriptographer.aip"
# SUBTRACT LINK32 /pdb:none /incremental:no

!ELSEIF  "$(CFG)" == "Scriptographer - Win32 Illustrator CS Release"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 1
# PROP BASE Output_Dir "Scriptographer___Win32_Illustrator_CS_Release0"
# PROP BASE Intermediate_Dir "Scriptographer___Win32_Illustrator_CS_Release0"
# PROP BASE Ignore_Export_Lib 0
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 1
# PROP Output_Dir "Scriptographer_CS_Release"
# PROP Intermediate_Dir "Scriptographer_CS_Release"
# PROP Ignore_Export_Lib 0
# PROP Target_Dir ""
# ADD BASE CPP /nologo /MTd /Gi /GX /O1 /I "..\..\Illustrator 9.0 SDK\IllustratorAPI" /I "..\..\Illustrator 9.0 SDK\IllustratorAPI\ADM" /I "..\..\Illustrator 9.0 SDK\IllustratorAPI\Illustrator" /I "..\..\Illustrator 9.0 SDK\IllustratorAPI\General" /I "..\..\Illustrator 9.0 SDK\IllustratorAPI\PICA_SP" /I "..\..\Illustrator 9.0 SDK\IllustratorAPI\Undocumented" /I "..\..\Illustrator 9.0 SDK\IllustratorAPI\Legacy\v7" /I "..\Common\\" /I "..\Common\jsEngine\\" /I "..\Common\jsWrapper" /I "..\Common\plugin" /D "SERIALPORT" /D "WIN32" /D "_WINDOWS" /D "WIN_ENV" /D "XP_PC" /D "JSFILE" /D "EXPORT_JS_API" /D "_IEEE_LIBM" /D "JS_HAS_REGEXPS" /FR /YX"Common.h" /FD /c
# ADD CPP /nologo /MTd /Gi /GX /O1 /I "..\..\src\cpp\win" /I "..\..\src\cpp\jni" /I "..\..\src\cpp\plugin" /I "..\..\..\Illustrator CS SDK\IllustratorAPI" /I "..\..\..\Illustrator CS SDK\IllustratorAPI\ADM" /I "..\..\..\Illustrator CS SDK\IllustratorAPI\Illustrator" /I "..\..\..\Illustrator CS SDK\IllustratorAPI\General" /I "..\..\..\Illustrator CS SDK\IllustratorAPI\PICA_SP" /I "..\..\..\Illustrator CS SDK\IllustratorAPI\Illustrator\Legacy" /D "WIN32" /D "_WINDOWS" /D "WIN_ENV" /FR /YX"stdHeaders.h" /FD /c
# ADD BASE MTL /nologo /mktyplib203 /win32
# ADD MTL /nologo /mktyplib203 /win32
# ADD BASE RSC /l 0x409 /i "..\includes" /i ".\Win"
# ADD RSC /l 0x409
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib winmm.lib /nologo /subsystem:windows /dll /machine:I386 /out:"C:\Programme\Adobe\Illustrator 9.0\Plug-ins\Extensions\Scriptographer.aip"
# SUBTRACT BASE LINK32 /pdb:none /incremental:no /debug
# ADD LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib winmm.lib /nologo /subsystem:windows /dll /machine:I386 /out:"C:\Programme\Adobe\Illustrator CS\Plug-ins\Extensions\Scriptographer.aip"
# SUBTRACT LINK32 /pdb:none /incremental:no /debug

!ELSEIF  "$(CFG)" == "Scriptographer - Win32 Illustrator CS Debug"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 1
# PROP BASE Output_Dir "Scriptographer___Win32_Illustrator_CS_Debug"
# PROP BASE Intermediate_Dir "Scriptographer___Win32_Illustrator_CS_Debug"
# PROP BASE Ignore_Export_Lib 0
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 1
# PROP Output_Dir "Scriptographer_CS_Debug"
# PROP Intermediate_Dir "Scriptographer_CS_Debug"
# PROP Ignore_Export_Lib 0
# PROP Target_Dir ""
# ADD BASE CPP /nologo /MTd /Gm /Gi /GX /Zi /Od /I "..\..\Illustrator 10 SDK\IllustratorAPI" /I "..\..\Illustrator 10 SDK\IllustratorAPI\ADM" /I "..\..\Illustrator 10 SDK\IllustratorAPI\Illustrator" /I "..\..\Illustrator 10 SDK\IllustratorAPI\General" /I "..\..\Illustrator 10 SDK\IllustratorAPI\PICA_SP" /I "..\..\Illustrator 10 SDK\IllustratorAPI\Legacy\v7" /I "..\Common\\" /I "..\Common\jsEngine\\" /I "..\Common\jsWrapper" /I "..\Common\plugin" /D "SERIALPORT" /D "_DEBUG" /D "WIN32" /D "_WINDOWS" /D "WIN_ENV" /D "XP_PC" /D "JSFILE" /D "EXPORT_JS_API" /D "_IEEE_LIBM" /D "JS_HAS_REGEXPS" /FR /YX"Common.h" /FD /c
# ADD CPP /nologo /MTd /Gm /Gi /GX /Zi /Od /I "..\..\src\cpp\win" /I "..\..\src\cpp\jni" /I "..\..\src\cpp\plugin" /I "..\..\..\Illustrator CS SDK\IllustratorAPI" /I "..\..\..\Illustrator CS SDK\IllustratorAPI\ADM" /I "..\..\..\Illustrator CS SDK\IllustratorAPI\Illustrator" /I "..\..\..\Illustrator CS SDK\IllustratorAPI\General" /I "..\..\..\Illustrator CS SDK\IllustratorAPI\PICA_SP" /I "..\..\..\Illustrator CS SDK\IllustratorAPI\Illustrator\Legacy" /D "_DEBUG" /D "WIN32" /D "_WINDOWS" /D "WIN_ENV" /FR /YX"stdHeaders.h" /FD /c
# ADD BASE MTL /nologo /D "_DEBUG" /mktyplib203 /win32
# ADD MTL /nologo /D "_DEBUG" /mktyplib203 /win32
# SUBTRACT MTL /Oicf
# ADD BASE RSC /l 0x409 /i "..\includes" /i ".\Win" /d "_DEBUG"
# ADD RSC /l 0x409 /d "_DEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib winmm.lib /nologo /subsystem:windows /dll /debug /machine:I386 /out:"C:\Programme\Adobe\Illustrator 10\Plug-ins\Extensions\Scriptographer.aip"
# SUBTRACT BASE LINK32 /pdb:none /incremental:no
# ADD LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /subsystem:windows /dll /debug /machine:I386 /out:"C:\Programme\Adobe\Illustrator CS\Plug-ins\Extensions\Scriptographer\Scriptographer.aip"
# SUBTRACT LINK32 /pdb:none /incremental:no

!ENDIF 

# Begin Target

# Name "Scriptographer - Win32 Illustrator 10 Release"
# Name "Scriptographer - Win32 Illustrator 10 Debug"
# Name "Scriptographer - Win32 Illustrator CS Release"
# Name "Scriptographer - Win32 Illustrator CS Debug"
# Begin Group "Source"

# PROP Default_Filter "cpp;c;cxx;rc;def;r;odl;hpj;bat;for;f90"
# Begin Group "Java Source"

# PROP Default_Filter ""
# Begin Group "JNI Source"

# PROP Default_Filter ""
# Begin Group "ADM Source"

# PROP Default_Filter ""
# Begin Source File

SOURCE=..\..\src\cpp\jni\com_scriptographer_adm_Dialog.cpp
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\jni\com_scriptographer_adm_Drawer.cpp
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\jni\com_scriptographer_adm_HierarchyList.cpp
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\jni\com_scriptographer_adm_HierarchyListEntry.cpp
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\jni\com_scriptographer_adm_Image.cpp
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\jni\com_scriptographer_adm_Item.cpp
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\jni\com_scriptographer_adm_ItemGroup.cpp
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\jni\com_scriptographer_adm_ListEntry.cpp
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\jni\com_scriptographer_adm_ListItem.cpp
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\jni\com_scriptographer_adm_ModalDialog.cpp
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\jni\com_scriptographer_adm_PictureItem.cpp
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\jni\com_scriptographer_adm_TextEdit.cpp
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\jni\com_scriptographer_adm_TextItem.cpp
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\jni\com_scriptographer_adm_ToggleItem.cpp
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\jni\com_scriptographer_adm_Tracker.cpp
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\jni\com_scriptographer_adm_ValueItem.cpp
# End Source File
# End Group
# Begin Group "AI Source"

# PROP Default_Filter ""
# Begin Source File

SOURCE=..\..\src\cpp\jni\com_scriptographer_ai_Art.cpp
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\jni\com_scriptographer_ai_ArtSet.cpp
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\jni\com_scriptographer_ai_Color.cpp
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\jni\com_scriptographer_ai_Curve.cpp
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\jni\com_scriptographer_ai_Document.cpp
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\jni\com_scriptographer_ai_DocumentList.cpp
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\jni\com_scriptographer_ai_Group.cpp
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\jni\com_scriptographer_ai_Layer.cpp
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\jni\com_scriptographer_ai_LayerList.cpp
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\jni\com_scriptographer_ai_LiveEffect.cpp
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\jni\com_scriptographer_ai_MenuGroup.cpp
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\jni\com_scriptographer_ai_MenuItem.cpp
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\jni\com_scriptographer_ai_Path.cpp
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\jni\com_scriptographer_ai_Pathfinder.cpp
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\jni\com_scriptographer_ai_PathStyle.cpp
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\jni\com_scriptographer_ai_Raster.cpp
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\jni\com_scriptographer_ai_SegmentList.cpp
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\jni\com_scriptographer_ai_Tool.cpp
# End Source File
# End Group
# Begin Source File

SOURCE=..\..\src\cpp\jni\com_scriptographer_ScriptographerEngine.cpp
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\jni\com_scriptographer_Test.cpp
# End Source File
# End Group
# Begin Source File

SOURCE=..\..\src\cpp\jni\registerNatives.cpp
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\plugin\ScriptographerEngine.cpp
# End Source File
# End Group
# Begin Group "Plugin Source"

# PROP Default_Filter ""
# Begin Source File

SOURCE=..\..\src\cpp\plugin\exceptions.cpp
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\win\loadJava.cpp
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\plugin\Main.cpp
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\plugin\Plugin.cpp
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\plugin\Suites.cpp
# End Source File
# End Group
# End Group
# Begin Group "Headers"

# PROP Default_Filter "h;hpp;hxx;hm;inl;fi;fd"
# Begin Group "Java Headers"

# PROP Default_Filter ""
# Begin Source File

SOURCE=..\..\src\cpp\jni\admGlobals.h
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\jni\aiGlobals.h
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\plugin\jniMacros.h
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\plugin\ScriptographerEngine.h
# End Source File
# End Group
# Begin Group "Plugin Headers"

# PROP Default_Filter ""
# Begin Source File

SOURCE=..\..\src\cpp\plugin\exceptions.h
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\win\loadJava.h
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\plugin\Plugin.h
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\plugin\resourceIds.h
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\plugin\Suites.h
# End Source File
# End Group
# Begin Source File

SOURCE=..\..\src\cpp\plugin\stdHeaders.h
# End Source File
# End Group
# Begin Group "Resources"

# PROP Default_Filter "ico;cur;bmp;dlg;rc2;rct;bin;cnt;rtf;gif;jpg;jpeg;jpe"
# Begin Source File

SOURCE=..\..\src\cpp\win\PiPL.bin
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\win\plugin.rc
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\win\tool1.cur
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\win\tool1.ico
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\win\tool2.cur
# End Source File
# Begin Source File

SOURCE=..\..\src\cpp\win\tool2.ico
# End Source File
# End Group
# End Target
# End Project
