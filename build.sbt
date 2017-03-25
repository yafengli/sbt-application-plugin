import Build._

name := "sbt-application-plugin"

organization := "org.koala"

version := $("prod")

//scalaVersion := "2.11.8"

sbtPlugin := true

libraryDependencies ++= Seq(
   "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.6",
  "com.samskivert" % "jmustache" % $("jmustache"),
  "org.scalatest" %% "scalatest" % $("scalatest") % "test",
  "junit" % "junit" % $("junit") % "test")
