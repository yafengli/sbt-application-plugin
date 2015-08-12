import org.koala.sbt.SbtAppPlugin._

lazy val sbt_app_plugin = project.in(file(".")).settings(
	name := "demo",
	organization := "org.koala",
	version := "1.1.0",
	scalaVersion := "2.11.6",
	mainClass := Some("demo.Hello"),
	libraryDependencies ++= Seq(
		"ch.qos.logback" % "logback-classic" % "1.1.2"    
	)	
) settings(appSettings:_*) settings(dirSetting ++= Seq("ext/conf"))
