package uk.gov.hmrc.apprenticeshiplevy.endpoints

import java.io.File

import org.scalatest.Matchers._
import org.scalatest._
import org.scalatest.concurrent.{Eventually, IntegrationPatience}
import play.api.libs.json.Json
import scalaj.http.Http

@DoNotDiscover
class EmprefSpec extends FunctionalSpec with Eventually with IntegrationPatience {
  describe("Empref Endpoint") {
    contexts.foreach { case (pair) =>
      val context = pair._1
      implicit val environment = pair._2
      val folder = s"$dir/${pair._2}/empref"
      info(s"Generating tests for ${folder} for environment ${environment}")
      val files = new File(folder).listFiles.filter(_.getName.endsWith(".json"))

      files.foreach { case (file) =>
        val name = file.getName().splitAt(file.getName().indexOf("."))._1
        val emprefParts = name.splitAt(3)
        val empref = s"${emprefParts._1}%2F${emprefParts._2}"
        it (s"should when calling ${url}$context/epaye/$empref return employer details (${environment})") {
          // set up
          val expected = fileToStr(file)
          val expectedJson = Json.parse(expected)

          // test
          val result = eventually {
            Http(s"$url$context/epaye/$empref")
              .headers(standardHeaders)
              .asString
          }

          // check
          result.code shouldBe 200
          result.contentType shouldBe Some("application/hal+json")
          Json.parse(result.body) shouldBe expectedJson
        }
      }
    }
  }
}