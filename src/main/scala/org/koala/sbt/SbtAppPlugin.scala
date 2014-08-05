package org.koala.sbt

import sbt._
import Keys._
import scala.collection._

object SbtAppPlugin extends Plugin {
  //dir settings copy files from key to value.
  val dirSetting = settingKey[Seq[(String, String)]]("dir-setting")
  val prefix = settingKey[String]("file-setting")

  val copyDependencies = taskKey[Unit]("copy-dependencies")
  val distZip = taskKey[Unit]("dist-zip")

  val buffer = new StringBuffer
  val pattern = """^.*\.jar$""".r.pattern //x.x.x.jar pattern

  val filter = (o: Any) => {
    if (o.isInstanceOf[File]) {
      val f = o.asInstanceOf[File]
      if (!f.exists()) sys.error(s">>NOT FOUND ${f.getAbsolutePath}.")
      pattern.matcher(f.name).find() && f.exists()
    }
    else false
  }

  val appSettings = Seq(
    exportJars := true,
    prefix := s"${organization}-${name}-${version}",
    dirSetting := mutable.Seq("conf" -> "conf", "lib" -> "lib", "bin" -> ""),
    copyDependencies <<= (update, ivyConfiguration, crossTarget) map {
      (updateReport, ivy, out) =>
        updateReport.allFiles.foreach {
          srcPath =>
            buffer.append("lib/" + srcPath.getName + ":")
            val destPath = out / "lib" / srcPath.getName
            IO.copyFile(srcPath, destPath, preserveLastModified = true)

            updateReport.allFiles.filter(filter).foreach(f => println(">>" + f.getAbsolutePath))
            out.listFiles().filter(filter).foreach(f => println("::" + f.getAbsolutePath))
        }
    },
    distZip <<= (update, crossTarget, dependencyClasspath in Runtime, dirSetting, prefix) map {
      (updateReport, out, dr, ds, ps) =>

        val buffers = mutable.ArrayBuffer[(File, String)]()
        //dependencies jar
        updateReport.select(Set("compile")).filter(filter).foreach {
          file =>
            buffers += ((file, s"lib/${file.name}"))
        }
        //package jar
        out.listFiles.filter(filter).foreach {
          file =>
            buffers += ((file, s"lib/${file.name}"))
        }
        //module dependOn jar
        dr.filter(p => p.data.exists() && p.data.isDirectory).foreach {
          t =>
            val lib = t.data.getParentFile.listFiles().filter(filter)
            if (lib.size < 1) sys.error(s"${t.data.getParent} NOT FOUND JAR FILE.")
            else lib.foreach(file => buffers += ((file, s"lib/${file.name}")))
        }
        //copy jars
        ds.foreach {
          it =>
            copy(new File(it._1), it._2, buffers)
        }

        val dist = (out / s"../universal/${ps}.zip")
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