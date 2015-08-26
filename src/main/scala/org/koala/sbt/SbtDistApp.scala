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
  val pattern = """^.*[^javadoc|^sources]\.jar$""".r.pattern
  //x.x.x.jar pattern

  val filter = (o: Any) => {
    if (o.isInstanceOf[File]) {
      val f = o.asInstanceOf[File]
      if (!f.exists()) sys.error(s">>NOT FOUND ${f.getAbsolutePath}.")
      pattern.matcher(f.name).find() && f.exists()
    }
    else false
  }
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
    distZip <<= (update, crossTarget, dependencyClasspath in Runtime, dirSetting, mainClass, organization, name, version) map {
      (ur, out, dr, ds, mc, org, name, v) =>
        try {
          implicit val buffers = mutable.Buffer[(File, String)]()
          //dependencies jar package jar module dependOn jar
          (ur.select(Set("compile")) ++ out.listFiles ++ dr.filter(p => p.data.exists() && p.data.isDirectory)
            .flatMap(_.data.get)).filter(filter).foreach(f => buffers += f -> s"lib/${f.name}")
          //copy dirSetting files
          ds.foreach {
            n =>
              val d = new File(n)
              if (d.isDirectory) d.listFiles().foreach(copy(_, d.name)) else buffers += d -> d.name
          }
          //run shell
          if (mc.isDefined) buffers ++= <::((out / s"${name}.bat", SbtDistAppShell.windows, Map('mainClass -> mc.get)), (out / s"${name}", SbtDistAppShell.linux, Map('mainClass -> mc.get)))

          val dist = (out / s"../universal/${org}-${name}-${v}.zip")

          IO.zip(buffers, dist)
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

  private def writeToFile(f: File, s: String, templates: Map[Symbol, String]): Unit = {
    val pw = new java.io.PrintWriter(f)
    f.setExecutable(true)
    try pw.write(replaceTemplates(s, templates)) finally pw.close()
  }

  private def <::(ps: (File, String, Map[Symbol, String])*): Seq[(File, String)] = {
    ps.map {
      t =>
        writeToFile(t._1, t._2, t._3)
        (t._1, s"bin/${t._1.name}")
    }
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
