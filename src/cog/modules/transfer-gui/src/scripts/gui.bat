@echo off

if "%GLOBUS_LOCATION%" == "" goto nogl
goto run

:nogl

		echo Error: GLOBUS_LOCATION not set
		goto end

:run

		set _RUNJAVA=java
		if not "%JAVA_HOME%" == "" set _RUNJAVA="%JAVA_HOME%\bin\java"
		%_RUNJAVA% -DGLOBUS_LOCATION="%GLOBUS_LOCATION%" -Daxis.ClientConfigFile="%GLOBUS_LOCATION%"\client-config.wsdd -jar gui.jar
		
:end

	