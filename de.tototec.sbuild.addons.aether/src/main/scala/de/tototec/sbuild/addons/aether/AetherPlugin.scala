package de.tototec.sbuild.addons.aether

import de.tototec.sbuild.Plugin
import de.tototec.sbuild.Project
import de.tototec.sbuild.SchemeHandler

class Aether() {
  var remoteRepos: Seq[Repository] = Seq()
  var schemeName: String = "aether"
  var scopeDeps: Seq[(String, Seq[String])] = Seq()

  val version = InternalConstants.version

  def config(remoteRepos: Seq[Repository] = remoteRepos,
             schemeName: String = schemeName,
             scopeDeps: Seq[(String, Seq[String])] = scopeDeps): Aether = {
    this.remoteRepos = remoteRepos
    this.schemeName = schemeName
    this.scopeDeps = scopeDeps
    this
  }

}

class AetherPlugin(implicit project: Project) extends Plugin[Aether] {
  def applyToProject(instances: Seq[(String, Aether)]): Unit = {
    instances.map {
      case (name, pluginContext) =>
        SchemeHandler(pluginContext.schemeName, new AetherSchemeHandler(remoteRepos = pluginContext.remoteRepos))
    }
  }
  def create(name: String): Aether = new Aether().config(schemeName = if (name == "") "aether" else name)
  def instanceType: Class[Aether] = classOf[Aether]
}

case class AetherDeps(deps: Seq[String], excludes: Seq[String])
