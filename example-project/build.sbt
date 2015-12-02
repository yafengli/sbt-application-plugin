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
  version := "1.1.0",
  scalaVersion := "2.11.6",
  mainClass := Some("demo.Hello"),
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-kernel" % "2.3.12",
    "com.typesafe.akka" %% "akka-remote" % "2.3.12"
  ),
  list <<= (update, dependencyClasspath in Compile) map {
    (u, dr) =>
      dr.map(_.data).foreach {
        case f: File if f.isFile && f.name.endsWith(".jar") => println(f">:${f.absolutePath}")
        case d: File if d.isDirectory =>
          d.getParentFile.listFiles().filter(filter).headOption match {
            case Some(file) => println(s">[project]>${file.absolutePath}")
            case None => println(s":ERR:${d.getParentFile.absolutePath} NOT FOUND JAR FILE.")
          }
      }
  }).settings(dirSetting ++= Seq("work_1/ext"))

lazy val work_2 = project.in(file("work_2")).settings(
  exportJars := true,
  name := "work_2",
  organization := "org.koala",
  version := "1.1.0",
  scalaVersion := "2.11.6",
  mainClass := Some("demo.Hello"),
  libraryDependencies ++= Seq(
    "com.typesafe.slick" %% "slick" % "3.0.2",
    "com.typesafe.slick" %% "slick-codegen" % "3.0.2"
  ))
