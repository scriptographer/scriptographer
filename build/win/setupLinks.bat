@echo off

set BASE=..\..
set JAVA=%BASE%\build\java\build\
set JAVASCRIPT=%BASE%\src\js\core\
set REFERENCE=%BASE%\docs\js\

call :makeLinks CS
call :makeLinks CS2
call :makeLinks CS3
call :makeLinks CS4
call :makeLinks CS5

goto:eof

:makeLink
echo "Linking %2 -> %1"
if exist %2 (
	junction.exe -d %2
)
junction.exe %2 %1
goto:eof

:makeTargetLinks
if not exist "%1 %2" (
	mkdir "%1 %2"
)
if not exist "%1 %2\Core" (
	mkdir "%1 %2\Core"
)
call :makeLink %JAVA% "%1 %2\Core\Java"
call :makeLink %JAVASCRIPT% "%1 %2\Core\JavaScript"
call :makeLink %REFERENCE% "%1 %2\Reference"
goto:eof

:makeLinks
call :makeTargetLinks %1 Debug
call :makeTargetLinks %1 Release
GOTO:EOF
