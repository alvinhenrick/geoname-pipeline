name := "geoname-pipeline"

version := "0.1"

scalaVersion := "2.11.12"

val sparkVersion = "2.3.1"
val scoptVersion = "3.3.0"

resolvers += "Restlet Repository" at "http://maven.restlet.org"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion % Provided,
  "org.apache.spark" %% "spark-sql" % sparkVersion % Provided,
  "com.lucidworks.spark" % "spark-solr" % "3.5.1",
  "com.holdenkarau" %% "spark-testing-base" % "2.2.1_0.9.0" % Test
)

scalacOptions := Seq("-unchecked", "-deprecation")

// remove version-specific scala dirs
crossPaths := false

// suppress unnecessary java directories
unmanagedSourceDirectories in Compile ~= {
  _.filter(_.exists)
}
unmanagedSourceDirectories in Test ~= {
  _.filter(_.exists)
}
// arg h2o assembly jar includes WAY old s3 which messes with everything
assemblyShadeRules in assembly := Seq(
  ShadeRule.rename("com.amazonaws.**" -> "shadeaws.@1").inAll
)

artifact in(Compile, assembly) := {
  val art = (artifact in(Compile, assembly)).value
  art.withClassifier(Some("assembly"))
}

addArtifact(artifact in(Compile, assembly), assembly)

// no tests during assembly
test in assembly := {}

parallelExecution in test := false

fork in Test := true
javaOptions ++= Seq("-Xms512M", "-Xmx2048M", "-Xss2m", "-XX:+CMSClassUnloadingEnabled")


assemblyMergeStrategy in assembly := {
  case PathList("org", "aopalliance", xs@_*) => MergeStrategy.last
  case PathList("javax", "inject", xs@_*) => MergeStrategy.last
  case PathList("javax", "servlet", xs@_*) => MergeStrategy.last
  case PathList("javax", "activation", xs@_*) => MergeStrategy.last
  case PathList("javax", "xml", xs@_*) => MergeStrategy.last
  case PathList("javax", "ws", xs@_*) => MergeStrategy.last
  case PathList("org", "apache", xs@_*) => MergeStrategy.last
  case PathList("org", "objenesis", xs@_*) => MergeStrategy.last
  case PathList("org", "objectweb", xs@_*) => MergeStrategy.last
  case PathList("com", "google", xs@_*) => MergeStrategy.last
  case PathList("com", "esotericsoftware", xs@_*) => MergeStrategy.last
  case PathList("com", "codahale", xs@_*) => MergeStrategy.last
  case PathList("com", "yammer", xs@_*) => MergeStrategy.last
  case PathList("com", "twitter", xs@_*) => MergeStrategy.last
  case PathList("com", "glassfish", xs@_*) => MergeStrategy.last
  case PathList("com", "sun", xs@_*) => MergeStrategy.last

  case "META-INF/ECLIPSEF.RSA" => MergeStrategy.last
  case "META-INF/mailcap" => MergeStrategy.last
  case "META-INF/mimetypes.default" => MergeStrategy.last
  case "META-INF/git.properties" => MergeStrategy.last
  case "plugin.properties" => MergeStrategy.last
  case "log4j.properties" => MergeStrategy.last
  case "plugin.xml" => MergeStrategy.last
  case "parquet.thrift" => MergeStrategy.last
  case "codegen/config.fmpp" => MergeStrategy.discard
  case "git.properties" => MergeStrategy.last
  case "about.html" => MergeStrategy.discard
  case "overview.html" => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}