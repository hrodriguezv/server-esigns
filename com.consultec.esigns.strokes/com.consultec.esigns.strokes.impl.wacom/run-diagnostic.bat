@echo.
@setlocal
@set PROGRAM_FILES=%ProgramFiles(x86)%
@if "%ProgramFiles(x86)%32BIT"=="32BIT" (
  @set PROGRAM_FILES=%ProgramFiles%
)
@set JAR_PATHS="%PROGRAM_FILES%\Common Files\WacomGSS\flsx.jar;%PROGRAM_FILES%\Common Files\WacomGSS\wgssLicenceJNI.jar;"
@set DLL_PATHS="%PROGRAM_FILES%\Common Files\WacomGSS"
@
@echo java -classpath %JAR_PATHS% -Djava.library.path=%DLL_PATHS% -jar target/com.consultec.esigns.strokes.impl.wacom-1.0-SNAPSHOT.jar 
@java -classpath %JAR_PATHS% -Djava.library.path=%DLL_PATHS% -jar target/com.consultec.esigns.strokes.impl.wacom-1.0-SNAPSHOT.jar 

@goto END

:END
