package org.koala.sbt

import sbt._
import Keys._
import scala.collection._

object SbtAppPlugin extends Plugin {
  //dir settings copy files from key to value.
  val dirSetting = SettingKey[Seq[(String, String)]]("dir-setting")
  val prefix = SettingKey[String]("file-setting")

  val copyDependenciesTask = TaskKey[Unit]("copy-dependencies")
  val distZipTask = TaskKey[Unit]("dist-zip")

  val buffer = new StringBuffer
  val pattern = "^.*\\.jar$".r.pattern //x.x.x.jar pattern

  val appSettings = Seq(
    prefix := "dist",
    dirSetting := mutable.Seq("conf" -> "conf", "lib" -> "lib", "bin" -> ""),

    copyDependenciesTask <<= (update, ivyConfiguration, crossTarget) map {
      (updateReport, ivy, out) =>
        updateReport.allFiles.foreach {
          srcPath =>
            buffer.append("lib/" + srcPath.getName + ":")
            val destPath = out / "lib" / srcPath.getName
            IO.copyFile(srcPath, destPath, preserveLastModified = true)

            updateReport.allFiles.foreach(f => println("#" + f.getAbsolutePath))
            out.listFiles().foreach(f => println("@" + f.getAbsolutePath))

        }
    },
    distZipTask <<= (update, crossTarget, packageBin in Runtime, dirSetting, prefix) map {
      (updateReport, out, _, ds, fs) =>

        val buffers = mutable.ArrayBuffer[(File, String)]()
        //dependencies jar
        updateReport.select(Set("compile")).foreach {
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

  def copy(file: File, prefix: String, buffers: mutable.ArrayBuffer[(File, String)]) {
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