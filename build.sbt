name := "sbt-application-plugin"

organization := "org.koala"

version := "1.0.1"

sbtPlugin := true  

<<<<<<< HEAD
publishTo := Some(Resolver.file("file",  new File("e:/repository/.m2")))
=======
resolvers := Seq(Resolver.file("local",file("e:/repository/.ivy2")))

publishTo := Some(Resolver.file("m2",file("e:/repository/.m2")))
>>>>>>> 1049aee7b1bc0e35324d9869358745487942405a
