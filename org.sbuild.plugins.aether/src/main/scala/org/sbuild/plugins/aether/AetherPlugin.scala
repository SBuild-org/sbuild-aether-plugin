package org.sbuild.plugins.aether

import de.tototec.sbuild._

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
 * @param schemeName The name of the registered scheme handler.
 * @param remoteRepos Remote repositories Aether will refer to, to resolve the requested dependencies.
 * @param scopeDeps Dependencies collected by scope, whereas the scope is an alias for the dependencies.
 */
case class Aether(schemeName: String,
                  remoteRepos: Seq[Repository] = Seq(Repository.Central),
                  scopeDeps: Map[String, Seq[Dependency]] = Map(),
                  scopeExcludes: Map[String, Seq[Exclude]] = Map()) {

  def addDeps(scope: String)(deps: Dependency*): Aether =
    copy(scopeDeps = scopeDeps + (scope -> (scopeDeps.withDefault(scope => Seq())(scope) ++ deps)))

  def addExcludes(scope: String)(excludes: Exclude*): Aether =
    copy(scopeExcludes = scopeExcludes + (scope -> (scopeExcludes.withDefault(scope => Seq())(scope) ++ excludes)))

}

class AetherPlugin(implicit project: Project) extends Plugin[Aether] {

  override def create(name: String): Aether = Aether(
    schemeName = if (name == "") "aether" else name
  )

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
