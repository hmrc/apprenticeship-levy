package uk.gov.hmrc.apprenticeshiplevy.endpoints

import org.scalatest._
import org.scalatest.Matchers._
import org.scalatest.concurrent.{Eventually, IntegrationPatience}
import java.io.File
import scalaj.http.Http
import play.api.libs.json.Json

@DoNotDiscover
class EmploymentCheckSpec extends FunctionalSpec with Eventually with IntegrationPatience {
  describe("Employment Check Endpoint") {
    contexts.foreach { case (pair) =>
      val context = pair._1
      implicit val environment = pair._2
      val folder = s"$dir/${pair._2}/employed"
      info(s"Generating tests for ${folder}")
      val files = new File(folder).listFiles.filter(_.getName.endsWith(".json"))

      files.foreach { case (file) =>
        val name = file.getName().split("\\.")(0)
        val parts = name.split("-")
        val nino = parts(1)
        val emprefParts = parts(0).splitAt(3)
        val empref = s"${emprefParts._1}%2F${emprefParts._2}"
        val expected = fileToStr(file)
        val expectedJson = Json.parse(expected)
        val params = (expectedJson \ "params").as[String]
        it (s"should when calling ${url}$context/epaye/$empref/employed/$nino$params return employment details (${environment})") {
          // set up

          // test
          val result = eventually {
            Http(s"$url$context/epaye/$empref/employed/$nino$params")
              .headers(standardHeaders)
              .asString
          }

          // check
          info(result.body)
          result.code shouldBe 200
          result.contentType shouldBe Some("application/json")
          Json.parse(result.body) shouldBe (expectedJson \ "response").get
        }
      }
    }
  }
}