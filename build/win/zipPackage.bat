@echo off

copy "%~1\%2 Release\Scriptographer.aip" Scriptographer
zip -r Scriptographer_Win_%2_%3.zip Scriptographer
del Scriptographer\Scriptographer.aip
