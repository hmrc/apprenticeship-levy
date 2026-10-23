package endpoints

import org.scalatest.*
import org.scalatest.concurrent.{Eventually, IntegrationPatience}
import org.scalatest.matchers.must.Matchers.*
import play.api.libs.json.Json
import scalaj.http.Http

@DoNotDiscover
class RootSpec extends FunctionalSpec with Eventually with IntegrationPatience {
  info(s"Using bearer token ${System.getProperty("bearer.token."+System.getProperty("environment", "local").toLowerCase)}.")

  describe("Root Endpoint") {
    contexts.foreach { case (pair) =>
      val context = pair._1
      implicit val environment = pair._2
      val folder = s"$dir/${pair._2}"
      it (s"should when calling ${url}$context/ return links for each empref (${environment})") {
        val expected = fileToStr(s"${folder}/root.json")
        val expectedJson = Json.parse(expected)

        info(standardHeaders().mkString(" "))

        val result = eventually {
          Http(s"$url$context/")
            .headers(standardHeaders())
            .asString
        }

        result.code mustBe 200
        result.contentType mustBe Some("application/json")
        Json.parse(result.body) mustBe expectedJson
      }
    }
  }
}