val scala3Version = "3.2.2"

lazy val root = project
  .in(file("."))
  .settings(
    name := "wiki-path-finder",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "0.7.29" % Test,
      "com.softwaremill.sttp.client4" %% "core" % "4.0.0-M1",
      "com.softwaremill.sttp.client4" %% "circe" % "4.0.0-M1",
      "com.google.guava" % "guava" % "31.1-jre",
      "io.circe" %% "circe-core" % "0.14.5",
      "io.circe" %% "circe-generic" % "0.14.5"
    )
  )
