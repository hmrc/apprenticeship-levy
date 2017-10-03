package uk.gov.hmrc.apprenticeshiplevy.endpoints

import org.scalatest._
import org.scalatest.Matchers._
import org.scalatest.concurrent.{Eventually, IntegrationPatience}
import java.io.File
import scalaj.http.Http
import play.api.libs.json.Json

@DoNotDiscover
class FractionsSpec extends FunctionalSpec with Eventually with IntegrationPatience {
  describe("Fractions Endpoints") {
    contexts.foreach { case (pair) =>
      val context = pair._1
      implicit val environment = pair._2
      val baseDir = s"$dir/${pair._2}"
      it (s"should when calling ${url}$context/fraction-calculation-date return date of last fraction calculation  (${environment})") {
        // set up
        val expected = fileToStr(s"${baseDir}/fraction-calculation-date.json")
        val expectedJson = Json.parse(expected)

        // test
        val result = eventually {
          Http(s"$url$context/fraction-calculation-date")
            .headers(standardHeaders)
            .asString
        }

        // check
        result.code shouldBe 200
        result.contentType shouldBe Some("application/json")
        Json.parse(result.body) shouldBe expectedJson
      }

      val folder = s"${baseDir}/fraction"
      info(s"Generating tests for ${folder} (${environment})")
      val files = new File(folder).listFiles.filter(_.getName.endsWith(".json"))

      files.foreach { case (file) =>
        val name = file.getName().split("\\.")(0)
        val parts = name.split("-")
        val emprefParts = parts(0).splitAt(3)
        val empref = s"${emprefParts._1}%2F${emprefParts._2}"
        val expected = fileToStr(file)
        val expectedJson = Json.parse(expected)
        val params = (expectedJson \ "params").as[String]
        it (s"should when calling ${url}$context/epaye/$empref/fractions$params return fraction details (env: ${environment}, file: ${name})") {
          // set up

          // test
          val result = eventually {
            Http(s"$url$context/epaye/$empref/fractions$params")
              .headers(standardHeaders)
              .asString
          }

          // check
          result.code shouldBe 200
          result.contentType shouldBe Some("application/json")
          Json.parse(result.body) shouldBe (expectedJson \ "response").get
        }
      }
    }
  }
}
