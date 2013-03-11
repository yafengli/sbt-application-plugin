import sbt._
import Keys._
object MyPlugin extends Plugin
{
	// configuration points, like the built in `version`, `libraryDependencies`, or `compile`
    // by implementing Plugin, these are automatically imported in a user's `build.sbt`
    
    val newSetting = SettingKey[String]("new-setting")

     //add
  val copyDependenciesTask = TaskKey[Unit]("copy-deps")
  val distApp = TaskKey[Unit]("dist-tar")
  val buffer = new StringBuffer
  val suffix_r = "[\\d]\\.jar".r.pattern //x.x.x.jar pattern

    // a group of settings ready to be added to a Project
    // to automatically add them, do
    val newSettings = Seq(
        newSetting := "test",        
		copyDependenciesTask <<= (update, crossTarget) map {
    (updateReport, out) =>
      updateReport.allFiles foreach {
        srcPath =>
          buffer.append("lib/" + srcPath.getName + ":")
          val destPath = out / "lib" / srcPath.getName
          IO.copyFile(srcPath, destPath, preserveLastModified = true)
      }
  },
	distApp <<= (update, crossTarget, packageBin in Runtime) map {
    (updateReport, out, _) =>
      var all = Traversable[(File, String)]()

      //dependencies jar
      updateReport.select(Set("compile", "runtime")).foreach {
        file =>
          val jar = Traversable((file, "lib/%s".format(file.getName)))
          all ++= jar
      }
      //package jar
      val pattern = "^.*\\.jar$".r.pattern
      out.listFiles.filter(p => pattern.matcher(p.name).find()).foreach {
        file =>
          val jar = Traversable((file, "lib/%s".format(file.getName)))
          all ++= jar
      }
      //add conf
      val conf_file = new File("conf")
      if (conf_file.exists()) {
        conf_file.listFiles.foreach {
          file =>
            val jar = Traversable((file, "conf/%s".format(file.getName)))
            all ++= jar
        }
      }
      //add system jar
      val jar_file = new File("lib")
      if (jar_file.exists()) {
        jar_file.listFiles.foreach {
          file =>
            val jar = Traversable((file, "lib/%s".format(file.getName)))
            all ++= jar
        }
      }
      val bin_file = new File("bin")
      if (bin_file.exists()) {
        bin_file.listFiles.foreach {
          file =>
            val jar = Traversable((file, file.getName))
            all ++= jar
        }
      }
      val destPath = out / "dist.zip"
      IO.zip(all, destPath)
  }

    )
    
  override lazy val settings = Seq(commands ++= Seq(helloCommand))

  lazy val helloCommand =
    Command.command("hello") { (state: State) =>
      println("Hi!")
      state
    }
}