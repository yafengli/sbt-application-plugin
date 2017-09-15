import Build._

name := "sbt-application-plugin"

organization := "org.koala"

version := $("prod")

sbtPlugin := true

crossSbtVersions := Seq("0.13.16","1.0.1")

// scalaVersion := {
//    CrossVersion.binarySbtVersion(sbtVersion.value) match {
//     case "1.0" => "2.12.3"
//     case _ => "2.10.6"
//   }
// }

libraryDependencies ++= {
   CrossVersion.binarySbtVersion(sbtVersion.value) match {
    case "1.0" => Seq("org.scala-sbt" %% "scripted-sbt" % sbtVersion.value)
    case _ => Seq("org.scala-sbt" % "scripted-sbt" % sbtVersion.value)
  }
}

libraryDependencies ++= Seq(  
  "com.samskivert" % "jmustache" % $("jmustache"),
  "org.scalatest" %% "scalatest" % $("scalatest") % "test",
  "junit" % "junit" % $("junit") % "test")

lazy val testTask = TaskKey[Unit]("lyfHello")

testTask := {
  CrossVersion.binarySbtVersion(sbtVersion.value) match {
    case "0.13" =>
      println("0.13:"+sbtVersion.value)
    case t:String =>      
      println(t)
      Seq(        
        println("1.0.1:"+sbtVersion.value)
      )
  }
}