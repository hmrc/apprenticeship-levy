package uk.gov.hmrc.apprenticeshiplevy.endpoints

import java.io.File
import org.scalatest._
import uk.gov.hmrc.apprenticeshiplevy.config.Config

import scala.io.Source
import scala.util.Using

trait FunctionalSpec extends FunSpec {
  val url: String = Config.rootUrl
  val dir: String = Config.resourceDir
  val contexts: Seq[(String, String)] = Config.contexts

  def standardHeaders(implicit environment: String): Seq[(String,String)] = Seq("Accept" -> "application/vnd.hmrc.1.0+json",
                                                                            "Authorization" -> s"""Bearer ${System.getProperty("bearer.token." + System.getProperty("environment", "local").toLowerCase)}""")

  def fileToStr(filename: String): String = {
    Using(Source.fromFile(new File(s"$filename"))) {
      bufferedSource => bufferedSource.getLines.mkString("\n")
    }
  }.get

  def fileToStr(file: File): String = {
    Using(Source.fromFile(file)) {
      bufferedSource => bufferedSource.getLines.mkString("\n")
    }
  }.get

}