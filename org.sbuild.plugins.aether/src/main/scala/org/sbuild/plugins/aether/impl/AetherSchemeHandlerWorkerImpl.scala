package org.sbuild.plugins.aether.impl

import java.io.File
import scala.collection.JavaConverters._
import org.apache.maven.repository.internal.MavenRepositorySystemUtils
import org.apache.maven.wagon.Wagon
import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.collection.CollectRequest
import org.eclipse.aether.connector.file.FileRepositoryConnectorFactory
import org.eclipse.aether.connector.wagon.WagonProvider
import org.eclipse.aether.connector.wagon.WagonRepositoryConnectorFactory
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.repository.LocalRepository
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.resolution.DependencyRequest
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator
import org.sonatype.maven.wagon.AhcWagon
import org.sbuild.plugins.aether.AetherSchemeHandlerWorker
import org.sbuild.plugins.aether.Repository
import org.sbuild.plugins.aether.ArtifactDependency
import org.sbuild.plugins.aether.Exclude
import org.eclipse.aether.graph.Exclusion
import org.sbuild.plugins.aether.Exclude

object AetherSchemeHandlerWorkerImpl {

  class ManualWagonProvider extends WagonProvider {
    override def lookup(roleHint: String): Wagon = roleHint match {
      case "http" => new AhcWagon()
      // case "file" => new FileWagon()
      // case "scp" => new ScpWagon()
      // case "sftp" => new SftpWagon()
      case _ => null
    }
    override def release(wagon: Wagon) {}
  }

}

class AetherSchemeHandlerWorkerImpl(localRepoDir: File, remoteRepos: Seq[Repository]) extends AetherSchemeHandlerWorker {
  import AetherSchemeHandlerWorkerImpl._

  private[this] def newRepositorySystem() = {
    val locator = MavenRepositorySystemUtils.newServiceLocator()
    locator.setServices(classOf[WagonProvider], new ManualWagonProvider())
    locator.setService(classOf[RepositoryConnectorFactory], classOf[FileRepositoryConnectorFactory])
    locator.setService(classOf[RepositoryConnectorFactory], classOf[WagonRepositoryConnectorFactory])
    val system = locator.getService(classOf[RepositorySystem])
    assert(system != null)
    system
  }

  private[this] def newSession(system: RepositorySystem) = {
    val session = MavenRepositorySystemUtils.newSession()
    val localRepo = new LocalRepository(localRepoDir)
    session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo))
    session
  }

  lazy val repoSystem = newRepositorySystem()
  lazy val session = newSession(repoSystem)

  override def resolve(requestedArtifacts: Seq[ArtifactDependency]): Seq[File] = {

    def exclusion(exclude: Seq[Exclude]) = exclude.map {
      case Exclude(groupId, artifactId, classifier, extension) =>
        new Exclusion(groupId, artifactId, classifier.getOrElse("*"), extension.getOrElse("*"))
    }.asJavaCollection

    println("About to resolve deps: " + requestedArtifacts)
    
    // create Maven dependencies from it
    val deps = requestedArtifacts.map {
      case ArtifactDependency(g, a, v, None, exclude) =>
        new Dependency(new DefaultArtifact(g, a, "jar", v), "compile", false, exclusion(exclude))
      case ArtifactDependency(g, a, v, Some(c), exclude) =>
        new Dependency(new DefaultArtifact(g, a, c, "jar", v), "compile", false, exclusion(exclude))
    }

    val centralRepo = new RemoteRepository.Builder("central", "default", "http://repo1.maven.org/maven2").build()

    val collectRequest = new CollectRequest()
    deps.foreach { d => collectRequest.addDependency(d) }
    collectRequest.addRepository(centralRepo)

    val node = repoSystem.collectDependencies(session, collectRequest).getRoot()

    val dependencyRequest = new DependencyRequest()
    dependencyRequest.setRoot(node)

    repoSystem.resolveDependencies(session, dependencyRequest)

    val nlg = new PreorderNodeListGenerator()
    node.accept(nlg)

    val files = nlg.getNodes().asScala.toSeq.map { node =>
      val dep = node.getDependency()
      val artifact = if (dep != null) dep.getArtifact() else null
      val file = if (artifact != null) artifact.getFile else null
      if (file != null) file.getAbsoluteFile() else null
    }.filter(_ != null)

    files

  }

}

