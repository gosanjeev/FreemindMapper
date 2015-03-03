import sbt._
import sbt.Keys._
import xerial.sbt.Pack._

object Build extends sbt.Build {

  lazy val root = Project(
    id = "freemind",
    base = file("."),
    settings = Defaults.defaultSettings ++ packSettings ++
      Seq(
        scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
        // Mapping from program name -> Main class
        packMain := Map("createMaps" -> "org.freemind.tools.CreateMaps",
                        "bookmarksToMap" -> "org.freemind.tools.ChromeBookmarksToMindMap",
                        "bookmarksToMapChron" -> "org.freemind.tools.ChromeBookmarksToMindMapChron"),
        // custom settings here
        scalaVersion := "2.11.4",
        crossPaths := false,
        libraryDependencies ++= Seq(
          // Add dependent jars here
          "org.scala-lang.modules"  %% "scala-xml"      % "1.0.3",
          "org.xerial"               % "xerial-core"    % "3.2.2",
          "org.json4s"              %% "json4s-native"  % "3.2.11",
          "joda-time"                % "joda-time"      % "2.7",
          "org.scalatest"           %% "scalatest"      % "2.2.0"   % "test"
        )
      )
  )
}
