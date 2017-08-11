import Build._

val list = taskKey[Unit]("list")

val filter = (o: Any) => {
  if (o.isInstanceOf[File]) {
    val f = o.asInstanceOf[File]
    if (!f.exists()) sys.error(s">>NOT FOUND ${f.getAbsolutePath}.")
    pattern.matcher(f.name).find() && f.exists()
  }
  else false
}

lazy val root = project.in(file(".")).aggregate(work_1, work_2)

lazy val work_1 = project.in(file("work_1")).enablePlugins(SbtDistApp).dependsOn(work_2).settings(
  name := "work_1",
  organization := "org.koala",
  version := $("prod"),
  scalaVersion := $("scala"),
  mainClass := Some("demo.Hello"),
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-remote" % $("akka")
  ),
  list := {
    val (u, dr) = (update.value, dependencyClasspath.in(Compile).value)
    dr.map(_.data).foreach {
      case f: File if f.isFile && f.name.endsWith(".jar") => println(f">:${f.absolutePath}")
      case d: File if d.isDirectory =>
        d.getParentFile.listFiles().filter(filter).headOption match {
          case Some(file) => println(s">[project]>${file.absolutePath}")
          case None => println(s":ERR:${d.getParentFile.absolutePath} NOT FOUND JAR FILE.")
        }
    }
  }).settings(dirSetting ++= Seq("work_1/extii/hello.conf", "work_1/ext"))

lazy val work_2 = project.in(file("work_2")).settings(
  exportJars := true,
  name := "work_2",
  organization := "org.koala",
  version := $("prod"),
  scalaVersion := $("scala"),
  mainClass := Some("demo.Hello"),
  libraryDependencies ++= Seq(
    "com.typesafe.slick" %% "slick" % $("slick"),
    "com.typesafe.slick" %% "slick-codegen" % $("slick")
  ))
