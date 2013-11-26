package de.tototec.sbuild.addons.aether

object MavenGav {
  def apply(artifact: String): MavenGav = {
    val (groupId, artifactId, versionWithOptions: Array[String]) = artifact.split(":", 3).map(_.trim) match {
      case Array(g, a, versionWithOptions) => (g, a, versionWithOptions.split(";", 2).map(_.trim))
      case _ => throw new RuntimeException("Invalid format. Format must be: groupId:artifactId:version[;key=val]*")
    }

    val version = versionWithOptions(0)
    val options: Map[String, String] = versionWithOptions match {
      case Array(version, options) => options.split(";").map(_.trim).map(_.split("=", 2).map(_.trim)).map(
        _ match {
          case Array(a) => ((a, "true"))
          case Array(a, b) => ((a, b))
        }).toMap
      case _ => Map()
    }

    MavenGav(groupId, artifactId, version, options.get("classifier"))
  }
}
case class MavenGav(groupId: String, artifactId: String, version: String, classifier: Option[String])