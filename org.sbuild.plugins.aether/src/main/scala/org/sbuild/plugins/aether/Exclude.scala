package org.sbuild.plugins.aether

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
