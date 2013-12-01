package org.sbuild.plugins.aether

object Repository {
  implicit def fromFullName(fullName: String): Repository = fullName.split("::") match {
    case Array(name, layout, url) => Repository(name, layout, url)
    case _ => throw new IllegalArgumentException("Unsupported repository definition (required: <name>::<layout>::<url>): " + fullName)
  }

  val Central = Repository("central", "default", "http://repo1.maven.org/maven2")
}

case class Repository(name: String, layout: String, url: String)
