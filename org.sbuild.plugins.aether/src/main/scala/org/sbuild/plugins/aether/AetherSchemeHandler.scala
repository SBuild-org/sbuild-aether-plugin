package org.sbuild.plugins.aether

import java.io.File
import scala.Array.canBuildFrom
import de.tototec.sbuild.Logger
import de.tototec.sbuild.Project
import de.tototec.sbuild.SchemeHandler.SchemeContext
import de.tototec.sbuild.SchemeResolver
import de.tototec.sbuild.TargetContext
import de.tototec.sbuild.TargetRefs
import de.tototec.sbuild.TargetRefs.fromString
import org.sbuild.plugins.aether.impl.AetherSchemeHandlerWorkerImpl
import de.tototec.sbuild.ProjectConfigurationException

class AetherSchemeHandler(
  aetherClasspath: Seq[File] = Seq(),
  localRepoDir: File = new File(System.getProperty("user.home") + "/.m2/repository"),
  remoteRepos: Seq[Repository] = Seq(Repository.Central),
  scopeDeps: Map[String, Seq[Dependency]] = Map(),
  scopeExcludes: Map[String, Seq[Exclude]] = Map())(implicit project: Project)
    extends SchemeResolver {

  private[this] val log = Logger[AetherSchemeHandler]

  private[this] val worker: AetherSchemeHandlerWorker = new AetherSchemeHandlerWorkerImpl(localRepoDir, remoteRepos)

  def localPath(schemeCtx: SchemeContext): String = s"phony:${schemeCtx.fullName}"

  def resolveScopes(request: String, rawRequestedDeps: Seq[Dependency], excludes: Seq[Exclude] = Seq(), seenScopes: List[String] = Nil): Seq[ArtifactDependency] =
    rawRequestedDeps.flatMap {
      case x: ArtifactDependency =>
        // Apply the given excludes to each dependency
        Seq(x.copy(excludes = x.excludes ++ excludes))

      case ScopeRef(ref) if seenScopes.contains(ref) =>
        val ex = new ProjectConfigurationException("Cyclic dependencies references detected in dependency: " + request)
        ex.buildScript = Option(project.projectFile)
        throw ex
      case ScopeRef(ref) =>
        // replace scope Alias by their content
        scopeDeps.get(ref) match {
          case None =>
            val ex = new ProjectConfigurationException(s"Unknown referenced scope '${ref}'.")
            ex.buildScript = Option(project.projectFile)
            throw ex
          case Some(replacement) =>
            resolveScopes(
              request,
              replacement,
              excludes ++ scopeExcludes.withDefault(_ => Seq())(ref),
              ref :: seenScopes)
        }
    }

  def resolve(schemeCtx: SchemeContext, targetContext: TargetContext) {
    try {
      val rawRequestedDeps: Seq[Dependency] = schemeCtx.path.split(",").toSeq

      val requestedDeps = resolveScopes(schemeCtx.fullName, rawRequestedDeps)

      log.debug("About to resolve the following requested dependencies: " + requestedDeps.mkString(", "))
      println("About to resolve the following requested dependencies: " + requestedDeps.mkString(", "))

      val files = worker.resolve(requestedDeps)
      files.foreach { f => targetContext.attachFile(f) }

      //    println("Resolved files: " + files)

    } catch {
      case e: ClassNotFoundException =>
        // TODO: Lift exception into domain
        throw e
    }

  }

}

