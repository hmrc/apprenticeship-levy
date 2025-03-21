/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.apprenticeshiplevy.config

import java.net.{URLDecoder, URLEncoder}
import play.api.mvc.{PathBindable, QueryStringBindable}
import uk.gov.hmrc.apprenticeshiplevy.data.api._

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Try
import scala.util.matching.Regex

object QueryBinders {
  val datePattern: Regex = "^(\\d{4})-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$".r

  implicit def bindableLocalDate: QueryStringBindable[LocalDate] = new QueryStringBindable[LocalDate] {
    def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, LocalDate]] = {
      params.get(key).flatMap(_.headOption).map { (date: String) => (Try {
        date match {
          case datePattern(year, _*) if year.toInt >= 2000 => Right(LocalDate.parse(date))
          case _ =>
            Left(s"DATE_INVALID: '$date' date parameter is in the wrong format. Should be '${datePattern.toString()}' where date format is yyyy-MM-dd and year is 2000 or later.")
        }
      } recover {
        case _: Exception => Left(s"DATE_INVALID: date parameter is in the wrong format. Should be '${datePattern.toString()}' where date format is yyyy-MM-dd.")
      }).get
      }
    }

    def unbind(key: String, value: LocalDate): String = QueryStringBindable.bindableString.unbind(key, value.format(DateTimeFormatter.ISO_LOCAL_DATE))
  }
}

object PathBinders {
  private val emprefValidator = isValid("^\\d{3}/[0-9A-Z]{1,10}$".r)
  private val ninoValidator = isValidNino


  implicit def bindableEmploymentReference(implicit binder: PathBindable[String]): PathBindable[EmploymentReference] =
    bindable[String,EmploymentReference](emprefValidator,
                                         str => EmploymentReference(str),
                                         b => URLEncoder.encode(b.empref, "UTF-8"),
                                         "EMPREF_INVALID")

  implicit def bindableNino(implicit binder: PathBindable[String]): PathBindable[Nino] =
    bindable[String,Nino](ninoValidator,
                          str => Nino(str),
                          b => URLEncoder.encode(b.nino, "UTF-8"),
                          "NINO_INVALID")

  private[config] def bindable[A,B](validator: (String, String)=> Either[String, String],
                                    convertToB: String => B,
                                    convertToA: B => A,
                                    code: String)
                                   (implicit binder: PathBindable[A]): PathBindable[B] = new PathBindable[B] {
    override def bind(key: String, value: String): Either[String, B] = {
      for {
        theA <- binder.bind(key, value)
        bAsStr <- validator(URLDecoder.decode(theA.toString, "UTF-8"), code)
      } yield convertToB(bAsStr)
    }

    override def unbind(key: String, b: B): String = {
      binder.unbind(key,convertToA(b))
    }
  }

  private[config] def isValid(regex: Regex)(value: String, code: String): Either[String, String] = value match {
    case regex(_*) => Right(value)
    case _ => Left(s"$code: '$value' is in the wrong format. Should be ${regex.toString()} and url encoded.")
  }

  def isValidNino(value: String, code: String): Either[String, String] = if (Nino.isValid(value)) {
    Right(value)
  } else {
    Left(s"$code: '$value' is in the wrong format. Should have a prefix (one of ${uk.gov.hmrc.domain.Nino.validPrefixes}) and suffix (one of ${uk.gov.hmrc.domain.Nino.validSuffixes}) and url encoded.")
  }
}
