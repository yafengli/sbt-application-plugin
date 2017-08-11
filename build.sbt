import Build._

name := "sbt-application-plugin"

organization := "org.koala"

version := $("prod")

scalaVersion := $("scala")

sbtPlugin := true

libraryDependencies ++= Seq(
  "com.samskivert" % "jmustache" % $("jmustache"),
  "org.scalatest" %% "scalatest" % $("scalatest") % "test",
  "junit" % "junit" % $("junit") % "test")
