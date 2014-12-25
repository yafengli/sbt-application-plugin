package org.koala.sbt

import sbt.Keys._
import sbt._

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
    dirSetting := mutable.Buffer("conf" -> "conf", "lib" -> "lib", "bin" -> ""),
    copyDependencies <<= (update, ivyConfiguration, crossTarget) map {
      (ur, ivy, out) =>
        ur.allFiles.foreach {
          srcPath =>
            val destPath = out / "lib" / srcPath.getName
            IO.copyFile(srcPath, destPath, preserveLastModified = true)

            ur.allFiles.filter(filter).foreach(f => println(">>" + f.getAbsolutePath))
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
      (ur, out, dr, ds, ps) =>
        implicit val buffers = mutable.Buffer[(File, String)]()

        //dependencies jar package jar module dependOn jar
        (ur.select(Set("compile")) ++ out.listFiles ++ dr.filter(p => p.data.exists() && p.data.isDirectory)
          .flatMap(_.data.get)).filter(filter).foreach(f => buffers += f -> s"lib/${f.name}")

        //copy jars
        ds.foreach {
          it =>
            new File(it._1) match {
              case d: File if d.isDirectory => d.listFiles().foreach(copy(_, it._2))
              case _ =>
            }
        }

        val dist = (out / s"../universal/${ps}.zip")
        IO.zip(buffers, dist)
        IO.unzip(dist, (out / "../universal/stage"))
    }
  )

  def copy(file: File, prefix: String)(implicit buffers: mutable.Buffer[(File, String)]): Unit = {
    try {
      file match {
        case f: File if f.isFile => buffers += f -> path(prefix, f)
        case d: File if d.isDirectory => d.listFiles().foreach(f => copy(f, path(prefix, d)))
        case _ =>
      }
    } catch {
      case e: Exception => e.printStackTrace()
    }
  }

  private def path(prefix: String, f: File): String = {
    if (prefix != null && prefix.trim.length > 0) "%s/%s".format(prefix, f.name) else f.name
  }
}
