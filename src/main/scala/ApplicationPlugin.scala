import sbt._
import Keys._
import scala.collection.mutable.ArrayBuffer

object ApplicationPlugin extends Plugin {
  //dir settings copy files from key to value.
  val dirSetting = SettingKey[Map[String, String]]("dir-setting")
  val fileSetting = SettingKey[String]("file-setting")

  val copyDependenciesTask = TaskKey[Unit]("copy-dependencies")
  val distAppTask = TaskKey[Unit]("dist-zip")

  val buffer = new StringBuffer
  val pattern = "^.*\\.jar$".r.pattern //x.x.x.jar pattern


  val applicationSettings = Seq(
    dirSetting := Map("conf" -> "conf", "lib" -> "lib", "bin" -> ""),
    fileSetting := "dist",

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
    distAppTask <<= (update, crossTarget, packageBin in Runtime, dirSetting, fileSetting) map {
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

        buffers.foreach(it => println(it._2))

        IO.zip(buffers, (out / "%s.zip".format(fs)))
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