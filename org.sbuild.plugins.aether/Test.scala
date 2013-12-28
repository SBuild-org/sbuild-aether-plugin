import de.tototec.sbuild._

@version("0.7.1")
@classpath("target/org.sbuild.plugins.aether-0.0.9100.jar")
class Test(implicit _project: Project) {

  import org.sbuild.plugins.aether._

  Plugin[Aether]("aether") configure (aether => aether.
    addDeps("compile")("org.slf4j:slf4j-api:1.7.5", "org.testng:testng:6.8").
    addDeps("test")("compile", "ch.qos.logback:logback-classic:1.0.11").
    addDeps("cyclic-1")("compile", "test", "cyclic-2").
    addDeps("cyclic-2")("compile", "cyclic-1").
    addDeps("testng")("org.testng:testng:6.8").
    addDeps("testng-without-jcommander")("org.testng:testng:6.8").
    addExcludes("testng-without-jcommander")("com.beust:jcommander")
  )

  def printFiles(ctx: TargetContext) {
    println("Files:" + ctx.dependsOn.files.zipWithIndex.map { case (n, i) => "\n  " + (1 + i) + ". " + n }.mkString)
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

  Target("phony:test-resolve-testng") dependsOn "aether:testng" exec { ctx: TargetContext =>
    printFiles(ctx)
  }

  Target("phony:test-resolve-testng-without-jcommander") dependsOn "aether:testng-without-jcommander" exec { ctx: TargetContext =>
    println("Deps: " + Plugin[Aether]("aether").get.scopeDeps)
    println("Excludes: " + Plugin[Aether]("aether").get.scopeExcludes)
    printFiles(ctx)
  }

  Target("phony:test-resolve-cyclic") dependsOn "aether:cyclic-1" exec { ctx: TargetContext =>
    printFiles(ctx)
  } help "This target should fail because it has cyclic dependencies."

  Target("phony:all") dependsOn "test-resolve-simple" ~~ "test-resolve-compile" ~~ "test-resolve-test" ~~ "test-resolve-cyclic"

}
