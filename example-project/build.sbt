import org.koala.sbt.SbtAppPlugin._

lazy val sbt_app_plugin = project.in(file(".")).settings(
	name := "demo",
	organization := "org.koala",
	version := "1.0.1",
	scalaVersion := "2.11.6",	
	libraryDependencies ++= Seq(
		"ch.qos.logback" % "logback-classic" % "1.1.2"    
	)	
) settings(appSettings:_*)
