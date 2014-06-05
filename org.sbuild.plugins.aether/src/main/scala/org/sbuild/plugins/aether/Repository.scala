package org.sbuild.plugins.aether

object Repository {
  
  /**
   * Converts strings in the form of `name::layout::url` into a Repository instance.
   * 
   * @throws IlleagalArgumentException If the conversion fails, e.g. because of an invalid format. 
   */
  implicit def fromFullName(fullName: String): Repository = fullName.split("::") match {
    case Array(name, layout, url) => Repository(name, layout, url)
    case _ => throw new IllegalArgumentException("Unsupported repository definition (required: <name>::<layout>::<url>): " + fullName)
  }

  val Central = Repository("central", "default", "http://repo1.maven.org/maven2")
}

/**
 * A Aether (Maven) remote repository configuration.
 * 
 * With [Repository$#fromFullName] there exists some converter to easily specify a repository as a [String].
 * 
 * @param name The name of the repository.
 * @param layout The repository layout. Typically `default`.
 * @param url The url to the repository.
 */
case class Repository(name: String, layout: String, url: String)
