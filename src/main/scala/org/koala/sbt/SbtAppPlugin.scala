package org.koala.sbt

import sbt._
import Keys._
import scala.collection.mutable.ArrayBuffer

object SbtAppPlugin extends Plugin {
  //dir settings copy files from key to value.
  val dirSetting = SettingKey[Map[String, String]]("dir-setting")
  val prefix = SettingKey[String]("file-setting")

  val copyDependenciesTask = TaskKey[Unit]("copyDependencies")
  val distZipTask = TaskKey[Unit]("distZip")

  val buffer = new StringBuffer
  val pattern = "^.*\\.jar$".r.pattern //x.x.x.jar pattern


  val appSettings = Seq(
    dirSetting := Map("conf" -> "conf", "lib" -> "lib", "bin" -> ""),
    prefix := "dist",

    copyDependenciesTask <<= (update, crossTarget) map {
      (updateReport, out) =>
        updateReport.allFiles foreach {
          srcPath =>
            buffer.append("lib/" + srcPath.getName + ":")
            val destPath = out / "lib" / srcPath.getName
            IO.copyFile(srcPath, destPath, preserveLastModified = true)
        }
    }
    ,
    distZipTask <<= (update, crossTarget, packageBin in Runtime, dirSetting, prefix) map {
      (updateReport, out, _, ds, fs) =>

        val buffers = ArrayBuffer[(File, String)]()
        //dependencies jar
        updateReport.select(Set("compile", "runtime")).foreach {
          file =>
            buffers += ((file, "lib/%s".format(file.getName)))
        }
        //package jar
        out.listFiles.filter(p => pattern.matcher(p.name).find()).foreach {
          file =>
            buffers += ((file, "lib/%s".format(file.getName)))
        }
        ds.foreach {
          it =>
            copy(new File(it._1), it._2, buffers)
        }

        val dist = (out / "../universal/%s.zip".format(fs))
        IO.zip(buffers, dist)
        IO.unzip(dist, (out / "../universal/stage"))
    }
  )

  def copy(file: File, prefix: String, buffers: ArrayBuffer[(File, String)]) {
    if (file.exists()) {
      file.listFiles().foreach {
        it =>
          val path = if (prefix.trim.length > 0) "%s/%s".format(prefix, it.name) else it.name
          if (it.isFile) buffers += ((it, path))
          else if (it.isDirectory) copy(it, path, buffers)
      }
    }
  }
}