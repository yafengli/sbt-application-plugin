package org.koala.sbt

import sbt.Keys._
import sbt._

import scala.collection._

object Import {
  val dirSetting = settingKey[Seq[String]]("dirSetting")
  val distZip = taskKey[Unit]("distZip")
  val treeDeps = taskKey[Unit]("treeDependencies.")
  val copyDeps = taskKey[Unit]("copyDependencies.")

  val defaultDirs = mutable.Buffer("lib")
  val pattern = """^.*[^javadoc|^sources]\.jar$""".r.pattern
  //x.x.x.jar pattern
  val filter: File => Boolean = { f => f.exists() && pattern.matcher(f.name).find() }
}

object SbtDistApp extends AutoPlugin {

  import Import._

  val autoImport = Import

  override def requires = sbt.plugins.JvmPlugin

  override def trigger = noTrigger

  override lazy val projectSettings = Seq(
    exportJars := true,
    dirSetting := defaultDirs,
    distZip := {
      val pv = packageBin.in(Compile).value
      println(s"pv:${pv.getAbsolutePath}")
      val (out, dr, ds, mc, org, v, suffix) = (crossTarget.value, dependencyClasspath.in(Compile).value, dirSetting.value, mainClass.value, organization.value, version.value, name.value)
      try {
        implicit val map = mutable.HashMap[String, File]()

        //dependencies jar package jar module dependOn jar
        dr.map(_.data).foreach {
          case f: File if f.isFile && f.name.endsWith(".jar") => inject(map, s"lib/${f.name}", f)
          case d: File if d.isDirectory => d.getParentFile.listFiles().filter(filter).headOption.foreach(f => inject(map, s"lib/${f.name}", f))
        }
        //package jar
        out.listFiles().filter(filter).headOption.foreach(f => inject(map, s"lib/${f.name}", f))
        //run shell
        if (mc.isDefined) {
          val libs = map.values.map(f => f.name)
          map += SbtDistAppShell.windows(out / s"${suffix}.bat", libs, mc.get)
          map += SbtDistAppShell.linux(out / s"${suffix}", libs, mc.get)
        }

        //copy dirSetting files.
        ds.map(new File(_)).foreach {
          f =>
            if (f.isDirectory) f.listFiles().foreach(copy(_, f.name)) else if (!map.contains(f.name)) inject(map, path(f.getAbsoluteFile.getParentFile.getName, f), f)
        }

        val dist = (out / s"../universal/${org}-${suffix}-${v}.zip")

        //zip/unzip files
        IO.zip(map.map(e => e._2 -> e._1), dist)
        IO.unzip(dist, (out / "../universal/stage"))
      } catch {
        case e: Exception => e.printStackTrace()
      }
    },
    copyDeps := {
      val (ur, out) = (update.value, crossTarget.value)
      ur.allFiles.foreach {
        srcPath =>
          val destPath = out / "lib" / srcPath.getName
          IO.copyFile(srcPath, destPath, preserveLastModified = true)

          ur.allFiles.filter(filter).foreach(f => println(">>" + f.getAbsolutePath))
          out.listFiles().filter(filter).foreach(f => println("::" + f.getAbsolutePath))
      }
    },
    treeDeps := {
      val (u, d) = (update.value, dependencyClasspath.in(Runtime).value)
      u.allFiles.foreach(f => println(f.getAbsolutePath))
      d.foreach(f => println(f.data.getAbsolutePath))
    })

  def inject(map: mutable.HashMap[String, File], key: String, f: File): Unit = {
    println(s"Including......${f.getName}")
    map += key -> f
  }

  def copy(file: File, prefix: String)(implicit map: mutable.HashMap[String, File]): Unit = {
    try {
      file match {
        case f: File if f.isFile => inject(map, path(prefix, f), f)
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
