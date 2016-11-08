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

package uk.gov.hmrc.apprenticeshiplevy.data.des

import play.api.libs.json.Json

case class HodName(nameLine1: Option[String] = None, nameLine2: Option[String] = None)

case class HodTelephone(telephoneNumber: Option[String] = None, fax: Option[String] = None)

case class HodEmail(primary: Option[String] = None)

case class HodAddress(addressLine1: Option[String] = None,
                      addressLine2: Option[String] = None,
                      addressLine3: Option[String] = None,
                      addressLine4: Option[String] = None,
                      addressLine5: Option[String] = None,
                      postcode: Option[String] = None,
                      foreignCountry: Option[String] = None
                     )

case class HodContact(telephone: Option[HodTelephone] = None, email: Option[HodEmail] = None)

case class DesignatoryDetailsData(name: Option[HodName] = None, address: Option[HodAddress] = None, contact: Option[HodContact] = None)

case class DesignatoryDetails(empref:Option[String], employer: Option[DesignatoryDetailsData] = None, communication: Option[DesignatoryDetailsData] = None)

object DesignatoryDetails {
  implicit val hnformat = Json.format[HodName]
  implicit val haformat = Json.format[HodAddress]
  implicit val htformat = Json.format[HodTelephone]
  implicit val heformat = Json.format[HodEmail]
  implicit val hcformat = Json.format[HodContact]
  implicit val dddformat = Json.format[DesignatoryDetailsData]

  implicit val readDesignatoryDetailsFormat = Json.reads[DesignatoryDetails]
  implicit val writeDesignatoryDetailsFormat = Json.writes[DesignatoryDetails]
}
