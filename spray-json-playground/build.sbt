organization := "com.joshcough"

name := "spray-json-test"

version := "0.1.0"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "io.spray" %%  "spray-json" % "1.3.2"
 ,"io.spray" %%  "spray-http" % "1.3.2"
 ,"joda-time" % "joda-time" % "2.7"
 ,"org.scala-lang" % "scala-reflect" % scalaVersion.value
 ,"org.scalaz" %% "scalaz-core" % "7.1.2"
)

ideaExcludeFolders += ".idea"

ideaExcludeFolders += ".idea_modules"