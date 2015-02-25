import sbt._
import sbt.Keys._
import xerial.sbt.Pack._

object Build extends sbt.Build {

  lazy val root = Project(
    id = "scala-min",
    base = file("."),
    settings = Defaults.defaultSettings ++ packSettings ++
      Seq(
        scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
        // Mapping from program name -> Main class
        packMain := Map("hello" -> "scalamin.Hello", "createMaps" -> "org.freemind.tools.CreateMaps"),
        // custom settings here
        scalaVersion := "2.11.4",
        crossPaths := false,
        libraryDependencies ++= Seq(
          // Add dependent jars here
          "org.scala-lang.modules"  %% "scala-xml"   % "1.0.3",
          "org.xerial"               % "xerial-core" % "3.2.2",
          "org.scalatest"           %% "scalatest"   % "2.2.0"   % "test"
        )
      )
  )
}
