name := "artemis"

version := "1.0-SNAPSHOT"

javacOptions ++= Seq("-source", "1.7", "-target", "1.7")

libraryDependencies += "com.google.guava" % "guava-testlib" % "18.0" % "test"