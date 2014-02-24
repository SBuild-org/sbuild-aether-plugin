package org.sbuild.plugins.aether.impl

import org.scalatest.FreeSpec
import java.io.File
import org.sbuild.plugins.aether.Repository
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.graph.Exclusion
import org.eclipse.aether.collection.CollectRequest
import org.sbuild.plugins.aether.ArtifactDependency
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.resolution.DependencyRequest
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator
import scala.collection.JavaConverters._
import org.eclipse.aether.artifact.DefaultArtifact

class AetherTests extends FreeSpec {

  "Plain Aether" - {
    val repoDir = File.createTempFile("repo", "")
    repoDir.delete()
    repoDir.mkdirs()

    val remoteRepo = Repository.Central

    val worker = new AetherSchemeHandlerWorkerImpl(repoDir, Seq(remoteRepo))
    lazy val repoSystem = worker.newRepositorySystem()
    lazy val session = worker.newSession(repoSystem)

    def resolve(deps: Seq[Dependency]): Seq[File] = {
      println("About to resolve deps: " + deps)
      val collectRequest = new CollectRequest()
      deps.foreach { d => collectRequest.addDependency(d) }
      collectRequest.addRepository(
        new RemoteRepository.Builder(remoteRepo.name, remoteRepo.layout, remoteRepo.url).build())

      val node = repoSystem.collectDependencies(session, collectRequest).getRoot()

      val dependencyRequest = new DependencyRequest()
      dependencyRequest.setRoot(node)

      repoSystem.resolveDependencies(session, dependencyRequest)

      val nlg = new PreorderNodeListGenerator()
      node.accept(nlg)

      println("Resolved deps: ")
      var count = 1
      val files = nlg.getNodes().asScala.toSeq.map { node =>
        val dep = node.getDependency()
        println(count + ". " + dep)
        count += 1
        val artifact = if (dep != null) dep.getArtifact() else null
        val file = if (artifact != null) artifact.getFile() else null
        if (file != null) file.getAbsoluteFile() else null
      }.filter(_ != null)

      files
    }

    "should resolve testng:6.8" in {

      val result = resolve(Seq(
        new Dependency(
          new DefaultArtifact("org.testng", "testng", "jar", "6.8"),
          "compile", false,
          null
        )
      ))

      val expected = Set(
        "testng-6.8.jar",
        "junit-4.10.jar",
        "hamcrest-core-1.1.jar",
        "bsh-2.0b4.jar",
        "jcommander-1.27.jar",
        "snakeyaml-1.6.jar")

      assert(result.map(_.getName()).toSet === expected)
    }

    "should resolve testng:6.8 excluding jcommander" in {

      val result = resolve(Seq(
        new Dependency(
          new DefaultArtifact("org.testng", "testng", "jar", "6.8"),
          "compile", false,
          Seq(new Exclusion("com.beust", "jcommander", "*", "*")).asJava
        )
      ))

      val expected = Set(
        "testng-6.8.jar",
        "junit-4.10.jar",
        "hamcrest-core-1.1.jar",
        "bsh-2.0b4.jar",
        "snakeyaml-1.6.jar")

      assert(result.map(_.getName()).toSet === expected)
    }

  }

}