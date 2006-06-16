@echo off

REM CS
copy "%~1\Scriptographer_CS_Release\Scriptographer.aip" Scriptographer
zip -r Scriptographer_Win_CS_%2.zip Scriptographer
del Scriptographer\Scriptographer.aip

REM CS2
copy "%~1\Scriptographer_CS2_Release\Scriptographer.aip" Scriptographer
zip -r Scriptographer_Win_CS2_%2.zip Scriptographer
del Scriptographer\Scriptographer.aip
