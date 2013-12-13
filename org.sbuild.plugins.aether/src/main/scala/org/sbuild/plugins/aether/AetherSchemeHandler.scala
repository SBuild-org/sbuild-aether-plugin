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
  scopeDeps: Map[String, Seq[String]] = Map())(implicit project: Project)
    extends SchemeResolver {

  private[this] val log = Logger[AetherSchemeHandler]

  private[this] val worker: AetherSchemeHandlerWorker = new AetherSchemeHandlerWorkerImpl(localRepoDir, remoteRepos)

  def localPath(schemeCtx: SchemeContext): String = s"phony:${schemeCtx.fullName}"

  def resolve(schemeCtx: SchemeContext, targetContext: TargetContext) {
    try {
      val rawRequestedDeps = schemeCtx.path.split(",")

      var cycleGuard: List[String] = Nil
      def resolveScopes(rawRequestedDeps: Seq[String]): Seq[String] = {
        val cycleGuardBefore = cycleGuard
        val res = rawRequestedDeps.flatMap { name =>
          // replace scope Alias by their content
          scopeDeps.get(name.trim) match {
            case Some(replacement) if cycleGuard.contains(name) =>
              val ex = new ProjectConfigurationException("Cyclic dependencies references detected in dependency: " + schemeCtx.fullName)
              ex.buildScript = Option(project.projectFile)
              throw ex
            case Some(replacement) =>
              cycleGuard ::= name.trim
              replacement
            case None => Seq(name)
          }
        }
        if (cycleGuardBefore != cycleGuard) resolveScopes(res)
        else res
      }

      val requestedDeps = resolveScopes(rawRequestedDeps)

      val requestedMavenGavs = requestedDeps.map(p => MavenGav(p.trim))

      log.debug("About to resolve the following requested dependencies: " + requestedMavenGavs.mkString(", "))

      val files = worker.resolve(requestedMavenGavs)
      files.foreach { f => targetContext.attachFile(f) }

      //    println("Resolved files: " + files)

    } catch {
      case e: ClassNotFoundException =>
        // TODO: Lift exception into domain
        throw e
    }

  }

}

