package endpoints

import java.io.File
import org.scalatest.matchers.must.Matcher.*
import org.scalatest.*
import org.scalatest.concurrent.{Eventually, IntegrationPatience}
import play.api.libs.json.Json
import scalaj.http.Http

@DoNotDiscover
class FractionsSpec extends FunctionalSpec with Eventually with IntegrationPatience {
  describe("Fractions Endpoints") {
    contexts.foreach { case (pair) =>
      val context = pair._1
      implicit val environment: String = pair._2
      val baseDir = s"$dir/${pair._2}"
      it (s"should when calling ${url}$context/fraction-calculation-date return date of last fraction calculation  (${environment})") {
        val expected = fileToStr(s"${baseDir}/fraction-calculation-date.json")
        val expectedJson = Json.parse(expected)

        val result = eventually {
          Http(s"$url$context/fraction-calculation-date")
            .headers(standardHeaders())
            .asString
        }

        result.code mustBe 200
        result.contentType mustBe Some("application/json")
        Json.parse(result.body) mustBe expectedJson
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
          val result = eventually {
            Http(s"$url$context/epaye/$empref/fractions$params")
              .headers(standardHeaders())
              .asString
          }

          result.code mustBe 200
          result.contentType mustBe Some("application/json")
          Json.parse(result.body) mustBe (expectedJson \ "response").get
        }
      }
    }
  }
}
