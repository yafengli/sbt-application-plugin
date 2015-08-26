name := "sbt-application-plugin"

organization := "org.koala"

version := "1.1.1"

scalaVersion := "2.10.5"

sbtPlugin := true

publishTo := Some(Resolver.file("file",  new File("d:/repository/.m2")))
