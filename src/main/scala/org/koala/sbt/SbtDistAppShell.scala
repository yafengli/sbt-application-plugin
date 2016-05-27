package org.koala.sbt

import java.io.{InputStreamReader, PrintWriter}
import java.util

import com.samskivert.mustache.Mustache
import sbt._

object SbtDistAppShell {

  def windows(f: File, libs: Iterable[String], mainClass: String): (String, File) = {
    val scopes = new util.HashMap[String, Object]()
    scopes.put("mainClass", mainClass)
    scopes.put("libStr", libs.map("lib/" + _).mkString(";"))

    writeToFile(f) { w =>
      mustache(w)("windows.mustache", scopes)
    }
    "bin/" + f.name -> f
  }


  def linux(f: File, libs: Iterable[String], mainClass: String): (String, File) = {
    val scopes = new util.HashMap[String, Object]()
    scopes.put("mainClass", mainClass)
    scopes.put("libStr", libs.map("lib/" + _).mkString(":"))

    writeToFile(f) { w =>
      mustache(w)("linux.mustache", scopes)
    }
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


  def mustache(w: PrintWriter)(resourceTemplateName: String, scopes: util.HashMap[String, Object]): Unit = {
    val reader = new InputStreamReader(this.getClass.getClassLoader.getResourceAsStream(resourceTemplateName))
    try {
      val mc = Mustache.compiler()
      val mustache = mc.compile(reader)

      mustache.execute(scopes,w)
    } catch {
      case e: Exception => e.printStackTrace()
    } finally {
      reader.close()
    }
  }
}
