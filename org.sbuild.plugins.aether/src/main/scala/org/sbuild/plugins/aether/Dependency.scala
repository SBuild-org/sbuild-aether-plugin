package org.sbuild.plugins.aether

sealed trait Dependency
/**
 * This is a reference to a dependency scope.
 * 
 * If a ScopeRef is used as dependency, it is replaced by all the dependencies defined for this scope.
 * E.g. a dependency of `compile` would refer to the `compile` scope and will be expanded into all dependencies declared in the `compile` scope.
 */
case class ScopeRef(ref: String) extends Dependency
/**
 * A dependency refering a Maven artifact.
 * 
 * @param groupId The groupId of the artifact.
 * @param artifactId The artifactId of the artifact.
 * @param version The version of the artifact.
 * @param classifier The optional classifier of the artifact. Typical classifiers are: `jar`, `sources`, `javadoc`
 * @param exclude Excusion rules which will be applied, when the artifact is transitively resolved. 
 */
case class ArtifactDependency(groupId: String,
                              artifactId: String,
                              version: String,
                              classifier: Option[String] = None,
                              excludes: Seq[Exclude] = Seq()) extends Dependency

object Dependency {
  /**
   * A convenience [String] to [Dependency] conversion.
   * The basic format of the string is: `groupId:artifactId:version`
   * Additional properties are supported as key-value pairs like this: `groupId:artifactId:version;classifier=sources`
   */
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
  /**
   * Convenience conversion form [String] to [Exclude].
   * The format is: `groupId:artifactId`
   */
  def apply(exclude: String): Exclude = {
    exclude.split(":", 2).map(_.trim) match {
      case Array(g, a) => Exclude(g, a)
      case _ => throw new RuntimeException("Invalid exclude format. Format must be: groupId:artifactId")
    }
  }
}

/**
 * An exclusion rule, evaluated when the transitive dependency graph is calculated.
 * 
 * @param groupId The groupId of the excluded artifact. The `*` wildcard can be used to match all groupIds.
 * @param artifactId The artifactId of the excluded artifact. The `*` wildcard can be used to match all artifactIds.
 * @param classifier The optional classifier of the to-be excluded artifact.
 * @param extension The optional extension of the to-be excluded artifact. 
 */
case class Exclude(groupId: String,
                   artifactId: String,
                   classifier: Option[String] = None,
                   extension: Option[String] = None)
