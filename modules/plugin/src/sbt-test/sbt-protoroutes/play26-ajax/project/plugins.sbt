{
  val pluginVersion = System.getProperty("plugin.version")
  if (pluginVersion == null) {
    throw new RuntimeException(
      """|The system property 'plugin.version' is not defined.
         |Specify this property using the scriptedLaunchOpts -D.""".stripMargin
    )
  } else {
    addSbtPlugin("com.github.hirofumi" % "sbt-protoroutes" % pluginVersion)
  }
}

addSbtPlugin("com.typesafe.play" %% "sbt-plugin"      % "2.6.25")
addSbtPlugin("com.vmunier"       %  "sbt-web-scalajs" % "1.0.10-0.6")
