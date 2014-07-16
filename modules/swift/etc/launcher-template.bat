@echo off

if "X%SWIFT_HOME%" == "X" goto nocogpath
goto cogpath

:nocogpath
    set UNSET_SWIFT_HOME=1
    set SWIFT_HOME=..
    if NOT EXIST "%SWIFT_HOME%\lib\@jar@" goto nocogpath15
    goto cogpath
	
:nocogpath15
	if NOT EXIST "%SWIFT_HOME%\lib\cog.jar" goto nocogpath2
	goto cogpath

:nocogpath2
    rem test for expansion extensions first, so that we don't enter an infinite loop
    set PARTIAL_PATH=test
    set PARTIAL_PATH=%PARTIAL_PATH:~-2%
    if NOT "%PARTIAL_PATH%" == "st" goto nocogpath3
    set PARTIAL_PATH=%~f0
    set FIRST_BACKSLASH=1
    
:loop
    set LAST_CHAR=%PARTIAL_PATH:~-1%
    set PARTIAL_PATH=%PARTIAL_PATH:~0,-1%
    if "%LAST_CHAR%" == "\" goto found
    if "X%PARTIAL_PATH%" == "X" goto nocogpath3
    goto loop

    
:found
    if "%FIRST_BACKSLASH%" == "0" goto found2
    set FIRST_BACKSLASH=0
    goto loop

:found2
    set SWIFT_HOME=%PARTIAL_PATH%
    if NOT EXIST "%SWIFT_HOME%\lib\@jar@" goto nocogpath25
    goto cogpath
	
:nocogpath25
	if NOT EXIST "%SWIFT_HOME%\lib\cog.jar" goto nocogpath3
	goto cogpath
    
:nocogpath3
    echo Error: SWIFT_HOME not set and all attempts at guessing it failed
    goto end

:cogpath

	set OPTS=-DCOG_INSTALL_PATH="%SWIFT_HOME%" -Dswift.home="%SWIFT_HOME%"%COG_OPTS% -Djava.endorsed.dirs="%SWIFT_HOME%\lib\endorsed"

	set LOCALCLASSPATH=%CLASSPATH%;%SWIFT_HOME%\etc;%SWIFT_HOME%\libexec
	
	for %%J in ("%SWIFT_HOME%\lib\*.jar") DO call :setpath %%J
    
    set SAVECLASSPATH=%CLASSPATH%
    set CLASSPATH=%LOCALCLASSPATH%
    java %OPTS% @class@ %*
    set CLASSPATH=%SAVECLASSPATH%

:end
    if "%UNSET_SWIFT_HOME%"=="1" goto restore
    goto done
    
:setpath
    set LOCALCLASSPATH=%LOCALCLASSPATH%;%*
    goto done

:restore
    set SWIFT_HOME=
    set PARTIAL_PATH=
    set LAST_CHAR=
    set UNSET_SWIFT_HOME=
    set FIRST_BAKSLASH=

:done
