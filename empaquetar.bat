@echo off
setlocal EnableExtensions DisableDelayedExpansion
set "DIRECTORIO_INICIAL=%CD%"

for %%S in (auth catalogo ordenes arriendos api-gateway) do (
    call :empaquetar "%%S"
    if errorlevel 1 goto :error
)

cd /d "%DIRECTORIO_INICIAL%"
echo.
echo Empaquetado completado. Los JAR estan en el target de cada servicio.
endlocal
exit /b 0

:empaquetar
set "SERVICIO_ACTUAL=%~1"
echo.
echo Limpiando y empaquetando %SERVICIO_ACTUAL%...
cd /d "%~dp0%SERVICIO_ACTUAL%" || exit /b 1
if not exist "mvnw.cmd" (
    echo ERROR: No se encontro mvnw.cmd en %SERVICIO_ACTUAL%.
    exit /b 1
)
if not exist "pom.xml" (
    echo ERROR: No se encontro pom.xml en %SERVICIO_ACTUAL%.
    exit /b 1
)
call mvnw.cmd clean package -Dmaven.test.skip=true
exit /b %errorlevel%

:error
set "CODIGO_ERROR=%errorlevel%"
echo.
echo ERROR: Fallo el empaquetado de %SERVICIO_ACTUAL%. No se continuara con los demas servicios.
cd /d "%DIRECTORIO_INICIAL%"
endlocal & exit /b %CODIGO_ERROR%
