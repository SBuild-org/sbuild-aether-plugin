package org.sbuild.plugins.aether

import org.scalatest.FreeSpec

class AetherSchemeHandlerTest extends FreeSpec {

  "resolveScopes" - {
    "ScopeExcludes should be applied to all dependencies of that scope" - {

      implicit val p = TestSupport.createMainProject

      val aether = new AetherSchemeHandler(
        scopeDeps = Map("a" -> Seq("a:b:1", "c:d:2")),
        scopeExcludes = Map("a" -> Seq("e:f"))
      )

      "Exclude e:f" in {
        assert(aether.resolveScopes("a", Seq("a")) === Seq(
          ArtifactDependency("a", "b", "1", excludes = Seq("e:f")),
          ArtifactDependency("c", "d", "2", excludes = Seq("e:f"))
        ))
      }

    }
  }

}