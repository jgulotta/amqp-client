enablePlugins(DsaLib)

name := "amqp-client"

libraryDependencies ++= {
  val akkaVersion = "2.5.27"
  Seq(
    "com.rabbitmq"         % "amqp-client"          % "5.7.0",
    "com.typesafe.akka"    %% "akka-actor"          % akkaVersion % "provided",
    "com.typesafe.akka"    %% "akka-slf4j"          % akkaVersion % "test",
    "com.typesafe.akka"    %% "akka-testkit"        % akkaVersion % "test",
    "org.scalatest"        %% "scalatest"           % "3.0.8" % "test",
    "ch.qos.logback"       % "logback-classic"      % "1.2.3" % "test",
    "junit"           	   % "junit"                % "4.12" % "test"
  )
}

scalacOptions -= {
  scalaBinaryVersion.value match {
    case "2.13" => "-Wself-implicit"
    case _ => ""
  }
}
