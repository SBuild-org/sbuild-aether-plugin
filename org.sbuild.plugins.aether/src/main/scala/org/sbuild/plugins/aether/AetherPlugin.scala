package org.sbuild.plugins.aether

import de.tototec.sbuild._

class AetherPlugin(implicit project: Project) extends Plugin[Aether] {

  override def create(name: String): Aether = Aether(
    schemeName = if (name == "") "aether" else name
  )

  override def applyToProject(instances: Seq[(String, Aether)]): Unit = instances foreach {
    case (name, aether) =>
      val handler = new AetherSchemeHandler(
        remoteRepos = aether.remoteRepos,
        scopeDeps = aether.scopeDeps,
        scopeExcludes = aether.scopeExcludes
      )
      SchemeHandler(aether.schemeName, handler)
  }
}

//case class AetherDeps(deps: Seq[String], excludes: Seq[String])
