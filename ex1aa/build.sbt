ThisBuild / scalaVersion     := "2.13.4"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file(".")).settings(name := "ex1aa")

libraryDependencies  ++= Seq(
  "org.scalanlp"               %% "breeze-viz"      % "1.2",
  "com.typesafe.scala-logging" %% "scala-logging"   % "3.9.2",
  "ch.qos.logback"             %  "logback-classic" % "1.2.3", // required by scala-logging
  "com.lihaoyi"                %% "os-lib"          % "0.7.3"
)