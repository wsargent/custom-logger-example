name := """custom-logger-example"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.12"

// Guice 4.x doesn't work with OpenJDK 17
libraryDependencies += guice // needed for configuration of app loader
libraryDependencies += "com.google.inject" % "guice" % "5.1.0"

val echopraxiaVersion = "3.0.2"
val echopraxiaPlusScalaVersion = "1.3.0"

libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "logger" % echopraxiaPlusScalaVersion
libraryDependencies += "com.tersesystems.echopraxia" % "logstash" % echopraxiaVersion

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.4.8"
libraryDependencies += "net.logstash.logback" % "logstash-logback-encoder" % "7.4"

// Needed for DiagnosticLoggerMethod
libraryDependencies += "com.lihaoyi" %% "sourcecode" % "0.3.1"

// Jackson version doesn't work out of the box
libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.15.3"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.15" % Test

// https://mvnrepository.com/artifact/org.scalatestplus.play/scalatestplus-play
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test
