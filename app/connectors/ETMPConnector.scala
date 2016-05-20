package connectors

import uk.gov.hmrc.apprenticeshiplevy.data.LevyDeclarations
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet}

import scala.concurrent.Future

trait ETMPConnector {

  def etmpBaseUrl: String

  def httpGet: HttpGet

  def declarations(empref: String, months: Option[Int])(implicit hc: HeaderCarrier): Future[LevyDeclarations] = {
    val url = (s"/epaye/${empref}/declarations", months) match {
      case (url, Some(n)) => s"url/?months=$n"
      case (url, None) => url
    }

    httpGet.GET[LevyDeclarations](url)
  }


}
