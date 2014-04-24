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
      sbtPlugin := true,      
      resolvers ++= Seq(
        "Local Maven Repository" at "file:///f:/repository/"
      )
    )
  )
}
