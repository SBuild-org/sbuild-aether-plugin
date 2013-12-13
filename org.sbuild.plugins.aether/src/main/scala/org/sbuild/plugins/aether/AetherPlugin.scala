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
class Aether(val name: String) {
  /** The name of the registered scheme handler. */
  var schemeName: String = if (name == "") "aether" else name
  /**
   * Remote repositories Aether will refer to, to resolve the requested dependencies.
   */
  var remoteRepos: Seq[Repository] = Seq()
  /**
   * Dependencies collected by scope, whereas the scope is an alias for the dependencies.
   */
  var scopeDeps: Map[String, Seq[String]] = Map()

}

class AetherPlugin(implicit project: Project) extends Plugin[Aether] {
  override def create(name: String): Aether = new Aether(name)
  override def applyToProject(instances: Seq[(String, Aether)]): Unit = instances foreach {
    case (name, pluginContext) =>
      val handler = new AetherSchemeHandler(
        remoteRepos = pluginContext.remoteRepos,
        scopeDeps = pluginContext.scopeDeps
      )
      SchemeHandler(pluginContext.schemeName, handler)
  }
}

case class AetherDeps(deps: Seq[String], excludes: Seq[String])
