@echo off

REM CS
copy "%~1\CS Release\Scriptographer.aip" Scriptographer
zip -r Scriptographer_Win_CS_%2.zip Scriptographer
del Scriptographer\Scriptographer.aip

REM CS2
copy "%~1\CS2 Release\Scriptographer.aip" Scriptographer
zip -r Scriptographer_Win_CS2_%2.zip Scriptographer
del Scriptographer\Scriptographer.aip

REM CS3
copy "%~1\CS3 Release\Scriptographer.aip" Scriptographer
zip -r Scriptographer_Win_CS3_%2.zip Scriptographer
del Scriptographer\Scriptographer.aip

REM CS4
copy "%~1\CS4 Release\Scriptographer.aip" Scriptographer
zip -r Scriptographer_Win_CS4_%2.zip Scriptographer
del Scriptographer\Scriptographer.aip
