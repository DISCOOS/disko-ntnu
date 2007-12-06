@echo off

REM	Batch file for sara api demo


set SARADIR=.
call %SARADIR%\clientclasspath.cmd
set PROPS=http://saraserver/sara

java  -Xmx512m -Ddebug -Dproperty.server=%PROPS% org.rescuenorway.saraccess.SaraEvtDialog
