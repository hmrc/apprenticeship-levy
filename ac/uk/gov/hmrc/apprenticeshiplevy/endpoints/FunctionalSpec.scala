package uk.gov.hmrc.apprenticeshiplevy.endpoints

import org.scalatest._
import uk.gov.hmrc.apprenticeshiplevy.config.Config
import scala.io.Source
import java.io.File

trait FunctionalSpec extends FunSpec {
  val url = Config.rootUrl
  val dir = Config.resourceDir
  val contexts = Config.contexts

  def standardHeaders(): Seq[(String,String)] = Seq(("ACCEPT"->"application/vnd.hmrc.1.0+json"),
                                                    ("Environment"->"isit"),
                                                    ("Authorization"->s"Bearer ${System.getProperty("bearer.token")}"))

  def fileToStr(filename: String): String = Source.fromFile(new File(s"$filename")).getLines.mkString("\n")
  def fileToStr(file: File): String = Source.fromFile(file).getLines.mkString("\n")
}