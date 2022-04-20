import mill._, scalalib._, scalajslib._, scalanativelib._, publish._
import $ivy.`de.tototec::de.tobiasroeser.mill.vcs.version::0.1.4`
import de.tobiasroeser.mill.vcs.version.VcsVersion

val scala212 = "2.12.15"
val scala213 = "2.13.8"
val scala3 = "3.1.1"

val scalaVersions = Seq(scala212, scala213, scala3)

val scalaJSVersions = for {
  scalaV <- scalaVersions
  scalaJSV <- Seq("1.9.0")
  if scalaV.startsWith("2.") || scalaJSV.startsWith("1.")
} yield (scalaV, scalaJSV)

val scalaNativeVersions = for {
  scalaV <- scalaVersions
  scalaNativeV <- Seq("0.4.3")
} yield (scalaV, scalaNativeV)

object Deps {
  case object dev {
    case object zio {
      val version = "2.0.0-RC4"
      val zio = ivy"dev.zio::zio:$version"
      val `zio-prelude` = ivy"dev.zio::zio-prelude:1.0.0-RC12"
      val `zio-test` = ivy"dev.zio::zio-test:$version"
      val `zio-test-sbt` = ivy"dev.zio::zio-test-sbt:$version"

    }
  }
}

trait MorphirModule extends PublishModule {
  override def publishVersion: T[String] = VcsVersion.vcsState().format()
  def pomSettings = PomSettings(
    description = artifactName(),
    organization = "org.finos.morphir",
    url = "https://github.com/DamianReeves/finos-morphir",
    licenses = Seq(License.MIT),
    versionControl = VersionControl.github(owner = "DamianReeves", repo = "finos-morphir"),
    developers = Seq(
      Developer(
        "DamianReeves",
        "Damian Reeves",
        "https://github.com/DamianReeves"
      )
    )
  )
}

trait MorphirMainModule extends CrossSbtModule {
  def millSourcePath = super.millSourcePath / offset
  // def ivyDeps = Agg(ivy"com.lihaoyi::sourcecode::0.2.8")
  def offset: os.RelPath = os.rel
  def sources = T.sources(
    super
      .sources()
      .flatMap { source =>
        // println(source.path)
        val config = (source.path / os.up).last
        // println(s"config: $config")
        Seq(
          PathRef(
            source.path / os.up / os.up / os.up / os.up / "shared" / "src" / config / source.path.last
          ),
          source
        ).distinct
      }
  )
}

trait ZioTestModule extends TestModule {
  override def testFramework: T[String] = "zio.test.sbt.ZTestFramework"
}

trait MorphirTestModule extends ScalaModule with ZioTestModule {
  def crossScalaVersion: String
  def ivyDeps = Agg(
    ivy"dev.zio::zio-test-sbt:2.0.0-RC4",
    ivy"dev.zio::zio-test:2.0.0-RC4"
  )
  def offset: os.RelPath = os.rel
  // def millSourcePath = super.millSourcePath / os.up

  def sources = T.sources(
    super
      .sources()
      // .++(
      //   CrossModuleBase
      //     .scalaVersionPaths(crossScalaVersion, s => millSourcePath / s"src-$s")
      // )
      .flatMap { source =>
        // println(s"source: ${source.path}")
        Seq(
          // PathRef(source.path / os.up / "test" / source.path.last)
          PathRef(source.path / os.up / os.up / "test" / source.path.last),
          PathRef(
            source.path / os.up / os.up / os.up / os.up / "shared" / "src" / "test" / source.path.last
          )
        )
      }
      .distinct
  )
}

object finos extends Module {
  object morphir extends Module {
    object jvm extends Cross[JvmMorphirModule](scalaVersions: _*)
    class JvmMorphirModule(val crossScalaVersion: String)
        extends MorphirMainModule
        with ScalaModule
        with MorphirModule {

      def ivyDeps = Agg(Deps.dev.zio.zio, Deps.dev.zio.`zio-prelude`)
      object test extends Tests with MorphirTestModule {
        override def crossScalaVersion: String =
          JvmMorphirModule.this.crossScalaVersion
      }
    }
    object js extends Cross[JsMorphirModule](scalaJSVersions: _*)
    class JsMorphirModule(val crossScalaVersion: String, crossJSVersion: String)
        extends MorphirMainModule
        with ScalaJSModule
        with MorphirModule {
      def scalaJSVersion = crossJSVersion
      def offset = os.up
    }
    object native
    class NativeMorphirModule(
        val crossScalaVersion: String,
        crossScalaNativeVersion: String
    ) extends MorphirMainModule
        with ScalaNativeModule
        with MorphirModule {
      def scalaNativeVersion = crossScalaNativeVersion
    }
  }
}
