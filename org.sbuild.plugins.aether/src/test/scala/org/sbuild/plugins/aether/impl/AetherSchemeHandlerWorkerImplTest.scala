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
import org.sbuild.plugins.aether.ArtifactDependency
import org.sbuild.plugins.aether.Exclude

class AetherSchemeHandlerWorkerImplTest extends FreeSpec {

  "AetherSchemeHandlerWorkerImpl" - {
    val repoDir = File.createTempFile("repo", "")
    repoDir.delete()
    repoDir.mkdirs()

    val worker = new AetherSchemeHandlerWorkerImpl(repoDir, Seq(Repository.Central))

    "should resolve testng:6.8" in {

      val result = worker.resolve(Seq(ArtifactDependency("org.testng", "testng", "6.8")))

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

      val result = worker.resolve(Seq(
        ArtifactDependency("org.testng", "testng", "6.8",
          excludes = Seq(Exclude("com.beust", "jcommander"))
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

    "should resolve testng:6.8 excluding jcommander (2)" in {

      val result = worker.resolve(Seq(
        ArtifactDependency("org.testng", "testng", "6.8",
          excludes = Seq(Exclude("com.beust:jcommander"))
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