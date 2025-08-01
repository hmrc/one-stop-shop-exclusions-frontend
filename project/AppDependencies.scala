import play.core.PlayVersion
import sbt.*

object AppDependencies {

  private val bootstrapVersion = "9.18.0"
  private val hmrcMongoVersion = "2.7.0"
  private val httpVerbVersion = "15.1.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-frontend-play-30"             % bootstrapVersion,
    "uk.gov.hmrc"             %% "play-frontend-hmrc-play-30"             % "12.7.0",
    "uk.gov.hmrc"             %% "play-conditional-form-mapping-play-30"  % "3.3.0",
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-30"                     % hmrcMongoVersion,
    "uk.gov.hmrc"             %% "domain-play-30"                         % "10.0.0",
    "org.typelevel"           %% "cats-core"                              % "2.13.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"     % bootstrapVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-30"    % hmrcMongoVersion,
    "org.scalatest"           %% "scalatest"                  % "3.2.19",
    "org.scalatestplus"       %% "scalacheck-1-15"            % "3.2.11.0",
    "org.scalacheck"          %% "scalacheck"                 % "1.18.1",
    "org.jsoup"               %  "jsoup"                      % "1.21.1",
    "com.vladsch.flexmark"    %  "flexmark-all"               % "0.64.8",
    "org.scalatestplus.play"  %% "scalatestplus-play"         % "7.0.2",
    "org.playframework"       %% "play-test"                  % PlayVersion.current
  ).map(_ % "test, it")

  def apply(): Seq[ModuleID] = compile ++ test
}
