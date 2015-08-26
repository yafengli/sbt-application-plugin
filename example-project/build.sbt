lazy val root = project.in(file(".")).aggregate(work_1, work_2)

lazy val work_1 = project.in(file("work_1")).enablePlugins(SbtDistApp).settings(
  name := "work_1",
  organization := "org.koala",
  version := "1.1.0",
  scalaVersion := "2.11.6",
  mainClass := Some("demo.Hello"),
  libraryDependencies ++= Seq(
    "ch.qos.logback" % "logback-classic" % "1.1.2"
  ))

lazy val work_2 = project.in(file("work_2")).settings(
  name := "work_2",
  organization := "org.koala",
  version := "1.1.0",
  scalaVersion := "2.11.6",
  mainClass := Some("demo.Hello"),
  libraryDependencies ++= Seq(
    "ch.qos.logback" % "logback-classic" % "1.1.2"
  ))
