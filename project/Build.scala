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
      resolvers ++= Seq(
        "Local Maven Repository" at "file:///d:/repository/",
        "202" at "http://221.231.148.202:8081/nexus/content/groups/public/"
      )
    )
  )
}