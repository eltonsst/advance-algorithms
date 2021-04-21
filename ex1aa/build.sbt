ThisBuild / scalaVersion     := "2.13.4"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file(".")).settings(name := "ex1aa")

libraryDependencies  ++= Seq(
  "org.scalanlp" %% "breeze-viz" % "1.2"
)
/*
fork := true

javaOptions ++= Seq(
  "-verbose:gc"
)

 */