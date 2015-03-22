name := "artemis"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.6"

javacOptions ++= Seq("-Xlint:unchecked")

libraryDependencies += "com.google.guava" % "guava-testlib" % "18.0" % "test"

libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"

jacoco.settings