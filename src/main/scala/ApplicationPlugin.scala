import sbt._
import Keys._

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
        var all = Traversable[(File, String)]()
        //dependencies jar
        updateReport.select(Set("compile", "runtime")).foreach {
          file =>
            all ++= Traversable((file, "lib/%s".format(file.getName)))
        }
        //package jar

        out.listFiles.filter(p => pattern.matcher(p.name).find()).foreach {
          file =>
            all ++= Traversable((file, "lib/%s".format(file.getName)))
        }

        ds.foreach {
          it =>
            val path = new File(it._1)
            if (path.exists()) path.listFiles.foreach {
              file =>
                val path = if (it._2.length > 0) "%s/%s".format(it._2, file.getName) else "%s".format(file.getName)
                all ++= Traversable((file, path))
            }
        }
        IO.zip(all, (out / "%s.zip".format(fs)))
    }
  )
}