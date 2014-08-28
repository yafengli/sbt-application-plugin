package org.koala.sbt

import sbt._
import Keys._
import scala.collection._

object SbtAppPlugin extends Plugin {
  val dirSetting = settingKey[Seq[(String, String)]]("dir-setting")
  val prefix = settingKey[String]("file-setting")

  val copyDependencies = TaskKey[Unit]("copy-dependencies", "Copy all dependencies to target/lib")
  val distZip = TaskKey[Unit]("dist-zip", "Dist a .zip file include all executable.")
  val standalone = TaskKey[Unit]("standalone", "Pakcage a standalone executable jar.")

  val pattern = """^.*[^javadoc|^sources]\.jar$""".r.pattern //x.x.x.jar pattern

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
    prefix := s"${organization.value}-${name.value}-${version.value}",
    dirSetting := mutable.Seq("conf" -> "conf", "lib" -> "lib", "bin" -> ""),
    copyDependencies <<= (update, ivyConfiguration, crossTarget) map {
      (updateReport, ivy, out) =>
        updateReport.allFiles.foreach {
          srcPath =>
            val destPath = out / "lib" / srcPath.getName
            IO.copyFile(srcPath, destPath, preserveLastModified = true)

            updateReport.allFiles.filter(filter).foreach(f => println(">>" + f.getAbsolutePath))
            out.listFiles().filter(filter).foreach(f => println("::" + f.getAbsolutePath))
        }
    },
    standalone <<= (packageBin in Compile, crossTarget, dependencyClasspath in Runtime, dirSetting, prefix, streams) map {
      (artifact, out, classpath, ds, ps, s) =>
        val thisArtifactMapping = (artifact, (file("main") / artifact.name).getPath)
        val mappings = Seq(thisArtifactMapping) ++ Attributed.data(classpath).map(f => (f, (file("lib") / f.name).getPath)).filter(_._1 != artifact)
        val packageConf = new Package.Configuration(mappings, (out / s"${ps}.jar"), Seq())
        Package(packageConf, (out / s"${ps}.jar.tmp"), s.log)
    },
    distZip <<= (update, crossTarget, dependencyClasspath in Runtime, dirSetting, prefix) map {
      (updateReport, out, dr, ds, ps) =>

        val buffers = mutable.HashMap[File, String]()
        //dependencies jar
        updateReport.select(Set("compile")).filter(filter).foreach {
          file =>
            buffers += file -> s"lib/${file.name}"
        }
        //module dependOn jar
        dr.filter(p => p.data.exists() && p.data.isDirectory).foreach {
          t =>
            t.data.getParentFile.listFiles().filter(filter).headOption match {
              case Some(file) => buffers += file -> s"lib/${file.name}"
              case None =>
            }
        }
        //package jar
        out.listFiles.filter(filter).foreach {
          file =>
            buffers += file -> s"lib/${file.name}"
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

  def copy(file: File, prefix: String, buffers: mutable.HashMap[File, String]) {
    if (file.exists()) {
      file.listFiles().foreach {
        it =>
          val path = if (prefix.trim.length > 0) "%s/%s".format(prefix, it.name) else it.name
          if (it.isFile) buffers += it -> path
          else if (it.isDirectory) copy(it, path, buffers)
      }
    }
  }
}
