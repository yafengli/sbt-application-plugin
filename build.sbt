name := "sbt-application-plugin"

organization := "org.koala"

version := "1.0.0"

sbtPlugin := true  

resolvers := Seq(Resolver.file("local",file("e:/repository/.ivy2")))

publishTo := Some(Resolver.file("m2",file("e:/repository/.m2")))