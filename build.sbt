import Build._

name := "sbt-application-plugin"

organization := "org.koala"

version := "1.1.1"

scalaVersion := "2.10.5"

sbtPlugin := true

libraryDependencies ++= Seq(
  "com.github.spullara.mustache.java" % "compiler" % $("mustache.java"),
  "org.scalatest" %% "scalatest" % $("scalatest") % "test",
  "junit" % "junit" % $("junit") % "test")
