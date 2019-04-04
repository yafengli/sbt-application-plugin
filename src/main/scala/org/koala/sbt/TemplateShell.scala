package org.koala.sbt

import java.io.{InputStreamReader, Writer}
import java.util

import com.github.mustachejava.DefaultMustacheFactory
import sbt._

object TemplateShell {

  def windows(f: File, libs: Iterable[String], mainClass: String): (String, File) = {
    val scopes = storeScopes(libs, mainClass, ";")
    writeToFile(f)(w => mustache(w)("windows.mustache", scopes))
    "bin/" + f.name -> f
  }

  def linux(f: File, libs: Iterable[String], mainClass: String): (String, File) = {
    val scopes = storeScopes(libs, mainClass, ":")

    writeToFile(f)(w => mustache(w)("linux.mustache", scopes))
    "bin/" + f.name -> f
  }

  def storeScopes(libs: Iterable[String], mainClass: String, str: String): util.HashMap[String, Object] = {
    val scopes = new util.HashMap[String, Object]()
    scopes.put("mainClass", mainClass)
    scopes.put("libStr", libs.map("lib/" + _).mkString(str))
    scopes
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

  def mustache(w: Writer)(resourceTemplateName: String, scopes: util.HashMap[String, Object]): Unit = {
    val reader = new InputStreamReader(this.getClass.getClassLoader.getResourceAsStream(resourceTemplateName))
    try {
      val mf = new DefaultMustacheFactory
      val mc = mf.compile(reader, resourceTemplateName)

      mc.execute(w, scopes)
    } catch {
      case e: Exception => e.printStackTrace()
    }
    finally {
      reader.close()
    }
  }
}
