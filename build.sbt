name := "sbt-application-plugin"

organization := "org.koala"

version := "1.0.1"

sbtPlugin := true  

publishTo := Some(Resolver.file("file",  new File("e:/repository/.m2")))
