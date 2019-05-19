
name := "udemy-akka-essentials"

version := "0.1"

scalaVersion := "2.12.7"

val akkaVersion = "2.5.13"
val scalaTestVersion = "3.0.5"

scalacOptions ++= Seq("-deprecation")

// grading libraries
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % akkaVersion
libraryDependencies +=  "com.typesafe.akka" %% "akka-testkit" % akkaVersion
libraryDependencies += "org.scalatest" %% "scalatest" % scalaTestVersion


//libraryDependencies ++= Seq(
  //"com.typesafe.akka" %% "akka-actor" % akkaVersion,
  //"com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  //"org.scalatest" %% "scalatest" % scalaTestVersion
//)