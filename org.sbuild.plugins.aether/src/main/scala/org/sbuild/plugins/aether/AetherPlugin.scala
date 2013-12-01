package org.sbuild.plugins.aether

import de.tototec.sbuild.Plugin
import de.tototec.sbuild.Project
import de.tototec.sbuild.SchemeHandler

/**
 * Central configuration of the SBuild Aether Plugin.
 * The Aether Plugin will register a `[[de.tototec.sbuild.SchemeHandler]]` under the name `[[Aether.schemeName schemeName]]`.
 *
 * The following settings are available:
 *  - `[[Aether.remoteRepos]]` - A list of remote repositories to use.
 *  - `[[Aether.schemeName]]` - The name of the registered scheme handler.
 *  - `[[Aether.scopeDeps]]` - Dependencies collections by scope, whereas the scope is an alias for the dependencies.
 *
 * For further documentation refer to the respective methods/fields.
 *
 *
 */
class Aether() {
  /**
   * Remote repositories Aether will refer to, to resolve the requested dependencies.
   */
  var remoteRepos: Seq[Repository] = Seq()
  var schemeName: String = "aether"
  var scopeDeps: Map[String, Seq[String]] = Map()

  val version = InternalConstants.version

  /**
   * Reconfigured this Aether Plugin instance.
   *
   *  @param remoteRepos `[[Aether.remoteRepos]]` - A list of remote repositories to use.
   *  @param schemeName `[[Aether.schemeName]]` - The name of the registered scheme handler.
   *  @param scopeDeps `[[Aether.scopeDeps]]` - Dependencies collections by scope, whereas the scope is an alias for the dependencies.
   *
   * For further documentation of the method parameters refer to the respective methods/fields with the same name.
   */
  def config(remoteRepos: Seq[Repository] = remoteRepos,
             schemeName: String = schemeName,
             scopeDeps: Map[String, Seq[String]] = scopeDeps): Aether = {
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
        val handler = new AetherSchemeHandler(
          remoteRepos = pluginContext.remoteRepos,
          scopeDeps = pluginContext.scopeDeps
        )
        SchemeHandler(pluginContext.schemeName, handler)
    }
  }
  def create(name: String): Aether = new Aether().config(schemeName = if (name == "") "aether" else name)
  def instanceType: Class[Aether] = classOf[Aether]
}

case class AetherDeps(deps: Seq[String], excludes: Seq[String])
