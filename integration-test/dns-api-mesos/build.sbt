enablePlugins(JavaAppPackaging)
name := "bootstrap-demo-dns-api"

scalaVersion := "2.12.8"

def akkaManagementVersion(version: String) = version.split('+')(0)

libraryDependencies += "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % akkaManagementVersion(version.value)

libraryDependencies += "com.lightbend.akka.management" %% "akka-management-cluster-http" % akkaManagementVersion(version.value)

libraryDependencies += "com.lightbend.akka.discovery" %% "akka-discovery-dns" % akkaManagementVersion(version.value)
