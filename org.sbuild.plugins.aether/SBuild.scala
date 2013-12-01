import de.tototec.sbuild._
import de.tototec.sbuild.ant._
import de.tototec.sbuild.ant.tasks._
import de.tototec.sbuild.TargetRefs._

@version("0.6.0")
@classpath("mvn:org.apache.ant:ant:1.8.4")
class SBuild(implicit _project: Project) {

  val namespace = "org.sbuild.plugins.aether"
  val version = "0.0.9000"

  val sbuildVersion = "0.6.0.9004"
  val scalaVersion = "2.10.3"
  val jar = s"target/${namespace}-${version}.jar"
  val sourcesZip = s"target/${namespace}-${version}-sources.jar"

  val scalaCompiler = s"mvn:org.scala-lang:scala-compiler:$scalaVersion" ~
    s"mvn:org.scala-lang:scala-library:$scalaVersion" ~
    s"mvn:org.scala-lang:scala-reflect:$scalaVersion"

  // FIXME
  val sbuildCore = s"../../sbuild/de.tototec.sbuild/target/de.tototec.sbuild-${sbuildVersion}.jar"

  val aetherVersion = "0.9.0.M2"
  val wagonVersion = "2.4"
  val aetherCp =
    s"mvn:org.eclipse.aether:aether-api:${aetherVersion}" ~
      s"mvn:org.eclipse.aether:aether-spi:${aetherVersion}" ~
      s"mvn:org.eclipse.aether:aether-util:${aetherVersion}" ~
      s"mvn:org.eclipse.aether:aether-impl:${aetherVersion}" ~
      s"mvn:org.eclipse.aether:aether-connector-file:${aetherVersion}" ~
      s"mvn:org.eclipse.aether:aether-connector-asynchttpclient:${aetherVersion}" ~
      s"mvn:org.eclipse.aether:aether-connector-wagon:${aetherVersion}" ~
      "mvn:io.tesla.maven:maven-aether-provider:3.1.2" ~
      s"mvn:org.apache.maven.wagon:wagon-provider-api:${wagonVersion}" ~
      s"mvn:org.apache.maven.wagon:wagon-http:${wagonVersion}" ~
      s"mvn:org.apache.maven.wagon:wagon-file:${wagonVersion}" ~
      s"mvn:org.apache.maven.wagon:wagon-ssh:${wagonVersion}" ~
      "mvn:org.sonatype.maven:wagon-ahc:1.2.1" ~
      s"mvn:org.apache.maven.wagon:wagon-http-shared4:${wagonVersion}" ~
      s"mvn:org.codehaus.plexus:plexus-component-annotations:1.5.5" ~
      s"mvn:org.apache.httpcomponents:httpclient:4.2.5" ~
      s"mvn:org.apache.httpcomponents:httpcore:4.2.4" ~
      "mvn:javax.inject:javax.inject:1" ~
      "mvn:com.ning:async-http-client:1.6.5" ~
      "mvn:io.tesla.maven:maven-model:3.1.0" ~
      "mvn:io.tesla.maven:maven-model-builder:3.1.0" ~
      "mvn:io.tesla.maven:maven-repository-metadata:3.1.0" ~
      "mvn:org.jboss.netty:netty:3.2.5.Final" ~
      "mvn:org.eclipse.sisu:org.eclipse.sisu.inject:0.0.0.M1" ~
      "mvn:org.eclipse.sisu:org.eclipse.sisu.plexus:0.0.0.M1" ~
      "mvn:org.codehaus.plexus:plexus-classworlds:2.4" ~
      "mvn:org.codehaus.plexus:plexus-interpolation:1.16" ~
      "mvn:org.codehaus.plexus:plexus-utils:2.1" ~
      "mvn:org.sonatype.sisu:sisu-guava:0.9.9" ~
      "mvn:org.sonatype.sisu:sisu-guice:3.1.0" ~
      "mvn:org.slf4j:slf4j-api:1.6.2"

  val compileCp =
    s"mvn:org.scala-lang:scala-library:${scalaVersion}" ~
      sbuildCore ~
      aetherCp

  ExportDependencies("eclipse.classpath", compileCp)

  Target("phony:all") dependsOn sourcesZip ~ jar

  Target("phony:clean").evictCache exec {
    AntDelete(dir = Path("target"))
  }

  val versionScala = Target("target/generated-scala/InternalConstants.scala") dependsOn _project.projectFile exec { ctx: TargetContext =>
    AntMkdir(dir = ctx.targetFile.get.getParentFile)
    AntEcho(file = ctx.targetFile.get, message = s"""|// GENERATED
      |package ${namespace}
      |
      |/** Generated file. */
      |private object InternalConstants {
      |  /** The SBuild version this class was built with. */
      |  def version = "${version}"
      |}
      |""".stripMargin
    )
  }

  val sources = "scan:src/main/scala" ~ versionScala

  Target("phony:compile").cacheable dependsOn scalaCompiler ~ compileCp ~ sources exec {
    val output = "target/classes"
    addons.scala.Scalac(
      compilerClasspath = scalaCompiler.files,
      classpath = compileCp.files,
      sources = sources.files,
      destDir = Path(output),
      unchecked = true, deprecation = true, debugInfo = "vars"
    )
  }

  Target(sourcesZip) dependsOn sources ~ "LICENSE.txt" exec { ctx: TargetContext =>
    AntZip(destFile = ctx.targetFile.get, fileSets = Seq(
      AntFileSet(dir = Path("src/main/scala")),
      AntFileSet(dir = Path("target/generated-scala")),
      AntFileSet(file = Path("LICENSE.txt"))
    ))
  }

  Target("phony:scaladoc").cacheable dependsOn scalaCompiler ~ compileCp ~ sources exec {
    addons.scala.Scaladoc(
      scaladocClasspath = scalaCompiler.files,
      classpath = compileCp.files,
      sources = sources.files,
      destDir = Path("target/scaladoc"),
      deprecation = true, unchecked = true, implicits = true,
      docVersion = sbuildVersion,
      docTitle = s"SBuild Experimental API Reference"
    )
  }

  Target(jar) dependsOn "compile" ~ "LICENSE.txt" exec { ctx: TargetContext =>
    AntJar(
      destFile = ctx.targetFile.get,
      baseDir = Path("target/classes"),
      fileSet = AntFileSet(file = Path("LICENSE.txt")),
      manifestEntries = Map(
        "SBuild-ExportPackage" -> namespace,
        "SBuild-Plugin" ->
          s"""${namespace}.Aether=${namespace}.AetherPlugin;version="${version}"""",
        "SBuild-Classpath" -> Seq(
          s"mvn:org.eclipse.aether:aether-api:${aetherVersion}",
          s"mvn:org.eclipse.aether:aether-spi:${aetherVersion}",
          s"mvn:org.eclipse.aether:aether-util:${aetherVersion}",
          s"mvn:org.eclipse.aether:aether-impl:${aetherVersion}",
          s"mvn:org.eclipse.aether:aether-connector-file:${aetherVersion}",
          s"mvn:org.eclipse.aether:aether-connector-asynchttpclient:${aetherVersion}",
          s"mvn:org.eclipse.aether:aether-connector-wagon:${aetherVersion}",
          "mvn:io.tesla.maven:maven-aether-provider:3.1.2",
          s"mvn:org.apache.maven.wagon:wagon-provider-api:${wagonVersion}",
          s"mvn:org.apache.maven.wagon:wagon-http:${wagonVersion}",
          s"mvn:org.apache.maven.wagon:wagon-file:${wagonVersion}",
          s"mvn:org.apache.maven.wagon:wagon-ssh:${wagonVersion}",
          "mvn:org.sonatype.maven:wagon-ahc:1.2.1",
          s"mvn:org.apache.maven.wagon:wagon-http-shared4:${wagonVersion}",
          s"mvn:org.codehaus.plexus:plexus-component-annotations:1.5.5",
          s"mvn:org.apache.httpcomponents:httpclient:4.2.5",
          s"mvn:org.apache.httpcomponents:httpcore:4.2.4",
          "mvn:javax.inject:javax.inject:1",
          "mvn:com.ning:async-http-client:1.6.5",
          "mvn:io.tesla.maven:maven-model:3.1.0",
          "mvn:io.tesla.maven:maven-model-builder:3.1.0",
          "mvn:io.tesla.maven:maven-repository-metadata:3.1.0",
          "mvn:org.jboss.netty:netty:3.2.5.Final",
          "mvn:org.eclipse.sisu:org.eclipse.sisu.inject:0.0.0.M1",
          "mvn:org.eclipse.sisu:org.eclipse.sisu.plexus:0.0.0.M1",
          "mvn:org.codehaus.plexus:plexus-classworlds:2.4",
          "mvn:org.codehaus.plexus:plexus-interpolation:1.16",
          "mvn:org.codehaus.plexus:plexus-utils:2.1",
          "mvn:org.sonatype.sisu:sisu-guava:0.9.9",
          "mvn:org.sonatype.sisu:sisu-guice:3.1.0",
          "mvn:org.slf4j:slf4j-api:1.7.5",
          "mvn:org.slf4j:slf4j-simple:1.7.5"
        ).map("raw:" + _).mkString(",")
      )
    )
  }

}
