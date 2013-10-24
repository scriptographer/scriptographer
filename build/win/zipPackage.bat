@echo off

if not exist "%3" (
	mkdir "%3"
)


copy "%~1\%2 Debug\Scriptographer.aip" Scriptographer
rem "a" arg is added for 7-zip
zip a -r %3\Scriptographer_Win_%2_%3.zip Scriptographer
del Scriptographer\Scriptographer.aip
