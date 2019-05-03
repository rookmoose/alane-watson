import sbt.project

name := "watson"

version := "0.1"

scalaVersion := "2.12.8"

scalacOptions ++= Seq("-feature", "-unchecked", "-deprecation")

libraryDependencies += "org.apache.lucene" % "lucene-core" % "7.7.1"
libraryDependencies += "org.apache.lucene" % "lucene-queryparser" % "7.7.1"
libraryDependencies += "org.apache.lucene" % "lucene-analyzers-common" % "7.7.1"
libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.5"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"
libraryDependencies ++= Seq(
  "edu.stanford.nlp" % "stanford-corenlp" % "3.9.2",
  "edu.stanford.nlp" % "stanford-corenlp" % "3.9.2" classifier "models",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "ch.qos.logback" % "logback-classic" % "1.0.10",
  "org.slf4j" % "slf4j-api" % "1.7.10"
)