package uk.gov.hmrc.apprenticeshiplevy

import org.scalacheck.Gen
import org.scalatest.DoNotDiscover
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.play._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.apprenticeshiplevy.config.IntegrationTestConfig
import uk.gov.hmrc.apprenticeshiplevy.data.api.{LevyDeclaration, PayrollPeriod}
import views.html.helper

import java.time.{LocalDate, LocalDateTime}

@DoNotDiscover
class DeclarationsEndpointISpec extends WiremockFunSpec with IntegrationTestConfig with ConfiguredServer with ScalaCheckPropertyChecks {
  val sixteenToSeventeen = "16-17"
  describe("Declarations Endpoint") {
    val contexts = Seq("/sandbox", "")
    contexts.foreach { context =>
      describe(s"should when calling $localMicroserviceUrl$context/epaye/<empref>/declarations") {
        describe(s"with valid paramters") {
          it(s"should return levy declarations") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12341/declarations").withHeaders(standardDesHeaders(): _*)

            // test
            val result = route(app, request).get

            // check
            contentType(result) shouldBe Some("application/json")

            val json = contentAsJson(result)
            println(Console.MAGENTA + json + Console.RESET)
            (json \ "empref").as[String] shouldBe "123/AB12341"
            val declarations = (json \ "declarations").as[Array[LevyDeclaration]]
            declarations.length shouldBe 9
            info(declarations.mkString("\n"))
          }

          it(s"should return levy declarations where fromDate only specified") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12342/declarations?fromDate=2016-10-12").withHeaders(standardDesHeaders(): _*)

            // test
            val result = route(app, request).get

            // check
            contentType(result) shouldBe Some("application/json")

            val json = contentAsJson(result)
            (json \ "empref").as[String] shouldBe "123/AB12342"
            val declarations = (json \ "declarations").as[Array[LevyDeclaration]]
            declarations.length shouldBe 2
            //info(declarations.mkString("\n"))
          }

          it(s"should return levy declarations where toDate only specified") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12343/declarations?toDate=2016-02-21").withHeaders(standardDesHeaders(): _*)

            // test
            val result = route(app, request).get

            // check
            contentType(result) shouldBe Some("application/json")

            val json = contentAsJson(result)
            (json \ "empref").as[String] shouldBe "123/AB12343"
            val declarations = (json \ "declarations").as[Array[LevyDeclaration]]
            declarations.length shouldBe 5
            //info(declarations.mkString("\n"))
          }

          it(s"should return levy declarations where fromDate and toDate are specified") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12344/declarations?fromDate=2016-05-19&toDate=2016-05-21").withHeaders(standardDesHeaders(): _*)

            // test
            val result = route(app, request).get

            // check
            contentType(result) shouldBe Some("application/json")

            val json = contentAsJson(result)
            (json \ "empref").as[String] shouldBe "123/AB12344"
            val declarations = (json \ "declarations").as[Array[LevyDeclaration]]
            declarations.length shouldBe 1
            //info(declarations.mkString("\n"))
          }

          it(s"should handle no payment period") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12341/declarations").withHeaders(standardDesHeaders(): _*)

            // test
            val result = route(app, request).get

            // check
            contentType(result) shouldBe Some("application/json")

            val json = contentAsJson(result)
            (json \ "empref").as[String] shouldBe "123/AB12341"
            val declarations = (json \ "declarations").as[Array[LevyDeclaration]]
            declarations(2) shouldBe LevyDeclaration(567742487410L,
              LocalDateTime.parse("2016-04-20T14:25:32.000"),
              None,
              None,
              None,
              Some(PayrollPeriod(sixteenToSeventeen, 8)),
              None,
              None,
              Some(true),
              56774248741L)
          }

          it(s"should handle inactive period") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12341/declarations").withHeaders(standardDesHeaders(): _*)

            // test
            val result = route(app, request).get

            // check
            contentType(result) shouldBe Some("application/json")

            val json = contentAsJson(result)
            (json \ "empref").as[String] shouldBe "123/AB12341"
            val declarations = (json \ "declarations").as[Array[LevyDeclaration]]
            declarations(7) shouldBe LevyDeclaration(65732154551L,
              LocalDateTime.parse("2016-04-20T14:25:32.000"),
              None,
              Some(LocalDate.parse("2016-08-06")),
              Some(LocalDate.parse("2016-11-05")),
              None,
              None,
              None,
              None,
              6573215455L)
          }

          it(s"should handle ceased trading") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12341/declarations").withHeaders(standardDesHeaders(): _*)

            // test
            val result = route(app, request).get

            // check
            contentType(result) shouldBe Some("application/json")

            val json = contentAsJson(result)
            (json \ "empref").as[String] shouldBe "123/AB12341"
            val declarations = (json \ "declarations").as[Array[LevyDeclaration]]
            declarations(6) shouldBe LevyDeclaration(567742487423L,
              LocalDateTime.parse("2016-04-20T14:25:32.000"),
              Some(LocalDate.parse("2016-06-06")),
              None,
              None,
              None,
              None,
              None,
              None, 56774248742L)
          }

          it(s"should handle levies") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12341/declarations").withHeaders(standardDesHeaders(): _*)

            // test
            val result = route(app, request).get

            // check
            contentType(result) shouldBe Some("application/json")

            val json = contentAsJson(result)
            (json \ "empref").as[String] shouldBe "123/AB12341"
            val declarations = (json \ "declarations").as[Array[LevyDeclaration]]
            declarations(0) shouldBe LevyDeclaration(567742487432L,
              LocalDateTime.parse("2016-05-20T14:25:32.000"),
              None,
              None,
              None,
              Some(PayrollPeriod(sixteenToSeventeen, 2)),
              Some(98.64),
              Some(15000),
              None,
              56774248743L)
            declarations(3) shouldBe LevyDeclaration(567742487412L,
              LocalDateTime.parse("2016-04-20T14:25:32.000"),
              None,
              None,
              None,
              Some(PayrollPeriod(sixteenToSeventeen, 11)),
              Some(24.27),
              Some(15000),
              None,
              56774248741L)
            declarations(5) shouldBe LevyDeclaration(567742487422L,
              LocalDateTime.parse("2016-04-20T14:25:32.000"),
              None,
              None,
              None,
              Some(PayrollPeriod(sixteenToSeventeen, 11)),
              Some(24.27),
              Some(15000),
              None,
              56774248742L)
            declarations(8) shouldBe LevyDeclaration(65732154552L,
              LocalDateTime.parse("2016-04-20T14:25:32.000"),
              None,
              None,
              None,
              Some(PayrollPeriod(sixteenToSeventeen, 2)),
              Some(124.27),
              Some(15000),
              None,
              6573215455L)
          }

          it(s"should handle bad timestamps returned from DES in levies") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB88888/declarations").withHeaders(standardDesHeaders(): _*)

            // test
            val result = route(app, request).get

            // check
            contentType(result) shouldBe Some("application/json")

            val json = contentAsJson(result)
            (json \ "empref").as[String] shouldBe "123/AB88888"
            val declarations = (json \ "declarations").as[Array[LevyDeclaration]]
            declarations(0) shouldBe LevyDeclaration(567742487410L,
              LocalDateTime.parse("2013-04-17T09:00:55"),
              None,
              None,
              None,
              Some(PayrollPeriod(sixteenToSeventeen, 8)),
              None,
              None,
              Some(true),
              56774248741L)
          }
        }

        describe("with invalid parameters") {
          Seq("fromDate", "toDate").foreach { param =>
            it(s"should return 400 when $param param is invalid") {
              // set up
              val dates = for {str <- Gen.listOf(Gen.alphaNumChar)} yield str.mkString

              forAll(dates) { (date: String) =>
                whenever(date.nonEmpty) {
                  val requestUrl = param match {
                    case "fromDate" => s"$context/epaye/123%2FAB12345/declarations?fromDate=${helper.urlEncode(date)}&toDate=2015-06-30"
                    case _ => s"/sandbox/epaye/123%2FAB12345/declarations?fromDate=2015-06-03&toDate=${helper.urlEncode(date)}"
                  }
                  val request = FakeRequest(GET, requestUrl).withHeaders(standardDesHeaders(): _*)

                  // test
                  val result = route(app, request).get
                  val httpStatus = status(result)

                  // check
                  httpStatus shouldBe BAD_REQUEST
                  contentType(result) shouldBe Some("application/json")
                  contentAsString(result) should include (""" date parameter is in the wrong format. Should be '^(\\d{4})-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$' where date format is yyyy-MM-dd and year is 2000 or later.""")
                }
              }
            }
          }

          it(s"should return 400 when empref is unknown") {
            // set up
            val emprefs = for {empref <- genEmpref} yield empref

            forAll(emprefs) { (empref: String) =>
              whenever(empref.nonEmpty) {
                val request = FakeRequest(GET, s"$context/epaye/${helper.urlEncode(empref)}/declarations").withHeaders(standardDesHeaders(): _*)

                // test
                val result = route(app, request).get
                val httpStatus = status(result)

                // check
                httpStatus shouldBe BAD_REQUEST
                contentType(result) shouldBe Some("application/json")
                contentAsString(result) should include("""is in the wrong format. Should be ^\\d{3}/[0-9A-Z]{1,10}$ and url encoded."""")
              }
            }
          }

          it(s"should return 400 when DES returns 400") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/400%2FAB12345/declarations").withHeaders(standardDesHeaders(): _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe BAD_REQUEST
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_BAD_REQUEST","message":"Bad request error"}""")
          }

          it(s"should return http status 401 when DES returns 401") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/401%2FAB12345/declarations").withHeaders(standardDesHeaders(): _*)

            // test
            val result = route(app, request).get

            // check
            status(result)
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_UNAUTHORIZED","message":"DES unauthorised error"}""")
          }

          it(s"should return http status 403 when DES returns 403") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/403%2FAB12345/declarations").withHeaders(standardDesHeaders(): _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe 403
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_FORBIDDEN","message":"DES forbidden error"}""")
          }

          it(s"DES HTTP 404") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/404%2FAB12345/declarations").withHeaders(standardDesHeaders(): _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe 404
          }

          it(s"should return 400 when to date is before from date") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB12345/declarations?fromDate=2015-06-03&toDate=2015-03-30").withHeaders(standardDesHeaders(): _*)

            // test
            val result = route(app, request).get
            val httpStatus = status(result)

            // check
            httpStatus shouldBe BAD_REQUEST
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"BAD_REQUEST","message":"From date was after to date"}""")
          }
        }

        describe("when backend systems failing") {
          describe("but not as per specification dated 07/11/2016") {
            it(s"should return http status 404 when empty json returned") {
              // set up
              val request = FakeRequest(GET, s"$context/epaye/999%2FAB999999/declarations").withHeaders(standardDesHeaders(): _*)

              // test
              val result = route(app, request).get

              // check
              status(result) shouldBe 404
              contentType(result) shouldBe Some("application/json")
              contentAsJson(result) shouldBe Json.parse("""{"code":"NOT_FOUND","message":"Resource was not found"}""")
            }

            it(s"should return http status 500 when error json object returned on Http Status 200") {
              // set up
              val request = FakeRequest(GET, s"$context/epaye/999%2FAB999998/declarations").withHeaders(standardDesHeaders(): _*)

              // test
              val result = route(app, request).get

              // check
              status(result) shouldBe 500
              contentType(result) shouldBe Some("application/json")
              contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_BACKEND_FAILURE","message":"DES backend error"}""")
            }

            it(s"should return http status 404 when just empref field in Json object is returned") {
              // set up
              val request = FakeRequest(GET, s"$context/epaye/999%2FAB999997/declarations").withHeaders(standardDesHeaders(): _*)

              // test
              val result = route(app, request).get

              // check
              status(result) shouldBe 404
              contentType(result) shouldBe Some("application/json")
              contentAsJson(result) shouldBe Json.parse("""{"code":"NOT_FOUND","message":"Resource was not found"}""")
            }

            it(s"should return eps if using old fieldname of 'declarations'") {
              // set up
              val request = FakeRequest(GET, s"$context/epaye/999%2FAB999996/declarations").withHeaders(standardDesHeaders(): _*)

              // test
              val result = route(app, request).get

              // check
              status(result) shouldBe 200
              contentType(result) shouldBe Some("application/json")
              contentAsJson(result) shouldBe Json.parse(
                """{"empref":"999/AB999996","declarations":[
                  |{"id":65732154551,"submissionTime":"2016-10-20T14:25:32.000",
                  |"inactiveFrom":"2016-08-06",
                  |"inactiveTo":"2016-11-05",
                  |"submissionId":6573215455},
                  |{"id":65732154552,
                  |"submissionTime":"2016-10-20T14:25:32.000",
                  |"payrollPeriod":{"year":"16-17","month":1},
                  |"levyDueYTD":124.27,
                  |"levyAllowanceForFullYear":15000,
                  |"submissionId":6573215455}]}""".stripMargin)
            }
          }

          it("should return 503 when connection closed") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/999%2FAB12345/declarations").withHeaders(standardDesHeaders(): _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe SERVICE_UNAVAILABLE
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_IO","message":"DES connection error"}""")
          }

          it(s"should return http status 503 when empty response") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/888%2FAB12345/declarations").withHeaders(standardDesHeaders(): _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe SERVICE_UNAVAILABLE
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_IO","message":"DES connection error"}""")
          }

          it(s"should return http status 408 when timed out") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/777%2FAB12345/declarations").withHeaders(standardDesHeaders(): _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe 408
            contentType(result) shouldBe Some("application/json")
            contentAsString(result) should include("""{"code":"DES_ERROR_GATEWAY_TIMEOUT","message":"DES not responding error""")
          }

          it(s"should return http status 503 when DES HTTP 500") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/500%2FAB12345/declarations").withHeaders(standardDesHeaders(): _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe SERVICE_UNAVAILABLE
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_BACKEND_FAILURE","message":"DES 5xx error"}""")
          }

          it(s"should return http status 503 when DES HTTP 503") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/503%2FAB12345/declarations").withHeaders(standardDesHeaders(): _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe SERVICE_UNAVAILABLE
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"DES_ERROR_BACKEND_FAILURE","message":"DES 5xx error"}""")
          }

          it(s"should return http status 404 when empty json returned") {
            // set up
            val request = FakeRequest(GET, s"$context/epaye/123%2FAB99999/declarations").withHeaders(standardDesHeaders(): _*)

            // test
            val result = route(app, request).get

            // check
            status(result) shouldBe 404
            contentType(result) shouldBe Some("application/json")
            contentAsJson(result) shouldBe Json.parse("""{"code":"NOT_FOUND","message":"Resource was not found"}""")
          }
        }
      }
    }
  }
}
