package org.koala.sbt

object SbtDistAppShell {

  val windows =
    """|@echo off
      |setlocal enabledelayedexpansion

      |for %%? in ("%~dp0..") do set APP_HOME=%%~f?

      |for %%a in ("%APP_HOME%\lib\*.jar") do set APP_CP=!APP_CP!%%a;

      |java -cp %APP_CP% ${mainClass} %*
      |@echo on""".stripMargin

  val linux =
    """|#!/bin/sh
      |PRG="$0"

      |APP_HOME=`dirname "$PRG"`

      |APP_HOME=`cd "$APP_HOME"/.. ; pwd`

      |APP_CP=`ls "$APP_HOME"/lib/*.jar | sed ':a;N;$!ba;s/\\n/:/g'`

      |java -cp $APP_CP ${mainClass} $*""".stripMargin.replaceAll("\\r\\n", "\n")
}
