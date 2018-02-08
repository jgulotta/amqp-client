name := "amqp-client"

organization := "eu.shiftforward"

version := "1.6.1"

scalaVersion := "2.12.1"

crossScalaVersions := Seq("2.11.8", "2.12.1")

scalacOptions ++= Seq("-feature", "-language:postfixOps")

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= {
  val akkaVersion = "2.4.17"
  Seq(
    "com.rabbitmq"         % "amqp-client"          % "4.1.0",
    "com.typesafe.akka"    %% "akka-actor"          % akkaVersion % "provided",
    "com.typesafe.akka"    %% "akka-slf4j"          % akkaVersion % "test",
    "com.typesafe.akka"    %% "akka-testkit"        % akkaVersion % "test",
    "org.scalatest"        %% "scalatest"           % "3.0.1" % "test",
    "ch.qos.logback"       % "logback-classic"      % "1.2.1" % "test",
    "junit"           	   % "junit"                % "4.12" % "test"
  )
}

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true
publishArtifact in Test := false

licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.php"))
homepage := Some(url("https://github.com/ShiftForward/amqp-client"))

pomExtra := {
  <scm>
    <url>https://github.com/ShiftForward/amqp-client.git</url>
    <connection>scm:git:git@github.com:ShiftForward/amqp-client.git</connection>
  </scm>
  <developers>
    <developer>
      <id>sstone</id>
      <name>Fabrice Drouin</name>
      <roles>
        <role>developer</role>
      </roles>
    </developer>
  </developers>
}
