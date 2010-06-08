@echo off

if not exist "%3" (
	mkdir "%3"
)


copy "%~1\%2 Release\Scriptographer.aip" Scriptographer
zip -r %3\Scriptographer_Win_%2_%3.zip Scriptographer
del Scriptographer\Scriptographer.aip
