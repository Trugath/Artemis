name := "artemis"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.5"

javacOptions ++= Seq("-Xlint:unchecked")

crossScalaVersions := Seq("2.10.4", "2.11.5")

libraryDependencies += "com.google.guava" % "guava-testlib" % "18.0" % "test"

libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"

jacoco.settings