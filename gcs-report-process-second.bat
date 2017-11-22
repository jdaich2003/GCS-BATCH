@echo off
if '%1=='## goto ENVSET
SET APPHOME=%~dp0
SET LIBDIR=%APPHOME%lib
rem echo %LIBDIR%
SET CLSPATH=%APPHOME%bin
FOR %%c in (%LIBDIR%\*.jar) DO Call %0 ## %%c
echo %CLSPATH%
rem echo %0
goto RUN
:RUN
java -cp %CLSPATH% boc.gcs.batch.report.exe.DownAndUpReport2
goto END
:ENVSET
set CLSPATH=%CLSPATH%;%2
goto END
:END