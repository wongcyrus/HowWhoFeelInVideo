
name := "FaceAnalysis"

version := "1.0"

scalaVersion := "2.12.2"
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-java-sdk-core" % "1.+",
  "com.amazonaws" % "aws-java-sdk-s3" % "1.+",
  "com.amazonaws" % "aws-java-sdk-rekognition" % "1.+",
  "com.amazonaws" % "aws-lambda-java-core" % "1.+",
  "com.amazonaws" % "aws-xray-recorder-sdk-core" % "1.+",
  "com.amazonaws" % "aws-xray-recorder-sdk-aws-sdk" % "1.+",
  "com.fasterxml.jackson.module" % "jackson-module-scala_2.12" % "2.+",
  "org.json4s" %% "json4s-jackson" % "3.+"
)

javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs@_*) => MergeStrategy.discard
  case _ => MergeStrategy.first
}
