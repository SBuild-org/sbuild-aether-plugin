package org.sbuild.plugins.aether

sealed trait Dependency
case class ScopeRef(ref: String) extends Dependency
case class ArtifactDependency(groupId: String,
                              artifactId: String,
                              version: String,
                              classifier: Option[String] = None,
                              excludes: Seq[Exclude] = Seq()) extends Dependency

object Dependency {
  def apply(artifact: String): Dependency = {

    artifact.split(":", 3).map(_.trim) match {
      case Array(ref) => ScopeRef(ref)

      case Array(groupId, artifactId, versionPlus) =>
        val versionWithOptions: Array[String] = versionPlus.split(";", 2).map(_.trim)
        val version = versionWithOptions(0)
        val options: Map[String, String] = versionWithOptions match {
          case Array(version, options) => options.split(";").map(_.trim).map(_.split("=", 2).map(_.trim)).map(
            _ match {
              case Array(a) => ((a, "true"))
              case Array(a, b) => ((a, b))
            }).toMap
          case _ => Map()
        }
        ArtifactDependency(groupId, artifactId, version, options.get("classifier"))

      case _ => throw new RuntimeException("Invalid dependency format. Format must be: groupId:artifactId:version[;key=val]*")
    }

  }
}

object Exclude {
  def apply(exclude: String): Exclude = {
    exclude.split(":", 2).map(_.trim) match {
      case Array(g, a) => Exclude(g, a)
      case _ => throw new RuntimeException("Invalid exclude format. Format must be: groupId:artifactId")
    }
  }
}

case class Exclude(groupId: String,
                   artifactId: String,
                   classifier: Option[String] = None,
                   extension: Option[String] = None)
