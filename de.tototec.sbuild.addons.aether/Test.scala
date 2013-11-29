import de.tototec.sbuild._

@version("0.6.0.9004")
@classpath("target/de.tototec.sbuild.addons.aether-0.0.9000.jar")
class Test(implicit _project: Project) {

  Plugin[de.tototec.sbuild.addons.aether.Aether]("aether") configure ( c =>
    c.scopeDeps = Map(
      "compile" -> Seq(
        "org.slf4j:slf4j-api:1.7.5",
        "org.testng:testng:6.8"
      ),
      "test" -> Seq(
        "compile",
        "ch.qos.logback:logback-classic:1.0.11"
      ),
      "cyclic-1" -> Seq("compile", "test", "cyclic-2"),
      "cyclic-2" -> Seq("compile", "cyclic-1")
    )
  )

  def printFiles(ctx: TargetContext) {
    println("Files:" + ctx.dependsOn.files.zipWithIndex.map{case (n, i) => "\n  " + (1 + i) + ". " + n }.mkString)
  }

  Target("phony:test-resolve-simple") dependsOn "aether:org.testng:testng:6.8" exec { ctx: TargetContext =>
    printFiles(ctx)
  }

  Target("phony:test-resolve-compile") dependsOn "aether:compile" exec { ctx: TargetContext =>
    printFiles(ctx)
  }

  Target("phony:test-resolve-test") dependsOn "aether:test" exec { ctx: TargetContext =>
    printFiles(ctx)
  }

  Target("phony:test-resolve-cyclic") dependsOn "aether:cyclic-1" exec { ctx: TargetContext =>
    printFiles(ctx)
  } help "This target should fail because it has cyclic dependencies."

  Target("phony:all") dependsOn "test-resolve-simple" ~~ "test-resolve-compile" ~~ "test-resolve-test" ~~ "test-resolve-cyclic"

}
