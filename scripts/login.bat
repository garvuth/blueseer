@echo off

set mypath=%cd%

cd %~dp0

start jre8\bin\javaw -D"java.util.logging.config.file=bslogging.properties" -cp "dist\*" bsmf.MainFrame

