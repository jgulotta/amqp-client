name := "amqp-client"

organization := "eu.shiftforward"

version := "1.6.2-SNAPSHOT"

scalaVersion := "2.12.5"

crossScalaVersions := Seq("2.11.12", "2.12.5")

scalacOptions ++= Seq("-feature", "-language:postfixOps")

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= {
  val akkaVersion = "2.5.11"
  Seq(
    "com.rabbitmq"         % "amqp-client"          % "5.2.0",
    "com.typesafe.akka"    %% "akka-actor"          % akkaVersion % "provided",
    "com.typesafe.akka"    %% "akka-slf4j"          % akkaVersion % "test",
    "com.typesafe.akka"    %% "akka-testkit"        % akkaVersion % "test",
    "org.scalatest"        %% "scalatest"           % "3.0.5" % "test",
    "ch.qos.logback"       % "logback-classic"      % "1.2.3" % "test",
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
