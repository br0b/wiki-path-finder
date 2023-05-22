val scala3Version = "3.2.2"

lazy val root = project
  .in(file("."))
  .settings(
    name := "wiki-path-finder",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "0.7.29" % Test,
      "org.systemfw" %% "upperbound" % "0.4.0",
      "dev.zio" %% "zio-json" % "0.5.0",
      "com.softwaremill.sttp.client3" %% "core" % "3.8.15"
    )
  )
