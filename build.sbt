import Build._

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

lazy val root = (project in file(".")).settings(
    organization := "org.koala",
    name := "sbt-application-plugin",    
    version := $("prod"),
    sbtPlugin := true,
    scalaVersion := $("scala"),
    sbtVersion in Global := "1.0.4",
    crossSbtVersions := Seq("0.13.16","1.0.4"),
    publishMavenStyle := false,
    bintrayRepository := "maven",
    bintrayOrganization in bintray := None,
    libraryDependencies ++= Seq(  
      "com.samskivert" % "jmustache" % $("jmustache"),
      "org.scalatest" %% "scalatest" % $("scalatest") % "test",
      "junit" % "junit" % $("junit") % "test") ++ {
      val currentSbtVersion = (sbtVersion in pluginCrossBuild).value
      if(currentSbtVersion.startsWith("1.0"))
        Seq("org.scala-lang" % "scala-library" % scalaVersion.value, "org.scala-sbt" %% "scripted-sbt" % sbtVersion.value)
      else Seq()
    }
)    