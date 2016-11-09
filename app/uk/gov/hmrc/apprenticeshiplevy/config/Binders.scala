/*
 * Copyright 2016 HM Revenue & Customs
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

import org.joda.time.LocalDate
import play.api.mvc.{QueryStringBindable, PathBindable}
import uk.gov.hmrc.time.DateConverter
import scala.util.Try
import java.util.regex.Pattern
import uk.gov.hmrc.apprenticeshiplevy.data.api._
import scala.util.matching.Regex
import java.net.{URLDecoder, URLEncoder}

object QueryBinders {
  val DatePattern = AppContext.datePattern.r

  implicit def bindableLocalDate(implicit stringBinder: QueryStringBindable[String]): QueryStringBindable[LocalDate] = new QueryStringBindable[LocalDate] {
    def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, LocalDate]] = {
      params.get(key).flatMap(_.headOption).map { date: String => (Try {
        date match {
          case DatePattern(year, _*) if year.toInt >= 2000 => Right(DateConverter.parseToLocalDate(date))
          case _ => Left(s"'${date}' date parameter is in the wrong format. Should be ('${DatePattern.toString()}' where data is yyyy-MM-dd and year is 2000 or later)")
        }
      } recover {
        case e: Exception => Left(s"date parameter is in the wrong format. Should be ('${DatePattern.toString()}' where data is yyyy-MM-dd)")
      }).get
      }
    }

    def unbind(key: String, value: LocalDate): String = QueryStringBindable.bindableString.unbind(key, DateConverter.formatToString(value))
  }
}

object PathBinders {
  val EmployerReferencePattern = AppContext.employerReferencePattern.r
  val NinoPattern = AppContext.ninoPattern.r

  implicit def bindableEmploymentReference(implicit binder: PathBindable[String]) =
    bindable[String,EmploymentReference](EmployerReferencePattern, str => EmploymentReference(str), b => URLEncoder.encode(b.empref, "UTF-8"))

  implicit def bindableNino(implicit binder: PathBindable[String]) = bindable[String,Nino](NinoPattern, str => Nino(str), b => URLEncoder.encode(b.nino, "UTF-8"))

  private[config] def bindable[A,B](regex: Regex, convertToB: String => B, convertToA: B => A)
                                   (implicit binder: PathBindable[A]): PathBindable[B] = new PathBindable[B] {
    override def bind(key: String, value: String): Either[String, B] = {
      for {
        theA <- binder.bind(key, value).right
        bAsStr <- isValid(regex, URLDecoder.decode(theA.toString(), "UTF-8")).right
      } yield convertToB(bAsStr)
    }

    override def unbind(key: String, b: B): String = {
      binder.unbind(key,convertToA(b))
    }
  }

  private[config] def isValid(regex: Regex, value: String): Either[String, String] = value match {
    case regex(_*) => Right(value)
    case _ => Left(s"'${value}' is in the wrong format. Should be ${regex.toString()}")
  }
}