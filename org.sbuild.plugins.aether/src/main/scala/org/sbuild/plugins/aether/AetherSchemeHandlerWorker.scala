package org.sbuild.plugins.aether

import java.io.File

trait AetherSchemeHandlerWorker {
  def resolve(requestedArtifacts: Seq[MavenGav]): Seq[File]
}

