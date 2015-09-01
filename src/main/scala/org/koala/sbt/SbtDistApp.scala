package org.koala.sbt

import sbt.Keys._
import sbt._

import scala.annotation.tailrec
import scala.collection._

object Import {
  val dirSetting = settingKey[Seq[String]]("dir-setting")
  val distZip = taskKey[Unit]("dist-zip files.")
  val treeDeps = taskKey[Unit]("tree-dependencies.")
  val copyDeps = taskKey[Unit]("copy-dependencies.")
  val hello = taskKey[Unit]("hello.")
  val pattern = """^.*[^javadoc|^sources]\.jar$""".r.pattern //x.x.x.jar pattern

  val filter: File => Boolean = { f => f.exists() && pattern.matcher(f.name).find() }
}

object SbtDistApp extends AutoPlugin {

  import Import._

  val autoImport = Import

  override def requires = sbt.plugins.JvmPlugin

  override def trigger = noTrigger

  override lazy val projectSettings = Seq(
    exportJars := true,
    dirSetting := mutable.Buffer("conf", "lib", "bin"),
    hello := {
      println(update.value)
      (update, organization, name).map { (u, o, n) =>
        println(s"update:${u} org:${o} name:${n}")
      }
    },
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
          //copy dirSetting files.
          ds.map(new File(_)).foreach {
            f =>
              if (f.isDirectory) f.listFiles().foreach(copy(_, f.name)) else if (!map.contains(f.name)) map += f.name -> f
          }
          //run shell
          if (mc.isDefined) map ++= <<:((out / s"${name}.bat", SbtDistAppShell.windows, Map('mainClass -> mc.get)), (out / s"${name}", SbtDistAppShell.linux, Map('mainClass -> mc.get)))

          val dist = (out / s"../universal/${org}-${name}-${v}.zip")

          //loop buffers
          //map.foreach(t => println(t._2.absolutePath))

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

  private def writeToFile(f: File, s: String, templates: Map[Symbol, String]): Unit = {
    if (!f.getParentFile.exists()) f.getParentFile.mkdirs()
    val pw = new java.io.PrintWriter(f)
    f.setExecutable(true)
    try pw.write(replaceTemplates(s, templates)) finally pw.close()
  }

  private def <<:(ps: (File, String, Map[Symbol, String])*): Map[String, File] = {
    ps.map {
      t =>
        writeToFile(t._1, t._2, t._3)
        s"bin/${t._1.name}" -> t._1
    } toMap
  }

  private def path(prefix: String, f: File): String = {
    if (prefix != null && prefix.trim.length > 0) "%s/%s".format(prefix, f.name) else f.name
  }

  private def replaceTemplates(text: String, templates: Map[Symbol, String]): String = {
    val builder = new StringBuilder(text)
    @tailrec
    def loop(key: String,
             keyLength: Int,
             value: String): StringBuilder = {
      val index = builder.lastIndexOf(key)
      if (index < 0) builder
      else {
        builder.replace(index, index + keyLength, value)
        loop(key, keyLength, value)
      }
    }

    templates.foreach {
      case (key, value) =>
        val template = "${" + key.name + "}"
        loop(template, template.length, value)
    }

    builder.toString()
  }
}
