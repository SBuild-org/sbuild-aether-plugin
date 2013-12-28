package org.sbuild.plugins.aether

import java.io.File

trait AetherSchemeHandlerWorker {
  def resolve(requestedArtifacts: Seq[ArtifactDependency]): Seq[File]
}

