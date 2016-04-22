package org.koala.sbt

import sbt._

object SbtDistAppShell {

  def windows(f: File, libs: Iterable[String], mainClass: String): (String, File) = {
    val libStr = libs.map("%LIB_PATH%/" + _).mkString(";")
    val cmd =
      s"""|@echo off
          |@rem set JAVA_OPTS
          |
          |set LIB_PATH=lib
          |
          |set APP_CP=${libStr}
          |
          |java %JAVA_OPTS% -cp %APP_CP% ${mainClass} %*""".stripMargin.replaceAll("\\r\\n", "\n")
    writeToFile(f) { w => w.write(cmd) }
    "bin/" + f.name -> f
  }


  def linux(f: File, libs: Iterable[String], mainClass: String): (String, File) = {
    val libStr = libs.map("$LIB_PATH/" + _).mkString(":")
    val cmd =
      (s"""|#!/bin/sh
           |#set JAVA_OPTS
           |
           |LIB_PATH=lib
           |
           |APP_CP=${libStr}
           |
           |java """.stripMargin + "$JAVA_OPTS -cp $APP_CP " + s"${mainClass} " + "$*").replaceAll("\\r\\n", "\n")

    writeToFile(f) { w => w.write(cmd) }
    "bin/" + f.name -> f
  }

  def writeToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
    if (!f.getParentFile.exists()) f.getParentFile.mkdirs()
    val p = new java.io.PrintWriter(f)
    try {
      op(p)
    } finally {
      p.close()
    }
  }
}
