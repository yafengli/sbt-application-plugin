package org.koala.sbt

import sbt.Keys._
import sbt._

import scala.collection._

object Import {
  val dirSetting = settingKey[Seq[String]]("dir-setting")
  val distZip = taskKey[Unit]("dist-zip files.")
  val treeDeps = taskKey[Unit]("tree-dependencies.")
  val copyDeps = taskKey[Unit]("copy-dependencies.")

  val defaultDirs = mutable.Buffer("conf", "lib", "bin")
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
    distZip <<= (packageBin in Compile, crossTarget, dependencyClasspath in Compile, dirSetting, mainClass, organization, name, version) map {
      (p, out, dr, ds, mc, org, name, v) =>
        try {
          implicit val map = mutable.HashMap[String, File]()

          //dependencies jar package jar module dependOn jar
          dr.map(_.data).foreach {
            case f: File if f.isFile && f.name.endsWith(".jar") => map += s"lib/${f.name}" -> f
            case d: File if d.isDirectory =>
              d.getParentFile.listFiles().filter(filter).headOption match {
                case Some(f) => map += s"lib/${f.name}" -> f
                case None => println(s":ERR:${d.getParentFile.absolutePath} NOT FOUND JAR FILE.ADD [exportJars := true] TO SETTING.")
              }
          }
          if (out.listFiles() != null && out.listFiles().filter(filter).headOption.isDefined) {
            val f = out.listFiles().filter(filter).head
            map += s"lib/${f.name}" -> f
          } else println(s":ERR:${out.absolutePath} NOT FOUND JAR FILE.ADD [exportJar := true] TO SETTING.")

          //run shell
          if (mc.isDefined) {
            val libs = map.values.map(f => f.name)
            map += SbtDistAppShell.windows(out / s"${name}.bat", libs, mc.get)
            map += SbtDistAppShell.linux(out / s"${name}", libs, mc.get)
          }

          //copy dirSetting files.
          ds.map(new File(_)).foreach {
            f =>
              if (f.isDirectory) f.listFiles().foreach(copy(_, f.name)) else if (!map.contains(f.name)) map += f.name -> f
          }

          val dist = (out / s"../universal/${org}-${name}-${v}.zip")

          //zip/unzip files
          IO.zip(map.map(e => e._2 -> e._1), dist)
          IO.unzip(dist, (out / "../universal/stage"))
        } catch {
          case e: Exception => e.printStackTrace()
        }
    },
    copyDeps <<= (update, ivyConfiguration, crossTarget) map {
      (ur, ivy, out) =>
        ur.allFiles.foreach {
          srcPath =>
            val destPath = out / "lib" / srcPath.getName
            IO.copyFile(srcPath, destPath, preserveLastModified = true)

            ur.allFiles.filter(filter).foreach(f => println(">>" + f.getAbsolutePath))
            out.listFiles().filter(filter).foreach(f => println("::" + f.getAbsolutePath))
        }
    },
    treeDeps <<= (update, dependencyClasspath in Runtime) map {
      (u, d) =>
        u.allFiles.foreach(f => println(f.getAbsolutePath))
        println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^")
        d.foreach(f => println(f.data.getAbsolutePath))
    })

  def copy(file: File, prefix: String)(implicit map: mutable.HashMap[String, File]): Unit = {
    try {
      file match {
        case f: File if f.isFile => map += path(prefix, f) -> f
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
