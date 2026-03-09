@REM
@REM Copyright © 2016-2026 The Thingsboard Authors
@REM
@REM Licensed under the Apache License, Version 2.0 (the "License");
@REM you may not use this file except in compliance with the License.
@REM You may obtain a copy of the License at
@REM
@REM     http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing, software
@REM distributed under the License is distributed on an "AS IS" BASIS,
@REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM See the License for the specific language governing permissions and
@REM limitations under the License.
@REM

@ECHO OFF

setlocal ENABLEEXTENSIONS

@ECHO Upgrading ${pkg.name} ...

SET BASE=%~dp0

:loop
IF NOT "%1"=="" (
    IF "%1"=="--fromVersion" (
        SET fromVersion=%2
    )
    SHIFT
    GOTO :loop
)

SET LOADER_PATH=%BASE%\conf,%BASE%\extensions
SET SQL_DATA_FOLDER=%BASE%\data\sql
SET jarfile=%BASE%\lib\${pkg.name}.jar
SET installDir=%BASE%\data

PUSHD "%BASE%\conf"

java -cp "%jarfile%" -Dloader.main=org.thingsboard.server.ThingsboardInstallApplication^
                    -Dinstall.data_dir="%installDir%"^
                    -Dspring.jpa.hibernate.ddl-auto=none^
                    -Dinstall.upgrade=true^
                    -Dinstall.upgrade.from_version=%fromVersion%^
                    -Dlogging.config="%BASE%\install\logback.xml"^
                    org.springframework.boot.loader.launch.PropertiesLauncher

if errorlevel 1 (
   @echo ThingsBoard upgrade failed!
   POPD
   exit /b %errorlevel%
)
POPD

@ECHO ThingsBoard upgraded successfully!

GOTO END

:END
