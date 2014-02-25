import de.tototec.sbuild._
import de.tototec.sbuild.ant._
import de.tototec.sbuild.ant.tasks._

@version("0.7.1")
@classpath(
  "mvn:org.apache.ant:ant:1.8.4",
  "mvn:org.sbuild:org.sbuild.plugins.sbuildplugin:0.3.0",
  "mvn:org.sbuild:org.sbuild.plugins.mavendeploy:0.1.0"
)
class SBuild(implicit _project: Project) {

  val namespace = "org.sbuild.plugins.aether"
  val version = "0.1.0"
  val url = "https://github.com/SBuild-org/sbuild-aether-plugin"

  val jar = s"target/${namespace}-${version}.jar"
  val sourcesJar = s"target/${namespace}-${version}-sources.jar"
  val sourcesDir = "src/main/scala"

  val aetherVersion = "0.9.0.M4"
  val aetherConnectorVersion = "0.9.0.M2"
  val wagonVersion = "2.4"

  val aetherCp = Seq(
    s"mvn:org.eclipse.aether:aether-api:${aetherVersion}",
    s"mvn:org.eclipse.aether:aether-spi:${aetherVersion}",
    s"mvn:org.eclipse.aether:aether-util:${aetherVersion}",
    s"mvn:org.eclipse.aether:aether-impl:${aetherVersion}",
    s"mvn:org.eclipse.aether:aether-connector-file:${aetherConnectorVersion}",
    s"mvn:org.eclipse.aether:aether-connector-asynchttpclient:${aetherConnectorVersion}",
    s"mvn:org.eclipse.aether:aether-connector-wagon:${aetherConnectorVersion}",
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
    "mvn:org.slf4j:slf4j-api:1.7.5" //,
  //    "mvn:ch.qos.logback:logback-core:1.0.11",
  //    "mvn:ch.qos.logback:logback-classic:1.0.11"
  )

  import org.sbuild.plugins.sbuildplugin._

  Plugin[SBuildPlugin] configure {
    _.copy(
      sbuildVersion = SBuildVersion.v0_7_1,
      pluginClass = s"$namespace.Aether",
      pluginVersion = version,
      deps = aetherCp
    )
  }

  Target(sourcesJar) dependsOn s"scan:${sourcesDir}" ~ "LICENSE.txt" exec { ctx: TargetContext =>
    AntZip(destFile = ctx.targetFile.get, fileSets = Seq(
      AntFileSet(dir = Path(sourcesDir)),
      AntFileSet(file = Path("LICENSE.txt"))
    ))
  }

  import org.sbuild.plugins.mavendeploy._
  Plugin[MavenDeploy] configure { _.copy(
    groupId = "org.sbuild",
    artifactId = namespace,
    version = version,
    artifactName = Some("SBuild Aether Plugin"),
    description = Some("An SBuild Plugin that provides a SchemeHandler based on Eclipse Aether (Maven) to transitively resolve dependencies from Maven repositories."),
    repository = Repository.SonatypeOss,
    scm = Option(Scm(url = url, connection = url)),
    developers = Seq(Developer(id = "TobiasRoeser", name = "Tobias Roeser", email = "le.petit.fou@web.de")),
    gpg = true,
    licenses = Seq(License.Apache20),
    url = Some(url),
    files = Map(
      "jar" -> s"target/${namespace}-${version}.jar",
      "sources" -> sourcesJar,
      "javadoc" -> "target/fake.jar"
    )
  )}

  Target("target/fake.jar") dependsOn "LICENSE.txt" exec { ctx: TargetContext =>
    import de.tototec.sbuild.ant._
    tasks.AntJar(destFile = ctx.targetFile.get, fileSet = AntFileSet(file = "LICENSE.txt".files.head))
  }

}
