pushd %2
REM copy plugin binary
copy "%~1\Scriptographer_%4_Release\Scriptographer.aip" Scriptographer
REM zip it
zip -r Scriptographer_Win_%4_%3.zip Scriptographer
REM delete binary again
del Scriptographer\Scriptographer.aip
popd