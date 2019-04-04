import Build._

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

lazy val root = (project in file(".")).settings(
  organization := "org.koala",
  name := "sbt-dist",
  version := $("prod"),
  sbtPlugin := true,
  scalaVersion := $("scala"),
  sbtVersion in Global := "1.2.8",
  publishMavenStyle := false,
  bintrayRepository := "maven",
  bintrayOrganization in bintray := None,
  libraryDependencies ++= Seq(
    "com.github.spullara.mustache.java" % "compiler" % $("mustache"),
    "org.scalatest" %% "scalatest" % $("scalatest") % "test",
    "junit" % "junit" % $("junit") % "test",
    "org.scala-lang" % "scala-library" % scalaVersion.value, "org.scala-sbt" %% "scripted-sbt" % sbtVersion.value))