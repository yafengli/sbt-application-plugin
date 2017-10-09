import Build._

name := "sbt-application-plugin"

organization := "org.koala"

version := $("prod")

sbtPlugin := true

scalaVersion := $("scala")

sbtVersion in Global := "1.0.2"

crossSbtVersions := Seq("0.13.16","1.0.2")

libraryDependencies ++= Seq(  
  "com.samskivert" % "jmustache" % $("jmustache"),
  "org.scalatest" %% "scalatest" % $("scalatest") % "test",
  "junit" % "junit" % $("junit") % "test") ++ {
  val currentSbtVersion = (sbtVersion in pluginCrossBuild).value
  if(currentSbtVersion.startsWith("1.0"))
    Seq("org.scala-lang" % "scala-library" % scalaVersion.value, "org.scala-sbt" %% "scripted-sbt" % sbtVersion.value)
  else Seq()
}

lazy val testTask = TaskKey[Unit]("lyfHello")

testTask := {
  println("Usage: ^ lyfHello")
  val currentSbtVersion = (sbtVersion in pluginCrossBuild).value
  if(currentSbtVersion.startsWith("1.0")) println("1.0:"+currentSbtVersion)
  else println("0.13:"+currentSbtVersion)
}
