name := "Scala_parser"

version := "0.1"

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.39"

libraryDependencies ++= Seq(
  "org.scalameta"           %% "scalameta"                % "3.7.3"

)

scalaVersion := "2.12.0"