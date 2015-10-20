name := "artemis"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.7"

javacOptions ++= Seq("-Xlint:unchecked")

libraryDependencies += "org.scala-lang.modules" %% "scala-java8-compat" % "0.5.0"

scalacOptions ++= List("-Ybackend:GenBCode", "-Ydelambdafy:method", "-target:jvm-1.8")

libraryDependencies += "com.google.guava" % "guava-testlib" % "18.0" % "test"

libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"
