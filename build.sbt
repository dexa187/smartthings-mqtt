name := "smartthing-mqtt"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  "org.webjars" % "webjars-play" % "2.1.0",
  "org.fusesource.mqtt-client" % "mqtt-client" % "1.7",
  "org.webjars" % "bootstrap" % "3.0.2" exclude("org.webjars", "jquery"),
  "org.webjars" % "jquery" % "1.8.3"  // use 1.8.3 so that integration tests with HtmlUnit work.
)     

play.Project.playJavaSettings
