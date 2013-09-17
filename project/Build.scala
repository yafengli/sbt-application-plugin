import sbt._
import sbt.Keys._

object Build extends Build {
  lazy val sbt_app = Project(
    id = "sbt-application-plugin",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "sbt-application-plugin",
      organization := "org.koala",
      version := "1.0.0",
      scalaVersion := "2.10.2",
      sbtPlugin := true,
      publishTo := Some(Resolver.file("file",  new File( "D:/Tools/play-2.2.0-M2/repository")) ),
      resolvers ++= Seq(
        "Local Maven Repository" at "file:///d:/repository/"
      )
    )
  )
}
