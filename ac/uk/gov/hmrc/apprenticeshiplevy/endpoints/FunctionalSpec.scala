package uk.gov.hmrc.apprenticeshiplevy.endpoints

import org.scalatest._
import uk.gov.hmrc.apprenticeshiplevy.config.Config
import scala.io.Source
import java.io.File

trait FunctionalSpec extends FunSpec {
  val url = Config.rootUrl
  val dir = Config.resourceDir
  val contexts = Config.contexts

  def standardHeaders(implicit environment: String): Seq[(String,String)] = Seq(("Accept"->"application/vnd.hmrc.1.0+json"),
                                                                            ("Authorization"->s"""Bearer ${System.getProperty("bearer.token."+System.getProperty("environment", "local").toLowerCase)}"""))

  def fileToStr(filename: String): String = Source.fromFile(new File(s"$filename")).getLines.mkString("\n")
  def fileToStr(file: File): String = Source.fromFile(file).getLines.mkString("\n")
}