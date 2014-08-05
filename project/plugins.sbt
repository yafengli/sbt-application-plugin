resolvers ++= Seq("Local Maven Repository" at "file:///f:/repository/", "OSC Nexus" at "http://maven.oschina.net/content/groups/public/")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.5.1")
